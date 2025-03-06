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
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO con i dati comuni per la show terse, la create e l'update della Person.
 *
 * @author Cristian Lucchesi
 */
@Data
public class PersonBaseDto {

  @Schema(description = "Nome")
  @NotNull
  private String name;
  @Schema(description = "Cognome")
  @NotNull
  private String surname;
  @Schema(description = "Fullname")
  @NotNull
  private String fullname;

  @Schema(description = "Codice fiscale")
  private String fiscalCode;
  @Schema(description = "Email")
  @NotNull
  private String email;
  @Schema(description = "Matricola")
  private String number; //Matricola
  @Schema(description = "eppn - indicatore univoco all'interno dell'organizzazione")
  private String eppn;
}
