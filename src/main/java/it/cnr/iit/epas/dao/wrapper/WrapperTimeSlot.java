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
import it.cnr.iit.epas.manager.ContractManager;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMandatoryTimeSlot;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.TimeSlot;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * WrapperTimeSlot con alcune funzionalità aggiuntive.
 *
 * @author Cristian Lucchesi
 */
@Component
public class WrapperTimeSlot implements IWrapperTimeSlot {

  private TimeSlot value;
  private final ContractManager contractManager;
  private final ContractDao contractDao;

  @Inject
  WrapperTimeSlot(ContractManager contractManager, 
      ContractDao contractDao) {
    this.contractManager = contractManager;
    this.contractDao = contractDao;
  }

  public IWrapperTimeSlot setValue(TimeSlot ts) {
    this.value = ts;
    return this;
  }

  @Override
  public TimeSlot getValue() {
    return value;
  }

  /**
   * I contratti attivi che attualmente hanno impostato il TimeSlot.
   */
  @Override
  public List<Contract> getAssociatedActiveContract(Office office) {

    LocalDate today = LocalDate.now();
    List<Contract> activeContract = contractDao
        .getActiveContractsInPeriod(today, Optional.ofNullable(today), Optional.of(office));

    List<Contract> list = new ArrayList<Contract>();

    for (Contract contract : activeContract) {
      Optional<ContractMandatoryTimeSlot> current = contractManager
              .getContractMandatoryTimeSlotFromDate(contract, today);
      if (current.isPresent() && current.get().timeSlot.equals(this.value)) {
        list.add(contract);
      }
    }

    return list;
  }

  /**
   * Ritorna i periodi con questo tipo orario appartenti a contratti attualmente attivi.
   */
  @Override
  public List<ContractMandatoryTimeSlot> getAssociatedPeriodInActiveContract(Office office) {

    LocalDate today = LocalDate.now();
    List<Contract> activeContract = contractDao
        .getActiveContractsInPeriod(today, Optional.ofNullable(today), Optional.of(office));

    return activeContract.stream().flatMap(c -> c.getContractMandatoryTimeSlots().stream())
      .filter(cmts -> cmts.timeSlot.equals(this.value)).collect(Collectors.toList());
    
  }

  @Override
  public List<Contract> getAssociatedContract() {
    return this.value.contractMandatoryTimeSlots.stream()
        .map(cmts -> cmts.contract).collect(Collectors.toList());
  }
}
