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
import it.cnr.iit.epas.dto.v4.PersonMonthsDto;
import it.cnr.iit.epas.dto.v4.PersonStampingDayRecapDto;
import it.cnr.iit.epas.dto.v4.PersonStampingRecapDto;
import it.cnr.iit.epas.dto.v4.StampingTemplateDto;
import it.cnr.iit.epas.dto.v4.WorkingTimeTypeDayDto;
import it.cnr.iit.epas.dto.v4.WorkingTimeTypeDto;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingDayRecap;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingRecap;
import it.cnr.iit.epas.manager.recaps.personstamping.StampingTemplate;
import it.cnr.iit.epas.models.ContractMonthRecap;
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
 * Mapping delle informazioni per il riepilogo delle ore mensili nell'anno
 * in un DTO da esportare via REST.
 *
 * @author cristian
 *
 */
@Mapper(componentModel = "spring")
public interface PersonMonthsMapper {

  @Mapping(target = ".", source = "value")
  @Mapping(
      target = "hasResidualInitInYearMonth",
      expression =
          "java(contractMonthRecap.residualInitInYearMonth("
          + "contractMonthRecap.getValue().getYear(), "
              + "contractMonthRecap.getValue().getMonth()))")
  @Mapping(
      target = "getResidualLastYearInit",
      expression = "java(contractMonthRecap.getResidualLastYearInit())")
  @Mapping(
      target = "hasResidualLastYear",
      expression = "java(contractMonthRecap.hasResidualLastYear())")
  @Mapping(
      target = "previousRecap",
      source = "previousRecapInYear")  // Mappatura esplicita del campo Optional
  @Mapping(
      target = "previousRecapInYear",
      source = "previousRecapInYear")  // Mappatura esplicita del campo Optional
  PersonMonthsDto convert(IWrapperContractMonthRecap contractMonthRecap);

  @Mapping(
      target = "expireInMonth",
      expression = "java(contractMonthRecap.expireInMonth())")
  ContractMonthRecapDto convert(ContractMonthRecap contractMonthRecap);

  // Metodo helper per mappare un Optional<ContractMonthRecap> in Optional<ContractMonthRecapDto>
  default Optional<ContractMonthRecapDto> mapOptionalRecap(Optional<ContractMonthRecap> contractMonthRecapOptional) {
    return contractMonthRecapOptional.map(this::convert);  // Usa il metodo convert per l'oggetto effettivo
  }
}
