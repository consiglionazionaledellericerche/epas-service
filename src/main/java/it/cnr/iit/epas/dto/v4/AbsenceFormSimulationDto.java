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

import com.google.common.collect.Maps;
import it.cnr.iit.epas.models.absences.JustifiedType;
import java.util.Optional;
import java.util.SortedMap;
import lombok.Data;

/**
 * DTO per le tab della modale delle assenze.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class AbsenceFormSimulationDto {
  private Optional<Long> idPerson;
  private Optional<String> fiscalCode;
  private String from;
  private String to;
  private String categoryTabName;
  private String groupAbsenceTypeName;
  private String absenceTypeCode;
  private String justifiedTypeName;
  private int hours;
  private int minutes;
  private boolean forceInsert;
  private boolean switchGroup;
  private String recoveryDate;

}