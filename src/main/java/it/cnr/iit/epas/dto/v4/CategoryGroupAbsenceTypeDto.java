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
import java.util.Set;
import lombok.Data;

/**
 * DTO per le private ContractualClauseDto contractualClause.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class CategoryGroupAbsenceTypeDto {

  @Schema(description = "Nome categoria.")
  private String name;
  @Schema(description = "Descrizione categoria.")
  private String description;
  @Schema(description = "Priorità categoria.")
  private int priority;
  @Schema(description = "Tipologia di gruppo di assenze.")
  private Set<GroupAbsenceTypeDto> groupAbsenceTypes;
  @Schema(description = "Categorie di tab da mostrare nel menu per la gestione delle assenze.")
  private CategoryTabDto tab;
  @Schema(description = "Documentazione delle varie disposizioni contrattuali raggruppate "
      + "per tipologia di assenza")
  private ContractualClauseDto contractualClause;

}