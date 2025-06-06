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

package it.cnr.iit.epas.models;

import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Questa classe è in relazione con la classe delle persone e serve a tenere traccia
 * dei figli dei dipendenti per poter verificare se è possibile, per il dipendente in
 * questione, usufruire dei giorni di permesso per malattia dei figli che sono limitati nel
 * tempo e per l'età del figlio.
 *
 * @author Dario Tagliaferri
 */
@Getter
@Setter
@Entity
@Audited
public class PersonChildren extends BaseEntity {

  private static final long serialVersionUID = 2528486222814596830L;

  @NotNull
  private String name;

  @NotNull
  private String surname;

  //@CheckWith(LocalDatePast.class)
  @NotNull
  private LocalDate bornDate;

  private String taxCode;

  @ManyToOne(fetch = FetchType.LAZY)
  private Person person;

  private String externalId;

  @NotAudited
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  private void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}