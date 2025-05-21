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
import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Getter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Modello per le fasce di orario lavorativo dei dipendenti.
 * Utilizzato quando necessario per le fasce di orario obbligatorie.
 *
 * @author Cristian Lucchesi
 */
@Entity
@Audited
@Table(name = "time_slots")
public class TimeSlot extends BaseEntity {

  private static final long serialVersionUID = -3443521979786226461L;

  @ManyToOne
  @JoinColumn(name = "office_id")
  public Office office;

  @Getter
  @Column
  //@Unique("office,beginSlot,endSlot")
  public String description;
  
  //@Unique("office,beginSlot,endSlot")
  //@As(binder = LocalTimeBinder.class)
  @NotNull
  @Column(columnDefinition = "VARCHAR")
  public LocalTime beginSlot;
  
  //@As(binder = LocalTimeBinder.class)
  @NotNull
  @Column(columnDefinition = "VARCHAR")
  public LocalTime endSlot;
  
  @NotAudited
  @OneToMany(mappedBy = "timeSlot")
  public List<ContractMandatoryTimeSlot> contractMandatoryTimeSlots = Lists.newArrayList();

  @Column(name = "disabled")
  public boolean disabled = false;

  /**
   * Ritorna la denominazione del timeSlot.
   */
  @Transient
  public String getLabel() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
    return Strings.isNullOrEmpty(description)
        ? String.format("%s - %s", dtf.format(beginSlot), dtf.format(endSlot)) 
          : 
        String.format("%s (%s - %s)", description, dtf.format(beginSlot), dtf.format(endSlot));
  }

  @Override
  public String toString() {
    return description;
  }

}