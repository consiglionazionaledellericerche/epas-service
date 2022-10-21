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
package it.cnr.iit.epas.manager.listeners;

import com.google.common.base.Verify;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import java.time.LocalDate;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ConsistencyManagerListener {

  private ContractDao contractDao;
  private AbsenceComponentDao absenceComponentDao;
  private AbsenceService absenceService;

  @Inject
  public ConsistencyManagerListener(ContractDao contractDao,
      AbsenceComponentDao absenceComponentDao,
      AbsenceService absenceService) {
    this.contractDao = contractDao;
  }

  @AfterReturning(
      pointcut="execution(public java.util.Optional<it.cnr.iit.epas.models.Contract> it.cnr.iit.epas.manager.ConsistencyManager.updatePersonSituationEngine(Long,java.time.LocalDate,java.util.Optional<java.time.LocalDate>, boolean))",
      returning="contract")
  public void updatePersonSituationEngine(Optional<Contract> contract) {
    log.debug("ConsistencyManagerListener.updatePersonSituationEngine started, contract = {}", contract.orElse(null));
    if (contract.isPresent()) {
      Verify.verifyNotNull(contract.get().getId());
      Contract currentContract = contractDao.byId(contract.get().getId());
      Verify.verifyNotNull(currentContract, 
          String.format("currentcontract is null, contract.id = %s", contract.get().getId()));
      GroupAbsenceType vacationGroup = absenceComponentDao
          .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();
      absenceService.buildVacationSituation(currentContract, LocalDate.now().getYear(),
          vacationGroup, Optional.empty(), true);
      log.debug("ConsistencyManagerListener.updatePersonSituationEngine ended, contract = {}", contract.get());
    } else {
      log.debug("Listener ConsistencyManagerListener.updatePersonSituationEngine started but no contract present");
    }
  }
}