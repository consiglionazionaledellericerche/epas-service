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
import java.util.List;
import lombok.Data;

/**
 * DTO per la situazione ferie di una persona.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class PersonVacationDto {

  private boolean topQualification;
  private int year;
  private int month;
  private List<ContractDto> contracts;

  private List<VacationSituationDto> vacationSituations = Lists.newArrayList();
  private PeriodChainDto periodChain;

}