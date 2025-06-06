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

import com.google.common.base.Strings;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.enumerate.PaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Nuova gestione degli slot dei turni associati.
 *
 * @author Dario Tagliaferri
 */
@Getter
@Setter
@Audited
@Entity
public class OrganizationShiftSlot extends BaseEntity {

  private static final long serialVersionUID = 2019_10_28_1039L;

  private String name;

  /**
   * Ritorna il nome dello slot formato attraverso inizio e fine dell'orario.
   *
   * @return il nome dello slot.
   */
  @Transient
  public String getName() {
    if (Strings.isNullOrEmpty(this.name)) {
      return String.format("%s - %s", this.beginSlot, this.endSlot);
    } else {
      return name;
    }
  }

  @NotNull
  @Column(columnDefinition = "VARCHAR")
  private LocalTime beginSlot;

  @NotNull
  @Column(columnDefinition = "VARCHAR")
  private LocalTime endSlot;

  @Column(columnDefinition = "VARCHAR")
  private LocalTime beginMealSlot;

  @Column(columnDefinition = "VARCHAR")
  private LocalTime endMealSlot;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_type")
  private PaymentType paymentType;
 
  private Integer minutesPaid;

  @ManyToOne
  @JoinColumn(name = "shift_time_table_id")
  private OrganizationShiftTimeTable shiftTimeTable;
}