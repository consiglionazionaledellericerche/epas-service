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

import it.cnr.iit.epas.models.base.BaseEntity;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Tipologia di turno.
 */
@Getter
@Setter
@Entity
@Audited
@Table(name = "shift_type")
public class ShiftType extends BaseEntity {

  private static final long serialVersionUID = 3156856871540530483L;

  public String type;

  public String description;

  @Column(name = "allow_unpair_slots")
  public boolean allowUnpairSlots = false;

  @Min(0)
  @Column(name = "entrance_tolerance")
  public int entranceTolerance;

  @Min(0)
  @Column(name = "entrance_max_tolerance")
  public int entranceMaxTolerance;

  @Min(0)
  @Column(name = "exit_tolerance")
  public int exitTolerance;

  @Min(0)
  @Column(name = "exit_max_tolerance")
  public int exitMaxTolerance;

  //quantità massima di tolleranze concesse all'interno dell'attività
  @Max(3)
  @Min(0)
  @Column(name = "max_tolerance_allowed")
  public int maxToleranceAllowed;

  @Min(0)
  @Column(name = "break_in_shift")
  public int breakInShift;

  @Min(0)
  @Column(name = "break_max_in_shift")
  public int breakMaxInShift;

  @NotAudited
  @OneToMany(mappedBy = "shiftType")
  public List<PersonShiftShiftType> personShiftShiftTypes = new ArrayList<>();

  @NotAudited
  @OneToMany(mappedBy = "shiftType")
  public List<PersonShiftDay> personShiftDays = new ArrayList<>();

  @NotAudited
  @OneToMany(mappedBy = "type")
  public List<ShiftCancelled> shiftCancelled = new ArrayList<>();

  @NotAudited
  @ManyToOne
  @JoinColumn(name = "shift_time_table_id")
  public ShiftTimeTable shiftTimeTable;

  @NotAudited
  @ManyToOne
  @JoinColumn(name = "organization_shift_time_table_id")
  public OrganizationShiftTimeTable organizaionShiftTimeTable;
  
  //@Required
  @ManyToOne(optional = false)
  @JoinColumn(name = "shift_categories_id")
  public ShiftCategories shiftCategories;

  @OneToMany(mappedBy = "shiftType", cascade = CascadeType.REMOVE)
  @OrderBy("yearMonth DESC")
  public Set<ShiftTypeMonth> monthsStatus = new HashSet<>();

  @Override
  public String toString() {
    return shiftCategories.getDescription() + " - " + type;
  }

  /**
   * Tipologia di tolleranza.
   */
  public enum ToleranceType {
    entrance("entrance"),
    exit("exit"),
    both("both");

    public String description;

    ToleranceType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  /**
   * RItorna l'oggetto che contiene l'approvazione del turno ad una certa data.
   *
   * @param date la data da considerare
   * @return l'oggetto che contiene l'approvazione del turno ad una certa data.
   */
  @Transient
  public Optional<ShiftTypeMonth> monthStatusByDate(LocalDate date) {
    final YearMonth requestedMonth = YearMonth.from(date);
    return monthsStatus.stream()
        .filter(shiftTypeMonth -> shiftTypeMonth.getYearMonth().equals(requestedMonth)).findFirst();
  }

  /**
   * Controlla se il turno è stato approvato alla data passata come parametro.
   *
   * @param date la data da considerare
   * @return true se il turno è stato approvato alla data date, false altrimenti.
   */
  @Transient
  public boolean approvedOn(LocalDate date) {
    Optional<ShiftTypeMonth> monthStatus = monthStatusByDate(date);
    if (monthStatus.isPresent()) {
      return monthStatus.get().isApproved();
    } else {
      return false;
    }
  }

}
