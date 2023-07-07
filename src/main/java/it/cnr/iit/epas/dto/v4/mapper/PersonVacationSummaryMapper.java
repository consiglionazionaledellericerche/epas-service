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

import it.cnr.iit.epas.dto.v4.AbsencePeriodDto;
import it.cnr.iit.epas.dto.v4.AbsenceShowTerseDto;
import it.cnr.iit.epas.dto.v4.AbsenceSubPeriodDto;
import it.cnr.iit.epas.dto.v4.ComplationAbsenceDto;
import it.cnr.iit.epas.dto.v4.ContractShowDto;
import it.cnr.iit.epas.dto.v4.DayInPeriodDto;
import it.cnr.iit.epas.dto.v4.PersonDayTerseDto;
import it.cnr.iit.epas.dto.v4.PersonVacationSummaryDto;
import it.cnr.iit.epas.dto.v4.TakenAbsenceDto;
import it.cnr.iit.epas.dto.v4.VacationCodeDto;
import it.cnr.iit.epas.dto.v4.VacationSummaryDto;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationSummary;
import it.cnr.iit.epas.manager.services.absences.model.AbsencePeriod;
import it.cnr.iit.epas.manager.services.absences.model.ComplationAbsence;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod;
import it.cnr.iit.epas.manager.services.absences.model.TakenAbsence;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.enumerate.VacationCode;
import java.util.List;
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
public interface PersonVacationSummaryMapper {

  @Mapping(target = "absence.justifiedType", source = "absence.justifiedType.name")
  TakenAbsenceDto convert(TakenAbsence takenAbsences);

  @Mapping(target = "absence.justifiedType", source = "absence.justifiedType.name")
  ComplationAbsenceDto convert(ComplationAbsence complationAbsence);

  DayInPeriodDto convert(DayInPeriod daysInPeriod);

  PersonVacationSummaryDto convert(PersonVacationSummary vacationSummary);

  @Mapping(target = "takableWithLimit", expression = "java(period.isTakableWithLimit())")
  @Mapping(target = "periodTakableAmount", expression = "java(period.getPeriodTakableAmount())")
  @Mapping(target = "remainingAmount", expression = "java(period.getRemainingAmount())")
  AbsencePeriodDto convert(AbsencePeriod period);

  @Mapping(target = "total", expression = "java(vacationSummary.total())")
  @Mapping(target = "accrued", expression = "java(vacationSummary.accrued())")
  @Mapping(target = "absencesUsed", expression = "java(convert(vacationSummary.absencesUsed()))")
  @Mapping(target = "postPartum", expression = "java(convert(vacationSummary.postPartum()))")
  @Mapping(target = "used", expression = "java(vacationSummary.used())")
  @Mapping(target = "usableTotal", expression = "java(vacationSummary.usableTotal())")
  @Mapping(target = "usable", expression = "java(vacationSummary.usable())")
  @Mapping(target = "upperLimit", expression = "java(vacationSummary.upperLimit())")
  @Mapping(target = "sourced", expression = "java(vacationSummary.sourced())")
  @Mapping(target = "accruedDayTotal", expression = "java(vacationSummary.accruedDayTotal())")
  @Mapping(target = "postPartumSize", expression = "java(vacationSummary.postPartum().size())")
  @Mapping(target = "postPartumisEmpty", 
      expression = "java(vacationSummary.postPartum().isEmpty())")
  @Mapping(target = "title", expression = "java(vacationSummary.title())")
  VacationSummaryDto convert(VacationSummary vacationSummary);

  @Mapping(target = "personId", source = "person.id")
  PersonDayTerseDto convert(PersonDay personDay);

  @Mapping(target = "justifiedType", source = "justifiedType.name")
  @Mapping(target = "externalId", source = "externalIdentifier")
  @Mapping(target = "justifiedTime", expression = "java(absence.justifiedTime())")
  @Mapping(target = "date", source = "personDay.date")
  @Mapping(target = "nothingJustified", expression = "java(absence.nothingJustified())")
  AbsenceShowTerseDto convert(Absence absence);

  @Mapping(target = "personId", source = "person.id")
  ContractShowDto convert(Contract contract);

  List<AbsenceShowTerseDto> convert(List<Absence> absences);

  AbsenceSubPeriodDto convertToSubPeriod(AbsencePeriod period);

  /**
   * Trasformazione da Enum a DTO per i VacationCode.
   */
  default VacationCodeDto vacationCodeDto(VacationCode vacationCode) {
    if (vacationCode == null) {
      return null;
    }
    return new VacationCodeDto(
        vacationCode.getName(), vacationCode.getVacations(), vacationCode.getPermissions());
  }
}