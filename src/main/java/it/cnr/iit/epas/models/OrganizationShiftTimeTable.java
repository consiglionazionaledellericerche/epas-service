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
import it.cnr.iit.epas.models.enumerate.CalculationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Nuova relazione tra timetable delle organizzazioni e sedi.
 *
 * @author Dario Tagliaferri
 */
@Getter
@Setter
@Audited
@Entity
public class OrganizationShiftTimeTable extends BaseEntity {

  private static final long serialVersionUID = 8292047096977861290L;

  private String name;

  @OneToMany(mappedBy = "shiftTimeTable", fetch = FetchType.EAGER)
  private Set<OrganizationShiftSlot> organizationShiftSlot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "office_id")
  private Office office;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "calculation_type")
  private CalculationType calculationType;

  @OneToMany(mappedBy = "organizaionShiftTimeTable")
  private List<ShiftType> shiftTypes = new ArrayList<>();

  private boolean considerEverySlot = true;

  /**
   * Il numero degli slot.
   *
   * @return quanti slot ci sono.
   */
  @Transient
  public long slotCount() {
    long slots = this.organizationShiftSlot.stream()
        .filter(s -> s.getBeginSlot() != null && s.getEndSlot() != null).count();
    return slots;
  }
}