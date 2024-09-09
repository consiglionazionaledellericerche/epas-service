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

package it.cnr.iit.epas.dao.wrapper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;

/**
 * Contract con alcune funzionalità aggiuntive.
 *
 * @author Marco Andreini
 */
@Component
public class WrapperContract implements IWrapperContract {

  private Contract value;
  private final AbsenceDao absenceDao;
  
  @Inject
  WrapperContract(AbsenceDao absenceDao) {
    this.absenceDao = absenceDao;
  }

  @Override
  public Contract getValue() {
    return value;
  }

  public IWrapperContract setValue(Contract contract) {
    value = contract;
    return this;
  }

  /**
   * True se il contratto è l'ultimo contratto della persona per mese e anno selezionati.
   *
   * @param month mese
   * @param year  anno
   * @return esito.
   */
  @Override
  public boolean isLastInMonth(int month, int year) {

    DateInterval monthInterval = new DateInterval(LocalDate.of(year, month, 1),
        DateUtility.endOfMonth(LocalDate.of(year, month, 1)));

    for (Contract contract : value.person.getContracts()) {
      if (contract.getId().equals(value.getId())) {
        continue;
      }
      DateInterval contractInterval = getContractDateInterval();
      if (DateUtility.intervalIntersection(monthInterval, contractInterval) != null) {
        if (value.getBeginDate().isBefore(contract.getBeginDate())) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean isActive() {
    return DateUtility.isDateIntoInterval(LocalDate.now(), getContractDateInterval());
  }

  /**
   * True se il contratto è a tempo determinato.
   *
   * @return esito.
   */
  @Override
  public boolean isDefined() {

    return this.value.getEndDate() != null;
  }

  /**
   * L'intervallo effettivo per il contratto. Se endContract (data terminazione del contratto) è
   * presente sovrascrive contract.endDate.
   *
   * @return l'intervallo.
   */
  @Override
  public DateInterval getContractDateInterval() {
    if (value.getEndContract() != null) {
      return new DateInterval(value.getBeginDate(), value.getEndContract());
    } else {
      return new DateInterval(value.getBeginDate(), value.getEndDate());
    }
  }

  /**
   * L'intervallo ePAS per il contratto. Se è presente una data di inizializzazione generale del
   * contratto sovrascrive contract.beginDate.
   *
   * @return l'intervallo.
   */
  @Override
  public DateInterval getContractDatabaseInterval() {

    // TODO: verificare il funzionamento.
    // Assumo che initUse in configurazione sia ininfluente perchè se definita
    // allora automaticamente deve essere definito sourceContract.

    DateInterval contractInterval = getContractDateInterval();
    if (value.getSourceDateResidual() != null) {
      return new DateInterval(value.getSourceDateResidual(),
          contractInterval.getEnd());
    }

    return contractInterval;
  }

  /**
   * L'intervallo ePAS per il contratto dal punto di vista dei buoni pasto. Se è presente una data
   * di inizializzazione buoni pasto del contratto sovrascrive contract.beginDate.
   *
   * @return l'intervallo.
   */
  @Override
  public DateInterval getContractDatabaseIntervalForMealTicket() {

    DateInterval contractDatebaseInterval = getContractDatabaseInterval();
    if (value.getSourceDateMealTicket() != null) {
      return new DateInterval(value.getSourceDateMealTicket(),
          contractDatebaseInterval.getEnd());
    }

    return contractDatebaseInterval;
  }

  /**
   * Il mese del primo riepilogo esistente per il contratto. absent() se non ci sono i dati per
   * costruire il primo riepilogo.
   *
   * @return il mese (absent se non esiste).
   */
  @Override
  public Optional<YearMonth> getFirstMonthToRecap() {

    if (initializationMissing()) {
      return Optional.<YearMonth>empty();
    }
    if (value.getSourceDateResidual() != null) {
      return Optional.ofNullable((YearMonth.from(value.getSourceDateResidual())));
    }
    return Optional.ofNullable(YearMonth.from(value.getBeginDate()));
  }

  /**
   * Il mese dell'ultimo riepilogo esistente per il contratto (al momento della chiamata).<br> Il
   * mese attuale se il contratto termina dopo di esso. Altrimenti il mese di fine contratto.
   *
   * @return il mese
   */
  @Override
  public YearMonth getLastMonthToRecap() {
    YearMonth currentMonth = YearMonth.from(LocalDate.now());
    YearMonth lastMonth = YearMonth.from(getContractDateInterval().getEnd());
    if (currentMonth.isAfter(lastMonth)) {
      return lastMonth;
    }
    return currentMonth;
  }
  
  /**
   * Se il contratto è stato inizializzato per la parte residuale nel mese passato come argomento.
   *
   * @param yearMonth mese
   * @return esito
   */
  @Override
  public boolean residualInitInYearMonth(YearMonth yearMonth) {
    if (value.getSourceDateResidual() == null) {
      return false;
    }
    return YearMonth.from(value.getSourceDateResidual()).equals(yearMonth);
  }

  /**
   * Il riepilogo mensile attualmente persistito (se esiste).
   *
   * @param yearMonth mese
   * @return il riepilogo (absent se non presente)
   */
  @Override
  public Optional<ContractMonthRecap> getContractMonthRecap(YearMonth yearMonth) {

    for (ContractMonthRecap cmr : value.contractMonthRecaps) {
      if (cmr.year == yearMonth.getYear()
          && cmr.month == yearMonth.getMonthValue()) {
        return Optional.ofNullable(cmr);
      }
    }
    return Optional.empty();
  }

  /**
   * Se sono state definite sia la inizializzazione generale che quella buoni pasto e quella dei
   * buoni pasto è precedente a quella generale.
   *
   * @return esito
   */
  @Override
  public boolean mealTicketInitBeforeGeneralInit() {
    if (value.getSourceDateResidual() != null && value.getSourceDateMealTicket() != null
        && value.getSourceDateResidual().isAfter(value.getSourceDateMealTicket())) {
      return true;
    } else {
      return false;
    }
  }


  /**
   * Se il contratto necessita di inizializzazione.
   *
   * @return esito
   */
  @Override
  public boolean initializationMissing() {

    LocalDate dateForInit = dateForInitialization();

    if (value.getSourceDateResidual() != null) {
      return false;
    }

    return value.getBeginDate().isBefore(dateForInit);
  }

  /**
   * Se per il contratto c'è almeno un riepilogo necessario non persistito.
   *
   * @return esito.
   */
  @Override
  public boolean monthRecapMissing() {

    Optional<YearMonth> monthToCheck = getFirstMonthToRecap();
    if (!monthToCheck.isPresent()) {
      return true;
    }
    YearMonth nowMonth = YearMonth.now();
    while (!monthToCheck.get().isAfter(nowMonth)) {
      // FIXME: renderlo efficiente, un dao che li ordina.
      if (!getContractMonthRecap(monthToCheck.get()).isPresent()) {
        return true;
      }
      monthToCheck = Optional.ofNullable(monthToCheck.get().plusMonths(1));
    }

    return false;
  }

  /**
   * Se un riepilogo per il mese passato come argomento non è persistito.
   *
   * @param yearMonth mese
   * @return esito.
   */
  @Override
  public boolean monthRecapMissing(YearMonth yearMonth) {
    return !getContractMonthRecap(yearMonth).isPresent();
  }

  /**
   * La data più recente tra creazione del contratto e creazione della persona.
   *
   * @return La data più recente tra la creazione del contratto e la creazione della persona.
   */
  @Override
  public LocalDate dateForInitialization() {

    LocalDate officeBegin = value.person.getOffice().getBeginDate();
    LocalDate personCreation = LocalDate.from(value.person.getBeginDate());
    LocalDate candidate = value.getBeginDate();

    if (candidate.isBefore(officeBegin)) {
      candidate = officeBegin;
    }
    if (candidate.isBefore(personCreation)) {
      candidate = personCreation;
    }

    return candidate;
  }
  
  /**
   * La data più recente per l'inizializzazione.
   *
   * @return La data più recente tra la creazione del contratto e la creazione della persona.
   */
  @Override
  public LocalDate dateForMealInitialization() {

    if (initializationMissing()) {
      return null;
    }
    
    if (value.getSourceDateResidual() != null) {
      return value.getSourceDateResidual();
    }
    
    return value.getBeginDate().minusDays(1);
  }

  /**
   * Se il contratto è finito prima che la sede della persona fosse installata in ePAS.
   *
   * @return esito
   */
  @Override
  public boolean noRelevant() {

    LocalDate officeInstallation = value.person.getOffice().getBeginDate();

    return officeInstallation.isAfter(getContractDateInterval().getEnd());
  }

  /**
   * Se il contratto ha tutti i riepiloghi necessari per calcolare il riepilogo per l'anno passato
   * come parametro.
   *
   * @param yearToRecap anno
   * @return esito
   */
  @Override
  @Deprecated //legato alla vecchia implementazione ferie
  public boolean hasMonthRecapForVacationsRecap(int yearToRecap) {

    // se non ho il contratto inizializzato il riepilogo ferie non esiste
    //o non è veritiero.
    if (initializationMissing()) {
      return false;
    }

    // se il contratto inizia nell'anno non ho bisogno del recap.
    if (value.getBeginDate().getYear() == yearToRecap) {
      return true;
    }

    // se source date cade nell'anno non ho bisogno del recap.
    if (value.getSourceDateResidual() != null
        && value.getSourceDateResidual().getYear() == yearToRecap) {
      return true;
    }

    // Altrimenti ho bisogno del riepilogo finale dell'anno precedente.
    Optional<ContractMonthRecap> yearMonthToCheck =
        getContractMonthRecap(YearMonth.of(yearToRecap - 1, 12));

    if (yearMonthToCheck.isPresent()) {
      return true;
    }

    return false;
  }

  /**
   * Ritorna la lista delle assenze nell'intervallo di tempo relative al tipo ab.
   *
   * @return la lista di assenze effettuate dal titolare del contratto del tipo ab nell'intervallo
   *     temporale inter.
   */
  public List<Absence> getAbsenceDays(DateInterval inter, Contract contract, AbsenceType ab) {

    DateInterval contractInterInterval =
        DateUtility.intervalIntersection(inter, getContractDateInterval());
    if (contractInterInterval == null) {
      return new ArrayList<Absence>();
    }

    List<Absence> absences =
        absenceDao.getAbsenceByCodeInPeriod(
            Optional.ofNullable(contract.person), Optional.ofNullable(ab.getCode()),
            contractInterInterval.getBegin(), contractInterInterval.getEnd(),
            Optional.<JustifiedTypeName>empty(), false, true);

    return absences;

  }
}
