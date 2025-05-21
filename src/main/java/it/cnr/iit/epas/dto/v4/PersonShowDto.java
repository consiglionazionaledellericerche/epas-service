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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO per mostrare i dati principali di una persona.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PersonShowDto extends PersonMutableDto {

  @Schema(description = "Id della persona")
  private Long id;

  @Schema(description = "Data inizio validità")
  private LocalDate beginDate;
  @Schema(description = "Data fine validità")
  private LocalDate endDate;

  @Schema(description = "Utente collegato alla persona")
  private UserShowTerseDto user;
  @Schema(description = "Ufficio collegato alla persona")
  private OfficeShowTerseDto office;

  @Schema(description = "Data ultimo aggiornamento")
  private LocalDateTime updatedAt;

  @Schema(description = "Data di nascita")
  private LocalDate birthDate;

  @Schema(description = "Residenza")
  private String residence;
}