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

package it.cnr.iit.epas.manager.recaps.personvacation;

import it.cnr.iit.epas.manager.services.absences.model.AbsencePeriod;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary;
import it.cnr.iit.epas.models.Person;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;


/**
 * Oggetto che modella il contenuto della vista contenente il riepilogo delle ferie e permessi.
 *
 * @author Andrea Generosi
 */
@Slf4j
public class PersonVacationSummarySubperiod {

  public Person person;
  public AbsencePeriod period;
  public long subAmount;
  public boolean subFixedPostPartum;
  public long subAmountBeforeFixedPostPartum;
  public long subTotalAmount;
  public long subDayProgression;
  public long subDayPostPartum;
  public long subDayToFixPostPartum;
  public boolean subAccrued;
  public LocalDate contractEndFirstYearInPeriod;
  public long dayInInterval;

  /**
   * Costruisce l'oggetto contenente tutte le informazioni da renderizzare nella pagina riepilogo
   * ferie e permessi.
   *
   * @param period              AbsencePeriod
   */

  public PersonVacationSummarySubperiod(VacationSummary vacationSummary, AbsencePeriod period) {
    final long start = System.currentTimeMillis();

    subAmount = vacationSummary.subAmount(period);
    subFixedPostPartum = vacationSummary.subFixedPostPartum(period);
    subAmountBeforeFixedPostPartum = vacationSummary.subAmountBeforeFixedPostPartum(period);
    subTotalAmount = vacationSummary.subTotalAmount(period);
    subDayProgression = vacationSummary.subDayProgression(period);
    subDayPostPartum = vacationSummary.subDayPostPartum(period);
    subDayToFixPostPartum = vacationSummary.subDayToFixPostPartum(period);
    subAccrued = vacationSummary.subAccrued(period);
    contractEndFirstYearInPeriod = vacationSummary.contractEndFirstYearInPeriod(period);
    dayInInterval = period.periodInterval().dayInInterval();

    log.debug(
        "fine creazione nuovo PersonVacationSummarySubperiod. vacationSummary = {}, period = {}",
        System.currentTimeMillis() - start, vacationSummary, period);
  }
}
