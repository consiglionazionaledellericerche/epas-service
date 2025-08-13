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
import lombok.Data;

/**
 * DTO per l'esportazione via REST delle informazioni
 * principali di PersonReperibilityDay.
 *
 * @since versione 4 dell'API REST
 * @author Cristian Lucchesi
 *
 */
@Data
public class PersonReperibilityDayDto {

  @Schema(description = "Reperibilità della persona")
  private PersonReperibilityDto personReperibility;

  @Schema(description = "Data di reperibilità")
  private LocalDate date;

  @Schema(description = "Flag per indicare se si tratta di un giorno di festa")
  private Boolean holidayDay;

}