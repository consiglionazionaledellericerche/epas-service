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

import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.base.InformationRequest;
import it.cnr.iit.epas.models.flows.enumerate.InformationRequestEventType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Evento del flusso di approvazione di una richiesta informativa.
 *
 * @author dario
 *
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@ToString
@Entity
@Table(name = "information_request_events")
public class InformationRequestEvent extends BaseEntity {

  private static final long serialVersionUID = 8098464195779075326L;

  @NotNull
  private LocalDateTime createdAt;
  

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "information_request_id")
  private InformationRequest informationRequest;

  @NotNull
  @ManyToOne
  private User owner;

  private String description;

  @NotNull
  @Enumerated(EnumType.STRING)
  private InformationRequestEventType eventType;

  @PrePersist
  private void onUpdate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }
}
