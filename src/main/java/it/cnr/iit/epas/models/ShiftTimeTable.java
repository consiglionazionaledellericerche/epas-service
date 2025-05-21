/*
 * Copyright (C) 2025  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.models;

import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.enumerate.CalculationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Tabella orario di un turno.
 */
@Getter
@Setter
@Entity
@Table(name = "shift_time_table")
public class ShiftTimeTable extends BaseEntity {

  private static final long serialVersionUID = -7869931573320174606L;

  @OneToMany(mappedBy = "shiftTimeTable")
  public List<ShiftType> shiftTypes = new ArrayList<>();

  // start time of morning shift
  @Column(name = "start_morning", columnDefinition = "VARCHAR")
  public LocalTime startMorning;

  // end time of morning shift
  @Column(name = "end_morning", columnDefinition = "VARCHAR")
  public LocalTime endMorning;

  // start time of afternoon shift
  @Column(name = "start_afternoon", columnDefinition = "VARCHAR")
  public LocalTime startAfternoon;

  // end time of afternoon shift
  @Column(name = "end_afternoon", columnDefinition = "VARCHAR")
  public LocalTime endAfternoon;

  @Column(name = "start_evening", columnDefinition = "VARCHAR")
  public LocalTime startEvening;

  @Column(name = "end_evening", columnDefinition = "VARCHAR")
  public LocalTime endEvening;

  // start time for morning lunch break
  @Column(name = "start_morning_lunch_time", columnDefinition = "VARCHAR")
  public LocalTime startMorningLunchTime;

  // end time for the morning lunch break
  @Column(name = "end_morning_lunch_time", columnDefinition = "VARCHAR")
  public LocalTime endMorningLunchTime;

  // start time for the lunch break
  @Column(name = "start_afternoon_lunch_time", columnDefinition = "VARCHAR")
  public LocalTime startAfternoonLunchTime;

  // end time for the lunch break
  @Column(name = "end_afternoon_lunch_time", columnDefinition = "VARCHAR")
  public LocalTime endAfternoonLunchTime;

  // start time for the lunch break
  @Column(name = "start_evening_lunch_time", columnDefinition = "VARCHAR")
  public LocalTime startEveningLunchTime;

  // end time for the lunch break
  @Column(name = "end_evening_lunch_time", columnDefinition = "VARCHAR")
  public LocalTime endEveningLunchTime;

  // total amount of working minutes
  @Column(name = "total_working_minutes")
  public Integer totalWorkMinutes;

  // Paid minuts per shift
  @Column(name = "paid_minutes_morning")
  public Integer paidMinutesMorning;

  // Paid minuts per shift
  @Column(name = "paid_minutes_afternoon")
  public Integer paidMinutesAfternoon;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "office_id")
  public Office office;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "calculation_type")
  public CalculationType calculationType;

  /**
   * Quanti slot ci sono nella timetable.
   *
   * @return la quantità di slot presenti nella timetable.
   */
  @Transient
  public int slotCount() {
    int slots = 0;
    if (startMorning != null && endMorning != null) {
      slots++;
    }
    if (startAfternoon != null && endAfternoon != null) {
      slots++;
    }
    if (startEvening != null && endEvening != null) {
      slots++;
    }
    return slots;
  }
}