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

package it.cnr.iit.epas.dto.v4;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * DTO per l'esportazione via REST delle informazioni 
 * principali di una tipologia di codici di assenza.
 *
 * @author Cristian Lucchesi
 * @version 3
 *
 */
@ToString
@Data
@EqualsAndHashCode(callSuper = true)
public class AbsenceTypeShowTerseDto extends BaseModelDto {

  private String code;

  private String certificateCode;
  private String description;

  private LocalDate validFrom;
  private LocalDate validTo;

  private boolean internalUse; 
  private boolean consideredWeekEnd;
  private Integer justifiedTime;
  

}