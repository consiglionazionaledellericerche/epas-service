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

package it.cnr.iit.epas.dto.v4.mapper;

import it.cnr.iit.epas.dto.v4.PersonShowDto;
import it.cnr.iit.epas.dto.v4.UserShowDto;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.User;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da AbsenceGroups al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface SeatOrganizationChartMapper {

  default String mapRoleToName(Role role) {
    return role.getName();
  }

  @Mapping(target = "birthDate", source = "birthday")
  @Mapping(target = "qualification", source = "person.qualification.id")
  PersonShowDto convert(Person person);

  List<UserShowDto> convert(List<User> users);

  // Converte Map<Role, List<User>> -> Map<String, List<UserShowDto>>
  Map<String, List<UserShowDto>> convert(Map<Role, List<User>> users);
  
  List<String> convertRoles(List<Role> roles);

  @Mapping(target = "fullname", expression = "java(user.getPerson().getFullname())")
  UserShowDto userToUserShowDto(User user);

}