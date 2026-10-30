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

package it.cnr.iit.epas.controller.v4;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.dto.v4.workflow.AbsenceInsertDto;
import it.cnr.iit.epas.dto.v4.workflow.AbsenceInsertReportDto;
import it.cnr.iit.epas.dto.v4.workflow.ApprovalScopeDto;
import it.cnr.iit.epas.dto.v4.workflow.ApproversCheckDto;
import it.cnr.iit.epas.dto.v4.workflow.GeneralSettingDto;
import it.cnr.iit.epas.dto.v4.workflow.OvertimeApprovalDto;
import it.cnr.iit.epas.dto.v4.workflow.OvertimeSituationDto;
import it.cnr.iit.epas.dto.v4.workflow.ReperibilityChangeDto;
import it.cnr.iit.epas.dto.v4.workflow.TeleworkValidationDto;
import it.cnr.iit.epas.dto.v4.workflow.WorkflowPersonDto;
import it.cnr.iit.epas.dto.v4.workflow.WorkflowUserDto;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.manager.workflow.WorkflowCompetenceSupportManager;
import it.cnr.iit.epas.manager.workflow.WorkflowSupportManager;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.security.NoCheck;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servizi per epas-workflow-service, che gestisce i flussi di approvazione delle richieste.
 *
 * <p>Gli endpoint sono riservati ai client di servizio configurati in
 * epas.security.workflow-client-ids (vedi SecurityConfig), per questo non passano dalle
 * regole drools.</p>
 */
@NoCheck
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION)
@Tag(name = "Workflow controller",
    description = "Servizi riservati a epas-workflow-service per i flussi di approvazione.")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + WorkflowController.PATH)
public class WorkflowController {

  public static final String PATH = "/workflow";

  private final WorkflowSupportManager workflowSupportManager;
  private final WorkflowCompetenceSupportManager competenceSupportManager;
  private final UserDao userDao;
  private final PersonDao personDao;
  private final OfficeDao officeDao;

  @Operation(summary = "Utente individuato dallo username.")
  @GetMapping("/users/byUsername/{username}")
  @Transactional
  public ResponseEntity<WorkflowUserDto> userByUsername(@PathVariable String username) {
    return ResponseEntity.ok(workflowSupportManager.user(
        notFoundIfEmpty(Optional.ofNullable(userDao.byUsername(username)), "User")));
  }

  @Operation(summary = "Utente individuato dall'eppn.")
  @GetMapping("/users/byEppn/{eppn}")
  @Transactional
  public ResponseEntity<WorkflowUserDto> userByEppn(@PathVariable String eppn) {
    return ResponseEntity.ok(workflowSupportManager.user(
        notFoundIfEmpty(userDao.byEppn(eppn), "User")));
  }

  @Operation(summary = "Utente individuato dall'id.")
  @GetMapping("/users/{userId}")
  @Transactional
  public ResponseEntity<WorkflowUserDto> user(@PathVariable Long userId) {
    return ResponseEntity.ok(workflowSupportManager.user(findUser(userId)));
  }

  @Operation(summary = "Persone sulle cui richieste l'utente ha un ruolo di approvazione.")
  @GetMapping("/users/{userId}/approvalScope")
  @Transactional
  public ResponseEntity<ApprovalScopeDto> approvalScope(@PathVariable Long userId) {
    return ResponseEntity.ok(workflowSupportManager.approvalScope(findUser(userId)));
  }

  @Operation(summary = "Informazioni sulla persona usate per configurare i flussi.")
  @GetMapping("/people/{personId}")
  @Transactional
  public ResponseEntity<WorkflowPersonDto> person(@PathVariable Long personId) {
    return ResponseEntity.ok(workflowSupportManager.person(findPerson(personId)));
  }

  @Operation(summary = "Presenza degli approvatori per le richieste della persona.")
  @GetMapping("/people/{personId}/approversCheck")
  @Transactional
  public ResponseEntity<ApproversCheckDto> approversCheck(@PathVariable Long personId) {
    return ResponseEntity.ok(workflowSupportManager.approversCheck(findPerson(personId)));
  }

  @Operation(summary = "Valore di un parametro di configurazione dell'ufficio alla data.")
  @GetMapping("/offices/{officeId}/configurations/{epasParam}")
  @Transactional
  public ResponseEntity<String> configuration(@PathVariable Long officeId,
      @PathVariable EpasParam epasParam,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
    Office office = notFoundIfEmpty(officeDao.byId(officeId), "Office");
    return ResponseEntity.ok(workflowSupportManager.configuration(office, epasParam, date));
  }

  @Operation(summary = "Impostazioni generali usate dai flussi.")
  @GetMapping("/generalSettings")
  public ResponseEntity<GeneralSettingDto> generalSettings() {
    return ResponseEntity.ok(workflowSupportManager.generalSettings());
  }

  @Operation(summary = "Simula l'inserimento delle assenze di una richiesta, senza salvarle.")
  @PostMapping("/people/{personId}/absences/simulate")
  @Transactional
  public ResponseEntity<AbsenceInsertReportDto> simulateAbsences(@PathVariable Long personId,
      @Valid @RequestBody AbsenceInsertDto absences) {
    return ResponseEntity.ok(
        workflowSupportManager.simulateAbsences(findPerson(personId), absences));
  }

  @Operation(summary = "Inserisce le assenze di una richiesta approvata.")
  @PostMapping("/people/{personId}/absences")
  public ResponseEntity<AbsenceInsertReportDto> insertAbsences(@PathVariable Long personId,
      @Valid @RequestBody AbsenceInsertDto absences) {
    return ResponseEntity.ok(
        workflowSupportManager.insertAbsences(findPerson(personId), absences));
  }

  @Operation(summary = "Giorni del periodo in cui la persona è in turno o reperibilità.")
  @GetMapping("/people/{personId}/shiftReperibilityDays")
  @Transactional
  public ResponseEntity<List<LocalDate>> shiftReperibilityDays(@PathVariable Long personId,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
    return ResponseEntity.ok(
        workflowSupportManager.shiftReperibilityDays(findPerson(personId), from, to));
  }

  @Operation(summary = "Situazione degli straordinari della persona per l'anno e il mese.")
  @GetMapping("/people/{personId}/overtimeSituation")
  @Transactional
  public ResponseEntity<OvertimeSituationDto> overtimeSituation(@PathVariable Long personId,
      @RequestParam int year, @RequestParam int month) {
    return ResponseEntity.ok(
        competenceSupportManager.overtimeSituation(findPerson(personId), year, month));
  }

  @Operation(summary = "Ore di straordinario ancora assegnabili dall'approvatore.")
  @GetMapping("/people/{personId}/overtimeAvailable")
  @Transactional
  public ResponseEntity<Integer> overtimeAvailable(@PathVariable Long personId,
      @RequestParam int year, @RequestParam Long approverUserId,
      @RequestParam Optional<Long> managerUserId) {
    return ResponseEntity.ok(competenceSupportManager.overtimeAvailable(findPerson(personId),
        year, findUser(approverUserId), managerUserId.map(this::findUser)));
  }

  @Operation(summary = "Imposta lo straordinario approvato di una richiesta conclusa.")
  @PostMapping("/people/{personId}/overtimes")
  public ResponseEntity<Void> approveOvertime(@PathVariable Long personId,
      @Valid @RequestBody OvertimeApprovalDto overtime) {
    competenceSupportManager.approveOvertime(findPerson(personId), overtime);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Scambia i giorni di reperibilità di una richiesta conclusa.")
  @PostMapping("/people/{personId}/reperibilityChanges")
  @Transactional
  public ResponseEntity<Void> changeReperibility(@PathVariable Long personId,
      @Valid @RequestBody ReperibilityChangeDto change) {
    competenceSupportManager.changeReperibility(findPerson(personId),
        findPerson(change.getTeamMateId()), change);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Registra l'approvazione del telelavoro del mese.")
  @PostMapping("/people/{personId}/teleworkValidations")
  public ResponseEntity<Void> validateTelework(@PathVariable Long personId,
      @Valid @RequestBody TeleworkValidationDto telework) {
    competenceSupportManager.validateTelework(findPerson(personId), telework);
    return ResponseEntity.ok().build();
  }

  private User findUser(Long userId) {
    return notFoundIfEmpty(Optional.ofNullable(userDao.byId(userId)), "User");
  }

  private Person findPerson(Long personId) {
    return notFoundIfEmpty(personDao.byId(personId), "Person");
  }

  private static <T> T notFoundIfEmpty(Optional<T> value, String entity) {
    return value.orElseThrow(() -> new EntityNotFoundException(entity + " not found"));
  }
}
