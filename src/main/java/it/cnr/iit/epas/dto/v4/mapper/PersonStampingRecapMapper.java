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

import it.cnr.iit.epas.dao.wrapper.IWrapperContractMonthRecap;
import it.cnr.iit.epas.dto.v4.AbsenceShowDto;
import it.cnr.iit.epas.dto.v4.AbsenceShowTerseDto;
import it.cnr.iit.epas.dto.v4.AbsenceToRecoverDto;
import it.cnr.iit.epas.dto.v4.AbsenceTypeShowTerseDto;
import it.cnr.iit.epas.dto.v4.ContractMonthRecapDto;
import it.cnr.iit.epas.dto.v4.PersonDayDto;
import it.cnr.iit.epas.dto.v4.PersonStampingDayRecapDto;
import it.cnr.iit.epas.dto.v4.PersonStampingRecapDto;
import it.cnr.iit.epas.dto.v4.StampingTemplateDto;
import it.cnr.iit.epas.dto.v4.WorkingTimeTypeDayDto;
import it.cnr.iit.epas.dto.v4.WorkingTimeTypeDto;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingDayRecap;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingRecap;
import it.cnr.iit.epas.manager.recaps.personstamping.StampingTemplate;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.WorkingTimeType;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapping delle informazioni per il riepilogo di un mese lavorativo
 * in un DTO da esportare via REST.
 *
 * @author cristian
 *
 */
@Mapper(componentModel = "spring")
public interface PersonStampingRecapMapper {

  @Mapping(target = "personId", source = "person.id")
  @Mapping(target = "topQualification", source = "person.topQualification")
  PersonStampingRecapDto convert(PersonStampingRecap personDay);

  @Mapping(target = ".", source = "value")
  @Mapping(
      target = "expireInMonth", 
      expression = "java(contractMonthRecap.getValue().expireInMonth())")
  @Mapping(
      target = "residualLastYearInit", 
      expression = "java(contractMonthRecap.getResidualLastYearInit())")
  @Mapping(
      target = "hasResidualInitInYearMonth",
      expression = 
      "java(contractMonthRecap.residualInitInYearMonth(contractMonthRecap.getValue().getYear(), "
      + "contractMonthRecap.getValue().getMonth()))")
  @Mapping(
      target = "hasResidualLastYear", 
      expression = "java(contractMonthRecap.hasResidualLastYear())")
  @Mapping(
      target = "previousRecapInYearPresent", 
      expression = "java(contractMonthRecap.getPreviousRecapInYear().isPresent())")
  @Mapping(
      target = "previousRecapInYearRemainingMinutesCurrentYear", 
      expression = "java(contractMonthRecap.getPreviousRecapInYear().isPresent() "
          + "? contractMonthRecap.getPreviousRecapInYear().get().getRemainingMinutesCurrentYear() "
          + ": 0)")
  ContractMonthRecapDto convert(IWrapperContractMonthRecap contractMonthRecap);

  @Mapping(target = "id", source = "personDay.id")
  @Mapping(target = "stampingTemplates", source = "stampingsTemplate")
  PersonStampingDayRecapDto convert(PersonStampingDayRecap personStampingDayRecap);

  @Mapping(target = "absenceId", source = "absence.id")
  AbsenceToRecoverDto convert(it.cnr.iit.epas.models.dto.AbsenceToRecoverDto absenceToRecover);

  @Mapping(target = "justifiedType", source = "absence.justifiedType.name")
  @Mapping(target = "externalId", source = "absence.externalIdentifier")
  @Mapping(target = "justifiedTime", expression = "java(absence.justifiedTime())")
  @Mapping(target = "date", expression = "java(absence.getAbsenceDate())")
  @Mapping(target = "replacingAbsencesGroup", source = "replacingAbsencesGroup")
  AbsenceShowDto convert(Absence absence, List<Object> replacingAbsencesGroup);

  @Mapping(target = "justifiedType", source = "absence.justifiedType.name")
  @Mapping(target = "externalId", source = "absence.externalIdentifier")
  @Mapping(target = "justifiedTime", expression = "java(absence.justifiedTime())")
  @Mapping(target = "nothingJustified", expression = "java(absence.nothingJustified())")
  @Mapping(target = "date", expression = "java(absence.getAbsenceDate())")
  AbsenceShowTerseDto convertTerse(Absence absence);


  @Mapping(target = "id", source = "stamping.id")
  @Mapping(target = "showPopover", expression = "java(stamping.showPopover())")
  StampingTemplateDto convert(StampingTemplate stamping);

  AbsenceTypeShowTerseDto convert(AbsenceType absenceType);

  WorkingTimeTypeDayDto convert(WorkingTimeTypeDay workingTimeTypeDay);

  WorkingTimeTypeDto convert(WorkingTimeType workingTimeType);

  @Mapping(target = "personId", source = "person.id")
  PersonDayDto convert(PersonDay personDay);

  default Optional<WorkingTimeTypeDayDto> convertOptional(
      Optional<WorkingTimeTypeDay> workingTimeTypeDay) {
    return Optional.of(convert(workingTimeTypeDay.get()));
  }

}