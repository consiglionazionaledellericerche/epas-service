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

package it.cnr.iit.epas.dao.wrapper.function;

import com.google.common.base.Function;
import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.dao.wrapper.IWrapperContractMonthRecap;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dao.wrapper.IWrapperOffice;
import it.cnr.iit.epas.dao.wrapper.IWrapperPerson;
import it.cnr.iit.epas.dao.wrapper.IWrapperTimeSlot;
import it.cnr.iit.epas.dao.wrapper.IWrapperWorkingTimeType;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.TimeSlot;
import it.cnr.iit.epas.models.WorkingTimeType;
import javax.inject.Inject;
import javax.inject.Provider;
import org.springframework.stereotype.Component;

/**
 * Factory per alcune Function di utilità da utilizzare nei Wrapper.
 */
@Deprecated
@Component
public class WrapperModelFunctionFactory {

  private final Provider<IWrapperFactory> factory;

  @Inject
  WrapperModelFunctionFactory(Provider<IWrapperFactory> factory) {
    this.factory = factory;
  }

  /**
   * Permette la creazione di un'istanza wrapperWorkingTyimeType a partire dall'oggetto
   * del modello.
   *
   * @return un wrapper di un workingTimeType.
   */
  public Function<WorkingTimeType, IWrapperWorkingTimeType> workingTimeType() {
    return new Function<WorkingTimeType, IWrapperWorkingTimeType>() {

      @Override
      public IWrapperWorkingTimeType apply(WorkingTimeType input) {
        return factory.get().create(input);
      }
    };
  }

  /**
   * Permette la creazione di un'istanza wrapperTimeSlot a partire dall'oggetto TimeSlot.
   *
   * @return un wrapper di un timeslot.
   */
  public Function<TimeSlot, IWrapperTimeSlot> timeSlot() {
    return new Function<TimeSlot, IWrapperTimeSlot>() {

      @Override
      public IWrapperTimeSlot apply(TimeSlot input) {
        return factory.get().create(input);
      }
    };
  }
  
  /**
   * Permette la creazione di un'istanza wrapperPerson a partire dall'oggetto del modello person.
   *
   * @return un wrapper di una person.
   */
  public Function<Person, IWrapperPerson> person() {
    return new Function<Person, IWrapperPerson>() {

      @Override
      public IWrapperPerson apply(Person input) {
        return factory.get().create(input);
      }
    };
  }

  /**
   * Permette la creazione di un'istanza wrapperOffice a partire dall'oggetto del modello office.
   *
   * @return un wrapper di un office.
   */
  public Function<Office, IWrapperOffice> office() {
    return new Function<Office, IWrapperOffice>() {

      @Override
      public IWrapperOffice apply(Office input) {
        return factory.get().create(input);
      }
    };
  }

  /**
   * Permette la creazione di un'istanza wrapperContract a partire dall'oggetto del modello 
   * contract.
   *
   * @return un wrapper di un contract.
   */
  public Function<Contract, IWrapperContract> contract() {
    return new Function<Contract, IWrapperContract>() {

      @Override
      public IWrapperContract apply(Contract input) {
        return factory.get().create(input);
      }
    };
  }

  /**
   * Permette la creazione di un'istanza wrapperContractMonthRecap a partire dall'oggetto del 
   * modello contractMonthRecap.
   *
   * @return un wrapper di un contractMonthRecap.
   */
  public Function<ContractMonthRecap, IWrapperContractMonthRecap> contractMonthRecap() {
    return new Function<ContractMonthRecap, IWrapperContractMonthRecap>() {

      @Override
      public IWrapperContractMonthRecap apply(ContractMonthRecap input) {
        return factory.get().create(input);
      }
    };
  }
}