/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dao.PersonReperibilityDayDao;
import it.cnr.iit.epas.dao.PersonShiftDayDao;
import it.cnr.iit.epas.dao.WorkingTimeTypeDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dao.wrapper.IWrapperPerson;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.manager.response.AbsenceInsertReport;
import it.cnr.iit.epas.manager.response.AbsencesResponse;
import it.cnr.iit.epas.manager.services.absences.AbsenceService.InsertReport;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.PersonReperibilityDay;
import it.cnr.iit.epas.models.PersonShiftDay;
import it.cnr.iit.epas.models.WorkingTimeType;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import it.cnr.iit.epas.models.enumerate.AbsenceTypeMapping;
import it.cnr.iit.epas.models.enumerate.MealTicketBehaviour;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import java.sql.Blob;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.springframework.stereotype.Component;


/**
 * Manager per le assenze.
 *
 * @author Alessandro Martelli
 */
@Slf4j
@Component
public class AbsenceManager {

  private static final String DATE_NON_VALIDE = "L'intervallo di date specificato non è corretto";
  private final PersonDao personDao;
  private final ContractMonthRecapManager contractMonthRecapManager;
  private final WorkingTimeTypeDao workingTimeTypeDao;
  private final PersonManager personManager;
  private final PersonDayDao personDayDao;
  private final ContractDao contractDao;
  private final AbsenceDao absenceDao;
  private final PersonReperibilityDayDao personReperibilityDayDao;
  private final PersonShiftDayDao personShiftDayDao;
  private final ConsistencyManager consistencyManager;
  private final ConfigurationManager configurationManager;
  private final IWrapperFactory wrapperFactory;
  private final PersonDayManager personDayManager;
  private final AbsenceComponentDao absenceComponentDao;
  private final NotificationManager notificationManager;
  private final SecurityRules rules;
  private final SecureUtils secureUtils;

  /**
   * Costruttore.
   *
   * @param personDayDao              personDayDao
   * @param workingTimeTypeDao        workingTimeTypeDao
   * @param contractDao               contractDao
   * @param absenceDao                absenceDao
   * @param personReperibilityDayDao  personReperibilityDayDao
   * @param personShiftDayDao         personShiftDayDao
   * @param contractMonthRecapManager contractMonthRecapManager
   * @param personManager             personManager
   * @param consistencyManager        consistencyManager
   * @param configurationManager      configurationManager
   * @param wrapperFactory            wrapperFactory
   */
  @Inject
  public AbsenceManager(
      PersonDao personDao,
      PersonDayDao personDayDao,
      WorkingTimeTypeDao workingTimeTypeDao,
      ContractDao contractDao,
      AbsenceDao absenceDao,
      AbsenceComponentDao absenceComponentDao,
      PersonReperibilityDayDao personReperibilityDayDao,
      PersonShiftDayDao personShiftDayDao,
      ContractMonthRecapManager contractMonthRecapManager,
      PersonManager personManager,
      ConsistencyManager consistencyManager,
      ConfigurationManager configurationManager,
      PersonDayManager personDayManager,
      IWrapperFactory wrapperFactory,
      NotificationManager notificationManager,
      SecurityRules rules,
      SecureUtils secureUtils) {
    this.personDao = personDao;
    this.absenceComponentDao = absenceComponentDao;
    this.contractMonthRecapManager = contractMonthRecapManager;
    this.workingTimeTypeDao = workingTimeTypeDao;
    this.personManager = personManager;
    this.personDayDao = personDayDao;
    this.configurationManager = configurationManager;
    this.contractDao = contractDao;
    this.absenceDao = absenceDao;
    this.personReperibilityDayDao = personReperibilityDayDao;
    this.personShiftDayDao = personShiftDayDao;
    this.consistencyManager = consistencyManager;
    this.wrapperFactory = wrapperFactory;
    this.personDayManager = personDayManager;
    this.notificationManager = notificationManager;
    this.rules = rules;
    this.secureUtils = secureUtils;
  }

  /**
   * Salva l'assenza.
   *
   * @param insertReport il report di inserimento assenza
   * @param person la persona per cui salvare l'assenza
   * @param from la data da cui salvare
   * @param recoveryDate se esiste una data entro cui occorre recuperare l'assenza (es.:91CE)
   * @param justifiedType il tipo di giustificazione
   * @param groupAbsenceType il gruppo di appartenenza dell'assenza
   */
  public List<Absence> saveAbsences(InsertReport insertReport, Person person, LocalDate from, 
      LocalDate recoveryDate, JustifiedType justifiedType, GroupAbsenceType groupAbsenceType) {

    List<Absence> newAbsences = Lists.newArrayList();
    //Persistenza
    if (!insertReport.absencesToPersist.isEmpty()) {
      for (Absence absence : insertReport.absencesToPersist) {
        PersonDay personDay = personDayManager
            .getOrCreateAndPersistPersonDay(person, absence.getAbsenceDate());
        absence.setPersonDay(personDay);
        if (justifiedType.getName().equals(JustifiedTypeName.recover_time)) {

          absence = handleRecoveryAbsence(absence, person, recoveryDate);
        }
        personDay.getAbsences().add(absence);
        rules.check("AbsenceGroups.save", absence);
        absenceDao.merge(absence);
        newAbsences.add(absence);
        personDayDao.merge(personDay);

        notificationManager.notificationAbsencePolicy(secureUtils.getCurrentUser().get(),
            absence, groupAbsenceType, true, false, false);

      }
      if (!insertReport.reperibilityShiftDate().isEmpty()) {
        sendReperibilityShiftEmail(person, insertReport.reperibilityShiftDate());
        log.info("Inserite assenze con reperibilità e turni {} {}. Le email sono disabilitate.",
            person.fullName(), insertReport.reperibilityShiftDate());
      }
      log.trace("Prima del lancio dei ricalcoli");
      personDao.getEntityManager().flush();
      log.trace("Flush dell'entity manager effettuata");
      
      consistencyManager.updatePersonSituation(person.getId(), from);
    }
    return newAbsences;
  }
  
  /**
   * Verifica la possibilità che la persona possa usufruire di un riposo compensativo nella data
   * specificata. Se voglio inserire un riposo compensativo per il mese successivo a oggi considero
   * il residuo a ieri. N.B Non posso inserire un riposo compensativo oltre il mese successivo a
   * oggi.
   */
  private boolean canTakeCompensatoryRest(Person person, LocalDate date,
      List<Absence> otherCompensatoryRest) {
    //Data da considerare

    // (1) Se voglio inserire un riposo compensativo per il mese successivo considero il residuo
    // a ieri.
    //N.B Non posso inserire un riposo compensativo oltre il mese successivo.
    LocalDate dateForRecap = date;
    //Caso generale
    if (dateForRecap.getMonthValue() == LocalDate.now().getMonthValue() + 1) {
      dateForRecap = LocalDate.now();
    } else if (dateForRecap.getYear() == LocalDate.now().getYear() + 1
        && dateForRecap.getMonthValue() == 1 && LocalDate.now().getMonthValue() == 12) {
      //Caso particolare dicembre - gennaio
      dateForRecap = LocalDate.now();
    }

    // (2) Calcolo il residuo alla data precedente di quella che voglio considerare.
    if (dateForRecap.getDayOfMonth() > 1) {
      dateForRecap = dateForRecap.minusDays(1);
    }

    Contract contract = contractDao.getContract(dateForRecap, person);

    Optional<YearMonth> firstContractMonthRecap = wrapperFactory
        .create(contract).getFirstMonthToRecap();
    if (!firstContractMonthRecap.isPresent()) {
      //TODO: Meglio ancora eccezione.
      return false;
    }

    ContractMonthRecap cmr = new ContractMonthRecap();
    cmr.year = dateForRecap.getYear();
    cmr.month = dateForRecap.getMonthValue();
    cmr.contract = contract;

    YearMonth yearMonth = YearMonth.from(dateForRecap);

    //Se serve il riepilogo precedente devo recuperarlo.
    Optional<ContractMonthRecap> previousMonthRecap = Optional.<ContractMonthRecap>empty();

    if (yearMonth.isAfter(firstContractMonthRecap.get())) {
      previousMonthRecap = wrapperFactory.create(contract)
          .getContractMonthRecap(yearMonth.minusMonths(1));
      if (!previousMonthRecap.isPresent()) {
        //TODO: Meglio ancora eccezione.
        return false;
      }
    }

    Optional<ContractMonthRecap> recap = contractMonthRecapManager.computeResidualModule(cmr,
        previousMonthRecap, yearMonth, dateForRecap, otherCompensatoryRest, Optional.empty());

    if (recap.isPresent()) {
      int residualMinutes = recap.get().remainingMinutesCurrentYear
          + recap.get().remainingMinutesLastYear;

      return residualMinutes >= workingTimeTypeDao
          .getWorkingTimeType(date, contract.person).get().getWorkingTimeTypeDays()
          .get(date.getDayOfWeek().getValue() - 1).workingTime;
    }
    return false;
  }

  /**
   * Se si vuole solo simulare l'inserimento di una assenza. - no persistenza assenza - no ricalcoli
   * person situation - no invio email per conflitto reperibilità
   */
  public AbsenceInsertReport insertAbsenceSimulation(
      Person person, LocalDate dateFrom, Optional<LocalDate> dateTo, 
      AbsenceType absenceType, Optional<Blob> file, 
      Optional<String> mealTicket, Optional<Integer> justifiedMinutes) {

    return insertAbsence(person, dateFrom, dateTo,  
        absenceType, file, mealTicket, justifiedMinutes,
        true, false);
  }

  /**
   * Metodo full per inserimento assenza. - persistenza assenza - ricalcoli person situation - invio
   * email per conflitto reperibilità
   */
  public AbsenceInsertReport insertAbsenceRecompute(
      Person person, LocalDate dateFrom, Optional<LocalDate> dateTo, 
      Optional<LocalDate> recoveryDate, AbsenceType absenceType, Optional<Blob> file, 
      Optional<String> mealTicket, Optional<Integer> justifiedMinutes) {

    return insertAbsence(person, dateFrom, dateTo, 
        absenceType, file, mealTicket, justifiedMinutes,
        false, true);
  }

  /**
   * Metodo per inserimento assenza senza ricalcoli. (Per adesso utilizzato solo da solari roma per
   * import iniziale di assenze molto indietro nel tempo. Non ritengo ci siano ulteriori utilità
   * future). - persistenza assenza - no ricalcoli person situation - no invio email per conflitto
   * reperibilità
   */
  public AbsenceInsertReport insertAbsenceNotRecompute(
      Person person, LocalDate dateFrom, Optional<LocalDate> dateTo, 
      Optional<LocalDate> recoveryDate, AbsenceType absenceType, Optional<Blob> file, 
      Optional<String> mealTicket, Optional<Integer> justifiedMinutes) {

    return insertAbsence(person, dateFrom, dateTo,  
        absenceType, file, mealTicket, justifiedMinutes,
        false, false);
  }

  private AbsenceInsertReport insertAbsence(
      Person person, LocalDate dateFrom, Optional<LocalDate> dateTo, 
      AbsenceType absenceType, Optional<Blob> file, 
      Optional<String> mealTicket, Optional<Integer> justifiedMinutes, 
      boolean onlySimulation, boolean recompute) {

    Preconditions.checkNotNull(person);
    Preconditions.checkNotNull(absenceType);
    Preconditions.checkNotNull(dateFrom);
    Preconditions.checkNotNull(dateTo);
    Preconditions.checkNotNull(file);
    Preconditions.checkNotNull(mealTicket);

    log.debug("Ricevuta richiesta di inserimento assenza per {}. AbsenceType = {} dal {} al {}, "
            + "mealTicket = {}. Attachment = {}, justifiedMinites = {}", 
            person.fullName(), absenceType.code, dateFrom, dateTo.orElse(dateFrom), mealTicket.orElse(null),
            file.orElse(null), justifiedMinutes.orElse(null));

    AbsenceInsertReport air = new AbsenceInsertReport();

    if (!absenceType.getQualifications().contains(person.getQualification())) {
      log.info("codice {} non utilizzabile per {} con qualifica {}", 
          absenceType, person.getFullname(), person.getQualification());
      air.getWarnings().add(AbsencesResponse.CODICE_NON_UTILIZZABILE);
      return air;
    }


    if (dateTo.isPresent() && dateFrom.isAfter(dateTo.get())) {
      air.getWarnings().add(DATE_NON_VALIDE);
      air.getDatesInTrouble().add(dateFrom);
      air.getDatesInTrouble().add(dateTo.get());
      return air;
    }

    List<Absence> absenceTypeAlreadyExisting = absenceTypeAlreadyExist(
        person, dateFrom, dateTo.orElse(dateFrom), absenceType);
    if (absenceTypeAlreadyExisting.size() > 0) {
      air.getWarnings().add(AbsencesResponse.CODICE_FERIE_GIA_PRESENTE);
      air.getDatesInTrouble().addAll(
          Collections2.transform(absenceTypeAlreadyExisting, AbsenceToDate.INSTANCE));
      return air;
    }

    List<Absence> allDayAbsenceAlreadyExisting =
        absenceDao.allDayAbsenceAlreadyExisting(person, dateFrom, dateTo);
    if (allDayAbsenceAlreadyExisting.size() > 0) {
      air.getWarnings().add(AbsencesResponse.CODICE_GIORNALIERO_GIA_PRESENTE);
      air.getDatesInTrouble().addAll(
          Collections2.transform(allDayAbsenceAlreadyExisting, AbsenceToDate.INSTANCE));
      return air;
    }

    LocalDate actualDate = dateFrom;

    List<Absence> otherAbsences = Lists.newArrayList();

    while (!actualDate.isAfter(dateTo.orElse(dateFrom))) {

      List<AbsencesResponse> aiList = Lists.newArrayList();

      if (AbsenceTypeMapping.RIPOSO_COMPENSATIVO.is(absenceType)) {
        aiList.add(
            handlerCompensatoryRest(
                person, actualDate, absenceType, file, otherAbsences, !onlySimulation));
      } else {
        aiList.add(handlerGenericAbsenceType(person, actualDate, absenceType, file,
            mealTicket, justifiedMinutes, !onlySimulation));
      }

      if (onlySimulation) {
        for (AbsencesResponse ai : aiList) {
          if (ai.getAbsenceAdded() != null) {
            otherAbsences.add(ai.getAbsenceAdded());
          } else {
            log.debug("Simulazione inserimento assenza");
          }
        }
      }

      for (AbsencesResponse ai : aiList) {
        air.add(ai);
      }

      actualDate = actualDate.plusDays(1);
    }

    if (!onlySimulation && recompute) {

      //Al termine dell'inserimento delle assenze aggiorno tutta la situazione dal primo giorno
      //di assenza fino ad oggi
      consistencyManager.updatePersonSituation(person.getId(), dateFrom);

      if (air.getAbsenceInReperibilityOrShift() > 0) {
        sendReperibilityShiftEmail(person, air.datesInReperibilityOrShift());
      }
    }
    return air;
  }

  /**
   * Inserisce l'assenza absenceType nel person day della persona nella data. Se dateFrom = dateTo
   * inserisce nel giorno singolo.
   *
   * @return un resoconto dell'inserimento tramite la classe AbsenceInsertModel
   */
  private AbsencesResponse insert(
      Person person, LocalDate date, AbsenceType absenceType, Optional<Blob> file,
      Optional<Integer> justifiedMinutes, boolean persist) {

    Preconditions.checkNotNull(person);
    Preconditions.checkState(personDao.isPersistent(person));
    Preconditions.checkNotNull(date);
    Preconditions.checkNotNull(absenceType);
    //Preconditions.checkState(absenceType.isPersistent());
    Preconditions.checkNotNull(file);

    AbsencesResponse ar = new AbsencesResponse(date, absenceType.code);

    Absence absence = new Absence();
    absence.date = date;
    absence.absenceType = absenceType;
    if (absence.absenceType.getJustifiedTypesPermitted().size() == 1) {
      absence.justifiedType = absence.absenceType.getJustifiedTypesPermitted().iterator().next();
    } else if (justifiedMinutes.isPresent()) {
      absence.justifiedMinutes = justifiedMinutes.get();
      absence.justifiedType = absenceComponentDao
          .getOrBuildJustifiedType(JustifiedTypeName.specified_minutes);
    } else {
      absence.justifiedType = absenceComponentDao
          .getOrBuildJustifiedType(JustifiedTypeName.all_day);
    }

    //se non devo considerare festa ed è festa non inserisco l'assenza
    if (!absenceType.isConsideredWeekEnd() && personDayManager.isHoliday(person, date)) {
      ar.setHoliday(true);
      ar.setWarning(AbsencesResponse.NON_UTILIZZABILE_NEI_FESTIVI);
      ar.setAbsenceInError(absence);

    } else {
      // check sulla reperibilità e turno
      if (checkIfAbsenceInReperibilityOrInShift(person, date)) {
        ar.setDayInReperibilityOrShift(true);
      }
      //controllo se la persona è in reperibilità
      ar.setDayInReperibility(
          personReperibilityDayDao.getPersonReperibilityDay(person, date).isPresent());
      //controllo se la persona è in turno
      ar.setDayInShift(personShiftDayDao.getPersonShiftDay(person, date).isPresent());

      final PersonDay pd = personDayManager.getOrCreateAndPersistPersonDay(person, date);

      LocalDate startAbsence = null;
      if (file.isPresent()) {
        startAbsence = beginDateToSequentialAbsences(date, person, absenceType);
        if (startAbsence == null) {
          ar.setWarning(AbsencesResponse.PERSONDAY_PRECEDENTE_NON_PRESENTE);
          return ar;
        }
      } else {
        startAbsence = date;
      }

      if (persist) {
        //creo l'assenza e l'aggiungo
        absence.setPersonDay(pd);
        absence.absenceType = absenceType;
        PersonDay beginAbsence = personDayDao.getPersonDay(person, startAbsence).orElse(null);
      //FIXME: ripristinare prima del pasasggio a spring boot
//        if (beginAbsence.getDate().isEqual(date)) {
//          absence.absenceFile = file.orElse(null);
//        } else {
//          for (Absence abs : beginAbsence.getAbsences()) {
//            if (abs.absenceFile == null) {
//              absence.absenceFile = file.orElse(null);
//            }
//          }
//        }

        log.info("Inserita nuova assenza {} per {} in data: {}",
            absence.absenceType.code, absence.getPersonDay().getPerson().getFullname(),
            absence.getPersonDay().getDate());

        pd.getAbsences().add(absence);
        personDayDao.merge(pd);

      } else {
        absence.date = pd.getDate();

        log.debug("Simulato inserimento nuova assenza {} per {} (matricola = {}) in data: {}",
            absence.absenceType.code, pd.getPerson(), pd.getPerson().getNumber(), absence.getDate());
      }

      ar.setAbsenceAdded(absence);
      ar.setAbsenceCode(absenceType.code);
      ar.setInsertSucceeded(true);
    }
    return ar;
  }

  /**
   * Controlla che nell'intervallo passato in args non esistano già assenze per quel tipo.
   */
  private List<Absence> absenceTypeAlreadyExist(Person person, LocalDate dateFrom,
      LocalDate dateTo, AbsenceType absenceType) {

    return absenceDao.findByPersonAndDate(person, dateFrom, Optional.of(dateTo),
        Optional.of(absenceType)).list();
  }

  /**
   * Metodo che invia la mail contenente i giorni in cui ci sono inserimenti di assenza in turno o
   * reperibilità.
   */
  public void sendReperibilityShiftEmail(Person person, List<LocalDate> dates) {
    MultiPartEmail email = new MultiPartEmail();

    try {
      String replayTo = (String) configurationManager
          .configValue(person.getOffice(), EpasParam.EMAIL_TO_CONTACT);

      email.addTo(person.getEmail());
      email.addReplyTo(replayTo);
      email.setSubject("Segnalazione inserimento assenza in giorno con reperibilità/turno");
      String date = "";
      for (LocalDate data : dates) {
        date = date + data + ' ';
      }
      email.setMsg("E' stato richiesto l'inserimento di una assenza per il giorno " + date
          + " per il quale risulta una reperibilità o un turno attivi. \n"
          + "Controllare tramite la segreteria del personale.\n"
          + "\n Servizio ePas");

    } catch (EmailException ex) {
      // TODO GESTIRE L'Eccezione nella generazione dell'email
      ex.printStackTrace();
    }
    //FIXME: correggere prima del passaggio a spring boot
    //Mail.send(email);
  }

  /**
   * controlla se si sta prendendo un codice di assenza in un giorno in cui si è reperibili.
   *
   * @return true se si sta prendendo assenza per un giorno in cui si è reperibili, false altrimenti
   */
  private boolean checkIfAbsenceInReperibilityOrInShift(Person person, LocalDate date) {

    //controllo se la persona è in reperibilità
    Optional<PersonReperibilityDay> prd =
        personReperibilityDayDao.getPersonReperibilityDay(person, date);
    //controllo se la persona è in turno
    Optional<PersonShiftDay> psd = personShiftDayDao.getPersonShiftDay(person, date);

    return psd.isPresent() || prd.isPresent();
  }

  /**
   * Gestisce l'inserimento dei codici 91 (1 o più consecutivi).
   */
  private AbsencesResponse handlerCompensatoryRest(
      Person person, LocalDate date, AbsenceType absenceType,
      Optional<Blob> file, List<Absence> otherAbsences, boolean persist) {

    // I riposi compensativi sono su base annua e non 'per contratto'
    final LocalDate beginOfYear = LocalDate.of(date.getYear(), 1, 1);
    int used = personManager.numberOfCompensatoryRestUntilToday(person, beginOfYear, date)
        + otherAbsences.size();

    Integer maxRecoveryDays;
    if (person.getQualification().getQualification() <= 3) {
      maxRecoveryDays = (Integer) configurationManager
          .configValue(person.getOffice(), EpasParam.MAX_RECOVERY_DAYS_13, date.getYear());
    } else {
      maxRecoveryDays = (Integer) configurationManager
          .configValue(person.getOffice(), EpasParam.MAX_RECOVERY_DAYS_49, date.getYear());
    }

    // Raggiunto il limite dei riposi compensativi utilizzabili
    // maxRecoveryDays = 0 -> nessun vincolo sul numero utilizzabile
    if (maxRecoveryDays != 0 && (used >= maxRecoveryDays)) {
      return new AbsencesResponse(date, absenceType.code,
          String.format(AbsencesResponse.RIPOSI_COMPENSATIVI_ESAURITI + " - Usati %s", used));
    }

    //Controllo del residuo
    if (canTakeCompensatoryRest(person, date, otherAbsences)) {
      return insert(person, date, absenceType, file, Optional.<Integer>empty(), persist);
    }

    return new AbsencesResponse(date, absenceType.code, AbsencesResponse.MONTE_ORE_INSUFFICIENTE);
  }

  private AbsencesResponse handlerGenericAbsenceType(
      Person person, LocalDate date, AbsenceType absenceType, Optional<Blob> file,
      Optional<String> mealTicket, Optional<Integer> justifiedMinutes, boolean persist) {

    AbsencesResponse aim = insert(person, date, absenceType, file, justifiedMinutes, persist);
    if (mealTicket.isPresent() && aim.isInsertSucceeded()) {
      checkMealTicket(date, person, mealTicket.get(), absenceType, persist);
    }

    return aim;
  }

  /**
   * Gestore della logica ticket forzato dall'amministratore, risponde solo in caso di codice 92.
   */
  private void checkMealTicket(LocalDate date, Person person, String mealTicket,
      AbsenceType abt, boolean persist) {

    if (!persist) {
      return;
    }

    Optional<PersonDay> option = personDayDao.getPersonDay(person, date);
    PersonDay pd;
    if (option.isPresent()) {
      pd = option.get();
    } else {
      pd = new PersonDay(person, date);
    }

    if (abt == null || !abt.code.equals("92")) {
      pd.setTicketForcedByAdmin(false);    //una assenza diversa da 92 ha per forza campo calcolato
      personDayDao.persist(pd);
      return;
    }
    if (mealTicket != null && mealTicket.equals("si")) {
      pd.setTicketForcedByAdmin(true);
      pd.setTicketAvailable(MealTicketBehaviour.allowMealTicket);
      personDayDao.persist(pd);
      return;
    }
    if (mealTicket != null && mealTicket.equals("no")) {
      pd.setTicketForcedByAdmin(true);
      pd.setTicketAvailable(MealTicketBehaviour.notAllowMealTicket);
      personDayDao.persist(pd);
      return;
    }

    if (mealTicket != null && mealTicket.equals("calcolato")) {
      pd.setTicketForcedByAdmin(false);
      personDayDao.persist(pd);
      return;
    }
  }
  
  /**
   * Metodo di utilità per popolare correttamente i cmapi dell'absence.
   *
   * @param absence l'assenza
   * @param person la persona
   * @param recoveryDate la data entro cui recuperare il riposo compensativo CE
   * @return l'assenza con aggiunti i campi utili per i riposi compensativi a recupero.
   */
  public Absence handleRecoveryAbsence(Absence absence, Person person, LocalDate recoveryDate) {
    absence.expireRecoverDate = recoveryDate;
    IWrapperPerson wrPerson = wrapperFactory.create(person);
    Optional<WorkingTimeType> wtt = wrPerson.getCurrentWorkingTimeType();
    if (wtt.isPresent()) {
      java.util.Optional<WorkingTimeTypeDay> wttd = wtt.get().getWorkingTimeTypeDays().stream()
          .filter(w -> w.dayOfWeek == absence.getAbsenceDate().getDayOfWeek().getValue()).findFirst();
      if (wttd.isPresent()) {
        absence.timeToRecover = wttd.get().workingTime;
      } else {
        absence.timeToRecover = 432;
      }
      
    }
    return absence;
  }

  /**
   * Rimuove una singola assenza.
   *
   * @param absence l'assenza da rimuovere
   */
  public void removeAbsence(Absence absence) {
    val pd = absence.getPersonDay();
      //FIXME: correggere prima del passaggio a spring boot 
//    if (absence.absenceFile.exists()) {
//      absence.absenceFile.getFile().delete();
//    }
    
    absenceDao.delete(absence);
    pd.getAbsences().remove(absence);
    pd.setWorkingTimeInMission(0);
    pd.setTicketForcedByAdmin(false);
    personDayDao.merge(pd);
    val person = pd.getPerson();
    consistencyManager.updatePersonSituation(person.getId(), pd.getDate());
    log.info("Rimossa assenza del {} per {}", 
        absence.getDate(), absence.getPersonDay().getPerson().getFullname());
  }

  /**
   * Rimuove le assenze della persona nel periodo selezionato per il tipo di assenza.
   *
   * @param person      persona
   * @param dateFrom    data inizio
   * @param dateTo      data fine
   * @param absenceType tipo assenza da rimuovere
   * @return numero di assenze rimosse
   */
  public int removeAbsencesInPeriod(Person person, LocalDate dateFrom,
      LocalDate dateTo, AbsenceType absenceType) {

    LocalDate today = LocalDate.now();
    LocalDate actualDate = dateFrom;
    int deleted = 0;
    while (!actualDate.isAfter(dateTo)) {

      List<PersonDay> personDays =
          personDayDao.getPersonDayInPeriod(person, actualDate, Optional.<LocalDate>empty());
      PersonDay pd = FluentIterable.from(personDays).first().orNull();

      //Costruisco se non esiste il person day
      if (pd == null) {
        actualDate = actualDate.plusDays(1);
        continue;
      }

      List<Absence> absenceList =
          absenceDao
              .getAbsencesInPeriod(
                  Optional.ofNullable(person), actualDate, Optional.<LocalDate>empty(), false);

      for (Absence absence : absenceList) {
        if (absence.absenceType.code.equals(absenceType.code)) {
          //FIXME: correggere prima del passaggio a spring boot
//          if (absence.absenceFile.exists()) {
//            absence.absenceFile.getFile().delete();
//          }
          
          absenceDao.delete(absence);
          pd.getAbsences().remove(absence);
          pd.setWorkingTimeInMission(0);
          pd.setTicketForcedByAdmin(false);
          deleted++;
          personDayDao.merge(pd);
          log.info("Rimossa assenza del {} per {}", actualDate, person.getFullname());
        }
      }
      if (pd.getDate().isAfter(today) && pd.getAbsences().isEmpty() && pd.getStampings().isEmpty()) {
        pd.reset();
        personDayDao.merge(pd);
      }
      actualDate = actualDate.plusDays(1);
    }

    //Al termine della cancellazione delle assenze aggiorno tutta la situazione dal primo
    //giorno di assenza fino ad oggi
    consistencyManager.updatePersonSituation(person.getId(), dateFrom);

    return deleted;
  }

  /**
   * Costruisce la liste delle persone assenti nel periodo indicato.
   *
   * @param absencePersonDays lista di giorni di assenza effettuati
   * @return absentPersons lista delle persone assenti coinvolte nelle assenze passate
   * @author arianna
   */
  public List<Person> getPersonsFromAbsentDays(List<Absence> absencePersonDays) {
    List<Person> absentPersons = new ArrayList<Person>();
    for (Absence abs : absencePersonDays) {
      if (!absentPersons.contains(abs.getPersonDay().getPerson())) {
        absentPersons.add(abs.getPersonDay().getPerson());
      }
    }

    return absentPersons;
  }

  /**
   * La data iniziale di una sequenza consecutiva di assenze dello stesso tipo.
   *
   * @param date        data
   * @param person      persona
   * @param absenceType tipo assenza
   * @return data iniziale.
   */
  private LocalDate beginDateToSequentialAbsences(LocalDate date, Person person,
      AbsenceType absenceType) {

    boolean begin = false;
    LocalDate startAbsence = date;
    while (begin == false) {
      PersonDay pdPrevious = personDayDao.getPreviousPersonDay(person, startAbsence);
      if (pdPrevious == null) {
        log.warn("Non è presente il personday precedente a quello in cui "
            + "si vuole inserire il primo giorno di assenza per il periodo. Verificare");
        return null;
      }
      List<Absence> abList = absenceDao.getAbsencesInPeriod(Optional.ofNullable(person),
          pdPrevious.getDate(), Optional.<LocalDate>empty(), false);
      if (abList.size() == 0) {
        begin = true;
      } else {
        for (Absence abs : abList) {
          if (!abs.absenceType.code.equals(absenceType.code)) {
            begin = true;
          } else {
            startAbsence = startAbsence.minusDays(1);
          }
        }
      }
    }
    return startAbsence;
  }

  /**
   * Function per la trasformazione da Absence a LocalDate.
   */
  public enum AbsenceToDate implements Function<Absence, LocalDate> {
    INSTANCE;

    @Override
    public LocalDate apply(Absence absence) {
      return absence.getPersonDay().getDate();
    }
  }
}
