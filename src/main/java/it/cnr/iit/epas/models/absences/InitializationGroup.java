/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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
package it.cnr.iit.epas.models.absences;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.base.BaseEntity;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Dati per l'inizializzazione di un gruppo di assenza di un dipendente.
 */
@NoArgsConstructor
@Getter
@Setter
@Audited
@Entity
@Table(name = "initialization_groups")
public class InitializationGroup extends BaseEntity {

  private static final long serialVersionUID = -1963061850354314327L;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id", nullable = false)
  private Person person;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "group_absence_type_id", nullable = false)
  private GroupAbsenceType groupAbsenceType;

  private LocalDate date;

  private LocalDate forcedBegin;

  private LocalDate forcedEnd;

  // if (groupAbsenceType.pattern == programmed)

  private Integer unitsInput = 0;

  private Integer hoursInput = 0;

  private Integer minutesInput = 0;

  private Integer averageWeekTime;

  private Integer takableTotal;

  // if (groupAbsenceType.pattern == vacationsCnr)

  private Integer vacationYear;

  //if (groupAbsenceType.pattern == compensatoryRestCnr)

  private Integer residualMinutesLastYear;

  private Integer residualMinutesCurrentYear;

  /**
   * Constructor.
   *
   * @param person persona
   * @param groupAbsenceType gruppo
   * @param date data
   */
  public InitializationGroup(Person person, GroupAbsenceType groupAbsenceType, LocalDate date) {
    this.person = person;
    this.groupAbsenceType = groupAbsenceType;
    this.date = date;
  }
  
  /**
   * I minuti in input.
   *
   * @return i minuti
   */
  public int inputMinutes() {
    return this.hoursInput * 60 + this.minutesInput;
  }
  
  /**
   * I minuti inseribili.
   *
   * @return list
   */
  public List<Integer> selectableMinutes() {
    List<Integer> hours = Lists.newArrayList();
    for (int i = 0; i <= 59; i++) {
      hours.add(i);
    }
    return hours;
  }

}
