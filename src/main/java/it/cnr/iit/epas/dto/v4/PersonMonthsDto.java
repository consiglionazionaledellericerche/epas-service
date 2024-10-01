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

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO per rappresentare i IWrapperContractMonthRecap.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
@AllArgsConstructor
public class PersonMonthsDto {

  private ContractMonthRecapDto value;
  private Optional<ContractMonthRecapDto> previousRecap;
  private Optional<ContractMonthRecapDto> previousRecapInYear;
  private boolean hasResidualInitInYearMonth;
  private boolean hasResidualLastYear;
  private int getResidualLastYearInit;

}