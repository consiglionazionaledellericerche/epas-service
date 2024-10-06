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

package it.cnr.iit.epas.manager.recaps.absencegroups;

import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.security.SecureUtils;
import java.time.LocalDate;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Factory per AbsenceGroupsRecap.
 */
@Component
public class AbsenceGroupsRecapFactory {

  private final UserDao userDao;
  private final AbsenceComponentDao absenceComponentDao;
  private final AbsenceService absenceService;
  private final SecureUtils securityUtils;

  /**
   * Costruttore per l'injection.
   */
  @Inject
  AbsenceGroupsRecapFactory(UserDao userDao, AbsenceComponentDao absenceComponentDao,
      AbsenceService absenceService, SecureUtils securityUtils) {

    this.absenceComponentDao = absenceComponentDao;
    this.userDao = userDao;
    this.absenceService = absenceService;
    this.securityUtils = securityUtils;
  }

  /**
   * Costruisce il riepilogo delle absence groups.
   */
  public AbsenceGroupsRecap create(Person person, Long groupAbsenceTypeId, LocalDate from) {

    return new AbsenceGroupsRecap(userDao, absenceComponentDao, absenceService,
        person, groupAbsenceTypeId, from, securityUtils);
  }

}
