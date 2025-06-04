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

import it.cnr.iit.epas.dao.InstituteDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dao.QualificationDao;
import it.cnr.iit.epas.dto.v4.ContractCreateDto;
import it.cnr.iit.epas.dto.v4.ContractUpdateDto;
import it.cnr.iit.epas.dto.v4.OfficeCreateDto;
import it.cnr.iit.epas.dto.v4.PersonCreateDto;
import it.cnr.iit.epas.dto.v4.PersonUpdateDto;
import it.cnr.iit.epas.dto.v4.StampingCreateDto;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.Stamping;
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
  @Inject
  protected PersonDao personDao;
  @Inject
  protected PersonDayDao personDayDao;
  @Inject
  protected OfficeDao officeDao;
  @Inject
  protected QualificationDao qualificationDao;

  @Mapping(target = "institute", 
      expression = "java(instituteDao.byId(officeDto.getInstituteId())"
          + ".orElseThrow(() -> "
          + "new jakarta.persistence."
          + "EntityNotFoundException(\"Institute not found\")))")
  public abstract void update(@MappingTarget Office office, OfficeCreateDto officeDto);

  @Mapping(target = "qualification", 
      expression = "java(qualificationDao.byId(personDto.getQualification())"
          + ".orElseThrow(() -> "
          + "new jakarta.persistence."
          + "EntityNotFoundException(\"Qualification not found\")))")
  public abstract void update(@MappingTarget Person person, PersonUpdateDto personDto);

  public abstract void update(@MappingTarget Contract contract, ContractUpdateDto contractDto);

  @Mapping(target = "office", 
      expression = "java(officeDao.byId(personDto.getOfficeId())"
          + ".orElseThrow(() -> "
          + "new jakarta.persistence."
          + "EntityNotFoundException(\"Office not found\")))")
  @Mapping(target = "qualification", 
      expression = "java(qualificationDao.byId(personDto.getQualification())"
      + ".orElseThrow(() -> "
      + "new jakarta.persistence."
      + "EntityNotFoundException(\"Qualification not found\")))")
  public abstract void create(@MappingTarget Person person, PersonCreateDto personDto);

  @Mapping(target = "person", 
      expression = "java(personDao.byId(contractDto.getPersonId())"
          + ".orElseThrow(() -> "
          + "new jakarta.persistence."
          + "EntityNotFoundException(\"Person not found\")))")
  public abstract void create(@MappingTarget Contract contract, ContractCreateDto contractDto);

  @Mapping(target = "id", source = "stampingId")
  @Mapping(target = "personDay",
      expression = "java(personDayDao.getPersonDay(personDao.getPersonById("
          + "stampingDto.getPersonId()), stampingDto.getDate()).orElse(null))")
  public abstract void create(@MappingTarget Stamping stamping, StampingCreateDto stampingDto);

}