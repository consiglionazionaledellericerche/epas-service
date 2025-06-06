/*
 * Copyright (C) 2025  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.manager;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dao.PersonReperibilityDayDao;
import it.cnr.iit.epas.dao.ReperibilityTypeMonthDao;
import it.cnr.iit.epas.dao.history.HistoricalDao;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.messages.Messages;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.PersonReperibility;
import it.cnr.iit.epas.models.PersonReperibilityDay;
import it.cnr.iit.epas.models.PersonReperibilityType;
import it.cnr.iit.epas.models.ReperibilityTypeMonth;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.dto.HolidaysReperibilityDto;
import it.cnr.iit.epas.models.dto.WorkDaysReperibilityDto;
import it.cnr.iit.epas.repo.PersonReperibilityTypeRepository;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * Gestore delle operazioni sulla reperibilità ePAS.
 *
 * @author Dario Tagliaferri
 */

@Slf4j
@Component
public class ReperibilityManager2 {
  @Autowired
  private final PersonReperibilityDayDao reperibilityDayDao;
  @Autowired
  private final PersonDayDao personDayDao;
  private final PersonDayManager personDayManager;
  private final CompetenceDao competenceDao;
  private final PersonReperibilityDayDao reperibilityDao;
  private final ReperibilityTypeMonthDao reperibilityTypeMonthDao;
  private final ConfigurationManager configurationManager;
  private final SecureUtils secureUtils;
  private final PersonReperibilityTypeRepository personReperibilityTypeRepository;
  @Autowired
  private final HistoricalDao historicalDao;
  private final Messages messages;

  /**
   * Injection.
   *
   * @param reperibilityDayDao il dao sui giorni di reperibilità
   * @param personDayDao       il dao sui personday
   * @param personDayManager   il manager coi metodi sul personday
   * @param competenceDao      il dao sulle competenze
   * @param reperibilityDao    il dao sulla reperibilità
   */
  @Inject
  public ReperibilityManager2(PersonReperibilityDayDao reperibilityDayDao,
      PersonDayDao personDayDao, PersonDayManager personDayManager,
      CompetenceDao competenceDao,
      PersonReperibilityDayDao reperibilityDao, ReperibilityTypeMonthDao reperibilityTypeMonthDao,
      ConfigurationManager configurationManager,
      SecureUtils secureUtils, PersonReperibilityTypeRepository personReperibilityTypeRepository,
      HistoricalDao historicalDao,
      Messages messages) {
    this.reperibilityDayDao = reperibilityDayDao;
    this.personDayDao = personDayDao;
    this.personDayManager = personDayManager;
    this.competenceDao = competenceDao;
    this.reperibilityDao = reperibilityDao;
    this.reperibilityTypeMonthDao = reperibilityTypeMonthDao;
    this.configurationManager = configurationManager;
    this.secureUtils = secureUtils;
    this.personReperibilityTypeRepository = personReperibilityTypeRepository;
    this.historicalDao = historicalDao;
    this.messages = messages;
  }

  /**
   * La lista delle attività di reperibilità visibili all'utente che ne fa la richiesta.
   *
   * @return la lista delle attività di reperibilità visibili all'utente che ne fa la richiesta.
   */
  public List<PersonReperibilityType> getUserActivities() {
    List<PersonReperibilityType> activities = Lists.newArrayList();
    User currentUser = secureUtils.getCurrentUser().get();
    Person person = currentUser.getPerson();
    if (person != null) {
      if (!person.getReperibilityTypes().isEmpty()) {
        activities.addAll(person.getReperibilityTypes().stream()
            .sorted(Comparator.comparing(PersonReperibilityType::getDescription))
            .collect(Collectors.toList()));
      }
      if (!person.getReperibilities().isEmpty()) {
        activities.addAll(new ArrayList<>(person.getReperibilities()));
      }
      if (!person.getReperibility().isEmpty()) {
        for (PersonReperibility rep : person.getReperibility()) {
          activities.add(rep.getPersonReperibilityType());
        }
      }
      if (currentUser.hasRoles(Role.PERSONNEL_ADMIN)) {
        activities.addAll(currentUser.getUsersRolesOffices().stream()
            .flatMap(uro -> uro.getOffice().getPersonReperibilityTypes()
                .stream().filter(prt -> !prt.isDisabled())
                .sorted(Comparator.comparing(o -> o.getDescription())))
            .collect(Collectors.toList()));
      }
    } else {
      if (currentUser.isSystemUser()) {
        Iterable<PersonReperibilityType> iterable = 
            personReperibilityTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        Collection<PersonReperibilityType> collection = new ArrayList<>();
        for (PersonReperibilityType item : iterable) {
          collection.add(item);
        }
        activities.addAll(collection);
      }
    }
    return activities.stream().distinct().collect(Collectors.toList());
  }

  /**
   * La lista di tutte le persone abilitate su quell'attività nell'intervallo di tempo specificato.
   *
   * @param reperibilityType attività di reperibilità
   * @param start            data di inizio del periodo
   * @param end              data di fine del periodo
   * @return La lista di tutte le persone abilitate su quell'attività nell'intervallo di tempo
   *     specificato.
   */
  public List<PersonReperibility> reperibilityWorkers(
      PersonReperibilityType reperibilityType, LocalDate start,
      LocalDate end) {
    // reperibilityType.isPersistent()  non trovare il metodo isPersistent
    if (start != null && end != null) {
      return reperibilityType.getPersonReperibilities().stream()
          .filter(pr -> pr.dateRange().isConnected(
              Range.closed(start, end)))
          .collect(Collectors.toList());
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * Salva il personReperibilityDay ed effettua i ricalcoli.
   *
   * @param personReperibilityDay il personReperibilityDay da salvare
   */
  public void save(PersonReperibilityDay personReperibilityDay) {
    log.debug("Richiesta cambio esistente save prd {}", personReperibilityDay.getDate());
    reperibilityDayDao.save(personReperibilityDay);
    recalculate(personReperibilityDay);
  }

  /**
   * Cancella il personReperibilityDay.
   *
   * @param personReperibilityDay il personReperibilityDay da cancellare
   */
  public void delete(PersonReperibilityDay personReperibilityDay) {
    reperibilityDayDao.delete(personReperibilityDay);
    recalculate(personReperibilityDay);
  }

  @Transactional
  private void recalculate(PersonReperibilityDay personReperibilityDay) {

    final PersonReperibilityType reperibilityType = personReperibilityDay.getReperibilityType();

    // Aggiornamento del ReperibilityTypeMonth
    if (reperibilityType != null) {

      //FIXME: servono questi due controlli???
      // Ricalcoli sul turno
      if (reperibilityDayDao.isPersistent(personReperibilityDay)) {
        checkReperibilityValid(personReperibilityDay);
      }

      // Ricalcoli sui giorni coinvolti dalle modifiche
      checkReperibilityDayValid(personReperibilityDay.getDate(), reperibilityType);

      /*
       *  Recupera la data precedente dallo storico e verifica se c'è stato un
       *  cambio di date sul turno. In tal caso effettua il ricalcolo anche
       *  sul giorno precedente (spostamento di un turno da un giorno all'altro)
       */
      historicalDao.lastRevisionsOf(PersonReperibilityDay.class, personReperibilityDay.getId())
          .stream().limit(1).map(historyValue -> {
            PersonReperibilityDay pd = (PersonReperibilityDay) historyValue.value;
            return pd.getDate();
          }).filter(Objects::nonNull).distinct().forEach(localDate -> {
            if (!localDate.equals(personReperibilityDay.getDate())) {
              checkReperibilityDayValid(localDate, reperibilityType);
            }
          });

      // Aggiornamento del relativo ReperibilityTypeMonth (per incrementare il campo version)
      ReperibilityTypeMonth newStatus =
          reperibilityType.monthStatusByDate(personReperibilityDay.getDate())
              .orElse(new ReperibilityTypeMonth());

      if (newStatus.getPersonReperibilityType() != null) {
        newStatus.setUpdatedAt(LocalDateTime.now());
      } else {
        newStatus.setYearMonth(YearMonth.from(personReperibilityDay.getDate()));
        newStatus.setPersonReperibilityType(reperibilityType);
      }
      reperibilityTypeMonthDao.merge(newStatus);

    }
  }

  /**
   * Verifica se un turno puo' essere inserito senza violare le regole dei turni.
   *
   * @param personReperibilityDay il personShiftDay da inserire
   * @return l'eventuale stringa contenente l'errore evidenziato in fase di inserimento del turno.
   */
  public Optional<String> reperibilityPermitted(PersonReperibilityDay personReperibilityDay) {

    /*
     * 0. Verificare se la persona è segnata in quell'attività in quel giorno
     *    return shift.personInactive
     * 1. La Persona non deve essere già reperibile per quel giorno
     * 2. La Persona non deve avere assenze giornaliere.
     * 3. La reperibilità non sia già presente
     * 4. Controllare anche il quantitativo di giorni di reperibilità feriale e festiva massimi?
     */

    log.debug("reperibilityPermitted personReperibilityDay.getPersonReperibility().getPerson() {}",
        personReperibilityDay.getPersonReperibility().getPerson());

    //Verifica se la persona è attiva in quell'attività in quel giorno
    Optional<PersonReperibility> rep = reperibilityDao
        .byPersonDateAndType(personReperibilityDay.getPersonReperibility().getPerson(),
            personReperibilityDay.getDate(), personReperibilityDay.getReperibilityType());
    if (!rep.isPresent()) {
      return Optional.of(messages.get("reperibility.personInactive"));
    }

    // Verifica che la persona non abbia altre reperibilità nello stesso giorno 
    final Optional<PersonReperibilityDay> personReperibility = reperibilityDayDao
        .getPersonReperibilityDay(
            personReperibilityDay.getPersonReperibility().getPerson(),
            personReperibilityDay.getDate());
    log.debug("Richiesta inserimento nuova reperibilità getPersonReperibility {}", 
        personReperibilityDay.getPersonReperibility().getPerson());
    log.debug("Richiesta inserimento nuova reperibilità personReperibilityDay.getDate() {}", 
        personReperibilityDay.getDate());

    log.debug("Richiesta inserimento nuova reperibilità personReperibility.isPresent() {}", 
        personReperibility.isPresent());
    if (personReperibility.isPresent()) {
      return Optional.of(messages.get("reperibility.alreadyInReperibility",
          personReperibility.get().getReperibilityType()));
    }

    // verifica che la persona non sia assente nel giorno
    final Optional<PersonDay> personDay =
        personDayDao.getPersonDay(
            personReperibilityDay.getPersonReperibility().getPerson(),
            personReperibilityDay.getDate());

    if (personDay.isPresent()
        && !personDayManager.isAbsenceCompatibleWithReperibility(personDay.get())) {
      return Optional.of(messages.get("reperibility.absenceInDay"));
    }

    List<PersonReperibilityDay> list = reperibilityDayDao
        .getPersonReperibilityDayFromPeriodAndType(
            personReperibilityDay.getDate(), personReperibilityDay.getDate(),
            personReperibilityDay.getReperibilityType(), Optional.empty());

    //controlla che la reperibilità nel giorno sia già stata assegnata ad un'altra persona
    if (!list.isEmpty()) {
      return Optional.of(messages.get("reperibility.dayAlreadyAssigned",
          personReperibilityDay.getPersonReperibility().getPerson().fullName()));
    }

    return Optional.empty();
  }


  public void checkReperibilityValid(PersonReperibilityDay personReperibilityDay) {
    /*
     * 0. Dev'essere una reperibilità persistente.
     * 1. Non ci siano assenze giornaliere
     * 2. Non ci devono essere già reperibili per quel giorno
     * 3.
     */
    //TODO: va implementato davvero?
  }

  public void checkReperibilityDayValid(LocalDate date, PersonReperibilityType type) {
    //TODO: va implementato davvero?
  }

  /**
   * Ritorna una mappa con i giorni maturati di reperibilità per persona.
   *
   * @param reperibility attività sulla quale effettuare i calcoli
   * @param from         data di inizio da cui calcolare
   * @param to           data di fine
   * @return Restituisce una mappa con i giorni di reperibilità maturati per ogni persona.
   */
  public Map<Person, Integer> calculateReperibilityWorkDaysCompetences(
      PersonReperibilityType reperibility, LocalDate from, LocalDate to) {

    final Map<Person, Integer> reperibilityWorkDaysCompetences = new HashMap<>();

    final LocalDate today = LocalDate.now();

    final LocalDate lastDay;
    LocalDate lastDay1;
    log.trace("reperibility = {}, from={}, to={}", reperibility, from, to);
    log.trace("reperibility.getOffice() = {}", reperibility.getOffice());
    log.trace("EpasParam = {}", EpasParam.ENABLE_REPERIBILITY_APPROVAL_BEFORE_END_MONTH);
    log.trace("EpasParam = {}", configurationManager.configValue(reperibility.getOffice(),
        EpasParam.ENABLE_REPERIBILITY_APPROVAL_BEFORE_END_MONTH));

    try {
      if ((Boolean) configurationManager.configValue(reperibility.getOffice(),
          EpasParam.ENABLE_REPERIBILITY_APPROVAL_BEFORE_END_MONTH)) {
        lastDay1 = to;
      } else {
        if (to.isAfter(today)) {
          lastDay1 = today;
        } else {
          lastDay1 = to;
        }
      }
    } catch (NullPointerException e) {
      // Gestisci l'eccezione in qualche modo, ad esempio assegnando un valore predefinito
      lastDay1 = today;
    }
    lastDay = lastDay1;

    log.trace("reperibility.getMonthlyCompetenceType = {}", 
        reperibility.getMonthlyCompetenceType());
    log.trace("reperibility.getMonthlyCompetenceType = {}", 
        reperibility.getMonthlyCompetenceType().getWorkdaysCode());

    CompetenceCode code = reperibility.getMonthlyCompetenceType().getWorkdaysCode();
    involvedReperibilityWorkers(reperibility, from, to).forEach(person -> {
      int competences =
          calculatePersonReperibilityCompetencesInPeriod(reperibility, person, from, lastDay, code);
      reperibilityWorkDaysCompetences.put(person, competences);
    });

    return reperibilityWorkDaysCompetences;
  }

  /**
   * Una lista di persone che sono effettivamente coinvolte in reperibilità in un determinato
   * periodo (Dipendenti con le reperibilità attive in quel periodo).
   *
   * @param reperibility attività di reperibilità
   * @param from         data di inizio
   * @param to           data di fine
   * @return Una lista di persone che sono effettivamente coinvolte in reperibilità in un
   *     determinato periodo (Dipendenti con le reperibilità attive in quel periodo).
   */
  public List<Person> involvedReperibilityWorkers(PersonReperibilityType reperibility,
      LocalDate from, LocalDate to) {
    return reperibilityDayDao.byTypeAndPeriod(reperibility, from, to)
        .stream().map(rep -> rep.getPerson()).distinct().collect(Collectors.toList());
  }

  /**
   * Il numero di giorni di competenza maturati in base alle reperibilità effettuate nel periodo
   * selezionato (di norma serve calcolarli su un intero mese al massimo).
   *
   * @param reperibility attività di turno
   * @param person       Persona sulla quale effettuare i calcoli
   * @param from         data iniziale
   * @param to           data finale
   * @return il numero di giorni di competenza maturati in base alle reperibilità effettuate nel
   *     periodo selezionato (di norma serve calcolarli su un intero mese al massimo).
   */
  public int calculatePersonReperibilityCompetencesInPeriod(
      PersonReperibilityType reperibility, Person person,
      LocalDate from, LocalDate to, CompetenceCode code) {

    // TODO: 08/06/17 Sicuramente vanno differenziati per tipo di competenza.....
    // c'è sono da capire qual'è la discriminante
    int reperibilityCompetences = 0;
    final List<PersonReperibilityDay> reperibilities = reperibilityDayDao
        .getPersonReperibilityDaysByPeriodAndType(from, to, reperibility, person);

    if (code.equals(reperibility.getMonthlyCompetenceType().getWorkdaysCode())) {
      reperibilityCompetences = (int) reperibilities.stream()
          .filter(rep -> !personDayManager.isHoliday(person, rep.getDate())).count();
    } else {
      reperibilityCompetences = (int) reperibilities.stream()
          .filter(rep -> personDayManager.isHoliday(person, rep.getDate())).count();
    }

    return reperibilityCompetences;
  }

  /**
   * La mappa contenente i giorni di reperibilità festiva per ogni dipendente reperibile.
   *
   * @param reperibility il tipo di reperibilità
   * @param start        la data di inizio da cui conteggiare
   * @param end          la data di fine entro cui conteggiare
   * @return la mappa contenente i giorni di reperibilità festiva per ogni dipendente reperibile.
   */
  public Map<Person, Integer> calculateReperibilityHolidaysCompetences(
      PersonReperibilityType reperibility, LocalDate start, LocalDate end) {

    final Map<Person, Integer> reperibilityHolidaysCompetences = new HashMap<>();

    final LocalDate today = LocalDate.now();

    final LocalDate lastDay;
    LocalDate lastDay1;

    try {
      if ((Boolean) configurationManager.configValue(reperibility.getOffice(),
          EpasParam.ENABLE_REPERIBILITY_APPROVAL_BEFORE_END_MONTH)) {
        lastDay1 = end;
      } else {
        if (end.isAfter(today)) {
          lastDay1 = today;
        } else {
          lastDay1 = end;
        }
      }
    } catch (NullPointerException e) {
      // Gestisci l'eccezione in qualche modo, ad esempio assegnando un valore predefinito
      lastDay1 = today;
    }
    lastDay = lastDay1;

    CompetenceCode code = reperibility.getMonthlyCompetenceType().getHolidaysCode();
    involvedReperibilityWorkers(reperibility, start, end).forEach(person -> {
      int competences = calculatePersonReperibilityCompetencesInPeriod(reperibility,
          person, start, lastDay, code);
      reperibilityHolidaysCompetences.put(person, competences);
    });

    return reperibilityHolidaysCompetences;
  }


  /**
   * Effettua i calcoli delle competenze relative alle reperibilità sulle attività approvate per le
   * persone coinvolte in una certa attività e un determinato mese. Da utilizzare in seguito ad ogni
   * approvazione/disapprovazione delle reperibilità.
   *
   * @param reperibilityTypeMonth lo stato dell'attività di reperibilità in un determinato mese
   */
  public void assignReperibilityCompetences(ReperibilityTypeMonth reperibilityTypeMonth) {
    Verify.verifyNotNull(reperibilityTypeMonth);
    //stabilisco le date di inizio e fine periodo da considerare per i calcoli
    final LocalDate monthBegin = reperibilityTypeMonth.getYearMonth().atDay(1);
    final LocalDate monthEnd = DateUtility.endOfMonth(monthBegin);
    final int year = reperibilityTypeMonth.getYearMonth().getYear();
    final int month = reperibilityTypeMonth.getYearMonth().getMonth().getValue();

    final LocalDate today = LocalDate.now();

    final LocalDate lastDay;

    if ((Boolean) configurationManager.configValue(reperibilityTypeMonth
            .getPersonReperibilityType().getOffice(),
        EpasParam.ENABLE_REPERIBILITY_APPROVAL_BEFORE_END_MONTH)) {
      lastDay = monthEnd;
    } else {
      if (monthEnd.isAfter(today)) {
        lastDay = today;
      } else {
        lastDay = monthEnd;
      }
    }

    //cerco le persone reperibili nel periodo di interesse
    final List<Person> involvedReperibilityPeople = involvedReperibilityWorkers(
        reperibilityTypeMonth.getPersonReperibilityType(), monthBegin, monthEnd);
    CompetenceCode reperibilityHoliday = reperibilityTypeMonth.getPersonReperibilityType()
        .getMonthlyCompetenceType().getHolidaysCode();
    CompetenceCode reperibilityWorkdays = reperibilityTypeMonth.getPersonReperibilityType()
        .getMonthlyCompetenceType().getWorkdaysCode();

    //per ogni persona approvo le reperibilità feriali e festive 
    involvedReperibilityPeople.forEach(person -> {
      WorkDaysReperibilityDto dto = new WorkDaysReperibilityDto();
      dto.person = person;
      dto.workdaysReperibility = calculatePersonReperibilityCompetencesInPeriod(
          reperibilityTypeMonth.getPersonReperibilityType(), person,
          monthBegin, lastDay, reperibilityWorkdays);
      dto.workdaysPeriods = getReperibilityPeriod(person, monthBegin, monthEnd,
          reperibilityTypeMonth.getPersonReperibilityType(), false);

      HolidaysReperibilityDto dto2 = new HolidaysReperibilityDto();
      dto2.person = person;
      dto2.holidaysReperibility = calculatePersonReperibilityCompetencesInPeriod(
          reperibilityTypeMonth.getPersonReperibilityType(), person,
          monthBegin, lastDay, reperibilityHoliday);
      dto2.holidaysPeriods = getReperibilityPeriod(person, monthBegin, monthEnd,
          reperibilityTypeMonth.getPersonReperibilityType(), true);

      Optional<Competence> reperibilityHolidayCompetence = competenceDao
          .getCompetence(person, year, month, reperibilityHoliday);

      Competence holidayCompetence = reperibilityHolidayCompetence
          .orElse(new Competence(person, reperibilityHoliday, year, month));
      holidayCompetence.setValueApproved(dto2.holidaysReperibility);
      holidayCompetence.setReason(getReperibilityDates(dto2.holidaysPeriods));
      competenceDao.merge(holidayCompetence);

      log.info("Salvata {}", holidayCompetence);

      Optional<Competence> reperibilityWorkdaysCompetence = competenceDao
          .getCompetence(person, year, month, reperibilityWorkdays);

      Competence workdayCompetence = reperibilityWorkdaysCompetence
          .orElse(new Competence(person, reperibilityWorkdays, year, month));
      workdayCompetence.setValueApproved(dto.workdaysReperibility);
      workdayCompetence.setReason(getReperibilityDates(dto.workdaysPeriods));
      competenceDao.merge(workdayCompetence);

      log.info("Salvata {}", workdayCompetence);
    });

  }

  /**
   * La lista dei range di date in cui un dipendente è stato reperibile.
   *
   * @param person   il reperibile
   * @param begin    la data da cui cercare i giorni di reperibilità
   * @param end      la data entro cui cercare i giorni di reperibilità
   * @param type     l'attività su cui è reperibile il dipendente
   * @param holidays true se occcorre filtrare sui giorni false se occorre filtrare sui feriali
   * @return la lista dei range di date in cui un dipendente è stato reperibile.
   */
  public List<Range<LocalDate>> getReperibilityPeriod(Person person, LocalDate begin,
      LocalDate end, PersonReperibilityType type, boolean holidays) {

    List<PersonReperibilityDay> days = reperibilityDao
        .getPersonReperibilityDaysByPeriodAndType(begin, end, type, person);

    List<PersonReperibilityDay> newList = null;
    if (holidays) {
      newList = days.stream().filter(
          day -> personDayManager.isHoliday(person, day.getDate())).collect(Collectors.toList());
    } else {
      newList = days.stream().filter(
          day -> !personDayManager.isHoliday(person, day.getDate())).collect(Collectors.toList());
    }
    if (newList.isEmpty()) {
      return null;
    }
    LocalDate first = newList.get(0).getDate();
    List<Range<LocalDate>> list = Lists.newArrayList();
    Range<LocalDate> range = null;

    for (PersonReperibilityDay day : newList) {
      if (first.equals(day.getDate())) {
        range = Range.closed(day.getDate(), day.getDate());
      } else {
        if (day.getDate().equals(range.upperEndpoint().plusDays(1))) {
          range = Range.closed(range.lowerEndpoint(), day.getDate());
        } else {
          list.add(range);
          range = Range.closed(day.getDate(), day.getDate());
        }
      }
    }
    list.add(range);
    return list;
  }

  /**
   * La stringa formattata contenente le date dei giorni di reperibilità effettuati.
   *
   * @param list la lista dei periodi di reperibilità all'interno del mese
   * @return la stringa formattata contenente le date dei giorni di reperibilità effettuati.
   */
  private String getReperibilityDates(List<Range<LocalDate>> list) {
    String str = "";
    if (list == null || list.isEmpty()) {
      return str;
    }
    for (Range<LocalDate> range : list) {
      str = str + range.lowerEndpoint().getDayOfMonth()
          + "-" + range.upperEndpoint().getDayOfMonth() + "/"
          + range.lowerEndpoint().getMonth().name() + " ";
    }
    return str;
  }

}