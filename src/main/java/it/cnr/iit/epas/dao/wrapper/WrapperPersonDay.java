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
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractStampProfile;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.PersonalWorkingTime;
import it.cnr.iit.epas.models.Stamping;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Wrapper personDay.
 *
 * @author Alessandro Martelli
 */
@Slf4j
@Component
public class WrapperPersonDay implements IWrapperPersonDay {

  private PersonDay value;
  private final ContractDao contractDao;
  private final PersonDayDao personDayDao;
  private final IWrapperFactory factory;
  private Optional<PersonDay> previousForProgressive = null;
  private Optional<PersonDay> previousForNightStamp = null;
  private Optional<Contract> personDayContract = null;
  private Boolean isFixedTimeAtWorkk = null;
  private Optional<WorkingTimeTypeDay> workingTimeTypeDay = null;
  private Optional<PersonalWorkingTime> personalWorkingTime = null;

  @Inject
  WrapperPersonDay(ContractDao contractDao,
                   PersonDayDao personDayDao, IWrapperFactory factory) {
    this.contractDao = contractDao;
    this.personDayDao = personDayDao;
    this.factory = factory;
  }

  public IWrapperPersonDay setValue(PersonDay pd) {
    this.value = pd;
    return this;
  }

  @Override
  public PersonDay getValue() {
    return value;
  }

  /**
   * Il personDay precedente per il calcolo del progressivo.
   */
  public Optional<PersonDay> getPreviousForProgressive() {

    if (this.previousForProgressive != null) {
      return this.previousForProgressive;
    }

    setPreviousForProgressive(Optional.<PersonDay>empty());
    return this.previousForProgressive;
  }

  /**
   * Instanzia la variabile lazy previousForProgressive. 
   * potentialOnlyPrevious se presente è l'unico candidato possibile.
   *
   * @param potentialOnlyPrevious se presente è l'unico candidato
   */
  public void setPreviousForProgressive(Optional<PersonDay> potentialOnlyPrevious) {

    this.previousForProgressive = Optional.<PersonDay>empty();

    if (!getPersonDayContract().isPresent()) {
      return;
    }

    //Assegnare logicamente il previousForProgressive
    if (this.value.getDate().getDayOfMonth() == 1) {
      //Primo giorno del mese
      return;
    }


    if (this.getPersonDayContract().get().sourceDateResidual != null
            && this.getPersonDayContract().get().sourceDateResidual
                    .isEqual(this.value.getDate())) {
      //Giorno successivo all'inizializzazione
      return;
    }

    PersonDay candidate = null;

    if (potentialOnlyPrevious.isPresent()) {
      candidate = potentialOnlyPrevious.get();

    } else {

      List<PersonDay> personDayInMonthAsc = personDayDao
              .getPersonDayInMonth(this.value.getPerson(),
                      YearMonth.from(this.value.getDate()));
      for (int i = 1; i < personDayInMonthAsc.size(); i++) {
        PersonDay current = personDayInMonthAsc.get(i);
        PersonDay previous = personDayInMonthAsc.get(i - 1);
        if (current.getId().equals(this.value.getId())) {
          candidate = previous;
        }
      }
    }
    if (candidate == null) {
      return;
    }

    //Non stesso contratto
    // TODO: (equivalente a caso this.value.equals(beginDate)
    if (!DateUtility.isDateIntoInterval(candidate.getDate(),
            factory.create(this.getPersonDayContract().get()).getContractDateInterval())) {
      return;
    }
    this.previousForProgressive = Optional.ofNullable(candidate);

  }

  /**
   * Il personDay precedente solo se immediatamente consecutivo. Altrimenti absent().
   */
  public Optional<PersonDay> getPreviousForNightStamp() {


    if (this.previousForNightStamp != null) {
      return this.previousForNightStamp;
    }

    setPreviousForNightStamp(Optional.<PersonDay>empty());
    return this.previousForNightStamp;

  }

  /**
   * Instanzia la variabile lazy previousForNightStamp. 
   * potentialOnlyPrevious se presente è l'unico candidato possibile.
   *
   * @param potentialOnlyPrevious se presente è l'unico candidato
   */
  public void setPreviousForNightStamp(Optional<PersonDay> potentialOnlyPrevious) {

    this.previousForNightStamp = Optional.empty();

    if (!getPersonDayContract().isPresent()) {
      return;
    }

    LocalDate realPreviousDate = this.value.getDate().minusDays(1);

    PersonDay candidate = null;

    if (potentialOnlyPrevious.isPresent()) {
      candidate = potentialOnlyPrevious.get();
    } else {

      candidate = personDayDao
              .getPreviousPersonDay(this.value.getPerson(), this.value.getDate());
    }

    //primo giorno del contratto
    if (candidate == null) {
      return;
    }

    //giorni non consecutivi
    if (!candidate.getDate().isEqual(realPreviousDate)) {
      return;
    }

    this.previousForNightStamp = Optional.ofNullable(candidate);
  }

  /**
   * Ritorna il contratto associato al personDay, se presente. Instanzia una variabile lazy.
   *
   * @return il contratto
   */
  public Optional<Contract> getPersonDayContract() {

    if (this.personDayContract != null) {
      return this.personDayContract;
    }

    Contract contract = contractDao.getContract(this.value.getDate(), this.value.getPerson());

    if (contract == null) {
      this.personDayContract = Optional.empty();
      return this.personDayContract;
    }

    this.personDayContract = Optional.ofNullable(contract);

    return this.personDayContract;
  }

  /**
   * True se il personDay cade in uno stampProfile con fixedTimeAtWork = true.
   */
  public boolean isFixedTimeAtWork() {
    if (this.isFixedTimeAtWorkk != null) {
      return this.isFixedTimeAtWorkk;
    }

    this.isFixedTimeAtWorkk = false;

    Optional<Contract> contract = getPersonDayContract();

    if (!contract.isPresent()) {
      return this.isFixedTimeAtWorkk;
    }

    for (ContractStampProfile csp : contract.get().getContractStampProfile()) {

      DateInterval cspInterval = new DateInterval(csp.getBeginDate(), csp.getEndDate());

      if (DateUtility.isDateIntoInterval(this.value.getDate(), cspInterval)) {
        this.isFixedTimeAtWorkk = csp.fixedworkingtime;
        return this.isFixedTimeAtWorkk;
      }
    }

    return this.isFixedTimeAtWorkk;
  }

  /**
   * Ritorna il tipo orario associato al personDay, se presente. Instanzia una variabile lazy.
   *
   * @return il tipo orario
   */
  public Optional<WorkingTimeTypeDay> getWorkingTimeTypeDay() {

    if (this.workingTimeTypeDay != null) {
      return this.workingTimeTypeDay;
    }

    if (getPersonDayContract().isPresent()) {
      log.trace("WrapperPersonDay::getWorkingTimeTypeDay() -> trovato contratto nel giorno {}", getValue().getDate());
      for (ContractWorkingTimeType cwtt :
              this.getPersonDayContract().get().getContractWorkingTimeType()) {

        if (DateUtility.isDateIntoInterval(this.value.getDate(),
                factory.create(cwtt).getDateInverval())) {

          WorkingTimeTypeDay wttd = cwtt.workingTimeType.getWorkingTimeTypeDays()
                  .get(this.value.getDate().getDayOfWeek().getValue() - 1);

          Preconditions.checkState(wttd.dayOfWeek == value.getDate().getDayOfWeek().getValue());
          return Optional.ofNullable(wttd);
        }

      }
    } else {
      log.info("WrapperPersonDay::getWorkingTimeTypeDay() -> contratto non presente "
          + "per {} nel giorno {}", 
          getValue().getPerson().getFullname(), getValue().getDate());
    }
    return Optional.empty();
  }

  /**
   * L'ultima timbratura in ordine di tempo nel giorno.
   */
  public Stamping getLastStamping() {
    Stamping last = null;
    for (Stamping s : value.getStampings()) {
      if (last == null) {
        last = s;
      } else if (last.getDate().isBefore(s.getDate())) {
        last = s;
      }
    }
    return last;
  }

  @Override
  public Optional<PersonalWorkingTime> getPersonalWorkingTime() {
    if (this.personalWorkingTime != null) {
      return this.personalWorkingTime;
    }

    if (getPersonDayContract().isPresent() 
        && !getPersonDayContract().get().getPersonalWorkingTimes().isEmpty()) {
      
      for (PersonalWorkingTime pwt : getPersonDayContract().get().getPersonalWorkingTimes()) {
        if (DateUtility.isDateIntoInterval(this.value.getDate(),
            new DateInterval(pwt.getBeginDate(), pwt.getEndDate()))) {
          return Optional.ofNullable(pwt);
        }
      }
      
    }
    return Optional.empty();
  }

}
