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

import it.cnr.iit.epas.dto.v4.OfficeShowTerseDto;
import it.cnr.iit.epas.dto.v4.PersonChildrenShowDto;
import it.cnr.iit.epas.dto.v4.PersonShowDto;
import it.cnr.iit.epas.dto.v4.UserShowTerseDto;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonChildren;
import it.cnr.iit.epas.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da Person al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface PersonShowMapper {

  @Mapping(target = "birthDate", source = "birthday")
  @Mapping(target = "qualification", source = "qualification.id")
  PersonShowDto convert(Person person);

  OfficeShowTerseDto convert(Office office);

  UserShowTerseDto convert(User user);

  PersonChildrenShowDto convert(PersonChildren personChildren);
}