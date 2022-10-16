package it.cnr.iit.epas.dao.wrapper;

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
import javax.inject.Provider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
    this.wrapperTimeSlot = null;
    this.wrapperCompetenceCode = null;
    this.wrapperOffice = null;
    this.wrapperPersonDay = null;
    this.wrapperContractMonthRecap = null;
    this.wrapperContractWorkingTimeType = null;
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