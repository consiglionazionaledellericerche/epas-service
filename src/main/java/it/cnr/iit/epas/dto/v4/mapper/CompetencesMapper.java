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

package it.cnr.iit.epas.dto.v4.mapper;

import it.cnr.iit.epas.dto.v4.CompetenceCodeDto;
import it.cnr.iit.epas.dto.v4.CompetenceDto;
import it.cnr.iit.epas.dto.v4.ContractShowDto;
import it.cnr.iit.epas.dto.v4.PersonMonthCompetenceRecapDto;
import it.cnr.iit.epas.manager.recaps.competences.PersonMonthCompetenceRecap;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da Competences al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface CompetencesMapper {

  @Mapping(target = "personId", source = "person.id")
  ContractShowDto convert(Contract contract);

  PersonMonthCompetenceRecapDto convertPersonMonthCompetenceRecap(PersonMonthCompetenceRecap recap);

  CompetenceCodeDto convert(CompetenceCode competenceCode);

  @Mapping(target = "personId", source = "person.id")
  CompetenceDto convertCompetence(Competence competence);
}