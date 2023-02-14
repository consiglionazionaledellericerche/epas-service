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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.manager.configurations.EpasParam.EpasParamValueType.LocalTimeInterval;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Dto per le informazioni giornaliere relative alle timbrature.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PersonStampingDayRecapDto extends BaseModelDto {

  private PersonDayDto personDay;
  private Optional<WorkingTimeTypeDayDto> wttd;
  //public Optional<PersonalWorkingTime> pwttd;
  private LocalTimeInterval lunchInterval;
  private LocalTimeInterval workInterval;
  private Optional<LocalTimeInterval> personalWorkInterval;
  private boolean ignoreDay = false;
  private boolean firstDay = false;
  private List<StampingTemplateDto> stampingTemplates = Lists.newArrayList();

  // visualizzazioni particolari da spostare
  private String mealTicket;

  //public StampModificationType fixedWorkingTimeCode = null;
  private String exitingNowCode = "";

  private List<String> note = Lists.newArrayList();
}