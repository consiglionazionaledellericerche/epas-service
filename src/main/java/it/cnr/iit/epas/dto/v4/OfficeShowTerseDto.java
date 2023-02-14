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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Informazioni principali di un ufficio.
 *
 * @author Cristian Lucchesi
 *
 */
@Schema(description = "Informazioni principali di un ufficio")
@ToString
@Data
@EqualsAndHashCode(callSuper = true)
public class OfficeShowTerseDto extends PeriodModelDto {

  @Schema(description = "Nome dell'ufficio", example = "IIT - Pisa")
  private String name;
  //Codice della sede, per esempio per la sede di Pisa è "044000"
  @Schema(description = "Codice della sede", example = "044000")
  private String code;
  @Schema(description = "Id dell'istituto a cui appartiene questo ufficio")
  private Long instituteId;
}