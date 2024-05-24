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
import it.cnr.iit.epas.dto.v4.AbsenceErrorDto;
import it.cnr.iit.epas.dto.v4.AbsenceFormDto;
import it.cnr.iit.epas.dto.v4.AbsenceFormSimulationResponseDto;
import it.cnr.iit.epas.dto.v4.AbsenceShowDto;
import it.cnr.iit.epas.dto.v4.AbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.CategoryGroupAbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.ContractualClauseDto;
import it.cnr.iit.epas.dto.v4.CriticalErrorDto;
import it.cnr.iit.epas.dto.v4.GroupAbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.PersonShowDto;
import it.cnr.iit.epas.dto.v4.TemplateRowDto;
import it.cnr.iit.epas.manager.services.absences.AbsenceForm;
import it.cnr.iit.epas.manager.services.absences.AbsenceService.InsertReport;
import it.cnr.iit.epas.manager.services.absences.errors.AbsenceError;
import it.cnr.iit.epas.manager.services.absences.errors.CriticalError;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod.TemplateRow;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.CategoryGroupAbsenceType;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType;
import it.cnr.iit.epas.models.contractuals.ContractualClause;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da AbsenceFormSimulationResponse al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface AbsenceFormSimulationResponseMapper {

  CriticalErrorDto convert(CriticalError criticalError);

  @Mapping(target = "absenceProblem", source = "absence.absenceProblem")
  AbsenceErrorDto convert(AbsenceError absence);

  @Mapping(target = "absence", source = "rowRecap.absence")
  TemplateRowDto convert(TemplateRow rowRecap);

  @Mapping(target = "justifiedType", source = "absence.justifiedType.name")
  @Mapping(target = "justifiedTime", expression = "java(absence.justifiedTime())")
  AbsenceShowDto convert(Absence absence);

  @Mapping(target = "howManySuccess", expression = "java(insertReport.howManySuccess())")
  @Mapping(target = "howManyReplacing", expression = "java(insertReport.howManyReplacing())")
  @Mapping(target = "howManyIgnored", expression = "java(insertReport.howManyIgnored())")
  @Mapping(target = "howManyError", expression = "java(insertReport.howManyError())")
  AbsenceFormSimulationResponseDto convert(InsertReport insertReport);

  @Mapping(target = ".", source = "name")
  String convert(JustifiedType justifiedType);

}