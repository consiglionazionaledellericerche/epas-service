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

import it.cnr.iit.epas.dto.v4.ContractShowDto;
import it.cnr.iit.epas.dto.v4.ContractWorkingTimeTypeDto;
import it.cnr.iit.epas.dto.v4.WorkingTimeTypeDayDto;
import it.cnr.iit.epas.dto.v4.WorkingTimeTypeDto;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.WorkingTimeType;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper per ShowCurrentContractWorkingTimeType.
 */
@Mapper(componentModel = "spring")
public interface ShowCurrentContractWttMapper {

  @Mapping(target = "endDate", expression = "java(value.getEndDate())")
  ContractShowDto mapContract(Contract value);

  WorkingTimeTypeDayDto convert(WorkingTimeTypeDay wttd);
  List<WorkingTimeTypeDayDto> convert( List<WorkingTimeTypeDay> wttd);

  @Mapping(target = "beginDate", expression = "java(cwtt.getBeginDate())")
  @Mapping(target = "endDate", expression = "java(cwtt.getEndDate())")
  ContractWorkingTimeTypeDto convert(ContractWorkingTimeType cwtt);

  WorkingTimeTypeDto convert(WorkingTimeType currentWorkingTimeType);
  WorkingTimeTypeDto map(Optional<WorkingTimeType> value);

}