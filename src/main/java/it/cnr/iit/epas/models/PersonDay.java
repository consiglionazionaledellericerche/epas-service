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
package it.cnr.iit.epas.models;

import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.enumerate.MealTicketBehaviour;
import it.cnr.iit.epas.models.enumerate.Troubles;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.beans.BeanUtils;

/**
 * Classe che rappresenta un giorno, sia esso lavorativo o festivo di una persona.
 *
 * @author Cristian Lucchesi
 * @author Dario Tagliaferri
 */
@Slf4j
@Entity
@Audited
@Table(name = "person_days",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"person_id", "date"})})
@Getter
@Setter
public class PersonDay extends BaseEntity {

  private static final long serialVersionUID = -5013385113251848310L;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "person_id", nullable = false)
  private Person person;

  @NotNull
  private LocalDate date;

  private Integer timeAtWork = 0;

  /**
   * Tempo all'interno di timbrature valide.
   */
  private Integer stampingsTime = 0;

  /**
   * Tempo lavorato al di fuori della fascia apertura/chiusura.
   */
  private Integer outOpening = 0;

  /**
   * Tempo lavorato al di fuori della fascia apertura/chiusura ed approvato.
   */
  private Integer approvedOutOpening = 0;

  /**
   * Tempo giustificato da assenze che non contribuiscono al tempo per buono pasto.
   */
  private Integer justifiedTimeNoMeal = 0;

  /**
   * Tempo giustificato da assenze che contribuiscono al tempo per buono pasto.
   */
  private Integer justifiedTimeMeal = 0;

  /**
   * Tempo giustificato per uscita/ingresso da zone diverse opportunamente definite.
   */
  private Integer justifiedTimeBetweenZones = 0;

  /**
   * Tempo di lavoro in missione. Si può aggiungere in fase di modifica del codice missione 
   * dal tabellone timbrature.
   */
  private Integer workingTimeInMission = 0;

  private Integer difference = 0;

  private Integer progressive = 0;

  /**
   * Minuti tolti per pausa pranzo breve.
   */
  private Integer decurtedMeal = 0;

  public boolean isTicketAvailable;

  public boolean isTicketForcedByAdmin;

  public boolean isWorkingInAnotherPlace;

  public boolean isHoliday;

  /**
   * Tempo lavorato in un giorno di festa.
   */
  private Integer onHoliday = 0;

  /**
   * Tempo lavorato in un giorni di festa ed approvato.
   */
  private Integer approvedOnHoliday = 0;

  @OneToMany(mappedBy = "personDay", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
  @OrderBy("date ASC")
  private List<Stamping> stampings = new ArrayList<Stamping>();

  @OneToMany(mappedBy = "personDay", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
  private List<Absence> absences = new ArrayList<Absence>();

  @NotAudited
  @OneToMany(mappedBy = "personDay", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
  private List<PersonDayInTrouble> troubles = new ArrayList<PersonDayInTrouble>();
  
  @ManyToOne
  @JoinColumn(name = "stamp_modification_type_id")
  private StampModificationType stampModificationType;

  @Transient
  private MealTicket mealTicketAssigned;

  @Transient
  private boolean isConsideredExitingNow;

  /**
   * Costruttore.
   *
   * @param person      person
   * @param date        date
   * @param timeAtWork  timeAtWork
   * @param difference  difference
   * @param progressive progressive
   */
  public PersonDay(Person person, LocalDate date,
      int timeAtWork, int difference, int progressive) {
    this.person = person;
    this.date = date;
    this.timeAtWork = timeAtWork;
    this.difference = difference;
    this.progressive = progressive;
  }

  /**
   * Costruttore.
   *
   * @param person person
   * @param date   date
   */
  public PersonDay(Person person, LocalDate date) {
    this(person, date, 0, 0, 0);
  }

  /**
   * Controlla che il personDay cada nel giorno attuale.
   */
  public boolean isToday() {
    return this.date.isEqual(LocalDate.now());
  }

  /**
   * Controlla se la data del personDay è passata rispetto a LocalDate.now().
   *
   * @return true se la data del personDay è passata, false altrimenti.
   */
  public boolean isPast() {
    return this.date.isBefore(LocalDate.now());
  }

  /**
   * Controlla se la data del personDay è futura rispetto a LocalDate.now().
   *
   * @return true se la data del personDay è futura, false altrimenti.
   */
  public boolean isFuture() {
    return this.date.isAfter(LocalDate.now());
  }

  /**
   * Orario decurtato perchè effettuato fuori dalla fascia di apertura/chiusura.
   */
  @Transient
  public int getDecurtedWork() {

    return this.outOpening - this.approvedOutOpening;
  }
  
  /**
   * Orario decurtato perchè effettuato in un giorno di festa.
   */
  @Transient
  public int getDecurtedWorkOnHoliday() {

    return this.onHoliday - this.approvedOnHoliday;
  }

  /**
   * Il tempo assegnabile è quello a lavoro meno i giustificativi.
   * assignableTime = timeAtWork - justifiedTimeMeal - justifiedTimeNoMeal
   */
  @Transient
  public int getAssignableTime() {
    return this.timeAtWork - this.justifiedTimeMeal - this.justifiedTimeNoMeal;
  }

  /**
   * 
   * @param mealTicketBehaviour
   */
  @Transient
  public void setTicketAvailable(MealTicketBehaviour mealTicketBehaviour) {
    switch (mealTicketBehaviour) {
      case allowMealTicket:
        this.isTicketAvailable = true;
        break;
      case notAllowMealTicket:
        this.isTicketAvailable = false;
        break;
      case preventMealTicket:
        this.isTicketAvailable = false;
        break;
        default:
          break;
    }
  }


  /**
   * metodo che resetta un personday azzerando i valori in esso contenuti.
   */
  @Transient
  public void reset() {
    long id = this.getId();
    //XXX: verificare che sia uguale a quella di apache commons beanutils
    BeanUtils.copyProperties(this, new PersonDay(this.person, this.date));
    this.setId(id);
    //FIXME: save non più utilizzabile qui
    //this.save();
  }

  @Transient
  public boolean hasError(Troubles trouble) {
    return this.troubles.stream().anyMatch(error -> error.getCause() == trouble);
  }

  @Override
  public String toString() {
    return String.format(
        "PersonDay[%d] - person.id = %d, date = %s, difference = %s, isTicketAvailable = %s, "
            + "isTicketForcedByAdmin = %s, modificationType = %s, "
            + "progressive = %s, timeAtWork = %s",
        getId(), person.getId(), date, difference, isTicketAvailable, isTicketForcedByAdmin,
        stampModificationType, progressive, timeAtWork);
  }

}
