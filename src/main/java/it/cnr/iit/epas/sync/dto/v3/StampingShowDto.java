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
package it.cnr.iit.epas.sync.dto.v3;

import it.cnr.iit.epas.dto.v4.PersonShowTerseDto;
import it.cnr.iit.epas.models.Stamping.WayType;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.ToString;

/**
 * DTO per l'esportazione via REST delle informazioni di una timbratura.
 *
 * @author Cristian Lucchesi
 * @version 3
 */
@ToString
@Data
public class StampingShowDto {

  private Long id;
  private LocalDateTime date;
  private WayType way;
  private String stampType;
  private String place;
  private String reason;
  private boolean markedByAdmin;
  private boolean markedByEmployee;
  private boolean markedByTelework;
  private String note;
  private String stampingZone;
  private PersonShowTerseDto person;

}