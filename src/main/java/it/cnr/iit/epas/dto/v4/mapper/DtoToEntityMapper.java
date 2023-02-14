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

import it.cnr.iit.epas.dao.InstituteDao;
import it.cnr.iit.epas.dto.v4.OfficeCreateDto;
import it.cnr.iit.epas.models.Office;
import javax.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

/**
 * Effettua il mapping da DTO ad Entity.
 *
 * @author Cristian Lucchesi
 *
 */
@Component
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DtoToEntityMapper {

  @Inject
  protected InstituteDao instituteDao;

  @Mapping(target = "institute", 
      expression = "java(instituteDao.byId(officeDto.getInstituteId())"
          + ".orElseThrow(() -> "
          + "new it.cnr.iit.epas.controller.exceptions."
          + "EntityNotFoundException(\"Institute not found\")))")
  public abstract void update(@MappingTarget Office office, OfficeCreateDto officeDto);

}