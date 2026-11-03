/*
 * Copyright (C) 2026  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.cnr.iit.epas.tests.mealtickets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.cnr.iit.epas.config.client.MealTicketServiceClient;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.UsersRolesOffices;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * I permessi degli utenti sono verificati da epas-service prima di inoltrare la richiesta a
 * epas-meal-ticket-service.
 */
@Transactional
@SpringBootTest
@TestPropertySource(properties = {
    "epas.security.oauth2.resourceserverEnabled=true",
    "epas.security.oauth2.userAuthIdentifier=username"})
class MealTicketManagementControllerTest {

  private static final String BASE = "/rest/v4/mealticketmanagement";
  private static final LocalDate BEGIN = LocalDate.of(2016, 1, 1);

  @Inject
  private WebApplicationContext context;
  @Inject
  private H2Examples h2Examples;
  @Inject
  private EntityManager em;
  @MockitoBean
  private MealTicketServiceClient client;

  private MockMvc mvc;
  private Person employee;
  private Contract contract;
  private Office office;
  private Office otherOffice;
  private Person admin;
  private Person otherAdmin;
  private Person manager;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    employee = h2Examples.normalEmployee(BEGIN, Optional.empty());
    em.flush();
    em.refresh(employee);
    contract = employee.getContracts().iterator().next();
    office = employee.getOffice();
    otherOffice = h2Examples.buildOffice(BEGIN, "Altra sede", "999999", "OTHER");

    admin = h2Examples.createPerson(office, "admin.sede");
    otherAdmin = h2Examples.createPerson(otherOffice, "admin.altra.sede");
    manager = h2Examples.createPerson(otherOffice, "gestore.buoni");
    grant(admin, office, Role.PERSONNEL_ADMIN);
    grant(otherAdmin, otherOffice, Role.PERSONNEL_ADMIN);
    grant(manager, office, Role.MEAL_TICKET_MANAGER);
    em.flush();

    when(client.forward(any(), any(), any(), any()))
        .thenReturn(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
            .body("{\"ok\":true}".getBytes()));
  }

  private void grant(Person person, Office target, String roleName) {
    person.getUser().setPerson(person);
    Role role = new Role();
    role.setName(roleName);
    em.persist(role);
    UsersRolesOffices uro = new UsersRolesOffices();
    uro.setUser(person.getUser());
    uro.setOffice(target);
    uro.setRole(role);
    em.persist(uro);
    person.getUser().getUsersRolesOffices().add(uro);
    target.getUsersRolesOffices().add(uro);
  }

  private static RequestPostProcessor as(Person person) {
    return jwt().jwt(token -> token
        .claim("azp", "epas-ui")
        .claim("preferred_username", person.getUser().getUsername()));
  }

  @Test
  void anonymousIsRejected() throws Exception {
    mvc.perform(get(BASE + "/offices/" + office.getId() + "/returned"))
        .andExpect(status().isUnauthorized());
    verify(client, never()).forward(any(), any(), any(), any());
  }

  @Test
  void employeeCannotAccessOfficeData() throws Exception {
    mvc.perform(get(BASE + "/offices/" + office.getId() + "/returned").with(as(employee)))
        .andExpect(status().isForbidden());
    verify(client, never()).forward(any(), any(), any(), any());
  }

  @Test
  void adminOfAnotherOfficeIsRejected() throws Exception {
    mvc.perform(get(BASE + "/offices/" + office.getId() + "/returned").with(as(otherAdmin)))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE + "/contracts/" + contract.getId() + "/recap").with(as(otherAdmin)))
        .andExpect(status().isForbidden());
    verify(client, never()).forward(any(), any(), any(), any());
  }

  @Test
  void adminAndMealTicketManagerOfTheOfficeAreForwarded() throws Exception {
    mvc.perform(get(BASE + "/contracts/" + contract.getId() + "/recap").with(as(admin)))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"ok\":true}"));
    mvc.perform(get(BASE + "/contracts/" + contract.getId() + "/recap").with(as(manager)))
        .andExpect(status().isOk());
    verify(client, times(2)).forward(eq(HttpMethod.GET),
        eq("/mealtickets/contracts/{contractId}/recap"),
        eq(Map.of("contractId", contract.getId())), isNull());
  }

  @Test
  @SuppressWarnings("unchecked")
  void adminIdIsAlwaysTheCurrentUser() throws Exception {
    mvc.perform(post(BASE + "/contracts/" + contract.getId() + "/blocks").with(as(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"codeBlock\":\"A1\",\"adminId\":-1}"))
        .andExpect(status().isOk());
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(client).forward(eq(HttpMethod.POST), eq("/mealtickets/contracts/{contractId}/blocks"),
        any(), body.capture());
    Map<String, Object> sent = (Map<String, Object>) body.getValue();
    assertEquals(admin.getId(), sent.get("adminId"));
    assertEquals("A1", sent.get("codeBlock"));
  }

  @Test
  void cardPermissionFollowsItsOwnerOffice() throws Exception {
    when(client.cardOwnerId(5L)).thenReturn(Optional.of(employee.getId()));
    mvc.perform(delete(BASE + "/cards/5").with(as(otherAdmin)))
        .andExpect(status().isForbidden());
    verify(client, never()).forward(any(), any(), any(), any());
    mvc.perform(delete(BASE + "/cards/5").with(as(admin))).andExpect(status().isOk());
    verify(client).forward(eq(HttpMethod.DELETE), eq("/mealticketcards/{id}"),
        eq(Map.of("id", 5L)), isNull());
  }

  @Test
  void cardWithUnknownOwnerAnswerIsBadGateway() throws Exception {
    when(client.cardOwnerId(5L)).thenReturn(Optional.empty());
    mvc.perform(get(BASE + "/cards/5").with(as(admin))).andExpect(status().isBadGateway());
    verify(client, never()).forward(any(), any(), any(), any());
  }

  @Test
  void newCardRequiresPermissionOnPersonAndDeliveryOffice() throws Exception {
    String json = "{\"personId\":%d,\"number\":\"1\",\"deliveryOfficeId\":%d}";
    mvc.perform(post(BASE + "/cards").with(as(admin)).contentType(MediaType.APPLICATION_JSON)
            .content(json.formatted(employee.getId(), otherOffice.getId())))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE + "/cards").with(as(otherAdmin)).contentType(MediaType.APPLICATION_JSON)
            .content(json.formatted(employee.getId(), otherOffice.getId())))
        .andExpect(status().isForbidden());
    verify(client, never()).forward(any(), any(), any(), any());
    mvc.perform(post(BASE + "/cards").with(as(admin)).contentType(MediaType.APPLICATION_JSON)
            .content(json.formatted(employee.getId(), office.getId())))
        .andExpect(status().isOk());
  }

  @Test
  void blockSearchIsLimitedToAnOfficeOfTheUser() throws Exception {
    mvc.perform(get(BASE + "/blocks").param("code", "A1")
            .param("officeId", office.getId().toString()).with(as(otherAdmin)))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE + "/blocks").param("code", "A1").with(as(admin)))
        .andExpect(status().isOk());
    verify(client).forward(eq(HttpMethod.GET), eq("/mealtickets/blocks"),
        eq(Map.of("code", "A1", "officeId", office.getId())), isNull());
  }

  @Test
  void missingResourcesAreNotFound() throws Exception {
    mvc.perform(get(BASE + "/contracts/-1/recap").with(as(admin)))
        .andExpect(status().isNotFound());
  }
}
