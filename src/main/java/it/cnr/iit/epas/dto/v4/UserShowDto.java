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
import io.swagger.v3.oas.annotations.media.Schema;
import it.cnr.iit.epas.models.enumerate.AccountRole;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO per mostrare i dati principali di uno User.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserShowDto extends UserShowTerseDto {

  @Schema(description = "Id dell'ufficio a cui appartiene questo utente (se non è una persona)")
  private Long ownerId;
  @Schema(description = "id della persona collegata all'utente")
  private Long personId;
  @Schema(description = "nome completo della persona collegata all'utente")
  private String fullname;
  @Schema(description = "Ruoli di sistema attribuiti all'utente")
  private Set<AccountRole> roles = Sets.newHashSet();
  @Schema(description = "abilitato si/no")
  private boolean disabled;



}