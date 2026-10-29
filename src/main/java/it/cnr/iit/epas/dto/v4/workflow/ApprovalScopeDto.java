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

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Data;

/**
 * Le persone sulle cui richieste un utente può intervenire, distinte per ruolo.
 */
@Data
public class ApprovalScopeDto {

  /** Persone dei gruppi attivi di cui l'utente è responsabile. */
  private Set<Long> groupManagerOf = Sets.newHashSet();
  /** Persone degli uffici di cui l'utente è responsabile di sede. */
  private Set<Long> seatSupervisorOf = Sets.newHashSet();
  /** Persone degli uffici di cui l'utente è amministratore del personale. */
  private Set<Long> personnelAdminOf = Sets.newHashSet();
}
