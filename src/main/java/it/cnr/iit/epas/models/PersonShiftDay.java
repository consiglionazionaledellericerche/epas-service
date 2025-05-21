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

import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.enumerate.ShiftSlot;
import it.cnr.iit.epas.models.enumerate.ShiftTroubles;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Giornata in turno di una persona.
 */
@Getter
@Setter
@Entity
@Audited
@Table(name = "person_shift_days")
public class PersonShiftDay extends BaseEntity {

  private static final long serialVersionUID = -2441219908198684741L;

  // morning or afternoon slot
  @Column(name = "shift_slot")
  @Enumerated(EnumType.STRING)
  private ShiftSlot shiftSlot;

  //@Required
  @ManyToOne
  private OrganizationShiftSlot organizationShiftSlot;

  // shift date
  @NotNull
  private LocalDate date;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "shift_type_id", nullable = false)
  private ShiftType shiftType;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "person_shift_id", nullable = false)
  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  private PersonShift personShift;

  //  Nuova relazione con gli errori associati ai personShiftDay
  @OneToMany(mappedBy = "personShiftDay", cascade = CascadeType.REMOVE)
  private Set<PersonShiftDayInTrouble> troubles = Sets.newHashSet();
  
  /**
   * numero di soglie (minime) superate.
   */
  @Column(name = "exceeded_thresholds")
  private int exceededThresholds;

  /**
   * Controlla l'orario di inizio dello slot.
   *
   * @return l'orario di inizio dello slot.
   */
  @Transient
  public LocalTime slotBegin() {
    switch (shiftSlot) {
      case MORNING:
        return shiftType.getShiftTimeTable().getStartMorning();
      case AFTERNOON:
        return shiftType.getShiftTimeTable().getStartAfternoon();
      case EVENING:
        return shiftType.getShiftTimeTable().getStartEvening();
      default:
        return null;
    }
  }

  /**
   * Controlla l'orario di fine dello slot.
   *
   * @return l'orario di fine dello slot.
   */
  @Transient
  public LocalTime slotEnd() {
    switch (shiftSlot) {
      case MORNING:
        return shiftType.getShiftTimeTable().getEndMorning();
      case AFTERNOON:
        return shiftType.getShiftTimeTable().getEndAfternoon();
      case EVENING:
        return shiftType.getShiftTimeTable().getEndEvening();
      default:
        return null;
    }
  }

  /**
   * Controlla l'inizio della pausa pranzo nel turno.
   *
   * @return l'inizio della pausa pranzo nel turno.
   */
  @Transient
  public LocalTime lunchTimeBegin() {
    switch (shiftSlot) {
      case MORNING:
        return shiftType.getShiftTimeTable().getStartMorningLunchTime();
      case AFTERNOON:
        return shiftType.getShiftTimeTable().getStartAfternoonLunchTime();
      case EVENING:
        return shiftType.getShiftTimeTable().getStartEveningLunchTime();
      default:
        return null;
    }
  }

  /**
   * Controlla la fine della pausa pranzo nel turno.
   *
   * @return l'orario di fine pausa pranzo nel turno.
   */
  @Transient
  public LocalTime lunchTimeEnd() {
    switch (shiftSlot) {
      case MORNING:
        return shiftType.getShiftTimeTable().getEndMorningLunchTime();
      case AFTERNOON:
        return shiftType.getShiftTimeTable().getEndAfternoonLunchTime();
      case EVENING:
        return shiftType.getShiftTimeTable().getEndEveningLunchTime();
      default:
        return null;
    }
  }

  @Transient
  public boolean hasError(ShiftTroubles trouble) {
    return troubles.stream().anyMatch(error -> error.getCause() == trouble);
  }

  /**
   * Controlla se ci sono errori nel turno.
   *
   * @param shiftTroubles la collezione di errori sul turno
   * @return true se ci sono errori sul turno, false altrimenti.
   */
  @Transient
  public boolean hasOneOfErrors(Collection<ShiftTroubles> shiftTroubles) {
    return troubles.stream().anyMatch(trouble -> {
      return shiftTroubles.contains(trouble.getCause());
    });
  }

}
