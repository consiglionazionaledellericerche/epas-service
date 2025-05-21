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

import it.cnr.iit.epas.models.base.PeriodModel;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Associazione di una persona ad una tipologia di turno.
 */
@Getter
@Setter
@Entity
@Table(name = "person_shift")
public class PersonShift extends PeriodModel {

  private static final long serialVersionUID = 651448817233184716L;

  private String description;

  @ManyToOne
  @JoinColumn(name = "person_id", nullable = false, updatable = false)
  private Person person;

  @OneToMany(mappedBy = "personShift")
  private List<PersonShiftShiftType> personShiftShiftTypes;

  @OneToMany(mappedBy = "personShift")
  private List<PersonShiftDay> personShiftDays = new ArrayList<>();

  private boolean disabled;

  @Override
  public String toString() {
    return person.getName() + " " + person.getSurname();
  }

}
