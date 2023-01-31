/*
 * Copyright (C) 2023  Consiglio Nazionale delle Ricerche
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

import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.manager.TimeSlotManager;
import it.cnr.iit.epas.models.PersonDay;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
class TimeSlotManagerListener {

  private final PersonDayDao personDayDao;

  private final TimeSlotManager timeSlotManager;


  @Inject
  TimeSlotManagerListener(PersonDayDao personDayDao, TimeSlotManager timeSlotManager) {
    this.personDayDao = personDayDao;
    this.timeSlotManager = timeSlotManager;
  }

  @AfterReturning(
      pointcut = "execution("
          + "public it.cnr.iit.epas.models.PersonDay "
          + "it.cnr.iit.epas.manager.TimeSlotManager."
          + "activateAfterRequesCheckAndManageMandatoryTimeSlot("
          + "it.cnr.iit.epas.models.PersonDay))",
          returning = "personDay")
  void checkAndManageMandatoryTimeSlot(PersonDay personDay) {
    log.debug("Invocato listener ConsistencyManagerLister::checkAndManageMandatoryTimeSlot");
    personDayDao.merge(personDay);
    timeSlotManager.checkAndManageMandatoryTimeSlot(personDay);
  }

}