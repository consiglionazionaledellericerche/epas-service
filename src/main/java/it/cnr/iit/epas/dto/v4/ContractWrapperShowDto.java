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

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO per mostrare i dati estesi di una persona.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractWrapperShowDto extends ContractShowDto {

  private boolean defined;
  private boolean initializationMissing;
  private boolean mealTicketInitBeforeGeneralInit;
}