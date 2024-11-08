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

import it.cnr.iit.epas.dto.v4.AbsenceShowDto;
import it.cnr.iit.epas.dto.v4.AbsenceShowTerseDto;
import it.cnr.iit.epas.dto.v4.AbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.AbsenceTypeJustifiedBehaviourDto;
import it.cnr.iit.epas.dto.v4.PersonDayTerseDto;
import it.cnr.iit.epas.manager.AbsenceManager;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.AbsenceTypeJustifiedBehaviour;
import java.util.List;
import javax.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

/**
 * Mapper da Absence al suo DTO per la visualizzazione via REST.
 */
@Component
@Mapper(componentModel = "spring")
public abstract class AbsenceGroupMapper {

  @Inject
  protected AbsenceManager absenceManager;

  @Mapping(target = "personId", source = "person.id")
  public abstract PersonDayTerseDto convert(PersonDay personDay);

  @Mapping(target = "printData", expression = "java(justifiedBehaviours.printData())")
  @Mapping(target = "justifiedBehaviour", source = "justifiedBehaviour.name")
  public abstract AbsenceTypeJustifiedBehaviourDto convert(
      AbsenceTypeJustifiedBehaviour justifiedBehaviours);

  @Mapping(target = "hasGroups",
      expression = "java(!absenceType.involvedGroupTaken(true).isEmpty())")
  @Mapping(target = "defaultTakableGroup",
      expression = "java(absenceType.defaultTakableGroup().category.tab != null ? absenceType.defaultTakableGroup().category.tab.getLabel():null)")
  @Mapping(target = "justifiedBehaviours",
      source = "absenceType.justifiedBehaviours")
  public abstract AbsenceTypeDto convert(AbsenceType absenceType);

  @Mapping(target = "justifiedType", source = "absence.justifiedType.name")
  @Mapping(target = "externalId", source = "absence.externalIdentifier")
  @Mapping(target = "justifiedTime",
      expression = "java(absence.justifiedTime())")
  @Mapping(target = "date", expression = "java(absence.getAbsenceDate())")
  @Mapping(target = "replacingAbsencesGroup", source = "replacingAbsencesGroup")
  @Mapping(target = "nothingJustified", expression = "java(absence.nothingJustified())")
  public abstract AbsenceShowDto convert(Absence absence, List<Object> replacingAbsencesGroup);

  @Mapping(target = "justifiedType", source = "absence.justifiedType.name")
  @Mapping(target = "externalId", source = "absence.externalIdentifier")
  @Mapping(target = "justifiedTime",
      expression = "java(absence.justifiedTime())")
  @Mapping(target = "nothingJustified", expression = "java(absence.nothingJustified())")
  @Mapping(target = "date", expression = "java(absence.getAbsenceDate())")
  public abstract AbsenceShowTerseDto convertTerse(Absence absence);

}