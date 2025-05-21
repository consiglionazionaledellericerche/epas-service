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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Ore associate ad una persona per straordinario.
 */
@NoArgsConstructor
@Getter
@Setter
@Table(name = "person_hour_for_overtime")
@Entity
public class PersonHourForOvertime extends BaseEntity {

  private static final long serialVersionUID = -298105801035472529L;

  /**
   * numero di ore assegnato (viene modificato mese per mese) di straordinari per quella persona che
   * è responsabile di gruppo.
   */
  private Integer numberOfHourForOvertime;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id")
  private Person person;


  public PersonHourForOvertime(Person person, Integer numberOfHourForOvertime) {
    this.person = person;
    this.numberOfHourForOvertime = numberOfHourForOvertime;
  }


  public Integer getNumberOfHourForOvertime() {
    return numberOfHourForOvertime;
  }


  public void setNumberOfHourForOvertime(Integer numberOfHourForOvertime) {
    this.numberOfHourForOvertime = numberOfHourForOvertime;
  }
}
