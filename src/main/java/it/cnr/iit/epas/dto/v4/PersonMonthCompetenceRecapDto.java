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

import lombok.Data;

/**
 * DTO per i CompetenceCode.
 *
 * @author Andrea Generosi
 *
 */
@Data
public class PersonMonthCompetenceRecapDto {
  private ContractShowDto contract;
  private int year;
  private int month;

  private int holidaysAvailability = 0;
  private int weekDayAvailability = 0;
  private int daylightWorkingDaysOvertime = 0;
  private int daylightholidaysOvertime = 0;
  private int ordinaryShift = 0;
  private int nightShift = 0;
  private int progressivoFinalePositivoMese = 0;

}
