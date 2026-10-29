/*
 * Copyright (C) 2026  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.dto.v4.workflow;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.enumerate.AccountRole;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * Utente con i ruoli di sistema e sugli uffici, per workflow-service.
 */
@Data
public class WorkflowUserDto {

  private Long id;
  private String username;
  private Long personId;
  private Set<AccountRole> roles = Sets.newHashSet();
  private List<WorkflowRoleOfficeDto> rolesOffices = Lists.newArrayList();
}
