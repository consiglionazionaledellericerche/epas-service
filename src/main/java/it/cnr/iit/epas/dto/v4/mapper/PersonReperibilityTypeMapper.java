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

import it.cnr.iit.epas.dto.v4.MonthlyCompetenceTypeDto;
import it.cnr.iit.epas.dto.v4.PersonReperibilityTypeTerseDto;
import it.cnr.iit.epas.models.MonthlyCompetenceType;
import it.cnr.iit.epas.models.PersonReperibilityType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da AbsenceGroups al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface PersonReperibilityTypeMapper {

  @Mapping(target = "name", expression = "java(monthlyCompetenceType.toString())")
  MonthlyCompetenceTypeDto convert(MonthlyCompetenceType monthlyCompetenceType);

  @Mapping(target = "id", source = "personReperibilityType.id")
  @Mapping(target = "description", source = "personReperibilityType.description")
  PersonReperibilityTypeTerseDto convert(PersonReperibilityType personReperibilityType);
}