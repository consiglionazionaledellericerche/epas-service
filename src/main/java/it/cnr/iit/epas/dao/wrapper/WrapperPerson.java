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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dao.PersonMonthRecapDao;
import it.cnr.iit.epas.manager.CompetenceManager;
import it.cnr.iit.epas.manager.PersonManager;
import it.cnr.iit.epas.models.CertificatedData;
import it.cnr.iit.epas.models.Certification;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.ContractStampProfile;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.VacationPeriod;
import it.cnr.iit.epas.models.WorkingTimeType;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Wrapper per la person.
 * Il @RequestScope è necessario per avere un'istanza diversa per ogni richesta, altrimenti
 * questo componente potrebbe portarsi dietro variabili d'istanza popolate in altre richieste.
 *
 * @author Marco Andreini
 */
@Component
@RequestScope
public class WrapperPerson implements IWrapperPerson {

  private Person value;
  private final ContractDao contractDao;
  //private final CompetenceManager competenceManager;
  private final PersonManager personManager;
  private final PersonDao personDao;
  private final PersonDayDao personDayDao;
  private final PersonMonthRecapDao personMonthRecapDao;
  private final Provider<IWrapperFactory> wrapperFactory;
  private final CompetenceDao competenceDao;
  private final Provider<EntityManager> emp;
  private final Provider<CompetenceManager> competenceManager;

  private List<Contract> sortedContracts;
  private Optional<Contract> currentContract;
  private Optional<Contract> previousContract;
  private Optional<WorkingTimeType> currentWorkingTimeType;
  private Optional<VacationPeriod> currentVacationPeriod;
  private Optional<ContractStampProfile> currentContractStampProfile;
  private Optional<ContractWorkingTimeType> currentContractWorkingTimeType;

  private Optional<Boolean> properSynchronized = Optional.empty();

  @Inject
  WrapperPerson(
      ContractDao contractDao,
      //CompetenceManager competenceManager,
      PersonManager personManager,
      PersonDao personDao, PersonMonthRecapDao personMonthRecapDao,
      PersonDayDao personDayDao, CompetenceDao competenceDao,
      Provider<IWrapperFactory> wrapperFactory, Provider<EntityManager> emp,
      Provider<CompetenceManager> competenceManager) {
    this.contractDao = contractDao;
    this.competenceManager = competenceManager;
    this.personManager = personManager;
    this.personDao = personDao;
    this.personMonthRecapDao = personMonthRecapDao;
    this.personDayDao = personDayDao;
    this.competenceDao = competenceDao;
    this.wrapperFactory = wrapperFactory;
    this.emp = emp;
  }

  @Override
  public Person getValue() {
    return value;
  }

  public IWrapperPerson setValue(Person person) {
    this.value = person;
    return this;
  }
  
  @Override
  public boolean isActiveInDay(LocalDate date) {
    for (Contract contract : orderedMonthContracts(date.getYear(), date.getMonthValue())) {
      if (DateUtility.isDateIntoInterval(date, contract.periodInterval())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isActiveInMonth(YearMonth yearMonth) {
    return getFirstContractInMonth(yearMonth.getYear(), yearMonth.getMonthValue()) != null;
  }

  /**
   * Calcola il contratto attualmente attivo.
   *
   * @return il contratto attualmente attivo per quella persona
   */
  @Override
  public Optional<Contract> getCurrentContract() {

    if (currentContract != null) {
      return currentContract;
    }
    if (currentContract == null) {
      currentContract = Optional.ofNullable(
          contractDao.getContract(LocalDate.now(), value));
    }

    return currentContract;
  }

  @Override
  public List<Contract> orderedMonthContracts(int year, int month) {

    List<Contract> contracts = Lists.newArrayList();

    LocalDate monthBegin = LocalDate.of(year, month, 1);
    DateInterval monthInterval = new DateInterval(monthBegin,
        DateUtility.endOfMonth(monthBegin));

    for (Contract contract : orderedContracts()) {
      if (DateUtility.intervalIntersection(monthInterval, wrapperFactory
          .get().create(contract).getContractDateInterval()) != null) {
        contracts.add(contract);
      }
    }
    return contracts;
  }

  @Override
  public List<Contract> orderedYearContracts(int year) {

    List<Contract> contracts = Lists.newArrayList();
    DateInterval yearInterval = new DateInterval(LocalDate.of(year, 1, 1),
        LocalDate.of(year, 12, 31));

    for (Contract contract : orderedContracts()) {
      if (DateUtility.intervalIntersection(yearInterval, wrapperFactory
          .get().create(contract).getContractDateInterval()) != null) {
        contracts.add(contract);
      }
    }
    return contracts;
  }

  @Override
  public List<Contract> orderedContracts() {
    if (sortedContracts != null) {
      return sortedContracts;
    }
    SortedMap<LocalDate, Contract> contracts = Maps.newTreeMap();
    for (Contract contract : value.getContracts()) {
      contracts.put(contract.getBeginDate(), contract);
    }
    sortedContracts = Lists.newArrayList(contracts.values());
    return sortedContracts;
  }

  /**
   * L'ultimo contratto attivo nel mese, se esiste.
   *
   * @param year l'anno
   * @param month il mese
   * @return l'ultimo contratto attivo nel mese.
   */
  @Override
  public Optional<Contract> getLastContractInMonth(int year, int month) {

    List<Contract> contractInMonth = orderedMonthContracts(year, month);

    if (contractInMonth.isEmpty()) {
      return Optional.empty();
    }

    return Optional.ofNullable(contractInMonth.get(contractInMonth.size() - 1));
  }

  /**
   * Il primo contratto attivo nel mese se esiste.
   *
   * @param year l'anno
   * @param month il mese
   * @return il primo contratto attivo nel mese.
   */
  @Override
  public Optional<Contract> getFirstContractInMonth(int year, int month) {

    List<Contract> contractInMonth = orderedMonthContracts(year, month);

    if (contractInMonth.isEmpty()) {
      return Optional.empty();
    }

    return Optional.ofNullable(contractInMonth.get(0));
  }

  /**
   * L'ultimo mese con contratto attivo.
   */
  @Override
  public YearMonth getLastActiveMonth() {

    Optional<Contract> lastContract = personDao.getLastContract(value);

    // Importante per sinc con Perseo:
    // devo assumere che la persona abbia almeno un contratto
    // attivo in ePAS. Altrimenti non dovrebbe essere in ePAS.
    Preconditions.checkState(lastContract.isPresent());

    YearMonth current = YearMonth.now();
    YearMonth contractBegin = YearMonth.from(lastContract.get().getBeginDate());

    if (contractBegin.isAfter(current)) {
      //vado in avanti
      while (true) {
        if (isActiveInMonth(current)) {
          return current;
        }
        current = current.plusMonths(1);
      }
    } else {
      //vado indietro
      while (true) {
        if (isActiveInMonth(current)) {
          return current;
        }
        current = current.minusMonths(1);
      }
    }
  }

  /**
   * True se la persona è passata da determinato a indeterminato durante l'anno.
   */
  @Override
  public boolean hasPassToIndefiniteInYear(int year) {

    List<Contract> orderedContractInYear = personDao.getContractList(value,
        LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));


    boolean hasDefinite = false;
    boolean hasPassToIndefinite = false;

    for (Contract contract : orderedContractInYear) {
      if (contract.getEndDate() != null) {
        hasDefinite = true;
      }

      if (hasDefinite && contract.getEndDate() == null) {
        hasPassToIndefinite = true;
      }
    }

    return hasPassToIndefinite;
  }

  @Override
  public Optional<ContractStampProfile> getCurrentContractStampProfile() {

    if (currentContractStampProfile != null) {
      return currentContractStampProfile;
    }

    if (currentContract == null) {
      getCurrentContract();
    }

    if (!currentContract.isPresent()) {
      return Optional.empty();
    }

    currentContractStampProfile = currentContract.get()
        .getContractStampProfileFromDate(LocalDate.now());

    return currentContractStampProfile;
  }

  @Override
  public Optional<WorkingTimeType> getCurrentWorkingTimeType() {

    if (currentWorkingTimeType != null) {
      return currentWorkingTimeType;
    }

    if (currentContract == null) {
      getCurrentContract();
    }

    if (!currentContract.isPresent()) {
      return Optional.empty();
    }

    //ricerca
    for (ContractWorkingTimeType cwtt : currentContract.get().getContractWorkingTimeType()) {
      if (DateUtility
          .isDateIntoInterval(
              LocalDate.now(), new DateInterval(cwtt.getBeginDate(), cwtt.getEndDate()))) {
        currentWorkingTimeType = Optional.ofNullable(cwtt.getWorkingTimeType());
        return currentWorkingTimeType;
      }
    }
    return Optional.empty();

  }

  @Override
  public Optional<ContractWorkingTimeType> getCurrentContractWorkingTimeType() {

    if (currentContractWorkingTimeType != null) {
      return currentContractWorkingTimeType;
    }

    if (currentContract == null) {
      getCurrentContract();
    }

    if (!currentContract.isPresent()) {
      return Optional.empty();
    }

    //ricerca
    for (ContractWorkingTimeType cwtt : currentContract.get().getContractWorkingTimeType()) {
      if (DateUtility.isDateIntoInterval(
          LocalDate.now(), new DateInterval(cwtt.getBeginDate(), cwtt.getEndDate()))) {
        currentContractWorkingTimeType = Optional.ofNullable(cwtt);
        return currentContractWorkingTimeType;
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<VacationPeriod> getCurrentVacationPeriod() {

    if (currentVacationPeriod != null) {
      return currentVacationPeriod;
    }

    if (currentContract == null) {
      getCurrentContract();
    }
    if (!currentContract.isPresent()) {
      return Optional.empty();
    }

    //ricerca
    for (VacationPeriod vp : currentContract.get().vacationPeriods) {
      if (DateUtility.isDateIntoInterval(
          LocalDate.now(), new DateInterval(vp.getBeginDate(), vp.calculatedEnd()))) {
        currentVacationPeriod = Optional.ofNullable(vp);
        return currentVacationPeriod;
      }
    }
    return Optional.empty();
  }


  /**
   * Getter per la competenza della persona con CompetenceCode, year, month.
   */
  @Override
  public Competence competence(final CompetenceCode code,
      final int year, final int month) {
    Optional<Competence> optCompetence = competenceDao.getCompetence(value, year, month, code);
    if (optCompetence.isPresent()) {
      return optCompetence.get();
    } else {
      Competence competence = new Competence(value, code, year, month);
      competence.valueApproved = 0;
      emp.get().persist(competence);
      //competence.save();
      return competence;
    }
  }

  /**
   * Il residuo positivo del mese fatto dalla person.
   */
  @Override
  public Integer getPositiveResidualInMonth(int year, int month) {
    return competenceManager.get().positiveResidualInMonth(value, year, month) / 60;
  }

  /**
   * L'esito dell'invio attestati per la persona (null se non è ancora stato effettuato).
   */
  @Override
  public CertificatedData getCertificatedData(int year, int month) {

    CertificatedData cd = personMonthRecapDao.getPersonCertificatedData(value, month, year);
    return cd;
  }

  /**
   * Diagnostiche sui dati della persona.
   */
  @Override
  public boolean currentContractInitializationMissing() {
    getCurrentContract();
    if (currentContract.isPresent()) {
      return wrapperFactory.get().create(currentContract.get()).initializationMissing();
    }
    return false;
  }

  @Override
  public boolean currentContractMonthRecapMissing() {
    getCurrentContract();
    if (currentContract.isPresent()) {
      YearMonth now = YearMonth.from(LocalDate.now());
      return wrapperFactory.get().create(currentContract.get())
          .monthRecapMissing(now);
    }
    return false;
  }

  /**
   * I tempo totale di ore lavorate dalla persona nei giorni festivi.
   */
  public int totalHolidayWorkingTime(Integer year) {
    return personManager.holidayWorkingTimeTotal(value,
        Optional.ofNullable(year), Optional.empty());
  }

  /**
   * I tempo totale di ore lavorate dalla persona nei giorni festivi e accettate.
   */
  public int totalHolidayWorkingTimeAccepted(Integer year) {
    return personManager.holidayWorkingTimeAccepted(value,
        Optional.ofNullable(year), Optional.empty());
  }

  /**
   * I giorni festivi con ore lavorate.
   */
  public List<PersonDay> holidyWorkingTimeDay(Integer year) {
    return personDayDao.getHolidayWorkingTime(value,
        Optional.ofNullable(year), Optional.empty());
  }

  /**
   * Diagnostiche sullo stato di sincronizzazione della persona.
   * Ha perseoId null oppure uno dei suoi contratti attivi o futuri ha perseoId null.
   */
  @Override
  public boolean isProperSynchronized() {

    if (properSynchronized.isPresent()) {
      return properSynchronized.get();
    }

    properSynchronized = Optional.of(false);

    if (value.getPerseoId() == null) {
      return properSynchronized.get();
    }

    for (Contract contract : value.getContracts()) {
      if (!contract.isProperSynchronized()) {
        return properSynchronized.get();
      }
    }

    properSynchronized = Optional.of(true);
    return properSynchronized.get();
  }

  /**
   * Il contratto della persona con quel perseoId.
   *
   * @param perseoId id di perseo della Persona
   * @return Contract.
   */
  @Override
  public Contract perseoContract(String perseoId) {
    if (perseoId == null) {
      return null;
    }
    for (Contract contract : value.getContracts()) {
      if (contract.getPerseoId() == null) {
        continue;
      }
      if (contract.getPerseoId().equals(perseoId)) {
        return contract;
      }
    }
    return null;
  }

  @Override
  public boolean isTechnician() {
    return value.getQualification().getQualification() > 3;
  }
  
  /**
   * L'ultimo invio attestati effettuato tramite ePAS.
   *
   * @return mese / anno
   */
  @Override
  public Optional<YearMonth> lastUpload() {
    if (value.certifications.isEmpty()) {
      return Optional.empty();
    }
    YearMonth last = null;
    for (Certification certification : value.certifications) {
      if (last == null 
          || last.isBefore(YearMonth.of(certification.getYear(), certification.getMonth()))) {
        last = YearMonth.of(certification.getYear(), certification.getMonth());
      }
    }
    return Optional.of(last);
  }

  @Override
  public Optional<Contract> getPreviousContract() {
    
    if (previousContract != null) {
      return previousContract;
    }
    
    if (previousContract == null) {
      previousContract = contractDao.getPreviousContract(getCurrentContract().get());
    }
    return previousContract;
  }

  @Override
  public int getNumberOfMealTicketsPreviousMonth(YearMonth yearMonth) {
    // ******************************************************************************************
    // DATI MENSILI
    // ******************************************************************************************
    // I riepiloghi mensili (uno per ogni contratto attivo nel mese)
    List<IWrapperContractMonthRecap> contractMonths = Lists.newArrayList();
    
    List<Contract> monthContracts = wrapperFactory.get().create(value)
        .orderedMonthContracts(yearMonth.getYear(), yearMonth.getMonthValue());

    for (Contract contract : monthContracts) {
      Optional<ContractMonthRecap> cmr =
          wrapperFactory.get().create(contract).getContractMonthRecap(yearMonth);
      if (cmr.isPresent()) {
        contractMonths.add(wrapperFactory.get().create(cmr.get()));
      }      
    }

    return contractMonths.stream().mapToInt(
        cm -> cm.getValue().buoniPastoDalMesePrecedente).reduce(0, Integer::sum);
  }
}
