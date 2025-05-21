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

import it.cnr.iit.epas.dto.v4.AbsenceShowTerseDto;
import it.cnr.iit.epas.dto.v4.BlockMealTicketDto;
import it.cnr.iit.epas.dto.v4.ContractShowTerseDto;
import it.cnr.iit.epas.dto.v4.DateIntervalDto;
import it.cnr.iit.epas.dto.v4.MealTicketDto;
import it.cnr.iit.epas.dto.v4.MealTicketRecapDto;
import it.cnr.iit.epas.dto.v4.OfficeShowTerseDto;
import it.cnr.iit.epas.dto.v4.PersonBaseDto;
import it.cnr.iit.epas.dto.v4.PersonDayDto;
import it.cnr.iit.epas.manager.services.mealtickets.BlockMealTicket;
import it.cnr.iit.epas.manager.services.mealtickets.MealTicketRecap;
import it.cnr.iit.epas.models.MealTicket;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.utils.DateInterval;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da Person al suo DTO per la visualizzazione coincisa (terse) via REST.
 */
@Mapper(componentModel = "spring")
public interface MealTicketRecapMapper {

  ContractShowTerseDto convert(ContractShowTerseDto contractShowTerseDto);

  @Mapping(target = "personId", source = "person.id")
  PersonDayDto convert(PersonDay personDay);

  @Mapping(target = "justifiedType", source = "absence.justifiedType.name")
  @Mapping(target = "externalId", source = "absence.externalIdentifier")
  @Mapping(target = "justifiedTime", expression = "java(absence.justifiedTime())")
  @Mapping(target = "nothingJustified", expression = "java(absence.nothingJustified())")
  @Mapping(target = "date", expression = "java(absence.getAbsenceDate())")
  AbsenceShowTerseDto convertTerse(Absence absence);

  DateIntervalDto convert(DateInterval dateIntervalDto);

  MealTicketDto convert(MealTicket mealTicket);

  @Mapping(target = "first", expression = "java(blockMealTicketRecap.getFirst())")
  @Mapping(target = "last", expression = "java(blockMealTicketRecap.getLast())")
  @Mapping(target = "getConsumed", expression = "java(blockMealTicketRecap.getConsumed())")
  @Mapping(target = "getRemaining", expression = "java(blockMealTicketRecap.getRemaining())")
  @Mapping(target = "getDimBlock", expression = "java(blockMealTicketRecap.getDimBlock())")
  @Mapping(target = "returned", expression = "java(blockMealTicketRecap.isReturned())")
  @Mapping(target = "getReceivedDate", expression = "java(blockMealTicketRecap.getReceivedDate())")
  @Mapping(target = "getExpireDate", expression = "java(blockMealTicketRecap.getExpireDate())")
  @Mapping(target = "getDate", expression = "java(blockMealTicketRecap.getDate())")
  BlockMealTicketDto convert(BlockMealTicket blockMealTicketRecap);

  List<BlockMealTicketDto> convert(List<BlockMealTicket> blockMealTicketRecap);

//  @Mapping(target = "blockMealTicketReceivedDeliveryDesc", expression = "java(mealTicketRecap.getBlockMealTicketReceivedDeliveryDesc())")
  MealTicketRecapDto convert(MealTicketRecap mealTicketRecap);

  OfficeShowTerseDto convert(Office office);

  //MealTicketCardDto convert(MealTicketCard mealTicketCard);

  PersonBaseDto convert(Person person);

}