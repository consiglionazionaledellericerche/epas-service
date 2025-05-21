/*
 * Copyright (C) 2025  Consiglio Nazionale delle Ricerche
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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO con i dati comuni per la show terse, la create e l'update degli Office.
 *
 * @author Cristian Lucchesi
 */
@Data
public class OfficeBaseDto {

  @NotNull
  @Schema(description = "Nome dell'ufficio", example = "IIT - Pisa")
  private String name;

  //Codice della sede, per esempio per la sede di Pisa è "044000"
  @Schema(description = "Codice della sede", example = "044000")
  private String code;

  //sedeId, serve per l'invio degli attestati, per esempio per la sede di Pisa è "223400"
  @NotNull
  @Schema(description = "sedeId della sede, al CNR serve per l'invio degli attestati", 
      example = "223400")
  private String codeId;
}