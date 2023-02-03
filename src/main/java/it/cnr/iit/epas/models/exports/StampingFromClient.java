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

package it.cnr.iit.epas.models.exports;

import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.Stamping.WayType;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import it.cnr.iit.epas.sync.dto.v3.StampingCreateDto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Esportazione delle informazioni relative ad una timbratura.
 *
 * @author Cristian Lucchesi
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StampingFromClient {

  private String numeroBadge;
  private Integer inOut;
  private StampTypes stampType;
  private Person person;
  private boolean markedByAdmin;
  private boolean markedByEmployee;
  private LocalDateTime dateTime;
  private String zona;
  private String note;
  private String reason;
  private String place;
  
  /**
   * Costruisce uno StampingFromClient a partire da
   * un StampingCreateDto.
   */
  public static StampingFromClient build(StampingCreateDto dto) {
    return StampingFromClient.builder()
      .numeroBadge(dto.getBadgeNumber())
      .inOut(dto.getWayType().equals(WayType.in) ? 0 : 1)
      .stampType(
          dto.getReasonType() != null ? StampTypes.byCode(dto.getReasonType().name()) : null)
      .markedByAdmin(dto.isMarkedByAdmin())
      .markedByEmployee(dto.isMarkedByEmployee())
      .dateTime(dto.getDateTime())
      .zona(dto.getStampingZone())
      .note(dto.getNote())
      .reason(dto.getReason())
      .place(dto.getPlace())
      .build();
  }
}
