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
package it.cnr.iit.epas.models.dto;

import it.cnr.iit.epas.models.absences.AmountType;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.enumerate.VacationCode;
import java.time.LocalDate;

/**
 * DTO AbsencePeriod
 */
public class AbsencePeriodDto {

  public VacationCode vacationCode;
  public LocalDate from;
  public LocalDate to;
  public AmountType takeAmountType;
  public GroupAbsenceType groupAbsenceType;
  public boolean takableWithLimit;
  public long periodTakableAmount;
  public long remainingAmount;

  /**
   * Costruttore.
   *
   * @param vacationCode
   * @param from from
   * @param to to
   * @param takeAmountType
   * @param groupAbsenceType
   * @param takableWithLimit
   * @param periodTakableAmount
   * @param remainingAmount
   */
  public AbsencePeriodDto(VacationCode vacationCode, LocalDate from,
      LocalDate to, AmountType takeAmountType, GroupAbsenceType groupAbsenceType, boolean takableWithLimit,
      long periodTakableAmount, long remainingAmount) {

    this.from = from;
    this.to = to;
    this.vacationCode = vacationCode;
    this.takeAmountType = takeAmountType;
    this.groupAbsenceType = groupAbsenceType;
    this.takableWithLimit = takableWithLimit;
    this.periodTakableAmount = periodTakableAmount;
    this.remainingAmount = remainingAmount;
  }

}