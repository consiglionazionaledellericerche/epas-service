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

import it.cnr.iit.epas.dto.v4.AbsencePeriodSummaryDto;
import it.cnr.iit.epas.dto.v4.AbsenceSubPeriodDto;
import it.cnr.iit.epas.dto.v4.ContractShowDto;
import it.cnr.iit.epas.dto.v4.VacationCodeDto;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationSummarySubperiod;
import it.cnr.iit.epas.manager.services.absences.model.AbsencePeriod;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.enumerate.VacationCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapping delle informazioni per il riepilogo di un mese lavorativo
 * in un DTO da esportare via REST.
 *
 * @author Cristian Lucchesi
 *
 */
@Mapper(componentModel = "spring")
public interface PersonVacationSummarySubperiodMapper {

  @Mapping(target = ".", source = "periodSummaryDto.period")
  AbsencePeriod createPeriodFromDto(AbsencePeriodSummaryDto periodSummaryDto);

  @Mapping(target = "absencePeriod", source = "summary.absencePeriod")
  @Mapping(target = "absencePeriod.subPeriods", source = "summary.absencePeriod.subPeriods")
  @Mapping(target = "year", source = "summary.year")
  @Mapping(target = "date", source = "summary.date")
  @Mapping(target = "contract", source = "summary.contract")
  VacationSummary createSummaryFromDto(AbsencePeriodSummaryDto periodSummaryDto);

  AbsenceSubPeriodDto convert(PersonVacationSummarySubperiod period);

  ContractShowDto convert(Contract contract);

  default VacationCodeDto vacationCodeDto(VacationCode vacationCode) {
    return new VacationCodeDto(
        vacationCode.getName(), vacationCode.getVacations(), vacationCode.getPermissions());
  }

  default VacationCode vacationCode(VacationCodeDto vacationCodeDto) {
    if (vacationCodeDto == null){
      return null;
    }
    for (VacationCode value : VacationCode.values()) {
      if (value.getName().equals(vacationCodeDto.getName())) {
        return value;
      }
    }
    throw new IllegalArgumentException("Nessun elemento MyEnum ha il parametro code corrispondente: " + vacationCodeDto.getName());

  }

}