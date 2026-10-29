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

package it.cnr.iit.epas.manager.workflow;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.GeneralSettingDao;
import it.cnr.iit.epas.dao.GroupDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonReperibilityDayDao;
import it.cnr.iit.epas.dao.PersonShiftDayDao;
import it.cnr.iit.epas.dao.RoleDao;
import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.dao.UsersRolesOfficesDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.dto.v4.workflow.AbsenceInsertDto;
import it.cnr.iit.epas.dto.v4.workflow.AbsenceInsertReportDto;
import it.cnr.iit.epas.dto.v4.workflow.ApprovalScopeDto;
import it.cnr.iit.epas.dto.v4.workflow.ApproversCheckDto;
import it.cnr.iit.epas.dto.v4.workflow.GeneralSettingDto;
import it.cnr.iit.epas.dto.v4.workflow.WorkflowPersonDto;
import it.cnr.iit.epas.dto.v4.workflow.WorkflowRoleOfficeDto;
import it.cnr.iit.epas.dto.v4.workflow.WorkflowUserDto;
import it.cnr.iit.epas.manager.AbsenceManager;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.manager.services.absences.AbsenceForm;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.AbsenceService.InsertReport;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * Servizi di supporto ai flussi di approvazione gestiti da epas-workflow-service.
 *
 * <p>Non effettua controlli sui permessi dell'utente: le chiamate arrivano solo dal client
 * di servizio di workflow-service, che verifica chi può fare cosa sulle richieste.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowSupportManager {

  /** Codice del permesso personale orario, con giustificazione a minuti specificati. */
  private static final String PERMESSO_PERSONALE_ORARIO = "661M";

  private final UserDao userDao;
  private final PersonDao personDao;
  private final GroupDao groupDao;
  private final RoleDao roleDao;
  private final UsersRolesOfficesDao uroDao;
  private final GeneralSettingDao generalSettingDao;
  private final ConfigurationManager configurationManager;
  private final AbsenceComponentDao absenceComponentDao;
  private final AbsenceService absenceService;
  private final AbsenceManager absenceManager;
  private final PersonReperibilityDayDao personReperibilityDayDao;
  private final PersonShiftDayDao personShiftDayDao;

  /**
   * L'utente con i suoi ruoli di sistema e sugli uffici.
   */
  public WorkflowUserDto user(User user) {
    val dto = new WorkflowUserDto();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setPersonId(user.getPerson() != null ? user.getPerson().getId() : null);
    dto.getRoles().addAll(user.getRoles());
    dto.setRolesOffices(user.getUsersRolesOffices().stream()
        .map(uro -> new WorkflowRoleOfficeDto(uro.getRole().getName(), uro.getOffice().getId()))
        .collect(Collectors.toList()));
    return dto;
  }

  /**
   * Le persone su cui l'utente ha un ruolo di approvazione: quelle dei gruppi attivi di cui è
   * responsabile e quelle degli uffici in cui è responsabile di sede o amministratore del
   * personale.
   */
  public ApprovalScopeDto approvalScope(User user) {
    val scope = new ApprovalScopeDto();
    if (user.getPerson() != null) {
      scope.setGroupManagerOf(groupDao.groupsByManager(Optional.of(user.getPerson())).stream()
          .flatMap(g -> g.getPeople(LocalDate.now()).stream())
          .map(Person::getId)
          .collect(Collectors.toSet()));
    }
    scope.setSeatSupervisorOf(peopleInOfficesWithRole(user, Role.SEAT_SUPERVISOR));
    scope.setPersonnelAdminOf(peopleInOfficesWithRole(user, Role.PERSONNEL_ADMIN));
    return scope;
  }

  private Set<Long> peopleInOfficesWithRole(User user, String roleName) {
    Set<Office> offices = user.getUsersRolesOffices().stream()
        .filter(uro -> uro.getRole().getName().equals(roleName))
        .map(uro -> uro.getOffice())
        .collect(Collectors.toSet());
    if (offices.isEmpty()) {
      return Set.of();
    }
    return personDao.peopleInOffices(offices).stream()
        .map(p -> p.id)
        .collect(Collectors.toSet());
  }

  /**
   * Le informazioni sulla persona usate per configurare i flussi.
   */
  public WorkflowPersonDto person(Person person) {
    val dto = new WorkflowPersonDto();
    dto.setId(person.getId());
    dto.setFullname(person.getFullname());
    dto.setUserId(person.getUser() != null ? person.getUser().getId() : null);
    if (person.getOffice() != null) {
      dto.setOfficeId(person.getOffice().getId());
      dto.setOfficeName(person.getOffice().getName());
    }
    dto.setTopQualification(person.isTopQualification());
    dto.setGroupManager(person.getUser() != null && person.isGroupManager());
    dto.setSeatSupervisor(person.getUser() != null && person.isSeatSupervisor());
    return dto;
  }

  /**
   * Verifica la presenza degli approvatori per le richieste della persona.
   */
  public ApproversCheckDto approversCheck(Person person) {
    val dto = new ApproversCheckDto();
    dto.setPersonnelAdminPresent(!uroDao.getUsersWithRoleOnOffice(
        roleDao.getRoleByName(Role.PERSONNEL_ADMIN), person.getOffice()).isEmpty());
    dto.setSeatSupervisorPresent(!uroDao.getUsersWithRoleOnOffice(
        roleDao.getRoleByName(Role.SEAT_SUPERVISOR), person.getOffice()).isEmpty());
    dto.setGroupManagerPresent(!groupDao.myGroups(person).isEmpty());
    return dto;
  }

  /**
   * Valore di un parametro di configurazione dell'ufficio alla data indicata.
   */
  public String configuration(Office office, EpasParam epasParam, LocalDate date) {
    return String.valueOf(configurationManager.configValue(office, epasParam, date));
  }

  /**
   * Le impostazioni generali usate dai flussi.
   */
  public GeneralSettingDto generalSettings() {
    val dto = new GeneralSettingDto();
    dto.setEnableAbsenceTopLevelAuthorization(
        generalSettingDao.generalSetting().isEnableAbsenceTopLevelAuthorization());
    return dto;
  }

  /**
   * Simula l'inserimento delle assenze senza salvarle.
   */
  public AbsenceInsertReportDto simulateAbsences(Person person, AbsenceInsertDto absences) {
    if (!insideContracts(person, absences)) {
      return outsideContracts(person, absences);
    }
    val form = buildAbsenceForm(person, absences);
    return report(person, absences, insert(person, form, absences));
  }

  /**
   * Inserisce le assenze di una richiesta approvata, se non ci sono errori critici.
   */
  @Transactional
  public AbsenceInsertReportDto insertAbsences(Person person, AbsenceInsertDto absences) {
    if (!insideContracts(person, absences)) {
      log.warn("Assenze di {} dal {} al {} non inserite: periodo non coperto da contratto",
          person.getFullname(), absences.getFrom(), absences.getTo());
      return outsideContracts(person, absences);
    }
    val form = buildAbsenceForm(person, absences);
    val insertReport = insert(person, form, absences);
    if (insertReport.criticalErrors.isEmpty()) {
      User operator = absences.getOperatorUserId() != null
          ? userDao.byId(absences.getOperatorUserId()) : person.getUser();
      absenceManager.saveAbsences(insertReport, person, form.from, null,
          form.justifiedTypeSelected, form.groupSelected, operator, false);
      log.info("Inserite le assenze della richiesta di {} dal {} al {} per conto di {}",
          person.getFullname(), absences.getFrom(), absences.getTo(),
          operator != null ? operator.getUsername() : null);
    } else {
      log.warn("Assenze di {} dal {} al {} non inserite per errori critici: {}",
          person.getFullname(), absences.getFrom(), absences.getTo(),
          insertReport.criticalErrors);
    }
    return report(person, absences, insertReport);
  }

  /**
   * I giorni del periodo in cui la persona è in turno o in reperibilità.
   */
  public List<LocalDate> shiftReperibilityDays(Person person, LocalDate from, LocalDate to) {
    List<LocalDate> days = Lists.newArrayList();
    for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
      if (personReperibilityDayDao.getPersonReperibilityDay(person, day).isPresent()
          || personShiftDayDao.getPersonShiftDay(person, day).isPresent()) {
        days.add(day);
      }
    }
    return days;
  }

  private AbsenceForm buildAbsenceForm(Person person, AbsenceInsertDto absences) {
    AbsenceType absenceType = null;
    JustifiedType justifiedType = null;
    if (absences.getAbsenceCode() != null) {
      absenceType = absenceComponentDao.absenceTypeByCode(absences.getAbsenceCode())
          .orElseThrow(() -> new EntityNotFoundException(
              "Codice di assenza " + absences.getAbsenceCode() + " non trovato"));
      if (absences.getAbsenceCode().equals(PERMESSO_PERSONALE_ORARIO)
          && absences.getHours() != null && absences.getMinutes() != null) {
        justifiedType =
            absenceComponentDao.getOrBuildJustifiedType(JustifiedTypeName.specified_minutes);
      }
    }
    GroupAbsenceType group = null;
    if (absences.getGroupAbsenceType() != null) {
      group = absenceComponentDao.groupAbsenceTypeByName(absences.getGroupAbsenceType())
          .orElseThrow(() -> new EntityNotFoundException(
              "Gruppo di assenze " + absences.getGroupAbsenceType() + " non trovato"));
    } else if (absenceType != null) {
      group = absenceType.defaultTakableGroup();
    }
    val to = absences.getTo() != null ? absences.getTo() : absences.getFrom();
    return absenceService.buildAbsenceForm(person, absences.getFrom(), null, to, null, group,
        false, absenceType, justifiedType, absences.getHours(), absences.getMinutes(),
        false, true);
  }

  private InsertReport insert(Person person, AbsenceForm form, AbsenceInsertDto absences) {
    return absenceService.insert(person, form.groupSelected, form.from, form.to,
        form.absenceTypeSelected, form.justifiedTypeSelected, absences.getHours(),
        absences.getMinutes(), false, absenceManager);
  }

  /**
   * Il calcolo delle assenze richiede un contratto su tutto il periodo.
   */
  private boolean insideContracts(Person person, AbsenceInsertDto absences) {
    val from = absences.getFrom();
    val to = absences.getTo() != null ? absences.getTo() : from;
    return person.getContracts().stream()
        .anyMatch(c -> c.getRange().contains(from) && c.getRange().contains(to));
  }

  private AbsenceInsertReportDto outsideContracts(Person person, AbsenceInsertDto absences) {
    val dto = new AbsenceInsertReportDto();
    dto.setInsideContracts(false);
    dto.getCriticalErrors().add("Periodo non coperto da nessun contratto del dipendente");
    dto.setMonthAlreadyCertified(
        !person.checkLastCertificationDate(YearMonth.from(absences.getFrom())));
    return dto;
  }

  private AbsenceInsertReportDto report(Person person, AbsenceInsertDto absences,
      InsertReport insertReport) {
    val from = absences.getFrom();
    val to = absences.getTo() != null ? absences.getTo() : from;
    val dto = new AbsenceInsertReportDto();
    dto.setCriticalErrors(insertReport.criticalErrors.stream()
        .map(e -> String.format("%s%s", e.criticalProblem,
            e.date != null ? " (" + e.date + ")" : ""))
        .collect(Collectors.toList()));
    dto.setHowManySuccess(insertReport.howManySuccess());
    dto.setHowManyError(insertReport.howManyError());
    if (!insertReport.insertTemplateRows.isEmpty()) {
      dto.setLastInsertableDate(
          insertReport.insertTemplateRows.get(insertReport.insertTemplateRows.size() - 1).date);
    }
    dto.setReperibilityShiftDates(shiftReperibilityDays(person, from, to));
    dto.setInsideContracts(true);
    dto.setMonthAlreadyCertified(!person.checkLastCertificationDate(YearMonth.from(from)));
    return dto;
  }
}
