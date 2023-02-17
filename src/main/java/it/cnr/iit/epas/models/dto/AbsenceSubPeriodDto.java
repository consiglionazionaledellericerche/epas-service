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
import java.time.LocalDate;

/**
 * DTO AbsencePeriod
 */
public class AbsenceSubPeriodDto {

  public long subAmount;
  public boolean subFixedPostPartum;
  public long subAmountBeforeFixedPostPartum;
  public long subTotalAmount;
  public long subDayProgression;
  public long subDayPostPartum;
  public long subDayToFixPostPartum;
  public boolean subAccrued;
  public LocalDate contractEndFirstYearInPeriod;
  public long dayInInterval;

  /**
   * Costruttore.
   *
   * @param subAmount
   * @param subFixedPostPartum
   * @param subAmountBeforeFixedPostPartum
   * @param subTotalAmount
   * @param subDayProgression
   * @param subDayPostPartum
   * @param subDayToFixPostPartum
   * @param subAccrued
   * @param contractEndFirstYearInPeriod
   * @param dayInInterval
   * */
  public AbsenceSubPeriodDto(long subAmount, boolean subFixedPostPartum,
      long subAmountBeforeFixedPostPartum, long subTotalAmount, long subDayProgression,
      long subDayPostPartum, long subDayToFixPostPartum, boolean subAccrued,
      LocalDate contractEndFirstYearInPeriod, long dayInInterval) {

    this.subAmount = subAmount;
    this.subFixedPostPartum = subFixedPostPartum;
    this.subAmountBeforeFixedPostPartum = subAmountBeforeFixedPostPartum;
    this.subTotalAmount = subTotalAmount;
    this.subDayProgression = subDayProgression;
    this.subDayPostPartum = subDayPostPartum;
    this.subDayToFixPostPartum = subDayToFixPostPartum;
    this.subAccrued = subAccrued;
    this.contractEndFirstYearInPeriod = contractEndFirstYearInPeriod;
    this.dayInInterval = dayInInterval;
  }

}