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

import com.google.common.collect.ImmutableSet;
import it.cnr.iit.epas.dao.CompetenceCodeDao;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.GeneralSettingDao;
import it.cnr.iit.epas.dao.GroupDao;
import it.cnr.iit.epas.dao.OvertimeBudgetDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonReperibilityDayDao;
import it.cnr.iit.epas.dao.TeleworkValidationDao;
import it.cnr.iit.epas.dto.v4.workflow.OvertimeApprovalDto;
import it.cnr.iit.epas.dto.v4.workflow.OvertimeSituationDto;
import it.cnr.iit.epas.dto.v4.workflow.ReperibilityChangeDto;
import it.cnr.iit.epas.dto.v4.workflow.TeleworkValidationDto;
import it.cnr.iit.epas.manager.CompetenceManager;
import it.cnr.iit.epas.manager.ConsistencyManager;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonReperibilityDay;
import it.cnr.iit.epas.models.PersonReperibilityType;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.TeleworkValidation;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.flows.Group;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * Supporto ai flussi delle richieste di competenza e di telelavoro di epas-workflow-service:
 * monte ore di straordinario, approvazione degli straordinari, cambi di reperibilità e
 * validazione del telelavoro.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowCompetenceSupportManager {

  private final OvertimeBudgetDao overtimeBudgetDao;
  private final CompetenceDao competenceDao;
  private final CompetenceCodeDao competenceCodeDao;
  private final CompetenceManager competenceManager;
  private final ConsistencyManager consistencyManager;
  private final GeneralSettingDao generalSettingDao;
  private final GroupDao groupDao;
  private final PersonDao personDao;
  private final PersonReperibilityDayDao reperibilityDayDao;
  private final TeleworkValidationDao teleworkValidationDao;

  /**
   * Situazione degli straordinari della persona per l'anno e il mese della richiesta.
   */
  public OvertimeSituationDto overtimeSituation(Person person, int year, int month) {
    val dto = new OvertimeSituationDto();
    val code = competenceCodeDao.getCompetenceCodeByCode(OvertimeBudgetDao.OVERTIME_CODE);
    competenceDao.getCompetence(person, year, month, code)
        .ifPresent(c -> dto.setApprovedInMonth(c.getValueApproved()));
    dto.setOvertimePerPersonEnabled(
        generalSettingDao.generalSetting().isEnableOvertimePerPerson());
    dto.setPersonResidual(overtimeBudgetDao.personHours(person, year)
        - overtimeBudgetDao.approvedHours(Set.of(person.getId()), year));
    groupDao.myGroups(person).stream().findFirst()
        .ifPresent(g -> dto.setGroupResidual(groupResidual(g, year)));
    dto.setSeatResidual(seatResidual(person.getOffice(), year));
    return dto;
  }

  /**
   * Ore di straordinario ancora assegnabili dall'approvatore: quelle del gruppo per il
   * responsabile di gruppo, quelle della sede per il responsabile di sede.
   *
   * @param person la persona che ha richiesto lo straordinario
   * @param year l'anno della richiesta
   * @param approver l'utente che approva
   * @param manager il responsabile di gruppo che ha già approvato, per la seconda approvazione
   *     del responsabile di sede
   */
  public int overtimeAvailable(Person person, int year, User approver, Optional<User> manager) {
    int available = 0;
    if (approver.hasRoles(Role.GROUP_MANAGER) && approver.getPerson() != null) {
      Optional<Group> group = groupDao.checkManagerPerson(approver.getPerson(), person);
      if (group.isEmpty() && manager.isPresent() && manager.get().getPerson() != null) {
        group = groupDao.checkManagerPerson(manager.get().getPerson(), person);
      }
      if (group.isPresent()) {
        available = groupResidual(group.get(), year);
      }
    }
    if (approver.hasRoles(Role.SEAT_SUPERVISOR) && approver.getPerson() != null) {
      available = seatResidual(approver.getPerson().getOffice(), year);
    }
    return available;
  }

  private int groupResidual(Group group, int year) {
    val people = group.getPeople(LocalDate.now()).stream().map(Person::getId)
        .collect(Collectors.toSet());
    return overtimeBudgetDao.groupHours(group, LocalDate.now().getYear())
        - overtimeBudgetDao.approvedHours(people, year);
  }

  private int seatResidual(Office office, int year) {
    val seatTotal = competenceManager.getTotalOvertime(
        competenceDao.getTotalOvertime(LocalDate.now().getYear(), office));
    val people = personDao.peopleInOffices(ImmutableSet.of(office)).stream()
        .map(p -> p.id).collect(Collectors.toSet());
    return seatTotal - overtimeBudgetDao.approvedHours(people, year);
  }

  /**
   * Imposta lo straordinario approvato del mese e ricalcola la situazione della persona.
   */
  @Transactional
  public Competence approveOvertime(Person person, OvertimeApprovalDto overtime) {
    val code = competenceCodeDao.getCompetenceCodeByCode(OvertimeBudgetDao.OVERTIME_CODE);
    Competence competence = competenceDao
        .getCompetence(person, overtime.getYear(), overtime.getMonth(), code)
        .orElseGet(() -> {
          val created = new Competence(person, code, overtime.getYear(), overtime.getMonth());
          competenceDao.persist(created);
          return created;
        });
    competence.setValueApproved(overtime.getValue());
    competenceDao.merge(competence);
    log.info("Approvate {} ore di straordinario a {} per {}/{}", overtime.getValue(),
        person.getFullname(), overtime.getMonth(), overtime.getYear());
    consistencyManager.updatePersonSituation(person.getId(),
        LocalDate.of(overtime.getYear(), overtime.getMonth(), 1));
    return competence;
  }

  /**
   * Scambia i giorni di reperibilità tra il richiedente e il collega: i giorni da cedere
   * passano al collega, quelli da chiedere passano al richiedente.
   */
  @Transactional
  public void changeReperibility(Person person, Person teamMate, ReperibilityChangeDto change) {
    if (change.getBeginDateToGive().isAfter(change.getEndDateToGive())) {
      throw new ValidationException("Le date dei giorni da cedere non sono congruenti");
    }
    val reperibilityType = reperibilityDayDao.byListOfPerson(List.of(person, teamMate))
        .orElseThrow(() -> new ValidationException(
            "Le due persone non appartengono a uno stesso servizio di reperibilità"));
    val toAsk = change.getBeginDateToAsk() != null && change.getEndDateToAsk() != null;

    removeDays(person, change.getBeginDateToGive(), change.getEndDateToGive());
    if (toAsk) {
      removeDays(teamMate, change.getBeginDateToAsk(), change.getEndDateToAsk());
    }
    reperibilityDayDao.getEntityManager().flush();

    addDays(teamMate, reperibilityType, change.getBeginDateToGive(), change.getEndDateToGive());
    if (toAsk) {
      addDays(person, reperibilityType, change.getBeginDateToAsk(), change.getEndDateToAsk());
    }
    log.info("Scambiati i giorni di reperibilità tra {} e {}: {}", person.getFullname(),
        teamMate.getFullname(), change);
  }

  private void removeDays(Person person, LocalDate from, LocalDate to) {
    for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
      final LocalDate date = day;
      val reperibilityDay = reperibilityDayDao.getPersonReperibilityDay(person, day)
          .orElseThrow(() -> new ValidationException(String.format(
              "%s non è in reperibilità il %s", person.getFullname(), date)));
      reperibilityDayDao.delete(reperibilityDay);
    }
  }

  private void addDays(Person person, PersonReperibilityType type, LocalDate from,
      LocalDate to) {
    for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
      final LocalDate date = day;
      val personReperibility = reperibilityDayDao.byPersonDateAndType(person, day, type)
          .orElseThrow(() -> new ValidationException(String.format(
              "Non è stato possibile inserire la giornata di reperibilità del %s per %s",
              date, person.getFullname())));
      val reperibilityDay = new PersonReperibilityDay();
      reperibilityDay.setDate(day);
      reperibilityDay.setReperibilityType(type);
      reperibilityDay.setPersonReperibility(personReperibility);
      reperibilityDayDao.persist(reperibilityDay);
    }
  }

  /**
   * Registra l'approvazione del telelavoro del mese.
   */
  @Transactional
  public TeleworkValidation validateTelework(Person person, TeleworkValidationDto telework) {
    val validation = new TeleworkValidation();
    validation.setPerson(person);
    validation.setYear(telework.getYear());
    validation.setMonth(telework.getMonth());
    validation.setApproved(true);
    validation.setApprovationDate(LocalDate.now());
    teleworkValidationDao.persist(validation);
    return validation;
  }
}
