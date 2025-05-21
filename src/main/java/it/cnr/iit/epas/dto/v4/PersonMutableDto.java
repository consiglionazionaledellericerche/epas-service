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
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO con i dati comuni per la creazione e l'aggiornamento di una persona.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PersonMutableDto extends PersonBaseDto {

  @NotNull
  @Schema(description = "livello")
  private Long qualification;
  @Schema(description = "Id di collegamento con l'anagrafica CNR")
  private Long perseoId;
  @Schema(description = "Data di nascita")
  private LocalDate birthday;
  @Schema(description = "Residenza")
  private String residence;
  @Schema(description = "Numero del telefono ufficio")
  private String telephone;
  @Schema(description = "Numero di fax")
  private String fax;
  @Schema(description = "Numero di cellulare")
  private String mobile;
  @Schema(description = "Abilitato invio delle email si/no")
  private boolean wantEmail;
}
