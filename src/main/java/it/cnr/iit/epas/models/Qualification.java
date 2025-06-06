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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Qualifiche CNR (livello 1, 2, 3, 4, ...).
 *
 * @author Dario Tagliaferri
 */
@Getter
@Setter
@Entity
@Audited
@Table(name = "qualifications")
public class Qualification extends BaseEntity {

  private static final long serialVersionUID = 7147378609067987191L;

  @OneToMany(mappedBy = "qualification")
  private List<Person> person;

  //  @ManyToMany(mappedBy = "qualifications")
  //  public List<AbsenceType> absenceTypes;

  @NotNull
  private int qualification;

  @NotNull
  private String description;

  /**
   * I livelli I-III.
   */
  @Transient
  public boolean isTopQualification() {
    return qualification <= 3;
  }
  
  @Override
  public String getLabel() {
    return this.description;
  }

  @Override
  public String toString() {
    return getLabel();
  }

  
}
