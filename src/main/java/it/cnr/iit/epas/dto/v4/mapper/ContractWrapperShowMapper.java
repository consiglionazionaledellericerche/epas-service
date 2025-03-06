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

import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.dao.wrapper.IWrapperPerson;
import it.cnr.iit.epas.dto.v4.ContractShowDto;
import it.cnr.iit.epas.dto.v4.ContractStampProfileDto;
import it.cnr.iit.epas.dto.v4.ContractWrapperShowDto;
import it.cnr.iit.epas.dto.v4.OfficeShowTerseDto;
import it.cnr.iit.epas.dto.v4.PersonWrapperShowDto;
import it.cnr.iit.epas.dto.v4.UserShowTerseDto;
import it.cnr.iit.epas.dto.v4.VacationPeriodDto;
import it.cnr.iit.epas.dto.v4.WorkingTimeTypeDayDto;
import it.cnr.iit.epas.dto.v4.WorkingTimeTypeDto;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractStampProfile;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.VacationPeriod;
import it.cnr.iit.epas.models.WorkingTimeType;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da ContractWrapper al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface ContractWrapperShowMapper {

  @Mapping(target = ".", source = "value")
  @Mapping(target = "defined", expression = "java(contract.isDefined())")
  @Mapping(target = "initializationMissing", expression = "java(contract.initializationMissing())")
  @Mapping(target = "mealTicketInitBeforeGeneralInit", expression = "java(contract.mealTicketInitBeforeGeneralInit())")
  ContractWrapperShowDto convert(IWrapperContract contract);

  @Mapping(target = "endDate", expression = "java(value.getEndDate())")
  //@Mapping(target = "previousContract", expression = "java(value.previousContract)")
  ContractShowDto convert(Contract value);

}