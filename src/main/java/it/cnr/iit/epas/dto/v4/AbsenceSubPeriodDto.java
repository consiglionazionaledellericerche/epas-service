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

package it.cnr.iit.epas.dto.v4;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO per i sotto periodi di assenza (dipendono dai contratti).
 *
 * @author Cristian Lucchesi
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AbsenceSubPeriodDto extends AbsencePeriodTerseDto {

  private long subAmount;
  private boolean subFixedPostPartum;
  private long subAmountBeforeFixedPostPartum;
  private long subTotalAmount;
  private long subDayProgression;
  private long subDayPostPartum;
  private long subDayPostPartumProgression;
  private long subDayToFixPostPartum;
  private boolean subAccrued;
  private LocalDate contractEndFirstYearInPeriod;
  private long dayInInterval;

}