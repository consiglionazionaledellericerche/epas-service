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

import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.WorkingTimeType;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * WrapperWorkingTimeType con alcune funzionalità aggiuntive.
 *
 * @author Alessandro Martelli
 * @author Cristian Lucchesi
 */
@Component
public class WrapperWorkingTimeType implements IWrapperWorkingTimeType {

  private WorkingTimeType value;
  private final ContractDao contractDao;

  @Inject
  WrapperWorkingTimeType(ContractDao contractDao) {
    this.contractDao = contractDao;
  }

  public IWrapperWorkingTimeType setValue(WorkingTimeType wtt) {
    this.value = wtt;
    return this;
  }

  @Override
  public WorkingTimeType getValue() {
    return value;
  }

  /**
   * La lista dei contratti attivi che hanno un periodo attivo con associato
   * il tipo di orario di lavoro indicato.
   */
  public List<Contract> getAllAssociatedActiveContract() {
    return contractDao.getAllAssociatedActiveContracts(getValue());
  }

  /**
   * I contratti attivi che attualmente hanno impostato il WorkingTimeType.
   */
  @Override
  public List<Contract> getAssociatedActiveContract(Office office) {

    LocalDate today = LocalDate.now();
    List<Contract> activeContract = contractDao
        .getActiveContractsInPeriod(today, Optional.ofNullable(today), Optional.of(office));

    List<Contract> list = new ArrayList<Contract>();
    for (Contract contract : activeContract) {
      ContractWorkingTimeType current = 
          getContractWorkingTimeTypeFromDate(contract, today);
      if (current.getWorkingTimeType().equals(this.value)) {
        list.add(contract);
      }
    }

    return list;
  }

  /**
   * Il ContractWorkingTimeType associato ad un contratto in una specifica data.
   *
   * @param contract il contratto di cui prelevare il ContractWorkingTimeType
   * @param date     la data in cui controllare il ContractWorkingTimeType
   * @return il ContractWorkingTimeType di un contratto ad una data specifica
   */
  public final ContractWorkingTimeType getContractWorkingTimeTypeFromDate(final Contract contract,
      final LocalDate date) {
    //XXX: metodo spostato qui dal ContractManager in seguito al passaggio a Spring boot
    for (ContractWorkingTimeType cwtt : contract.getContractWorkingTimeType()) {

      if (DateUtility.isDateIntoInterval(date, new DateInterval(cwtt.getBeginDate(), cwtt.getEndDate()))) {
        return cwtt;
      }
    }
    // FIXME: invece del null utilizzare un Optional!
    return null;
  }

  /**
   * Ritorna i periodi con questo tipo orario appartenti a contratti attualmente attivi.
   */
  @Override
  public List<ContractWorkingTimeType> getAssociatedPeriodInActiveContract(Office office) {

    LocalDate today = LocalDate.now();
    List<Contract> activeContract = contractDao
        .getActiveContractsInPeriod(today, Optional.ofNullable(today), Optional.of(office));

    List<ContractWorkingTimeType> list = new ArrayList<ContractWorkingTimeType>();
    for (Contract contract : activeContract) {
      for (ContractWorkingTimeType cwtt : contract.getContractWorkingTimeType()) {
        if (cwtt.getWorkingTimeType().equals(this.value)) {
          list.add(cwtt);
        }
      }
    }

    return list;
  }

  @Override
  public List<Contract> getAssociatedContract() {
    return value.getContractWorkingTimeType().stream().map(cwtt -> cwtt.getContract())
        .collect(Collectors.toList());
  }
}
