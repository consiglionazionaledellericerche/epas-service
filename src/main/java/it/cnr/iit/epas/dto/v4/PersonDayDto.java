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

package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonDayDto extends BaseModelDto {

  private Long personId;
  private LocalDate date;
  private Integer timeAtWork = 0;

  /**
   * Tempo all'interno di timbrature valide.
   */
  private Integer stampingsTime = 0;

  /**
   * Tempo lavorato al di fuori della fascia apertura/chiusura.
   */
  private Integer outOpening = 0;

  private Integer approvedOutOpening = 0;

  private Integer justifiedTimeNoMeal = 0;
  private Integer justifiedTimeMeal = 0;

  private Integer justifiedTimeBetweenZones = 0;

  private Integer workingTimeInMission = 0;
  private Integer difference = 0;
  private Integer progressive = 0;

  /**
   * Minuti tolti per pausa pranzo breve.
   */
  private Integer decurtedMeal = 0;

  private boolean isTicketAvailable;
  private boolean isTicketForcedByAdmin;
  private boolean isWorkingInAnotherPlace;
  private boolean isHoliday;

  /**
   * Tempo lavorato in un giorno di festa.
   */
  private Integer onHoliday = 0;

  /**
   * Tempo lavorato in un giorni di festa ed approvato.
   */
  private Integer approvedOnHoliday = 0;

  //private List<StampingDto> stampings = new ArrayList<StampingDto>();

  private List<AbsenceShowTerseDto> absences = Lists.newArrayList();

  
  //private StampModificationType stampModificationType;
  
  private boolean future;
  private boolean consideredExitingNow;


}