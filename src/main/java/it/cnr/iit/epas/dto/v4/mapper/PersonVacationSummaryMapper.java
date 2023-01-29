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

import it.cnr.iit.epas.dto.v4.*;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationSummary;
import it.cnr.iit.epas.manager.services.absences.model.AbsencePeriod;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.VacationPeriod;
import it.cnr.iit.epas.models.absences.Absence;
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
public interface PersonVacationSummaryMapper {

  @Mapping(target = "personId", source = "person.id")
  PersonVacationSummaryDto convert(PersonVacationSummary vacationSummary);

  @Mapping(target = "total", expression = "java(vacationSummary.total())")
  @Mapping(target = "accrued", expression = "java(vacationSummary.accrued())")
  @Mapping(target = "used", expression = "java(vacationSummary.used())")
  @Mapping(target = "usableTotal", expression = "java(vacationSummary.usableTotal())")
  @Mapping(target = "usable", expression = "java(vacationSummary.usable())")
  @Mapping(target = "upperLimit", expression = "java(vacationSummary.upperLimit())")
  @Mapping(target = "sourced", expression = "java(vacationSummary.sourced())")
  @Mapping(target = "accruedDayTotal", expression = "java(vacationSummary.accruedDayTotal())")
  @Mapping(target = "postPartumSize", expression = "java(vacationSummary.postPartum().size())")
  @Mapping(target = "postPartumisEmpty", expression = "java(vacationSummary.postPartum().isEmpty())")
  VacationSummaryDto convert(VacationSummary vacationSummary);

  AbsenceSubPeriodDto convert(it.cnr.iit.epas.models.dto.AbsencePeriodDto absencePeriod);

  @Mapping(target = "personId", source = "person.id")
  @Mapping(target = "takableWithLimit", expression = "java(period.isTakableWithLimit())")
  @Mapping(target = "periodTakableAmount", expression = "java(period.getPeriodTakableAmount())")
  @Mapping(target = "remainingAmount", expression = "java(period.getRemainingAmount())")
  AbsencePeriodDto convert(AbsencePeriod period);

  @Mapping(target = "subAmount", expression = "java(vacationSummary.subAmount(period))")
  @Mapping(target = "subFixedPostPartum", expression = "java(vacationSummary.subFixedPostPartum(period))")
  @Mapping(target = "subAmountBeforeFixedPostPartum", expression = "java(vacationSummary.subAmountBeforeFixedPostPartum(period))")
  @Mapping(target = "subTotalAmount", expression = "java(vacationSummary.subTotalAmount(period))")
  @Mapping(target = "subDayProgression", expression = "java(vacationSummary.subDayProgression(period))")
  @Mapping(target = "subDayPostPartum", expression = "java(vacationSummary.subDayPostPartum(period))")
  @Mapping(target = "subDayToFixPostPartum", expression = "java(vacationSummary.subDayToFixPostPartum(period))")
  @Mapping(target = "subAccrued", expression = "java(vacationSummary.subAccrued(period))")
  @Mapping(target = "contractEndFirstYearInPeriod", expression = "java(vacationSummary.contractEndFirstYearInPeriod(period))")
  VacationSummaryDto convert(VacationSummary vacationSummary, AbsencePeriod period);

  @Mapping(target = "justifiedType", source = "justifiedType.name")
  @Mapping(target = "externalId", source = "externalIdentifier")
  @Mapping(target = "justifiedTime", expression = "java(absence.justifiedTime())")
  AbsenceDto convert(Absence absence);

  ContractDto convert(Contract contract);

  //  @Mapping(target = "absencesUsed", expression = "java(vacationSummary.absencesUsed())")
//  @Mapping(target = "postPartum", expression = "java(vacationSummary.postPartum())")


}