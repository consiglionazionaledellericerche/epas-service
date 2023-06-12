/*
 * Copyright (C) 2022 Consiglio Nazionale delle Ricerche
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.iit.epas.manager.recaps.absencegroups;

import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.manager.services.absences.AbsenceForm;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.model.AbsencePeriod;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod;
import it.cnr.iit.epas.manager.services.absences.model.PeriodChain;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Oggetto che modella il contenuto della vista contenente il riepilogo delle ferie e permessi.
 *
 * @author Andrea Generosi
 */
@Slf4j
public class AbsenceGroupsRecap {

  public LocalDate from;
  public boolean isAdmin;
  public GroupAbsenceType groupAbsenceType;
  public PeriodChain periodChain;
  public AbsenceForm categorySwitcher;


  /**
   * Costruisce l'oggetto contenente tutte le informazioni da renderizzare nella pagina riepilogo
   * ferie e permessi.
   *
   * @param absenceComponentDao absenceComponentDao
   * @param absenceService      absenceService
   */
  public AbsenceGroupsRecap(AbsenceComponentDao absenceComponentDao,
      AbsenceService absenceService, Person person, Long groupAbsenceTypeId, LocalDate from) {

    final long start = System.currentTimeMillis();
    log.trace("inizio AbsenceGroupsRecap. Person = {}, from = {}",
        person.getFullname(), from);

    this.from = from;
    this.isAdmin = false;

    groupAbsenceType = absenceComponentDao.groupAbsenceTypeById(groupAbsenceTypeId);
    groupAbsenceType = groupAbsenceType.firstOfChain();

    periodChain = absenceService.residual(person, groupAbsenceType, from);

    //se l'user è amministratore visualizzo lo switcher del gruppo
//    Optional<User> user = secureUtils.getCurrentUser();
//    if (currentUser.isSystemUser()
//        || userDao.getUsersWithRoles(person.getOffice(),
//            Role.PERSONNEL_ADMIN, Role.PERSONNEL_ADMIN_MINI)
//        .contains(currentUser)) {
//      this.isAdmin = true;
//    }

    if (!groupAbsenceType.isAutomatic()) {

    }
    categorySwitcher = absenceService
        .buildForCategorySwitch(person, from, groupAbsenceType);

    log.debug(
        "fine creazione nuovo AbsenceGroupsRecap in {} ms. person = {} groupAbsenceType = {}, from = {}",
        System.currentTimeMillis() - start, person, groupAbsenceType, from);
  }
}
