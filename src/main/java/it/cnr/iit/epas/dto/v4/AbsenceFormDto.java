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
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;
import lombok.Data;

/**
 * DTO per i gruppi di assenza.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class AbsenceFormDto {
  @Schema(description = "Persona")
  private PersonShowDto person;
  // switch date
  private LocalDate from;
  private LocalDate to;
  private List<GroupAbsenceTypeDto> groupsPermitted = Lists.newArrayList();
  private SortedMap<Integer, CategoryTabDto> tabsVisibile = Maps.newTreeMap();
  private boolean permissionDenied = false;

  //tab selected
  private CategoryTabDto categoryTabSelected;

  private boolean hasGroupChoice;
  private boolean hasAbsenceTypeChoice;
  private boolean hasJustifiedTypeChoice;
  private Long theOnlyAbsenceType;
  private GroupAbsenceTypeDto groupSelected;

  private boolean hasHourMinutesChoice;
  private List<Integer> selectableHours;
  private List<Integer> selectableMinutes;

  //for those absences who need future recovery of time
  private LocalDate recoveryDate;

  //automatic choice
  private boolean automaticChoiceExists;
  private boolean automaticChoiceSelected;

  //switch absenceType
  private List<AbsenceTypeDto> absenceTypes;
  private AbsenceTypeDto absenceTypeSelected;

  //switch justifiedType
  private List<String> justifiedTypes = Lists.newArrayList();
  private String justifiedTypeSelected;

  //quantity
  private Integer minutes = 0;
  private Integer hours = 0;}