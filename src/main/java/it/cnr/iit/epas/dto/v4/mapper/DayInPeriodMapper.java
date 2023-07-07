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

import it.cnr.iit.epas.dto.v4.AbsenceShowTerseDto;
import it.cnr.iit.epas.dto.v4.ComplationAbsenceDto;
import it.cnr.iit.epas.dto.v4.DayInPeriodDto;
import it.cnr.iit.epas.dto.v4.TakenAbsenceDto;
import it.cnr.iit.epas.dto.v4.TemplateRowDto;
import it.cnr.iit.epas.manager.services.absences.model.ComplationAbsence;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod;
import it.cnr.iit.epas.manager.services.absences.model.TakenAbsence;
import it.cnr.iit.epas.models.absences.Absence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da AbsenceGroups al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface DayInPeriodMapper {


  @Mapping(target = "justifiedType", source = "justifiedType.name")
  @Mapping(target = "externalId", source = "externalIdentifier")
  @Mapping(target = "justifiedTime", expression = "java(absence.justifiedTime())")
  @Mapping(target = "date", source = "personDay.date")
  @Mapping(target = "nothingJustified", expression = "java(absence.nothingJustified())")
  AbsenceShowTerseDto convert(Absence absence);

  @Mapping(target = "absence.justifiedType", source = "absence.justifiedType.name")
  TakenAbsenceDto convert(TakenAbsence takenAbsences);

  @Mapping(target = "absence.justifiedType", source = "absence.justifiedType.name")
  ComplationAbsenceDto convert(ComplationAbsence complationAbsence);

  TemplateRowDto convert(DayInPeriod.TemplateRow rowRecap);

  DayInPeriodDto convert(DayInPeriod dayInPeriod);

}