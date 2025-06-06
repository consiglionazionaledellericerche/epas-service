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

import com.google.common.base.MoreObjects;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entità ore di formazione.
 *
 * @author Cristian Lucchesi
 */
@NoArgsConstructor
@Getter
@Setter
@Table(name = "person_month_recap")
@Entity
public class PersonMonthRecap extends BaseEntity {

  private static final long serialVersionUID = -8423858325056981355L;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(updatable = false)
  private Person person;


  private Integer year;

  private Integer month;

  private LocalDate fromDate;

  private LocalDate toDate;

  private Integer trainingHours;

  private Boolean hoursApproved = false;
  
  /**
   * Costruisce un nuono oggetto di ore formazione.
   *
   * @param person person
   * @param year anno
   * @param month mese
   */
  public PersonMonthRecap(Person person, int year, int month) {
    this.person = person;
    this.year = year;
    this.month = month;
  }
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(PersonMonthRecap.class)
        .add("person", person.fullName())
        .add("matricola", person.getNumber())
        .add("year", year)
        .add("month", month)        
        .toString();
  }
  
  /**
   * Ritorna true se le ore si riferiscono al mese attuale od al mese precedente 
   * e non sono ancora state approvate.
   *
   * @return se possono essere modificate.
   */
  public boolean isEditable() {
    
    if (hoursApproved) {
      return false;
    }
    
    LocalDate date = LocalDate.now();
    //mese attuale
    if (month == date.getMonthValue() && year == date.getYear()) { 
      return true;
    }
    //mese precedente
    if (month == date.minusMonths(1).getMonthValue() && year == date.minusMonths(1).getYear()) {
      return true;
    }
    
    return false;
  }

}