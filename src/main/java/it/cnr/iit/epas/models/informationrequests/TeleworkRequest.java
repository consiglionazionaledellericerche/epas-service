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

package it.cnr.iit.epas.models.informationrequests;

import it.cnr.iit.epas.models.base.InformationRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * classe relativa alla richiesta di telelavoro.
 *
 * @author dario
 *
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "telework_requests")
@PrimaryKeyJoinColumn(name = "informationRequestId")
public class TeleworkRequest extends InformationRequest {

  private static final long serialVersionUID = -2647537080585976423L;

  @NotNull
  private int month;

  @NotNull
  private int year;

  private String context;
}