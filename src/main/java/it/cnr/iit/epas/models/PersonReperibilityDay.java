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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Rappresenta un giorno di reperibilità di una persona reperibile.
 *
 * @author Cristian Lucchesi
 */
@Getter
@Setter
@Audited
@Entity
@Table(
    name = "person_reperibility_days",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"person_reperibility_id", "date"})})
public class PersonReperibilityDay extends BaseEntity {

  private static final long serialVersionUID = 6170327692153445002L;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "person_reperibility_id", nullable = false)
  private PersonReperibility personReperibility;

  @NotNull
  private LocalDate date;

  @Column(name = "holiday_day")
  private Boolean holidayDay;

  @ManyToOne
  @JoinColumn(name = "reperibility_type")
  private PersonReperibilityType reperibilityType;
  
  @Transient
  public String getLabel() {
    return this.date.getDayOfMonth() + " " + this.date.getMonthValue();
  }

}