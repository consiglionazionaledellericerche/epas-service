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

import it.cnr.iit.epas.dto.v4.StampModificationTypeDto;
import it.cnr.iit.epas.dto.v4.StampTypeDto;
import it.cnr.iit.epas.dto.v4.StampingDto;
import it.cnr.iit.epas.models.StampModificationType;
import it.cnr.iit.epas.models.Stamping;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da Stamping al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface StampingDtoMapper {

  @Mapping(target = "personDayId", source = "personDay.id")
  StampingDto convert(Stamping stamping);

  @Mapping(target = "name", expression = "java(stampType.name())")
  StampTypeDto convert(StampTypes stampType);

  @Mapping(target = "code", source = "stampModificationType.code")
  StampModificationTypeDto convert(StampModificationType stampModificationType);
}