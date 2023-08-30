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

package it.cnr.iit.epas.manager.recaps.reperibilitycalendar;

import it.cnr.iit.epas.dao.ReperibilityTypeMonthDao;
import it.cnr.iit.epas.manager.ReperibilityManager2;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonReperibilityType;
import it.cnr.iit.epas.models.ReperibilityTypeMonth;
import java.time.LocalDate;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Oggetto che modella il contenuto della vista contenente il riepilogo delle ferie e permessi.
 *
 * @author Andrea Generosi
 */
@Slf4j
public class ReperibilityCalendarRecap {

  public PersonReperibilityType reperibility;
  public ReperibilityTypeMonth reperibilityTypeMonth;
  public Map<Person, Integer> workDaysReperibilityCalculatedCompetences;
  public Map<Person, Integer> holidaysReperibilityCalculatedCompetences;


  /**
   * Costruisce l'oggetto contenente tutte le informazioni da renderizzare nella pagina riepilogo
   * ferie e permessi.
   *
   * @param reperibility PersonReperibilityType
   */
  public ReperibilityCalendarRecap(ReperibilityTypeMonthDao reperibilityTypeMonthDao,
      ReperibilityManager2 reperibilityManager2,
      PersonReperibilityType reperibility, LocalDate start, LocalDate end) {

    this.reperibility = reperibility;

    log.debug("ReperibilityCalendarRecap = reperibility{}", reperibility);
    log.debug("ReperibilityCalendarRecap = start{}", start);
    log.debug("ReperibilityCalendarRecap = end{}", end);

    workDaysReperibilityCalculatedCompetences = reperibilityManager2
        .calculateReperibilityWorkDaysCompetences(reperibility, start, end);
    holidaysReperibilityCalculatedCompetences =
        reperibilityManager2.calculateReperibilityHolidaysCompetences(reperibility, start, end);

    reperibilityTypeMonth = reperibilityTypeMonthDao
        .byReperibilityTypeAndDate(reperibility, start).orElse(null);

    log.debug("ReperibilityCalendarRecap = workDaysReperibilityCalculatedCompetences  {}", workDaysReperibilityCalculatedCompetences);
    log.debug("ReperibilityCalendarRecap = holidaysReperibilityCalculatedCompetences  {}", holidaysReperibilityCalculatedCompetences);
    log.debug("ReperibilityCalendarRecap = reperibilityTypeMonth  {}", reperibilityTypeMonth);

  }
}
