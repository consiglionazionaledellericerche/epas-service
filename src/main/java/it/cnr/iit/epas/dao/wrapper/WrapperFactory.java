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
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.TimeSlot;
import it.cnr.iit.epas.models.WorkingTimeType;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

@Component
public class WrapperFactory implements IWrapperFactory {

  private final IWrapperPerson wrapperPerson;
  private final IWrapperContract wrapperContract;
  private final IWrapperWorkingTimeType wrapperWorkingTimeType;
  private final IWrapperTimeSlot wrapperTimeSlot;
  private final IWrapperCompetenceCode wrapperCompetenceCode;
  private final IWrapperOffice wrapperOffice;
  private final IWrapperPersonDay wrapperPersonDay;
  private final IWrapperContractMonthRecap wrapperContractMonthRecap;
  private final IWrapperContractWorkingTimeType wrapperContractWorkingTimeType;
  
  @Inject
  public WrapperFactory(IWrapperPerson wrapperPerson, IWrapperContract wrapperContract,
      IWrapperWorkingTimeType wrapperWorkingTimeType, IWrapperTimeSlot wrapperTimeSlot,
      IWrapperCompetenceCode wrapperCompetenceCode, IWrapperOffice wrapperOffice,
      IWrapperPersonDay wrapperPersonDay, IWrapperContractMonthRecap wrapperContractMonthRecap,
      IWrapperContractWorkingTimeType wrapperContractWorkingTimeType) {
    this.wrapperPerson = wrapperPerson;
    this.wrapperContract = wrapperContract;
    this.wrapperWorkingTimeType = wrapperWorkingTimeType;
    this.wrapperTimeSlot = wrapperTimeSlot;
    this.wrapperCompetenceCode = wrapperCompetenceCode;
    this.wrapperOffice = wrapperOffice;
    this.wrapperPersonDay = wrapperPersonDay;
    this.wrapperContractMonthRecap = wrapperContractMonthRecap;
    this.wrapperContractWorkingTimeType = wrapperContractWorkingTimeType;
  }
  
  public IWrapperPerson create(Person person) {
    return wrapperPerson.setValue(person);
  }

  @Override
  public IWrapperContract create(Contract contract) {
    return wrapperContract.setValue(contract);
  }

  @Override
  public IWrapperWorkingTimeType create(WorkingTimeType wtt) {
    return wrapperWorkingTimeType.setValue(wtt);
  }

  @Override
  public IWrapperTimeSlot create(TimeSlot ts) {
    return wrapperTimeSlot.setValue(ts);
  }

  @Override
  public IWrapperCompetenceCode create(CompetenceCode cc) {
    return wrapperCompetenceCode.setValue(cc);
  }

  @Override
  public IWrapperOffice create(Office office) {
    return wrapperOffice.setValue(office);
  }

  @Override
  public IWrapperPersonDay create(PersonDay pd) {
    Preconditions.checkNotNull(wrapperPersonDay);
    Preconditions.checkNotNull(pd);
    return wrapperPersonDay.setValue(pd);
  }

  @Override
  public IWrapperContractMonthRecap create(ContractMonthRecap cmr) {
    return wrapperContractMonthRecap.setValue(cmr);
  }

  @Override
  public IWrapperContractWorkingTimeType create(ContractWorkingTimeType cwtt) {
    return wrapperContractWorkingTimeType.setValue(cwtt);
  }
}