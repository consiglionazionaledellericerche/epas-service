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
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * Dto con i dati per la creazione di un nuovo ufficio.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class OfficeCreateDto {

  @NotNull
  @Schema(description = "Nome dell'ufficio", example = "IIT - Pisa")
  private String name;
  //Codice della sede, per esempio per la sede di Pisa è "044000"
  @Schema(description = "Codice della sede", example = "044000")
  private String code;
  @NotNull
  //sedeId, serve per l'invio degli attestati, per esempio per la sede di Pisa è "223400"
  @Schema(description = "i della sede, al CNR serve per l'invio degli attestati", 
      example = "223400")
  private String codeId;
  @Schema(description = "Identificato univoco esterno ad ePAS")
  private String externalId;
  @Schema(description = "Indirizzo postal della sede")
  private String address;
  @Schema(description = "Id dell'istituto a cui appartiene questo ufficio")
  private Long instituteId;
  @Schema(description = "Indica se è la sede principale o meno", example = "true")
  private boolean headQuarter;
  @Schema(description = "Data inizio validità")
  private LocalDate beginDate;
  @Schema(description = "Data fine validità")
  private LocalDate endDate;
}
