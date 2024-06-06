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

import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO per gli AbsenceType.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AbsenceTypeDto extends BaseModelDto {

  private String code;
  private String description;
  private boolean hasGroups;
  private Integer numberOfDays = 0;

  private String defaultTakableGroup;

  private Optional<String> categoryTabName=null;

}