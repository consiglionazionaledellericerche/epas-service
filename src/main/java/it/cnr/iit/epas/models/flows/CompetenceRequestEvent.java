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

package it.cnr.iit.epas.models.flows;

import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.flows.enumerate.CompetenceRequestEventType;
import jakarta.persistence.Column;
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
 * Eventi relativi al flusso di approvazione delle competenze mensili.
 *
 * @author Dario Tagliaferri
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@ToString
@Entity
@Table(name = "competence_request_events")
public class CompetenceRequestEvent extends BaseEntity {

  private static final long serialVersionUID = 7592753994383456988L;

  @NotNull
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @NotNull  
  @ManyToOne(optional = false)
  @JoinColumn(name = "competence_request_id")
  private CompetenceRequest competenceRequest;

  @NotNull
  @ManyToOne
  private User owner;

  private String description;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "event_type")
  private CompetenceRequestEventType eventType;

  @PrePersist
  private void onUpdate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }
}