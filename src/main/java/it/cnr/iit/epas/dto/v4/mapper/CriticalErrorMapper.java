/*
 * Copyright (C) 2024  Consiglio Nazionale delle Ricerche
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
import it.cnr.iit.epas.dto.v4.AbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.AbsenceTypeJustifiedBehaviourDto;
import it.cnr.iit.epas.dto.v4.GroupAbsenceTypeDto;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.AbsenceTypeJustifiedBehaviour;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * Mapper da CriticalError al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface CriticalErrorMapper {

  GroupAbsenceTypeDto convert(GroupAbsenceType groupAbsenceType);

  @Mapping(target = "printData", expression = "java(justifiedBehaviours.printData())")
  @Mapping(target = "justifiedBehaviour", source = "justifiedBehaviour.name")
  AbsenceTypeJustifiedBehaviourDto convert(AbsenceTypeJustifiedBehaviour justifiedBehaviours);

  @Mapping(target = "hasGroups",
      expression = "java(!absenceType.involvedGroupTaken(true).isEmpty())")
  @Mapping(target = "defaultTakableGroup",
      expression = "java(absenceType.defaultTakableGroup().category.tab != null ? "
          + "absenceType.defaultTakableGroup().category.tab.getLabel():null)")
  @Mapping(target = "justifiedBehaviours",
      source = "absenceType.justifiedBehaviours")
  AbsenceTypeDto convert(AbsenceType absenceType);

  @Mapping(target = "justifiedType", source = "absence.justifiedType.name")
  @Mapping(target = "justifiedTime", expression = "java(absence.justifiedTime())")
  AbsenceShowDto convert(Absence absence);

  @Mapping(target = ".", source = "name")
  String convert(JustifiedType justifiedType);

}