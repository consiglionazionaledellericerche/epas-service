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

package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StampingEditFormDto extends StampingCreateDto  {

  private PersonShowDto person;
  private boolean ownStamping;
  private List<ZoneDto> zones;
  private List<StampTypeDto> stampTypes = Lists.newArrayList();
  private StampTypeDto stampTypeOpt;

  private boolean serviceReasons;
  private boolean offSiteWork;

  private String place;
  private String reason;

  private List<HistoryValueDto> historyStamping;

}
