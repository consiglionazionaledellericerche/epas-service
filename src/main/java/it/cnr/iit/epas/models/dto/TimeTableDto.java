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

import javax.validation.constraints.NotNull;

/**
 * Rappresentazione di una time table per i turni.
 */
public class TimeTableDto {

  @NotNull
  public String startMorning;

  @NotNull
  public String endMorning;

  @NotNull
  public String startAfternoon;

  @NotNull
  public String endAfternoon;
  
  
  public String startEvening;
  
  
  public String endEvening;

  @NotNull
  public String startMorningLunchTime;

  @NotNull
  public String endMorningLunchTime;

  @NotNull
  public String startAfternoonLunchTime;

  @NotNull
  public String endAfternoonLunchTime;
  
  
  public String startEveningLunchTime;

  
  public String endEveningLunchTime;

  @NotNull
  public Integer totalWorkMinutes;

  @NotNull
  public Integer paidMinutes;

}