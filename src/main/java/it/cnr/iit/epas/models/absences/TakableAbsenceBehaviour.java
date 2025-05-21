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

package it.cnr.iit.epas.models.absences;

import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.absences.definitions.DefaultTakable;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;


/**
 * Rappresenta il comportamento (codici presi, codici prendibili, etc) delle assenze prendibili.
 *
 * @author Alessandro Martelli
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "takable_absence_behaviours")
public class TakableAbsenceBehaviour extends BaseEntity {

  private static final long serialVersionUID = 486763865630858142L;

  public static final String NAME_PREFIX = "T_";

  private String name;
  
  @OneToMany(mappedBy = "takableAbsenceBehaviour", fetch = FetchType.LAZY)
  private Set<GroupAbsenceType> groupAbsenceTypes = Sets.newHashSet();
  
  @Enumerated(EnumType.STRING)
  private AmountType amountType;

  @ManyToMany
  @JoinTable(name = "taken_codes_group", 
      joinColumns = { @JoinColumn(name = "takable_behaviour_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "absence_types_id") })
  @OrderBy("code")
  private Set<AbsenceType> takenCodes = Sets.newHashSet();
  
  //  @Column(name = "takable_count_behaviour")
  //  @Enumerated(EnumType.STRING)
  //  public TakeCountBehaviour takableCountBehaviour;
  

  @ManyToMany
  @JoinTable(name = "takable_codes_group", 
      joinColumns = { @JoinColumn(name = "takable_behaviour_id") }, 
      inverseJoinColumns = { @JoinColumn(name = "absence_types_id") })
  @OrderBy("code")
  private Set<AbsenceType> takableCodes = Sets.newHashSet();

  private Integer fixedLimit;

  @Column(name = "takable_amount_adjust")
  @Enumerated(EnumType.STRING)
  private TakeAmountAdjustment takableAmountAdjustment;

  /**
   * Tipologia di comportamento per i codici di assenza presi. 
   *
   */
  public enum TakeCountBehaviour {
    period, sumAllPeriod, sumUntilPeriod; 
  }
  
  /**
   * Tipologie di aggiustamenti per il tempo totale dei codici di assenza.
   */
  public enum TakeAmountAdjustment {
    workingTimePercent(true, false),
    workingPeriodPercent(false, true),
    workingTimeAndWorkingPeriodPercent(true, true);
    
    public boolean workTime;
    public boolean periodTime;

    private TakeAmountAdjustment(boolean workTime, boolean periodTime) {
      this.workTime = workTime;
      this.periodTime = periodTime;
    }
  }
  
  /**
   * Se esiste fra gli enumerati un corrispondente e se è correttamente modellato.
   *
   * @return absent se il completamento non è presente in enum
   */
  @Transient
  public Optional<Boolean> matchEnum() {
    for (DefaultTakable defaultTakable : DefaultTakable.values()) {
      if (defaultTakable.name().equals(this.name)) {
        if (!defaultTakable.getAmountType().equals(this.amountType)) {
          return Optional.of(false);
        }
        if (defaultTakable.fixedLimit != this.fixedLimit) {
          return Optional.of(false); 
        }
        if (!ComplationAbsenceBehaviour.matchTypes(defaultTakable.takenCodes, this.takenCodes)) {
          return Optional.of(false);
        }
        if (!ComplationAbsenceBehaviour
            .matchTypes(defaultTakable.takableCodes, this.takableCodes)) {
          return Optional.of(false);
        }
        
        //campi nullable adjustment
        if (defaultTakable.takableAmountAdjustment == null) {
          if (this.takableAmountAdjustment != null) {
            return Optional.of(false);
          }
        } else {
          if (!defaultTakable.takableAmountAdjustment.equals(this.takableAmountAdjustment)) {
            return Optional.of(false);
          }
        }

        return Optional.of(true);
      } 
    }
    return Optional.empty();
  }

}