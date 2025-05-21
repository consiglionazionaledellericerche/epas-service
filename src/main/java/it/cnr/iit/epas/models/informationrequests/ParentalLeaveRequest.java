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
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Classe di richiesta di uscite di servizio.
 *
 * @author Dario Tagliaferri
 *
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "parental_leave_requests")
@PrimaryKeyJoinColumn(name = "informationRequestId")
public class ParentalLeaveRequest extends InformationRequest {

  private static final long serialVersionUID = -8903988853720152320L;
  
  @NotNull
  private LocalDate beginDate;

  @NotNull
  private LocalDate endDate;
  
  ///FIXME: da completare prima del passaggio a spring boot
  //private Blob bornCertificate;
  //private Blob expectedDateOfBirth;
}