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

import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Variazione nel monte orario di un dipendente in relazione ad alcune
 * tipologie di assenza (per esempio per gli ex 92CE).
 */
@Getter
@Setter
@Entity
@Audited
@Table(name = "time_variations")
public class TimeVariation extends BaseEntity {
  
  private static final long serialVersionUID = -6067037671772984710L;

  @Column(name = "date_variation")
  private LocalDate dateVariation;
  
  @Column(name = "time_variation")
  private int timeVariation;

  @ManyToOne//(fetch = FetchType.LAZY)
  @JoinColumn(name = "absence_id", nullable = false, updatable = false)
  private Absence absence;
}