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

import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.enumerate.AccountRole;
import java.time.LocalDate;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO per mostrare i dati principali di uno User.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserShowDto extends UserShowTerseDto {

  private Long ownerId;
  private Set<AccountRole> roles = Sets.newHashSet();
  private LocalDate expireDate;
  private String keycloakId;

}