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

import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.manager.services.absences.AbsenceForm;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.model.PeriodChain;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.security.SecureUtils;
import java.time.LocalDate;
import it.cnr.iit.epas.models.Role;
import lombok.extern.slf4j.Slf4j;

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
  public AbsenceGroupsRecap(UserDao userDao, AbsenceComponentDao absenceComponentDao,
      AbsenceService absenceService, Person person, Long groupAbsenceTypeId,
      LocalDate from, SecureUtils securityUtils) {

    final long start = System.currentTimeMillis();
    log.trace("inizio AbsenceGroupsRecap. Person = {}, from = {}",
        person.getFullname(), from);

    this.from = from;
    isAdmin = false;

    groupAbsenceType = absenceComponentDao.groupAbsenceTypeById(groupAbsenceTypeId);
    groupAbsenceType = groupAbsenceType.firstOfChain();

    periodChain = absenceService.residual(person, groupAbsenceType, from);

    //se l'user è amministratore visualizzo lo switcher del gruppo
        User currentUser = securityUtils.getCurrentUser().get();
        if (currentUser.isSystemUser()
            || userDao.getUsersWithRoles(person.getOffice(),
                Role.PERSONNEL_ADMIN, Role.PERSONNEL_ADMIN_MINI)
            .contains(currentUser)) {
          isAdmin = true;
        }

    /*if (!groupAbsenceType.isAutomatic()) {
      categorySwitcher = null;
    } else {*/
      categorySwitcher = absenceService
          .buildForCategorySwitch(person, from, groupAbsenceType);
    //}
    log.debug(
        "fine creazione nuovo AbsenceGroupsRecap in {} ms. "
        + "Person = {} groupAbsenceType = {}, from = {}",
        System.currentTimeMillis() - start, person, groupAbsenceType, from);
  }
}
