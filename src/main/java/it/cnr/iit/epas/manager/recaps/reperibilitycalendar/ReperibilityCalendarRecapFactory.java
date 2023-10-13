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

package it.cnr.iit.epas.manager.recaps.reperibilitycalendar;

import it.cnr.iit.epas.dao.ReperibilityTypeMonthDao;
import it.cnr.iit.epas.manager.ReperibilityManager2;
import it.cnr.iit.epas.models.PersonReperibilityType;
import java.time.LocalDate;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Factory per ReperibilityCalendarRecap.
 */
@Component
public class ReperibilityCalendarRecapFactory {

  private final ReperibilityTypeMonthDao reperibilityTypeMonthDao;
  private final ReperibilityManager2 reperibilityManager2;

  /**
   * Costruttore per l'injection.
   */
  @Inject
  ReperibilityCalendarRecapFactory(ReperibilityTypeMonthDao reperibilityTypeMonthDao,
      ReperibilityManager2 reperibilityManager2) {

    this.reperibilityTypeMonthDao = reperibilityTypeMonthDao;
    this.reperibilityManager2 = reperibilityManager2;
  }

  /**
   * Costruisce il riepilogo mensile delle timbrature.
   */
  public ReperibilityCalendarRecap create(
      PersonReperibilityType reperibility, LocalDate start, LocalDate end) {

    return new ReperibilityCalendarRecap(reperibilityTypeMonthDao, reperibilityManager2,
        reperibility, start, end);
  }

}
