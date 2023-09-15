/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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
import java.util.List;
import lombok.Data;

/**
 * DTO per l'esportazione via REST delle informazioni
 * principali di PersonReperibilityDto.
 *
 * @since versione 4 dell'API REST
 * @author Cristian Lucchesi
 *
 */
@Data
public class PersonReperibilityDto {

  @Schema(description = "Persona")
  private
  PersonShowDto person;

  @Schema(description = "Data di inizio reperibilità")
  private
  LocalDate startDate;

  @Schema(description = "Data di fine reperibilità")
  private
  LocalDate endDate;

  @Schema(description = "Tipologia di reperibilità")
  private
  PersonReperibilityTypeDto personReperibilityType;

  @Schema(description = "Note")
  private
  String note;

  @Schema(description = "Note")
  private
  List<PersonReperibilityDto> personReperibilityDays;
}