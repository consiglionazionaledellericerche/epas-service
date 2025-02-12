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

import lombok.Data;

/**
 * DTO per ShiftType.
 */
@Data
public class ShiftTypeTerseDto {

  private String type;
  private String description;

  private boolean allowUnpairSlots = false;

  private int entranceTolerance;
  private int entranceMaxTolerance;
  private int exitMaxTolerance;
  private int maxToleranceAllowed;
  private int breakInShift;
  private int breakMaxInShift;

  /*private List<PersonShiftShiftType> personShiftShiftTypes = new ArrayList<>();
  private List<PersonShiftDay> personShiftDays = new ArrayList<>();
  private List<ShiftCancelled> shiftCancelled = new ArrayList<>();
  private ShiftTimeTable shiftTimeTable;
  private OrganizationShiftTimeTable organizaionShiftTimeTable;
  private ShiftCategories shiftCategories;
  private Set<ShiftTypeMonth> monthsStatus = new HashSet<>();
  private String ToleranceType;*/
}