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

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * DTO per una Competenza.
 *
 * @author Andrea Generosi
 */
@Data
public class SeatOrganizationChartDto {

  //Map<Role, List<User>>
  private Map<String, List<UserShowDto>> seatSupervisors;
  private Map<String, List<UserShowDto>> personnelAdmins;
  private Map<String, List<UserShowDto>> technicalAdmins;
  private Map<String, List<UserShowDto>> registryManagers;
  private Map<String, List<UserShowDto>> mealTicketsManagers;
  private Map<String, List<UserShowDto>> personnelAdminsMini;
  private Map<String, List<UserShowDto>> shiftManagers;
  private Map<String, List<UserShowDto>> reperibilityManagers;
  private PersonShowDto currentPerson;
  private List<String> roles;
}
