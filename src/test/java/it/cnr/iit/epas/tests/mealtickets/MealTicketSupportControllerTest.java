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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Servizi per epas-meal-ticket-service: accesso riservato al client di servizio e campi letti
 * dal client del servizio.
 */
@Transactional
@SpringBootTest
@TestPropertySource(properties = "epas.security.oauth2.resourceserverEnabled=true")
class MealTicketSupportControllerTest {

  private static final String BASE = "/rest/v4/mealticketservice";
  private static final LocalDate BEGIN = LocalDate.of(2016, 1, 1);

  @Inject
  private WebApplicationContext context;
  @Inject
  private H2Examples h2Examples;
  @Inject
  private AbsenceService absenceService;
  @Inject
  private EntityManager em;

  private MockMvc mvc;
  private Person person;
  private Contract contract;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    person = h2Examples.normalEmployee(BEGIN, Optional.empty());
    person.setNumber("9535");
    em.flush();
    em.refresh(person);
    contract = person.getContracts().iterator().next();
  }

  private static RequestPostProcessor service() {
    return jwt().jwt(token -> token.claim("azp", "epas-meal-ticket-service"));
  }

  @Test
  void onlyTheServiceClientIsAllowed() throws Exception {
    String url = BASE + "/contracts/" + contract.getId();
    mvc.perform(get(url)).andExpect(status().isUnauthorized());
    mvc.perform(get(url).with(jwt().jwt(t -> t.claim("azp", "epas-workflow-service"))))
        .andExpect(status().isForbidden());
    mvc.perform(get(url).with(service())).andExpect(status().isOk());
  }

  @Test
  void findsPersonByItsKeys() throws Exception {
    Long officeId = person.getOffice().getId();
    mvc.perform(get(BASE + "/people/find").with(service()).param("id", person.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(person.getId()))
        .andExpect(jsonPath("$.surname").value(person.getSurname()))
        .andExpect(jsonPath("$.officeId").value(officeId));
    mvc.perform(get(BASE + "/people/find").with(service()).param("number", "9535"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(person.getId()));
    mvc.perform(get(BASE + "/people/find").with(service()))
        .andExpect(status().isNotFound());
    mvc.perform(get(BASE + "/people/find").with(service()).param("id", "-1"))
        .andExpect(status().isNotFound());
  }

  @Test
  void returnsContractsAndIntervals() throws Exception {
    String personUrl = BASE + "/people/" + person.getId();
    String contractUrl = BASE + "/contracts/" + contract.getId();
    mvc.perform(get(personUrl + "/currentContract").with(service()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(contract.getId()))
        .andExpect(jsonPath("$.personId").value(person.getId()))
        .andExpect(jsonPath("$.beginDate").value(BEGIN.toString()));
    mvc.perform(get(personUrl + "/contract").with(service()).param("date", "2016-06-01"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(contract.getId()));
    mvc.perform(get(personUrl + "/contract").with(service()).param("date", "2010-06-01"))
        .andExpect(status().isNotFound());
    mvc.perform(get(contractUrl + "/previous").with(service()))
        .andExpect(status().isNotFound());
    mvc.perform(get(contractUrl + "/interval").with(service()))
        .andExpect(status().isOk()).andExpect(jsonPath("$.begin").value(BEGIN.toString()));
    mvc.perform(get(contractUrl + "/mealTicketInterval").with(service()))
        .andExpect(status().isOk()).andExpect(jsonPath("$.end").isNotEmpty());
    mvc.perform(get(contractUrl + "/office").with(service()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(person.getOffice().getId()));
  }

  @Test
  void returnsOfficeInformation() throws Exception {
    String officeUrl = BASE + "/offices/" + person.getOffice().getId();
    mvc.perform(get(officeUrl + "/contractIds").with(service()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", Matchers.hasItem(contract.getId().intValue())));
    mvc.perform(get(officeUrl + "/currentBlockType").with(service()))
        .andExpect(status().isNotFound());
    person.getOffice().getConfigurations().stream()
        .filter(conf -> conf.epasParam == EpasParam.MEAL_TICKET_BLOCK_TYPE)
        .forEach(conf -> conf.fieldValue = "papery");
    mvc.perform(get(officeUrl + "/currentBlockType").with(service()))
        .andExpect(status().isOk()).andExpect(content().string("papery"));
    mvc.perform(get(officeUrl + "/configurations/date_start_meal_ticket").with(service()))
        .andExpect(status().isOk())
        .andExpect(content().string(Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}")));
    mvc.perform(get(officeUrl + "/configurations/some_other_param").with(service()))
        .andExpect(status().isNotFound());
    mvc.perform(get(BASE + "/offices/-1/contractIds").with(service()))
        .andExpect(status().isNotFound());
  }

  @Test
  void returnsMonthRecapsWithContractAndPerson() throws Exception {
    ContractMonthRecap recap = new ContractMonthRecap();
    recap.setContract(contract);
    recap.setYear(2016);
    recap.setMonth(3);
    recap.setRemainingMealTickets(7);
    recap.setBuoniPastoUsatiNelMese(4);
    em.persist(recap);
    em.flush();
    em.refresh(contract);

    mvc.perform(get(BASE + "/offices/" + person.getOffice().getId()
            + "/mealTicketRecaps/2016/3").with(service()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].remainingMealTickets").value(7))
        .andExpect(jsonPath("$[0].contract.id").value(contract.getId()))
        .andExpect(jsonPath("$[0].contract.personId").value(person.getId()));
    mvc.perform(get(BASE + "/contracts/" + contract.getId() + "/monthRecaps/2016/3")
            .with(service()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.buoniPastoUsatiNelMese").value(4));
    mvc.perform(get(BASE + "/contracts/" + contract.getId() + "/monthRecaps/2016/4")
            .with(service()))
        .andExpect(status().isNotFound());
  }

  @Test
  void returnsPersonDays() throws Exception {
    mvc.perform(get(BASE + "/people/" + person.getId() + "/personDays").with(service())
            .param("from", "2016-01-01").param("to", "2016-01-31")
            .param("onlyWithMealTicket", "true"))
        .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
  }

  @Test
  void updatesOnlyExistingPeople() throws Exception {
    absenceService.enumInitializator();
    String from = LocalDate.now().minusDays(5).toString();
    mvc.perform(post(BASE + "/people/" + person.getId() + "/updateRecaps").with(service())
            .param("from", from))
        .andExpect(status().isOk());
    mvc.perform(post(BASE + "/people/" + person.getId() + "/updateSituation").with(service())
            .param("from", from))
        .andExpect(status().isOk());
    mvc.perform(post(BASE + "/people/-1/updateRecaps").with(service())
            .param("from", "2016-01-01"))
        .andExpect(status().isNotFound());
    mvc.perform(post(BASE + "/people/-1/updateSituation").with(service())
            .param("from", "2016-01-01"))
        .andExpect(status().isNotFound());
  }
}
