/*
 * Copyright (C) 2023 Consiglio Nazionale delle Ricerche
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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.ReperibilityTypeMonthDao;
import it.cnr.iit.epas.manager.ReperibilityManager2;
import it.cnr.iit.epas.models.PersonReperibilityType;
import it.cnr.iit.epas.models.ReperibilityTypeMonth;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Oggetto che modella il contenuto della vista contenente il riepilogo delle ferie e permessi.
 *
 * @author Andrea Generosi
 */
@Slf4j
public class ReperibilityCalendarRecap {

  public PersonReperibilityType reperibility;
  public ReperibilityTypeMonth reperibilityTypeMonth;
  public List<ReperibilityCalculatedCompetences> workDaysReperibilityCalculatedCompetences = 
      Lists.newArrayList();
  public List<ReperibilityCalculatedCompetences> holidaysReperibilityCalculatedCompetences = 
      Lists.newArrayList();


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

    val workDaysRepCompetences = reperibilityManager2
        .calculateReperibilityWorkDaysCompetences(reperibility, start, end);

    workDaysRepCompetences.forEach((person, count) -> {
      val personCompRecap = new ReperibilityCalculatedCompetences();
      personCompRecap.setFullname(person.getFullname());
      personCompRecap.setCount(count);
      workDaysReperibilityCalculatedCompetences.add(personCompRecap);
    });
    val holidaysRepCompetences =
        reperibilityManager2.calculateReperibilityHolidaysCompetences(reperibility, start, end);

    holidaysRepCompetences.forEach((person, count) -> {
      val personCompRecap = new ReperibilityCalculatedCompetences();
      personCompRecap.setFullname(person.getFullname());
      personCompRecap.setCount(count);
      holidaysReperibilityCalculatedCompetences.add(personCompRecap);
    });

    reperibilityTypeMonth = reperibilityTypeMonthDao
        .byReperibilityTypeAndDate(reperibility, start).orElse(null);
  }
}
