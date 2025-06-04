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
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.WorkingTimeTypeDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.manager.recaps.recomputation.RecomputeRecap;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMandatoryTimeSlot;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.ContractStampProfile;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.VacationPeriod;
import it.cnr.iit.epas.models.WorkingTimeType;
import it.cnr.iit.epas.models.base.IPropertyInPeriod;
import it.cnr.iit.epas.models.enumerate.VacationCode;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Manager per Contract.
 *
 * @author alessandro
 */
@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class ContractManager {

  private final ConsistencyManager consistencyManager;
  private final ObjectProvider<IWrapperFactory> wrapperFactory;
  private final PeriodManager periodManager;
  private final PersonDayInTroubleManager personDayInTroubleManager;
  private final WorkingTimeTypeDao workingTimeTypeDao;
  private final ContractDao contractDao;
  private final ObjectProvider<EntityManager> emp;

  /**
   * Check di contratto valido con gli altri contratti della persona.
   *
   * @param contract contract
   * @return esito
   */
  public final boolean isContractNotOverlapping(final Contract contract) {

    DateInterval contractInterval = 
        wrapperFactory.getObject().create(contract).getContractDateInterval();
    for (Contract c : contract.person.getContracts()) {

      if (contract.getId() != null && c.getId().equals(contract.getId())) {
        continue;
      }

      if (DateUtility.intervalIntersection(contractInterval,
          wrapperFactory.getObject().create(c).getContractDateInterval()) != null) {
        log.debug("Il contratto {} si sovrappone con il contratto {}", contract, c);
        return false;
      }
    }
    return true;
  }

  /**
   * Check di date valide singolo contratto.
   *
   * @param contract contract
   * @return esito
   */
  public boolean isContractCrossFieldValidationPassed(final Contract contract) {

    if (contract.getEndDate() != null
        && contract.getEndDate().isBefore(contract.getBeginDate())) {
      return false;
    }

    if (contract.getEndContract() != null 
        && contract.getEndContract().isBefore(contract.getBeginDate())) {
      return false;
    }

    if (contract.getEndDate() != null && contract.getEndContract() != null
        && contract.getEndDate().isBefore(contract.getEndContract())) {
      return false;
    }

    if (!isContractNotOverlapping(contract)) {
      return false;
    }

    return true;
  }

  /**
   * Costruisce il contratto in modo sicuro e effettua il calcolo.
   *
   * @param contract contract
   * @param wtt      il tipo orari
   * @param recomputation      se effettuare subito il ricalcolo della persona.
   * @return esito costruzione
   */
  public boolean properContractCreate(final Contract contract, 
      Optional<WorkingTimeType> wtt, 
      boolean recomputation) {

    if (contract.getBeginDate() == null) {
      log.debug("Impossibile creare il contratto, beginDate è null");
      return false;
    }

    if (!isContractCrossFieldValidationPassed(contract)) {
      log.debug("Impossibile creare il contratto, crossData validation fallita");
      return false;
    }

    if (!isContractNotOverlapping(contract)) {
      log.debug("Impossibile creare il contratto, il contratto si sovrappone con altri contratti "
          + "preesistenti");
      return false;
    }
    emp.getObject().persist(contract);
    //contract.save();

    contract.vacationPeriods.addAll(contractVacationPeriods(contract));
    for (VacationPeriod vacationPeriod : contract.getVacationPeriods()) {
      emp.getObject().persist(vacationPeriod);
    }

    if (!wtt.isPresent()) {
      wtt = Optional.ofNullable(workingTimeTypeDao
          .workingTypeTypeByDescription("Normale", Optional.empty()));
      if (!wtt.isPresent()) {
        throw new IllegalStateException();
      }
    }

    ContractWorkingTimeType cwtt = new ContractWorkingTimeType();
    cwtt.setBeginDate(contract.getBeginDate());
    cwtt.setEndDate(contract.calculatedEnd());
    cwtt.setWorkingTimeType(wtt.get());
    cwtt.setContract(contract);
    emp.getObject().persist(cwtt);
    //cwtt.save();
    contract.getContractWorkingTimeType().add(cwtt);

    ContractStampProfile csp = new ContractStampProfile();
    csp.contract = contract;
    csp.setBeginDate(contract.getBeginDate());
    csp.setEndDate(contract.calculatedEnd());
    csp.fixedworkingtime = false;
    emp.getObject().persist(csp);
    //csp.save();
    contract.getContractStampProfile().add(csp);

    contract.setSourceDateResidual(null);
    emp.getObject().merge(contract);
    //contract.save();

    // FIXME: comando JPA per aggiornare la person
    contract.person.getContracts().add(contract);

    if (recomputation) {
      recomputeContract(contract, Optional.<LocalDate>empty(), true, false);
    }

    return true;

  }

  /**
   * Aggiorna il contratto in modo sicuro ed effettua il ricalcolo.
   *
   * @param contract   contract
   * @param from       da quando effettuare il ricalcolo.
   * @param onlyRecaps ricalcolare solo i riepiloghi mensili.
   */
  public boolean properContractUpdate(final Contract contract, final LocalDate from,
      final boolean onlyRecaps) {

    if (!isContractCrossFieldValidationPassed(contract)) {
      return false;
    }

    if (!isContractNotOverlapping(contract)) {
      return false;
    }
    emp.getObject().merge(contract);
    periodManager.updatePropertiesInPeriodOwner(contract);
    personDayInTroubleManager.cleanPersonDayInTrouble(contract.person);

    recomputeContract(contract, Optional.ofNullable(from), false, onlyRecaps);

    return true;
  }

  /**
   * Ricalcolo del contratto a partire dalla data from.
   *
   * @param contract    contract
   * @param dateFrom    dateFrom
   * @param newContract se il contratto è nuovo (non ho strutture da ripulire)
   * @param onlyRecaps  ricalcolare solo i riepiloghi mensili.
   */
  public void recomputeContract(final Contract contract, final Optional<LocalDate> dateFrom,
      final boolean newContract, final boolean onlyRecaps) {

    IWrapperContract wrContract = wrapperFactory.getObject().create(contract);

    LocalDate startDate = dateFrom
        .orElse(wrContract.getContractDatabaseInterval().getBegin());

    if (!newContract) {

      YearMonth yearMonthFrom = YearMonth.from(startDate);

      // Distruggere i riepiloghi esistenti da yearMonthFrom.
      // TODO: anche quelli sulle ferie quando ci saranno
      for (ContractMonthRecap cmr : contract.contractMonthRecaps) {
        if (!yearMonthFrom.isAfter(YearMonth.of(cmr.year, cmr.month))) {
          if (emp.getObject().contains(cmr)) {
            emp.getObject().remove(cmr);
          }
        }
      }
      //XXX: serve?
      emp.getObject().flush();
      //JPA.em().flush();
      emp.getObject().refresh(contract);
      //contract.refresh();   //per aggiornare la lista contract.contractMonthRecaps
    }

    consistencyManager.updateContractSituation(contract, startDate);
  }


  /**
   * Builder dell'oggetto vacationPeriod.
   *
   * @param contract contratto
   * @param vacationCode vacationCode
   * @param beginFrom da
   * @param endTo a
   * @return il vacationPeriod
   */
  private static VacationPeriod buildVacationPeriod(final Contract contract, 
      final VacationCode vacationCode, final LocalDate beginFrom, final LocalDate endTo) {

    VacationPeriod vacationPeriod = new VacationPeriod();
    vacationPeriod.setContract(contract);
    vacationPeriod.setBeginDate(beginFrom);
    vacationPeriod.setEndDate(endTo);
    vacationPeriod.setVacationCode(vacationCode);
    return vacationPeriod;
  }

  /**
   * Ritorna i vacation period di default per il contratto applicando la normativa
   * vigente.
   *
   * @param contract contract
   * @return i vacation period
   */
  public List<VacationPeriod> contractVacationPeriods(Contract contract)  {

    List<VacationPeriod> vacationPeriods = Lists.newArrayList();

    VacationCode v26 = VacationCode.CODE_26_4;
    VacationCode v28 = VacationCode.CODE_28_4;

    if (contract.getEndDate() == null) {

      // Tempo indeterminato, creo due vacation 3 anni più infinito
      vacationPeriods.add(buildVacationPeriod(contract, v26, contract.getBeginDate(),
          contract.getBeginDate().plusYears(3).minusDays(1)));
      vacationPeriods.add(
          buildVacationPeriod(contract, v28, contract.getBeginDate().plusYears(3), null));

    } else {

      if (contract.getEndDate().isAfter(contract.getBeginDate().plusYears(3).minusDays(1))) {

        // Tempo determinato più lungo di 3 anni
        vacationPeriods.add(buildVacationPeriod(contract, v26, contract.getBeginDate(),
            contract.getBeginDate().plusYears(3).minusDays(1)));
        vacationPeriods.add(
            buildVacationPeriod(contract, v28, contract.getBeginDate().plusYears(3), 
                contract.getEndDate()));
      } else {
        vacationPeriods.add(
            buildVacationPeriod(contract, v26, contract.getBeginDate(), contract.getEndDate()));
      }
    }
    return vacationPeriods;
  }

  /**
   * Sistema l'inizializzazione impostando i valori corretti se mancanti.
   *
   * @param contract contract
   */
  public void setSourceContractProperly(final Contract contract) {

    if (contract.sourceVacationLastYearUsed == null) {
      contract.sourceVacationLastYearUsed = 0;
    }
    if (contract.sourceVacationCurrentYearUsed == null) {
      contract.sourceVacationCurrentYearUsed = 0;
    }
    if (contract.sourcePermissionUsed == null) {
      contract.sourcePermissionUsed = 0;
    }
    if (contract.sourceRemainingMinutesCurrentYear == null) {
      contract.sourceRemainingMinutesCurrentYear = 0;
    }
    if (contract.sourceRemainingMinutesLastYear == null) {
      contract.sourceRemainingMinutesLastYear = 0;
    }
    if (contract.sourceRecoveryDayUsed == null) {
      contract.sourceRecoveryDayUsed = 0;
    }
    if (contract.sourceRemainingMealTicket == null) {
      contract.sourceRemainingMealTicket = 0;
    }
    emp.getObject().merge(contract);
    //contract.save();
  }

  /**
   * Azzera l'inizializzazione del contratto.
   *
   * @param contract contract
   */
  public final void cleanResidualInitialization(final Contract contract) {
    contract.sourceRemainingMinutesCurrentYear = 0;
    contract.sourceRemainingMinutesLastYear = 0;
  }

  /**
   * Azzera l'inizializzazione del contratto.
   *
   * @param contract contract
   */
  public final void cleanVacationInitialization(final Contract contract) {
    contract.sourceVacationLastYearUsed = 0;
    contract.sourceVacationCurrentYearUsed = 0;
    contract.sourcePermissionUsed = 0;
  }

  /**
   * Azzera l'inizializzazione del buono pasto.
   *
   * @param contract contract
   */
  public final void cleanMealTicketInitialization(final Contract contract) {
    contract.setSourceDateMealTicket(contract.getSourceDateResidual());
  }

  /**
   * Il ContractMandatoryTimeSlot associato ad un contratto in una specifica data.
   *
   * @param contract il contratto di cui prelevare il ContractMandatoryTimeSlot
   * @param date     la data in cui controllare il ContractMandatoryTimeSlot
   * @return il ContractMandatoryTimeSlot di un contratto ad una data specifica
   */
  public final Optional<ContractMandatoryTimeSlot> getContractMandatoryTimeSlotFromDate(
      final Contract contract, final LocalDate date) {

    for (ContractMandatoryTimeSlot cmts : contract.getContractMandatoryTimeSlots()) {
      if (DateUtility.isDateIntoInterval(
            date, new DateInterval(cmts.getBeginDate(), cmts.getEndDate()))) {
        return Optional.of(cmts);
      }
    }

    return Optional.empty();
  }

  /**
   * Sistema le durate dei piani ferie in relazione alle date dei piani ferie del contratto 
   * precedente.
   *
   * @param contract il contratto attuale
   * @param previousContract il contratto precedente
   */
  public void mergeVacationPeriods(Contract contract, Contract previousContract) {
    VacationPeriod twentysixplus4 = null;
    VacationPeriod twentyeightplus4 = null;
    VacationPeriod other = null;
    IWrapperContract wrappedContract = wrapperFactory.getObject().create(contract);
    List<VacationPeriod> vpList = previousContract.getVacationPeriods();
    VacationPeriod vp = null;
    for (VacationPeriod vpPrevious : vpList) {
      if (vpPrevious.getVacationCode().equals(VacationCode.CODE_26_4)) {
        twentysixplus4 = vpPrevious;
      } else if (vpPrevious.getVacationCode().equals(VacationCode.CODE_28_4)) {
        twentyeightplus4 = vpPrevious;
      } else {
        other = vpPrevious;
      }
    }
    if (twentysixplus4 == null && other != null) {
      //non c'è piano ferie iniziale, si è cominiciato con un part time...che si fa?
    }
    if (twentyeightplus4 != null && twentysixplus4 != null) {
      //in questo caso il nuovo contratto partirà già col piano ferie 28+4 dal primo giorno
      vp = new VacationPeriod();
      vp.setContract(contract);
      vp.setBeginDate(contract.getBeginDate());
      vp.setVacationCode(VacationCode.CODE_28_4);
      vp.setEndDate(contract.getEndDate());
    }
    if (twentysixplus4 != null && twentyeightplus4 == null) {
      // si calcola la durata del piano ferie 26+4 e in base a quello il nuovo contratto parte con
      // 26+4 e 28+4 parte dopo 3 anni dall'inizio di 26+4 nel vecchio contratto
      vp = new VacationPeriod();
      vp.setContract(contract);
      vp.setVacationCode(VacationCode.CODE_28_4);
      vp.setBeginDate(twentysixplus4.getBeginDate().plusYears(3));
      vp.setEndDate(contract.getEndDate());
    }
    if (vp != null) {
      List<IPropertyInPeriod> periodRecaps = periodManager.updatePeriods(vp, false);
      RecomputeRecap recomputeRecap =
          periodManager.buildRecap(wrappedContract.getContractDateInterval().getBegin(),
              Optional.ofNullable(wrappedContract.getContractDateInterval().getEnd()),
              periodRecaps, Optional.ofNullable(contract.getSourceDateResidual()));

      recomputeRecap.initMissing = wrappedContract.initializationMissing();
      periodManager.updatePeriods(vp, true);
      contract = contractDao.getContractById(contract.getId());
      emp.getObject().refresh(contract.getPerson());
      //contract.person.refresh();
      if (recomputeRecap.needRecomputation) {
        recomputeContract(contract,
            Optional.ofNullable(recomputeRecap.recomputeFrom), false, false);
      }
    }

  }

  /**
   * Ripristina i normali piani ferie del nuovo contratto come non continuativo del precedente.
   *
   * @param actualContract il contratto attuale
   */
  public void splitVacationPeriods(Contract actualContract) {

    for (VacationPeriod vp : actualContract.getVacationPeriods()) {
      emp.getObject().remove(vp);
      //vp.delete();
    }
    emp.getObject().merge(actualContract);
    //actualContract.save();
    emp.getObject().flush();
    //JPA.em().flush();
    emp.getObject().refresh(actualContract);
    //actualContract.refresh(); 

    actualContract.vacationPeriods.addAll(contractVacationPeriods(actualContract));
    for (VacationPeriod vacationPeriod : actualContract.getVacationPeriods()) {
      emp.getObject().merge(vacationPeriod);
      //vacationPeriod.save();
    }

    recomputeContract(actualContract, Optional.<LocalDate>empty(), true, false);

  }

  /**
   * Verifica se è possibile associare un precedente contratto al contratto attuale. 
   */
  public boolean canAppyPreviousContractLink(Contract contract) {
    return contractDao.getPreviousContract(contract).isPresent();
  }

  /**
   * Inserisce il collegamento al contratto precedente se il parametro linkedToPreviousContract
   * è true.
   *
   * @param contract il contratto su cui impostare o rimuovere il collegamento al contratto 
   *     precedente.
   * @param linkedToPreviousContract indica se il contratto precedente deve essere collegato o meno.
   * @return true se è possibile impostare o rimuovore il link al contratto precedente, false
   *     se si è richiesto di impostare il contratto precedente ma questo non è presente. 
   */
  public boolean applyPreviousContractLink(Contract contract, boolean linkedToPreviousContract) {
    //Controllo se il contratto deve essere linkato al precedente...
    if (linkedToPreviousContract) {
      if (contract.getPreviousContract() == null) {
        Optional<Contract> previousContract = contractDao.getPreviousContract(contract);
        if (previousContract.isPresent()) {
          contract.setPreviousContract(previousContract.get());
          if (contract.getBeginDate().minusDays(1).isEqual(previousContract.get().getEndDate())) {
            mergeVacationPeriods(contract, previousContract.get());
          }
        } else {
          return false;
        }
      }
    } else {
      Contract temp = contract.getPreviousContract();
      if (temp != null) {
        contract.setPreviousContract(null);
        if (temp.getEndDate() != null 
            && contract.getBeginDate().minusDays(1).isEqual(temp.getEndDate())) {
          splitVacationPeriods(contract);
        }
      }
    }
    return true;
  }

  /**
   * Controlla che se è presente un previousContract e che
   * sia effettivamento il contratto precedente, altrimenti 
   * lo corregge.
   *
   * @param contract il contratto da verificare e correggere se necessario
   */
  public boolean fixPreviousContract(Contract contract) {
    Verify.verifyNotNull(contract);
    if (contract.getPreviousContract() == null) {
      log.info("PreviousContract non presente, niente da correggere per {}.", contract);
      return false;
    }
    if (!contract.getPreviousContract().equals(contract)
        && contract.getPreviousContract().equals(
            contractDao.getPreviousContract(contract).orElse(null))) {
      log.info("PreviousContract corretto, niente da correggere per {}.", contract);
      return false;
    }

    log.info("Contratto con previousContract da correggere {}",
        contract, contract.getPerson().getFullname());
    if (contract.getPerson().getContracts().size() == 1) {
      log.debug("Contratto id={} singolo con riferimento al previousContract uguale a se stesso",
          contract.getId());
      applyPreviousContractLink(contract, false);
      log.info("Rimosso previousContract su contratto id={}", contract.getId());
    } else {
      log.debug("Sono presenti più contratti per {} e quello con id = {} presenta dei problemi",
          contract.getPerson().getFullname(), contract.getId());
      contract.setPreviousContract(null);
      log.debug("Rimosso temporaneamente previousContract su contratto id={}", contract.getId());
      contractDao.merge(contract);
      log.debug("contratto precedente corretto da impostare = {}", 
          contractDao.getPreviousContract(contract));
      applyPreviousContractLink(contract, true);
      log.info("Impostato previousContract {} su contratto id={}",
          contract.getPreviousContract(), contract.getId());
      contractDao.merge(contract);
    }
    log.info("Dopo la correzione -> {}", contract);
    contractDao.merge(contract);
    return true;
  }

  /**
   * Controlla tutti i contratti con previousContract impostato
   * che potrebbero avere dei problemi e li corregge se necessario.
   */
  public int fixContractsWithWrongPreviousContract(Optional<Integer> maxSize) {
    AtomicInteger contractFixed = new AtomicInteger(0);
    Stream<Contract> contractToFixStream = 
        contractDao.getContractsWithWrongPreviousContract().stream();
    if (maxSize.isPresent()) {
      contractToFixStream = contractToFixStream.limit(maxSize.get());
    }
    contractToFixStream.forEach(contract -> {
      if (fixPreviousContract(contract)) {
        contractFixed.getAndIncrement();
      }
    });
    return contractFixed.get();
  }

}