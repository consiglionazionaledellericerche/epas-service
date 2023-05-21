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

import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary.TypeSummary;
import java.time.LocalDate;
import lombok.Data;

/**
 * DTO contenente il resoconto della situazione delle ferie.
 */
@Data
public class VacationSummaryTerseDto {

  private TypeSummary type;
  private String title;
  private int year;
  private LocalDate date;
  private long total;
  private long accrued;
  private long used;
  private long usableTotal;
  private long usable;

}