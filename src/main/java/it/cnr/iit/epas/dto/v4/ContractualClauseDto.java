/*
 * Copyright (C) 2024  Consiglio Nazionale delle Ricerche
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

/**
 * DTO per le tipologie di gruppi di assenze.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class ContractualClauseDto {
  @Schema(description = "Esempio: Permessi retribuiti (art. 72)")
  private String name;
  @Schema(description = "Tempi di fruizione.")
  private String fruitionTime;
  @Schema(description = "Caratteristiche Giuridico Economiche.")
  private String legalAndEconomic;
  @Schema(description = "Documentazione giustificativa.")
  private String supportingDocumentation;
  @Schema(description = "Modalità di richiesta.")
  private String howToRequest;
  @Schema(description = "Altre informazioni")
  private String otherInfos;
  @Schema(description = "Contesto a cui si applicano le clausole contrattuali.")
  private String context;
//  @Schema(description = "Tipologie di gruppi di assenze.")
//  private Set<CategoryGroupAbsenceTypeDto> categoryGroupAbsenceTypes = Sets.newHashSet();
}