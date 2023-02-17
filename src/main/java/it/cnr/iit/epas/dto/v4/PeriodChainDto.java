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
 * DTO per riassuntivo di una lista dei periodi di assenza.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class PeriodChainDto {

  private GroupAbsenceTypeDto groupAbsenceType;
  private LocalDate date;
  private LocalDate from = null;
  private LocalDate to = null;
  private List<AbsencePeriodTerseDto> periods = Lists.newArrayList();

}