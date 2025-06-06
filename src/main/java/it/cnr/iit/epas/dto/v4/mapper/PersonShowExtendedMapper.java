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
import it.cnr.iit.epas.dto.v4.PersonReperibilityTypeDto;
import it.cnr.iit.epas.dto.v4.PersonReperibilityTypeTerseDto;
import it.cnr.iit.epas.dto.v4.PersonShiftDto;
import it.cnr.iit.epas.dto.v4.PersonShowExtendedDto;
import it.cnr.iit.epas.dto.v4.ShiftCategoryDto;
import it.cnr.iit.epas.dto.v4.UserShowTerseDto;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonReperibilityType;
import it.cnr.iit.epas.models.PersonShift;
import it.cnr.iit.epas.models.ShiftCategories;
import it.cnr.iit.epas.models.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da Person al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface PersonShowExtendedMapper {
  List<ShiftCategoryDto> convertShiftCategories(List<ShiftCategories> shiftCategories);

  List<PersonReperibilityTypeDto> convertPersonReperibility(
      List<PersonReperibilityType> reperibilities);

  List<PersonShiftDto> convertPersonShift(List<PersonShift> personShift);

  PersonReperibilityTypeTerseDto convert(PersonReperibilityType reperibility);


  @Mapping(target = "birthDate", source = "birthday")
  @Mapping(target = "qualification", source = "qualification.id")
  PersonShowExtendedDto convert(Person person);

  OfficeShowTerseDto convert(Office office);

  UserShowTerseDto convert(User user);

}