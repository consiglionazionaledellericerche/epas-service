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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.MealTicketDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.manager.cache.CompetenceCodeManager;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.manager.services.mealtickets.MealTicketsServiceImpl.MealTicketOrder;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.TimeVariation;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import org.springframework.stereotype.Component;

/**
 * Contentitore per le funzionalità relative ai ContractMonthRecap.
 *
 * @author Alessandro Martelli
 */
@Component
public class ContractMonthRecapManager {

  @Inject
  private MealTicketDao mealTicketDao;
  @Inject
  private PersonDayDao personDayDao;
  @Inject
  private CompetenceDao competenceDao;
  @Inject
  private CompetenceCodeManager competenceCodeManager;
  @Inject
  private AbsenceDao absenceDao;
  @Inject
  private Provider<IWrapperFactory> wrapperFactory;
  @Inject
  private ConfigurationManager configurationManager;

  /**
   * Nota bene: il metodo non effettua salvataggi ma solo assegnamenti.<br> Nota bene 2: il metodo
   * assume che se il precedente riepilogo è absent() significa che non serve ai fini della
   * computazione. Il controllo di fornire il parametro corretto è demandato al chiamante. <br>
   *
   * <p>Aggiorna i campi del riepilogo contenuto in cmr, nella parte residuale ovvero:<br> - residui
   * anno corrente <br> - residui anno passato <br> - buoni pasto rimanenti <br> alla data
   * calcolaFinoA.<br>
   * </p>
   * 
   * <p>Durante la computazione memorizza anche le seguenti informazioni in cmr: <br> - progressivo
   * finale totale, positivo e negativo del mese <br> - straordinari s1, s2, s3 assegnati nel
   * mese<br> - riposi compensativi effettuati in numero e minuti totali<br> - buoni pasto
   * consegnati ed consumati nel mese <br>
   * </p>
   *
   * @param recapPreviousMonth    se presente è il riepilogo precedente.
   * @param otherCompensatoryRest altri riposi compensativi non persistiti nel db.
   * @param compensatoryRestClosureSeatRecovering variazioni orarie per recuperare i riposi 
   *      compensativi per chiusura ente.
   */
  public Optional<ContractMonthRecap> computeResidualModule(ContractMonthRecap cmr,
      Optional<ContractMonthRecap> recapPreviousMonth, YearMonth yearMonth, LocalDate calcolaFinoA,
      List<Absence> otherCompensatoryRest, 
      Optional<List<TimeVariation>> compensatoryRestClosureSeatRecovering) {

    Contract contract = cmr.contract;

    //TODO: controllare che in otherCompensatoryRest ci siano solo riposi compensativi.

    //////////////////////////////////////////////////////////////////////////
    //Valori dei minuti residui all'inizio del mese richiesto
    // Se è il mese di beginContrat i residui sono 0
    int initMonteOreAnnoPassato = 0;
    int initMonteOreAnnoCorrente = 0;
    if (recapPreviousMonth.isPresent()) {
      //Se ho il riepilogo del mese precedente lo utilizzo.
      if (recapPreviousMonth.get().month == 12) {
        initMonteOreAnnoPassato =
            recapPreviousMonth.get().remainingMinutesCurrentYear
                + recapPreviousMonth.get().remainingMinutesLastYear;

      } else {
        initMonteOreAnnoCorrente = recapPreviousMonth.get().remainingMinutesCurrentYear;
        initMonteOreAnnoPassato = recapPreviousMonth.get().remainingMinutesLastYear;
      }
    }
    if (contract.sourceDateResidual != null
        && contract.sourceDateResidual.getYear() == yearMonth.getYear()
        && contract.sourceDateResidual.getMonthValue() == yearMonth.getMonthValue()) {
      //Se è il primo riepilogo dovuto ad inzializzazione utilizzo i dati
      //in source
      initMonteOreAnnoPassato = contract.sourceRemainingMinutesLastYear;
      initMonteOreAnnoCorrente = contract.sourceRemainingMinutesCurrentYear;
    }

    //////////////////////////////////////////////////////////////////////////
    //Valori dei buoni pasto residui all'inizio del mese richiesto
    // Se è il mese di beginContrat i residui sono 0
    cmr.buoniPastoDaInizializzazione = 0;
    if (recapPreviousMonth.isPresent()) {
      //Se ho il riepilogo del mese precedente lo utilizzo.
      cmr.buoniPastoDalMesePrecedente =
          recapPreviousMonth.get().remainingMealTickets;
    }
    //Se è il primo riepilogo dovuto ad inzializzazione utilizzo i dati
    //in source
    if (contract.sourceDateMealTicket != null
        && contract.sourceDateMealTicket.getYear() == yearMonth.getYear()
        && contract.sourceDateMealTicket.getMonthValue() == yearMonth.getMonthValue()) {
      cmr.buoniPastoDaInizializzazione = contract.sourceRemainingMealTicket;
      cmr.buoniPastoDalMesePrecedente = 0;
    }
    //Ma c'è il particolarissimo caso in cui il contratto inizia il primo del mese,
    // non ho definito inizializzazione generale, e voglio impostare il residuo iniziale 
    // (all'ultimo giorno del mese precedente)
    if (contract.sourceDateResidual == null 
        && contract.sourceDateMealTicket != null 
        && YearMonth.from(contract.getBeginDate()).compareTo(yearMonth) == 0
        && contract.getBeginDate().getDayOfMonth() == 1 
        && contract.sourceDateMealTicket.isEqual(contract.getBeginDate().minusDays(1))) {
      
      cmr.buoniPastoDaInizializzazione = 0;
      cmr.buoniPastoDalMesePrecedente = contract.sourceRemainingMealTicket;
    }

    //////////////////////////////////////////////////////////////////////////
    //Inizio Algoritmo
    cmr.wrContract = wrapperFactory.get().create(contract);
    cmr.person = contract.person;
    cmr.qualifica = cmr.person.getQualification().getQualification();

    cmr.initMonteOreAnnoCorrente = initMonteOreAnnoCorrente;
    cmr.initMonteOreAnnoPassato = initMonteOreAnnoPassato;

    //Per stampare a video il residuo da inizializzazione se riferito al mese
    if (contract.sourceDateResidual != null
        && contract.sourceDateResidual.getMonthValue() == cmr.month
        && contract.sourceDateResidual.getYear() == cmr.year) {
      cmr.initResiduoAnnoCorrenteNelMese = contract.sourceRemainingMinutesCurrentYear;
    }

    //Inizializzazione residui
    //Gennaio
    if (cmr.month == 1) {
      cmr.mesePrecedente = null;
      cmr.remainingMinutesLastYear = initMonteOreAnnoPassato;
      cmr.remainingMinutesCurrentYear = initMonteOreAnnoCorrente;

      //se il residuo iniziale e' negativo lo tolgo dal residio mensile positivo
      if (cmr.remainingMinutesLastYear < 0) {
        cmr.progressivoFinalePositivoMeseAux = cmr.progressivoFinalePositivoMeseAux
            + cmr.remainingMinutesLastYear;
        cmr.remainingMinutesLastYear = 0;
      }
    } else {
      cmr.mesePrecedente = recapPreviousMonth;
      cmr.remainingMinutesLastYear = initMonteOreAnnoPassato;
      cmr.remainingMinutesCurrentYear = initMonteOreAnnoCorrente;

      EpasParam param = cmr.qualifica > 3 ? EpasParam.MONTH_EXPIRY_RECOVERY_DAYS_49 :
          EpasParam.MONTH_EXPIRY_RECOVERY_DAYS_13;
      Integer monthExpiryRecoveryDay = (Integer) configurationManager
          .configValue(cmr.person.getOffice(), param, cmr.year);

      if (monthExpiryRecoveryDay != 0 && cmr.month > monthExpiryRecoveryDay) {
        cmr.possibileUtilizzareResiduoAnnoPrecedente = false;
        cmr.remainingMinutesLastYear = 0;
      }
    }

    //Costruzione degli intervalli e recupero delle informazioni dal db.
    DateInterval validDataForPersonDay = buildIntervalForProgressive(yearMonth,
        calcolaFinoA, contract);

    DateInterval validDataForCompensatoryRest =
        buildIntervalForCompensatoryRest(yearMonth, contract);

    DateInterval validDataForMealTickets = buildIntervalForMealTicket(yearMonth,
        contract);

    IWrapperContract wrContract = wrapperFactory.get().create(cmr.contract);

    setMealTicketsInformation(cmr, validDataForMealTickets);
    setPersonDayInformation(cmr, validDataForPersonDay, otherCompensatoryRest);
    setPersonMonthInformation(cmr, wrContract,
        validDataForCompensatoryRest, otherCompensatoryRest, compensatoryRestClosureSeatRecovering);

    //Imputazioni
    assegnaProgressivoFinaleNegativo(cmr);
    assegnaStraordinari(cmr);
    assegnaRiposiCompensativi(cmr);
    assegnaRiposiCompensativiChiusuraEnte(cmr);

    //Correzioni alle imputazioni

    //1) All'anno corrente imputo:
    //sia ciò che ho imputato al residuo del mese precedente dell'anno corrente
    //sia ciò che ho imputato al progressivo finale positivo del mese
    //perchè non ho interesse a visualizzarli separati nel template.
    cmr.progressivoFinaleNegativoMeseImputatoAnnoCorrente =
        cmr.progressivoFinaleNegativoMeseImputatoAnnoCorrente
            + cmr.progressivoFinaleNegativoMeseImputatoProgressivoFinalePositivoMese;
    cmr.riposiCompensativiMinutiImputatoAnnoCorrente =
        cmr.riposiCompensativiMinutiImputatoAnnoCorrente
            + cmr.riposiCompensativiMinutiImputatoProgressivoFinalePositivoMese;

    //2) Al monte ore dell'anno corrente aggiungo:
    // ciò che non ho utilizzato del progressivo finale positivo del mese
    cmr.remainingMinutesCurrentYear = cmr.remainingMinutesCurrentYear
        + cmr.progressivoFinalePositivoMeseAux;

    return Optional.ofNullable(cmr);
  }

  /**
   * Costruisce l'intervallo dei giorni da considerare per il calcolo dei progressivi.<br> 1) Parto
   * dall'intero intervallo del mese.<br> 2) Nel caso di calcolo riepilogo mese attuale considero i
   * giorni fino a ieri. (se oggi è il primo giorno del mese ritorna l'intervallo vuoto)<br> 3)
   * Riduco ulteriormente in base al parametro calcolaFinoA e sull'intervallo del contratto nel
   * database.
   */
  private DateInterval buildIntervalForProgressive(YearMonth yearMonth,
      LocalDate calcolaFinoA, Contract contract) {

    LocalDate firstDayOfRequestedMonth =
        LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1);
    DateInterval requestInterval =
        new DateInterval(firstDayOfRequestedMonth, calcolaFinoA);

    DateInterval contractDatabaseInterval =
        wrapperFactory.get().create(contract).getContractDatabaseInterval();

    LocalDate today = LocalDate.now();

    //Parto da tutti i giorni del mese
    LocalDate monthBegin = LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1);
    LocalDate monthEnd = DateUtility.endOfMonth(monthBegin);
    DateInterval monthInterval = new DateInterval(monthBegin, monthEnd);

    //Filtro se mese attuale
    if (DateUtility.isDateIntoInterval(today, monthInterval)) {

      if (today.getDayOfMonth() != 1) {

        //Se oggi non è il primo giorno del mese allora
        //tutti i giorni del mese fino a ieri.

        monthEnd = today.minusDays(1);
        monthInterval = new DateInterval(monthBegin, monthEnd);
      } else {

        //Se oggi è il primo giorno del mese allora nessun giorno.
        monthInterval = null;
      }
    }

    //Filtro per dati nel database e estremi del contratto
    DateInterval validDataForPersonDay = null;
    if (monthInterval != null) {
      validDataForPersonDay = DateUtility
          .intervalIntersection(monthInterval, requestInterval);
      validDataForPersonDay = DateUtility
          .intervalIntersection(validDataForPersonDay, contractDatabaseInterval);
    }

    return validDataForPersonDay;
  }

  /**
   * Costruisce l'intervallo dei giorni da considerare per il conteggio dei riposi compensativi
   * utilizzati.<br> 1) Parto dall'intero intervallo del mese.<br> 2) Nel caso di calcolo riepilogo
   * mese attuale considero tutti i giorni del mese attuale e di quello successivo<br> 3) Riduco
   * ulteriormente in base agli estremi del contratto nel database.
   */
  private DateInterval buildIntervalForCompensatoryRest(YearMonth yearMonth,
      Contract contract) {

    DateInterval contractDatabaseInterval =
        wrapperFactory.get().create(contract).getContractDatabaseInterval();

    LocalDate today = LocalDate.now();

    //Parto da tutti i giorni del mese
    LocalDate monthBegin = LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1);
    LocalDate monthEnd = DateUtility.endOfMonth(monthBegin);
    DateInterval monthInterval = new DateInterval(monthBegin, monthEnd);

    //Nel caso del mese attuale considero anche il mese successivo
    if (DateUtility.isDateIntoInterval(today, monthInterval)) {
      monthEnd = DateUtility.endOfMonth(monthEnd.plusMonths(1));
      monthInterval = new DateInterval(monthBegin, monthEnd);
    }

    //Filtro per dati nel database e estremi del contratto
    DateInterval validDataForCompensatoryRest = null;
    validDataForCompensatoryRest = DateUtility
        .intervalIntersection(monthInterval, contractDatabaseInterval);
    return validDataForCompensatoryRest;

  }

  /**
   * Costruisce l'intervallo dei giorni da considerare per il calcolo buoni pasto utilizzati.<br> Se
   * l'office della persona non ha una data di inizio utilizzo buoni pasto ritorna un intervallo
   * vuoto (null)<br> 1) Parto dall'intero intervallo del mese.<br> 2) Nel caso di calcolo riepilogo
   * mese attuale considero i giorni fino a oggi. <br> 3) Riduco ulteriormente in base al parametro
   * calcolaFinoA, in base all'inizializzazione del contratto per buoni pasto ed alla data di inizio
   * utilizzo buoni pasto dell'office.
   */
  private DateInterval buildIntervalForMealTicket(YearMonth yearMonth, Contract contract) {

    // FIXME: nel caso di buono pasto attribuito oggi non prenderei il dato
    // in fin dei conti la restrizione sul calcolaFinoA potrebbe essere inutile
    // nel caso dei buoni pasto attribuiti. Valutare

    //LocalDate firstDayOfRequestedMonth =
    //  new LocalDate(yearMonth.getYear(),yearMonth.getMonthOfYear(),1);
    //DateInterval requestInterval = new DateInterval(firstDayOfRequestedMonth, calcolaFinoA);

    LocalDate dateStartMealTicketInOffice = (LocalDate) configurationManager
        .configValue(contract.person.getOffice(), EpasParam.DATE_START_MEAL_TICKET);

    LocalDate today = LocalDate.now();

    //Parto da tutti i giorni del mese
    LocalDate monthBegin = LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1);
    LocalDate monthEnd = DateUtility.endOfMonth(monthBegin);
    DateInterval monthInterval = new DateInterval(monthBegin, monthEnd);

    //Nel caso del calcolo del mese attuale considero dall'inizio
    //del mese fino a oggi.
    if (DateUtility.isDateIntoInterval(today, monthInterval)) {
      monthEnd = today;
      monthInterval = new DateInterval(monthBegin, monthEnd);
    }

    //Filtro per dati nel database, estremi del contratto, inizio utilizzo buoni pasto
    DateInterval contractIntervalForMealTicket =
        wrapperFactory.get().create(contract).getContractDatabaseIntervalForMealTicket();
    DateInterval mealTicketIntervalInOffice =
        DateInterval.withBegin(dateStartMealTicketInOffice, Optional.<LocalDate>empty());

    DateInterval validDataForMealTickets = null;
    if (monthInterval != null) {
      validDataForMealTickets = DateUtility
          .intervalIntersection(monthInterval, contractIntervalForMealTicket);

      validDataForMealTickets = DateUtility
          .intervalIntersection(validDataForMealTickets, mealTicketIntervalInOffice);
    }

    return validDataForMealTickets;

  }

  /**
   * Assegna i seguenti campi del riepilogo mensile: <br> cmr.progressivoFinaleMese <br>
   * cmr.progressivoFinalePositivoMeseAux <br> cmr.progressivoFinaleNegativoMese <br>
   */
  private void setPersonDayInformation(ContractMonthRecap cmr,
      DateInterval validDataForPersonDay, List<Absence> otherCompensatoryRests) {

    if (validDataForPersonDay != null) {

      // TODO: implementare un metodo che no fa fetch di stampings... in
      // questo caso non servono.

      List<PersonDay> pdList = personDayDao.getPersonDayInPeriodDesc(
          cmr.person, validDataForPersonDay.getBegin(),
          Optional.ofNullable(validDataForPersonDay.getEnd()));

      //progressivo finale fine mese
      for (PersonDay pd : pdList) {
        if (pd != null) {
          cmr.progressivoFinaleMese = pd.getProgressive();
          break;
        } else {
          //
        }
      }

      //Nei giorni in cui simulo l'inserimento di riposi compensativi la differenza è zero.
      Set<LocalDate> otherCompensatoryDates = Sets.newHashSet(); 
      for (Absence otherCompensatoryRest : otherCompensatoryRests) {
        otherCompensatoryDates.add(otherCompensatoryRest.getAbsenceDate());
      }
      
      //progressivo finale positivo e negativo mese
      for (PersonDay pd : pdList) {
        if (otherCompensatoryDates.contains(pd.getDate())) {
          continue;
        }
        if (pd.getDifference() >= 0) {
          cmr.progressivoFinalePositivoMeseAux += pd.getDifference();
        } else {
          cmr.progressivoFinaleNegativoMese += pd.getDifference();
        }
        cmr.oreLavorate += pd.getTimeAtWork();
      }
      cmr.progressivoFinaleNegativoMese =
          cmr.progressivoFinaleNegativoMese * -1;

      cmr.progressivoFinalePositivoMese =
          cmr.progressivoFinalePositivoMeseAux;

    }
  }

  /**
   * Assegna i seguenti campi del riepilogo mensile: <br> cmr.buoniPastoUsatiNelMese <br>
   * cmr.buoniPastoConsegnatiNelMese <br> cmr.remainingMealTickets <br>
   *
   * @param validDataForMealTickets l'intervallo all'interno del quale ricercare i person day per
   *                                buoni pasto utilizzati e buoni pasto consegnati.
   */
  private void setMealTicketsInformation(ContractMonthRecap cmr,
      DateInterval validDataForMealTickets) {

    if (validDataForMealTickets != null) {
      List<PersonDay> pdList = personDayDao.getPersonDayInPeriod(cmr.person,
          validDataForMealTickets.getBegin(),
          Optional.ofNullable(validDataForMealTickets.getEnd()));

      //buoni pasto utilizzati
      for (PersonDay pd : pdList) {
        if (pd != null && pd.isTicketAvailable()) {
          cmr.buoniPastoUsatiNelMese++;
        }
      }

      //Numero ticket consegnati nel mese
      cmr.buoniPastoConsegnatiNelMese = mealTicketDao
          .contractMealTickets(cmr.contract, Optional.of(validDataForMealTickets),
              MealTicketOrder.ORDER_BY_EXPIRE_DATE_ASC, false).size();
    }

    //residuo
    cmr.remainingMealTickets = cmr.buoniPastoDalMesePrecedente
        + cmr.buoniPastoDaInizializzazione
        + cmr.buoniPastoConsegnatiNelMese
        - cmr.buoniPastoUsatiNelMese;
  }

  /**
   * Assegna i seguenti campi del riepilogo mensile: <br> cmr.straordinariMinuti <br>
   * cmr.straordinariMinutiS1Print <br> cmr.straordinariMinutiS2Print <br>
   * cmr.straordinariMinutiS3Print <br> cmr.riposiCompensativiMinuti <br> cmr.recoveryDayUsed <br>
   *
   * @param validDataForCompensatoryRest l'intervallo all'interno del quale ricercare i riposi
   *                                     compensativi
   * @param otherCompensatoryRest        i riposi compensativi inseriti e non persistiti (usato per
   *                                     le simulazioni di inserimento assenze).
   * @param compensatoryRestClosureSeatRecovering la lista delle variazioni orarie per 
   *      recuperare i riposi compensativi per chiusura ente fatte nel mese.
   */
  private void setPersonMonthInformation(
      ContractMonthRecap cmr, IWrapperContract wrContract,
      DateInterval validDataForCompensatoryRest, List<Absence> otherCompensatoryRest,
      Optional<List<TimeVariation>> compensatoryRestClosureSeatRecovering) {

    //gli straordinari li assegno solo all'ultimo contratto attivo del mese
    if (wrContract.isLastInMonth(cmr.month, cmr.year)) {

      CompetenceCode s1 = competenceCodeManager.getCompetenceCode("S1");
      CompetenceCode s2 = competenceCodeManager.getCompetenceCode("S2");
      CompetenceCode s3 = competenceCodeManager.getCompetenceCode("S3");
      List<CompetenceCode> codes = Lists.newArrayList();
      codes.add(s1);
      codes.add(s2);
      codes.add(s3);

      cmr.straordinariMinuti = 0;
      cmr.straordinariMinutiS1Print = 0;
      cmr.straordinariMinutiS2Print = 0;
      cmr.straordinariMinutiS3Print = 0;

      List<Competence> competences = 
          competenceDao.getCompetences(
              Optional.ofNullable(cmr.person), cmr.year, Optional.ofNullable(cmr.month), codes);

      for (Competence competence : competences) {

        if (competence.competenceCode.getId().equals(s1.getId())) {
          cmr.straordinariMinutiS1Print = (competence.valueApproved * 60);
        } else if (competence.competenceCode.getId().equals(s2.getId())) {
          cmr.straordinariMinutiS2Print = (competence.valueApproved * 60);
        } else if (competence.competenceCode.getId().equals(s3.getId())) {
          cmr.straordinariMinutiS3Print = (competence.valueApproved * 60);
        }
      }

      cmr.straordinariMinuti = cmr.straordinariMinutiS1Print
          + cmr.straordinariMinutiS2Print
          + cmr.straordinariMinutiS3Print;
    }

    if (validDataForCompensatoryRest != null) {

      cmr.riposiCompensativiMinuti = 0;
      cmr.recoveryDayUsed = 0;

      for (Absence riposo : otherCompensatoryRest) {
        if (DateUtility.isDateIntoInterval(riposo.date, validDataForCompensatoryRest)) {

          // TODO: rifattorizzare questa parte. Serve un metodo
          // .getWorkingTimeTypeDay(date) in WrapperContract

          LocalDate date = riposo.date;
          for (ContractWorkingTimeType cwtt :
              wrContract.getValue().getContractWorkingTimeType()) {

            if (DateUtility.isDateIntoInterval(date,
                wrapperFactory.get().create(cwtt).getDateInverval())) {

              WorkingTimeTypeDay wttd = cwtt.getWorkingTimeType().getWorkingTimeTypeDays()
                  .get(date.getDayOfWeek().getValue() - 1);

              Preconditions.checkState(wttd.getDayOfWeek() == date.getDayOfWeek().getValue());
              cmr.riposiCompensativiMinuti += wttd.workingTime;
              cmr.recoveryDayUsed++;
            }
          }
        }
      }

      LocalDate begin = validDataForCompensatoryRest.getBegin();
      LocalDate end = validDataForCompensatoryRest.getEnd();
      List<Absence> riposi =
          absenceDao.absenceInPeriod(cmr.person, begin, end, "91");

      for (Absence abs : riposi) {
        cmr.riposiCompensativiMinuti += wrapperFactory.get().create(abs.getPersonDay())
            .getWorkingTimeTypeDay().get().workingTime;
        cmr.recoveryDayUsed++;
      }

      cmr.riposiCompensativiMinutiPrint = cmr.riposiCompensativiMinuti;
      if (compensatoryRestClosureSeatRecovering.isPresent()) {
        for (TimeVariation tv : compensatoryRestClosureSeatRecovering.get()) {
          if (tv.getDateVariation().getYear() == cmr.year 
              && tv.getDateVariation().getMonthValue() == cmr.month) {
            cmr.riposiCompensativiChiusuraEnteMinuti += tv.getTimeVariation();
          }          
        }
        cmr.riposiCompensativiChiusuraEnteMinutiPrint = cmr.riposiCompensativiChiusuraEnteMinuti;
      }
      
    }
  }

  private void assegnaProgressivoFinaleNegativo(ContractMonthRecap monthRecap) {

    //quello che assegno al monte ore passato
    if (monthRecap.progressivoFinaleNegativoMese < monthRecap.remainingMinutesLastYear) {
      monthRecap.remainingMinutesLastYear =
          monthRecap.remainingMinutesLastYear - monthRecap.progressivoFinaleNegativoMese;
      monthRecap.progressivoFinaleNegativoMeseImputatoAnnoPassato =
          monthRecap.progressivoFinaleNegativoMese;
      return;
    } else {
      monthRecap.progressivoFinaleNegativoMeseImputatoAnnoPassato =
          monthRecap.remainingMinutesLastYear;
      monthRecap.remainingMinutesLastYear = 0;
      monthRecap.progressivoFinaleNegativoMese =
          monthRecap.progressivoFinaleNegativoMese
              -
              monthRecap.progressivoFinaleNegativoMeseImputatoAnnoPassato;
    }

    //quello che assegno al monte ore corrente
    if (monthRecap.progressivoFinaleNegativoMese < monthRecap.remainingMinutesCurrentYear) {
      monthRecap.remainingMinutesCurrentYear =
          monthRecap.remainingMinutesCurrentYear - monthRecap.progressivoFinaleNegativoMese;
      monthRecap.progressivoFinaleNegativoMeseImputatoAnnoCorrente =
          monthRecap.progressivoFinaleNegativoMese;
      return;
    } else {
      monthRecap.progressivoFinaleNegativoMeseImputatoAnnoCorrente =
          monthRecap.remainingMinutesCurrentYear;
      monthRecap.remainingMinutesCurrentYear = 0;
      monthRecap.progressivoFinaleNegativoMese =
          monthRecap.progressivoFinaleNegativoMese
              -
              monthRecap.progressivoFinaleNegativoMeseImputatoAnnoCorrente;
    }

    //quello che assegno al progressivo positivo del mese
    monthRecap.progressivoFinalePositivoMeseAux =
        monthRecap.progressivoFinalePositivoMeseAux - monthRecap.progressivoFinaleNegativoMese;
    monthRecap.progressivoFinaleNegativoMeseImputatoProgressivoFinalePositivoMese =
        monthRecap.progressivoFinaleNegativoMese;
    return;

  }

  private void assegnaStraordinari(ContractMonthRecap monthRecap) {
    monthRecap.progressivoFinalePositivoMeseAux =
        monthRecap.progressivoFinalePositivoMeseAux - monthRecap.straordinariMinuti;
  }

  private void assegnaRiposiCompensativi(ContractMonthRecap monthRecap) {
    //quello che assegno al monte ore passato
    if (monthRecap.riposiCompensativiMinuti < monthRecap.remainingMinutesLastYear) {
      monthRecap.remainingMinutesLastYear =
          monthRecap.remainingMinutesLastYear - monthRecap.riposiCompensativiMinuti;
      monthRecap.riposiCompensativiMinutiImputatoAnnoPassato = monthRecap.riposiCompensativiMinuti;
      return;
    } else {
      monthRecap.riposiCompensativiMinutiImputatoAnnoPassato = monthRecap.remainingMinutesLastYear;
      monthRecap.remainingMinutesLastYear = 0;
      monthRecap.riposiCompensativiMinuti =
          monthRecap.riposiCompensativiMinuti
              -
              monthRecap.riposiCompensativiMinutiImputatoAnnoPassato;
    }

    //quello che assegno al monte ore corrente
    if (monthRecap.riposiCompensativiMinuti < monthRecap.remainingMinutesCurrentYear) {
      monthRecap.remainingMinutesCurrentYear =
          monthRecap.remainingMinutesCurrentYear - monthRecap.riposiCompensativiMinuti;
      monthRecap.riposiCompensativiMinutiImputatoAnnoCorrente =
          monthRecap.riposiCompensativiMinuti;
      return;
    } else {
      monthRecap.riposiCompensativiMinutiImputatoAnnoCorrente =
          monthRecap.remainingMinutesCurrentYear;
      monthRecap.remainingMinutesCurrentYear = 0;
      monthRecap.riposiCompensativiMinuti =
          monthRecap.riposiCompensativiMinuti
              -
              monthRecap.riposiCompensativiMinutiImputatoAnnoCorrente;
    }
    //quello che assegno al progressivo positivo del mese
    monthRecap.progressivoFinalePositivoMeseAux =
        monthRecap.progressivoFinalePositivoMeseAux
            -
            monthRecap.riposiCompensativiMinuti;
    monthRecap.riposiCompensativiMinutiImputatoProgressivoFinalePositivoMese =
        monthRecap.riposiCompensativiMinuti;

  }
  
  private void assegnaRiposiCompensativiChiusuraEnte(ContractMonthRecap monthRecap) {
    //quello che assegno al monte ore passato
    if (monthRecap.riposiCompensativiChiusuraEnteMinuti < monthRecap.remainingMinutesLastYear) {
      monthRecap.remainingMinutesLastYear =
          monthRecap.remainingMinutesLastYear - monthRecap.riposiCompensativiChiusuraEnteMinuti;
      monthRecap.riposiCompensativiChiusuraEnteMinutiImputatoAnnoPassato 
          = monthRecap.riposiCompensativiChiusuraEnteMinuti;
      return;
    } else {
      monthRecap.riposiCompensativiChiusuraEnteMinutiImputatoAnnoPassato 
          = monthRecap.remainingMinutesLastYear;
      monthRecap.remainingMinutesLastYear = 0;
      monthRecap.riposiCompensativiChiusuraEnteMinuti =
          monthRecap.riposiCompensativiChiusuraEnteMinuti
              -
              monthRecap.riposiCompensativiChiusuraEnteMinutiImputatoAnnoPassato;
    }

    //quello che assegno al monte ore corrente
    if (monthRecap.riposiCompensativiChiusuraEnteMinuti < monthRecap.remainingMinutesCurrentYear) {
      monthRecap.remainingMinutesCurrentYear =
          monthRecap.remainingMinutesCurrentYear - monthRecap.riposiCompensativiChiusuraEnteMinuti;
      monthRecap.riposiCompensativiChiusuraEnteMinutiImputatoAnnoCorrente =
          monthRecap.riposiCompensativiChiusuraEnteMinuti;
      return;
    } else {
      monthRecap.riposiCompensativiChiusuraEnteMinutiImputatoAnnoCorrente =
          monthRecap.remainingMinutesCurrentYear;
      monthRecap.remainingMinutesCurrentYear = 0;
      monthRecap.riposiCompensativiChiusuraEnteMinuti =
          monthRecap.riposiCompensativiChiusuraEnteMinuti
              -
              monthRecap.riposiCompensativiChiusuraEnteMinutiImputatoAnnoCorrente;
    }
    //quello che assegno al progressivo positivo del mese
    monthRecap.progressivoFinalePositivoMeseAux =
        monthRecap.progressivoFinalePositivoMeseAux
            -
            monthRecap.riposiCompensativiChiusuraEnteMinuti;
    monthRecap.riposiCompensativiChiusuraEnteMinutiImputatoProgressivoFinalePositivoMese =
        monthRecap.riposiCompensativiChiusuraEnteMinuti;
  }
}