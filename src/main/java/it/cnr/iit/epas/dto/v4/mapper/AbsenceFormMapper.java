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

import it.cnr.iit.epas.dto.v4.AbsenceFormDto;
import it.cnr.iit.epas.dto.v4.AbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.AbsenceTypeJustifiedBehaviourDto;
import it.cnr.iit.epas.dto.v4.CategoryGroupAbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.ContractualClauseDto;
import it.cnr.iit.epas.dto.v4.GroupAbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.PersonShowDto;
import it.cnr.iit.epas.manager.services.absences.AbsenceForm;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.AbsenceTypeJustifiedBehaviour;
import it.cnr.iit.epas.models.absences.CategoryGroupAbsenceType;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType;
import it.cnr.iit.epas.models.contractuals.ContractualClause;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da AbsenceGroups al suo DTO per la visualizzazione via REST.
 */
@Mapper(componentModel = "spring")
public interface AbsenceFormMapper {

  CategoryGroupAbsenceTypeDto convert(CategoryGroupAbsenceType categoryGroupAbsenceType);

  GroupAbsenceTypeDto convert(GroupAbsenceType groupAbsenceType);

  ContractualClauseDto convert(ContractualClause contractualClause);

  @Mapping(target = "justifiedTypeSelected", source = "justifiedTypeSelected.name")
  @Mapping(target = "hasGroupChoice", expression = "java(absenceForm.hasGroupChoice())")
  @Mapping(target = "hasAbsenceTypeChoice", expression = "java(absenceForm.hasAbsenceTypeChoice())")
  @Mapping(target = "hasJustifiedTypeChoice", 
            expression = "java(absenceForm.hasJustifiedTypeChoice())")
  //@Mapping(target = "theOnlyAbsenceType", 
  //  expression = "java(absenceForm.theOnlyAbsenceType().getId())")
  @Mapping(target = "hasHourMinutesChoice", expression = "java(absenceForm.hasHourMinutesChoice())")
  @Mapping(target = "selectableHours", expression = "java(absenceForm.selectableHours())")
  @Mapping(target = "selectableMinutes", expression = "java(absenceForm.selectableMinutes())")
  AbsenceFormDto convert(AbsenceForm absenceForm);

  @Mapping(target = "birthDate", source = "birthday")
  @Mapping(target = "qualification", source = "person.qualification.id")
  PersonShowDto convert(Person person);

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

  @Mapping(target = ".", source = "name")
  String convert(JustifiedType justifiedType);



}