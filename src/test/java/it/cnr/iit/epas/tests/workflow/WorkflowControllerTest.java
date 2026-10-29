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

package it.cnr.iit.epas.tests.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.dto.v4.workflow.AbsenceInsertDto;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.workflow.WorkflowSupportManager;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Servizi per epas-workflow-service: accesso riservato al client di servizio e inserimento
 * delle assenze delle richieste approvate.
 */
@Transactional
@SpringBootTest
@TestPropertySource(properties = "epas.security.oauth2.resourceserverEnabled=true")
class WorkflowControllerTest {

  private static final String BASE = "/rest/v4/workflow";
  private static final LocalDate MONDAY = LocalDate.of(2016, 11, 7);
  private static final LocalDate WEDNESDAY = LocalDate.of(2016, 11, 9);

  @Inject
  private WebApplicationContext context;
  @Inject
  private H2Examples h2Examples;
  @Inject
  private AbsenceService absenceService;
  @Inject
  private AbsenceComponentDao absenceComponentDao;
  @Inject
  private WorkflowSupportManager workflowSupportManager;
  @Inject
  private UserDao userDao;

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  private static RequestPostProcessor client(String clientId) {
    return jwt().jwt(token -> token.claim("azp", clientId));
  }

  @Test
  void onlyWorkflowClientCanCallWorkflowEndpoints() throws Exception {
    mvc.perform(get(BASE + "/generalSettings")
        .with(client("epas-workflow-service")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enableAbsenceTopLevelAuthorization").exists());

    mvc.perform(get(BASE + "/generalSettings").with(client("epas-ui")))
        .andExpect(status().isForbidden());

    mvc.perform(get(BASE + "/generalSettings"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void personAndUser() throws Exception {
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    // la fixture non imposta il lato inverso user.person, che arriva dal db
    userDao.refresh(person.getUser());

    mvc.perform(get(BASE + "/people/" + person.getId())
        .with(client("epas-workflow-service")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(person.getId()))
        .andExpect(jsonPath("$.officeId").value(person.getOffice().getId()))
        .andExpect(jsonPath("$.userId").value(person.getUser().getId()))
        .andExpect(jsonPath("$.topQualification").value(false));

    mvc.perform(get(BASE + "/users/byUsername/" + person.getUser().getUsername())
        .with(client("epas-workflow-service")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.personId").value(person.getId()));

    mvc.perform(get(BASE + "/users/" + person.getUser().getId() + "/approvalScope")
        .with(client("epas-workflow-service")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groupManagerOf").isEmpty())
        .andExpect(jsonPath("$.seatSupervisorOf").isEmpty());

    mvc.perform(get(BASE + "/people/0").with(client("epas-workflow-service")))
        .andExpect(status().isNotFound());
  }

  @Test
  void simulateDoesNotInsertAbsences() throws Exception {
    absenceService.enumInitializator();
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());

    mvc.perform(post(BASE + "/people/" + person.getId() + "/absences/simulate")
        .with(client("epas-workflow-service"))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"groupAbsenceType\":\"FERIE_CNR\",\"from\":\"2016-11-07\","
            + "\"to\":\"2016-11-09\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.criticalErrors").isEmpty())
        .andExpect(jsonPath("$.howManySuccess").value(3))
        .andExpect(jsonPath("$.insideContracts").value(true))
        .andExpect(jsonPath("$.monthAlreadyCertified").value(false))
        .andExpect(jsonPath("$.lastInsertableDate").value("2016-11-09"));

    // la simulazione non ha salvato nulla: la stessa richiesta è ancora tutta inseribile
    val again = workflowSupportManager.simulateAbsences(person, vacation());
    assertEquals(3, again.getHowManySuccess());
  }

  @Test
  void insertSavesAbsencesOnce() {
    absenceService.enumInitializator();
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    assertTrue(absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).isPresent());

    val insertDto = vacation();
    insertDto.setOperatorUserId(person.getUser().getId());
    val report = workflowSupportManager.insertAbsences(person, insertDto);
    assertTrue(report.getCriticalErrors().isEmpty());
    assertEquals(3, report.getHowManySuccess());

    // i giorni sono ora occupati dalle assenze inserite
    val again = workflowSupportManager.simulateAbsences(person, vacation());
    assertFalse(again.getHowManySuccess() == 3);
  }

  @Test
  void requestOutsideContractsIsReported() {
    absenceService.enumInitializator();
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1),
        Optional.of(LocalDate.of(2016, 11, 8)));

    val report = workflowSupportManager.simulateAbsences(person, vacation());
    assertFalse(report.isInsideContracts());
    assertFalse(report.getCriticalErrors().isEmpty());
  }

  private static AbsenceInsertDto vacation() {
    val dto = new AbsenceInsertDto();
    dto.setGroupAbsenceType(DefaultGroup.FERIE_CNR.name());
    dto.setFrom(MONDAY);
    dto.setTo(WEDNESDAY);
    return dto;
  }
}
