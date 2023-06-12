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

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * DTO per una specifica giornata all'interno di un periodo di assenze.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class DayInPeriodDto {
  private LocalDate date;
  private AbsencePeriodDto absencePeriod;

  private List<TakenAbsenceDto> takenAbsences = Lists.newArrayList();
  private List<AbsenceShowTerseDto> existentComplations = Lists.newArrayList();
  private ComplationAbsenceDto complationAbsence; //quando è unico

  private List<TemplateRowDto> rowRecap = Lists.newArrayList();

  //Il rimpiazzamento
  private List<AbsenceShowTerseDto> existentReplacings = Lists.newArrayList();
  private AbsenceTypeDto correctReplacing;

}