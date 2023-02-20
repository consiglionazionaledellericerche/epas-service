/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

import it.cnr.iit.epas.dto.v4.AbsencePeriodSummaryDto;
import it.cnr.iit.epas.dto.v4.AbsenceSubPeriodDto;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationSummarySubperiod;
import it.cnr.iit.epas.manager.services.absences.model.AbsencePeriod;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary;
import it.cnr.iit.epas.models.enumerate.VacationCode;
import java.util.Arrays;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * Mapping delle informazioni per il riepilogo di un mese lavorativo
 * in un DTO da esportare via REST.
 *
 * @author cristian
 *
 */


//@Mapper(componentModel = "spring")
@Mapper(componentModel = "spring", uses = { VacationSummary.class, AbsencePeriod.class } )
public interface PersonVacationSummarySubperiodMapper {

//  VacationCode map(VacationCodeDto value);

  PersonVacationSummarySubperiodMapper INSTANCE = 
      Mappers.getMapper(PersonVacationSummarySubperiodMapper.class);

  @Mapping(target = ".", source = "periodSummaryDto.period")
  @Mapping(target = "vacationCode", source = "period.vacationCode", 
      qualifiedByName = "mapVacationCode")
  AbsencePeriod createPeriodFromDto(AbsencePeriodSummaryDto periodSummaryDto);

  @Mapping(target = "absencePeriod", source = "summary.absencePeriod")
  @Mapping(target = "absencePeriod.subPeriods", source = "summary.absencePeriod.subPeriods")
  @Mapping(target = "absencePeriod.vacationCode", source = "periodSummaryDto.summary.absencePeriod.vacationCode",
      qualifiedByName="mapVacationCode")
  @Mapping(target = "year", source = "summary.year")
  VacationSummary createSummaryFromDto(AbsencePeriodSummaryDto periodSummaryDto);

  AbsenceSubPeriodDto convert(PersonVacationSummarySubperiod period);

  @Named("mapVacationCode")
  default VacationCode mapVacationCode(String vacationCode){
    System.out.println("vacationCode {}"+vacationCode);
    VacationCode vc = Arrays.stream(VacationCode.values()).filter(value -> value.name.equals(vacationCode)).findFirst().orElse(null);
    return vc;
  }

}
