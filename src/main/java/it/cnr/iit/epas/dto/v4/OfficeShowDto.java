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
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Informazioni esportate in Json per l'ufficio.
 *
 * @author Cristian Lucchesi
 *
 */
@ToString
@Data
@EqualsAndHashCode(callSuper = true)
public class OfficeShowDto extends OfficeShowTerseDto {

  @Schema(description = "Identificato univoco esterno ad ePAS")
  private String externalId;
  @Schema(description = "Indirizzo postal della sede")
  private String address;
  @Schema(description = "Id dell'istituto a cui appartiene questo ufficio")
  private Long instituteId;
  @Schema(description = "Indica se è la sede principale o meno", example = "true")
  private boolean headQuarter;
  @Schema(description = "Data e ora di ultimo aggiornamento della sede")
  private LocalDateTime updatedAt;

}