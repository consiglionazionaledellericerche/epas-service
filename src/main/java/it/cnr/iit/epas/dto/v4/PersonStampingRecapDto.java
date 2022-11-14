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
import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class PersonStampingRecapDto {

  private Long personId;
  private boolean topQualification;
  private int year;
  private int month;

  private boolean currentMonth = false;

  // Informazioni sui permessi della persona
  private boolean canEditStampings = false;

  // Informazioni sul mese
  private int numberOfCompensatoryRestUntilToday = 0;
  private int basedWorkingDays = 0;
  private int totalWorkingTime = 0;
  private int positiveResidualInMonth = 0;

  // I riepiloghi di ogni giorno
  private List<PersonStampingDayRecapDto> daysRecap = Lists.newArrayList();

  // I riepiloghi codici sul mese
  private Set<StampModificationTypeDto> stampModificationTypeSet = Sets.newHashSet();
  private Set<StampTypes> stampTypeSet = Sets.newHashSet();
  private Map<AbsenceTypeShowTerseDto, Integer> absenceCodeMap = new HashMap<AbsenceTypeShowTerseDto, Integer>();
  private List<AbsenceShowTerseDto> absenceList = Lists.newArrayList();

  // I riepiloghi mensili (uno per ogni contratto attivo nel mese)
  private List<ContractMonthRecapDto> contractMonths = Lists.newArrayList();

  // Le informazioni su eventuali assenze a recupero (es.: 91CE)
  private boolean absenceToRecoverYet = false;
  private List<AbsenceToRecoverDto> absencesToRecoverList = Lists.newArrayList();

  // Template
  private int numberOfInOut = 0;

}