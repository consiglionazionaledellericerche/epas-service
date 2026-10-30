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

import it.cnr.iit.epas.dao.CompetenceCodeDao;
import it.cnr.iit.epas.dao.PersonReperibilityDayDao;
import it.cnr.iit.epas.dao.RoleDao;
import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.dto.v4.workflow.AbsenceInsertDto;
import it.cnr.iit.epas.dto.v4.workflow.OvertimeApprovalDto;
import it.cnr.iit.epas.dto.v4.workflow.ReperibilityChangeDto;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.workflow.WorkflowCompetenceSupportManager;
import it.cnr.iit.epas.manager.workflow.WorkflowSupportManager;
import it.cnr.iit.epas.models.MonthlyCompetenceType;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonReperibility;
import it.cnr.iit.epas.models.PersonReperibilityDay;
import it.cnr.iit.epas.models.PersonReperibilityType;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.TeleworkValidation;
import it.cnr.iit.epas.models.UsersRolesOffices;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
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
  @Inject
  private WorkflowCompetenceSupportManager competenceSupportManager;
  @Inject
  private CompetenceCodeDao competenceCodeDao;
  @Inject
  private PersonReperibilityDayDao reperibilityDayDao;
  @Inject
  private RoleDao roleDao;
  @Inject
  private EntityManager em;

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

  @Test
  void approvedOvertimeIsAssignedOnce() throws Exception {
    // competence-codes.xml non valorizza version, che nel db reale ha default 0
    em.createNativeQuery("update competence_codes set version = 0 where code = 'S1'")
        .executeUpdate();
    em.clear();
    absenceService.enumInitializator();
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    val before = competenceSupportManager.overtimeSituation(person, 2016, 11);
    assertEquals(null, before.getApprovedInMonth());

    mvc.perform(post(BASE + "/people/" + person.getId() + "/overtimes")
        .with(client("epas-workflow-service"))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"year\":2016,\"month\":11,\"value\":12}"))
        .andExpect(status().isOk());
    assertEquals(12, competenceSupportManager.overtimeSituation(person, 2016, 11)
        .getApprovedInMonth());

    // una nuova approvazione dello stesso mese aggiorna il valore
    val update = new OvertimeApprovalDto();
    update.setYear(2016);
    update.setMonth(11);
    update.setValue(15);
    competenceSupportManager.approveOvertime(person, update);
    assertEquals(15, competenceSupportManager.overtimeSituation(person, 2016, 11)
        .getApprovedInMonth());
  }

  @Test
  void approvedTeleworkIsValidated() throws Exception {
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());

    mvc.perform(post(BASE + "/people/" + person.getId() + "/teleworkValidations")
        .with(client("epas-workflow-service"))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"year\":2026,\"month\":10}"))
        .andExpect(status().isOk());

    val validations = em.createQuery(
        "select tv from TeleworkValidation tv where tv.person = :person",
        TeleworkValidation.class).setParameter("person", person).getResultList();
    assertEquals(1, validations.size());
    assertEquals(10, validations.get(0).getMonth());
    assertTrue(validations.get(0).isApproved());
  }

  @Test
  void reperibilityChangeSwapsDaysBetweenTeamMates() {
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    Person teamMate = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    Person supervisor = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    val type = reperibilityType(person.getOffice(), supervisor);
    val personService = reperibility(person, type);
    val teamMateService = reperibility(teamMate, type);
    reperibilityDay(personService, MONDAY);
    reperibilityDay(teamMateService, WEDNESDAY);
    em.flush();

    val change = new ReperibilityChangeDto();
    change.setTeamMateId(teamMate.getId());
    change.setBeginDateToGive(MONDAY);
    change.setEndDateToGive(MONDAY);
    change.setBeginDateToAsk(WEDNESDAY);
    change.setEndDateToAsk(WEDNESDAY);
    competenceSupportManager.changeReperibility(person, teamMate, change);
    em.flush();

    assertTrue(reperibilityDayDao.getPersonReperibilityDay(person, MONDAY).isEmpty());
    assertTrue(reperibilityDayDao.getPersonReperibilityDay(teamMate, WEDNESDAY).isEmpty());
    assertTrue(reperibilityDayDao.getPersonReperibilityDay(person, WEDNESDAY).isPresent());
    assertTrue(reperibilityDayDao.getPersonReperibilityDay(teamMate, MONDAY).isPresent());

    // la fixture non imposta i lati inversi delle relazioni, che arrivano dal db
    em.refresh(type);
    em.refresh(supervisor);
    em.refresh(supervisor.getUser());
    val scope = workflowSupportManager.approvalScope(supervisor.getUser());
    assertEquals(Set.of(person.getId(), teamMate.getId()), scope.getReperibilitySupervisorOf());
  }

  @Test
  void seatSupervisorScopeContainsPeopleOfTheOffice() {
    Person employee = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());
    Person supervisor = h2Examples.createPerson(employee.getOffice(), "supervisor");
    val uro = new UsersRolesOffices();
    uro.setUser(supervisor.getUser());
    uro.setOffice(employee.getOffice());
    uro.setRole(seatSupervisorRole());
    em.persist(uro);
    em.flush();
    em.refresh(supervisor.getUser());

    val scope = workflowSupportManager.approvalScope(supervisor.getUser());
    assertTrue(scope.getSeatSupervisorOf().contains(employee.getId()));
    assertTrue(scope.getPersonnelAdminOf().isEmpty());
  }

  private Role seatSupervisorRole() {
    val existing = roleDao.getRoleByName(Role.SEAT_SUPERVISOR);
    if (existing != null) {
      return existing;
    }
    val role = new Role();
    role.setName(Role.SEAT_SUPERVISOR);
    em.persist(role);
    return role;
  }

  private PersonReperibilityType reperibilityType(Office office, Person supervisor) {
    val code = competenceCodeDao.getCompetenceCodeByCode("S1");
    val competenceType = new MonthlyCompetenceType();
    competenceType.name = "Reperibilità";
    competenceType.workdaysCode = code;
    competenceType.holidaysCode = code;
    em.persist(competenceType);
    val type = new PersonReperibilityType();
    type.setDescription("Servizio di prova");
    type.setOffice(office);
    type.setSupervisor(supervisor);
    type.setMonthlyCompetenceType(competenceType);
    em.persist(type);
    return type;
  }

  private PersonReperibility reperibility(Person person, PersonReperibilityType type) {
    val reperibility = new PersonReperibility();
    reperibility.setPerson(person);
    reperibility.setPersonReperibilityType(type);
    reperibility.setStartDate(LocalDate.of(2016, 1, 1));
    em.persist(reperibility);
    return reperibility;
  }

  private void reperibilityDay(PersonReperibility reperibility, LocalDate date) {
    val day = new PersonReperibilityDay();
    day.setPersonReperibility(reperibility);
    day.setReperibilityType(reperibility.getPersonReperibilityType());
    day.setDate(date);
    em.persist(day);
  }

  private static AbsenceInsertDto vacation() {
    val dto = new AbsenceInsertDto();
    dto.setGroupAbsenceType(DefaultGroup.FERIE_CNR.name());
    dto.setFrom(MONDAY);
    dto.setTo(WEDNESDAY);
    return dto;
  }
}
