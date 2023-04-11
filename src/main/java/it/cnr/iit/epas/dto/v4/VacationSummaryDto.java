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

package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary.TypeSummary;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * DTO contenente il resoconto della situazione delle ferie.
 */
@Data
public class VacationSummaryDto {

  private TypeSummary type;
  private ContractBaseDto contract;
  private int year;
  private LocalDate date;  //data situazione. Tipicamenete oggi. Determina maturate e scadute.
  private long total;
  private long accrued;
  private long used;
  private long usableTotal;
  private long usable;
  private LocalDate upperLimit;
  private long sourced;
  private String title;

  private List<AbsenceShowDto> absencesUsed = Lists.newArrayList();
  private List<AbsenceShowDto> postPartum = Lists.newArrayList();
  private AbsencePeriodDto absencePeriod;
  private long postPartumSize;
  private boolean postPartumisEmpty;

  private long accruedDayTotal;
  private long subAmount;
  private boolean subFixedPostPartum;
  private long subAmountBeforeFixedPostPartum;
  private long subTotalAmount;
  private long subDayProgression;
  private long subDayPostPartum;
  private long subDayToFixPostPartum;
  private boolean subAccrued;
  private LocalDate contractEndFirstYearInPeriod;

}