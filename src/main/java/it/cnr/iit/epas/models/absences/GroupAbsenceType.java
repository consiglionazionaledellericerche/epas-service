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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.utils.DateInterval;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Tipologia di gruppo di assenze.
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "group_absence_types")
public class GroupAbsenceType extends BaseEntity {

  private static final long serialVersionUID = 3290760775533091791L;
  
  public static final String EMPLOYEE_NAME = "EMPLOYEE";

  @NotNull
  public String name;
  
  //Astensione facoltativa post partum 100% primo figlio 0-12 anni 30 giorni 
  @NotNull
  public String description;

  //Se i gruppi sono concatenati e si vuole una unica etichetta (da assegnare alla radice)
  // Esempio Congedi primo figlio 100%, Congedi primo figlio 30% hanno una unica chainDescription
  public String chainDescription;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_type_id")
  public CategoryGroupAbsenceType category;

  public int priority;

  @NotNull
  @Enumerated(EnumType.STRING)
  public GroupAbsenceTypePattern pattern;

  @NotNull
  @Enumerated(EnumType.STRING)
  public PeriodType periodType;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "takable_behaviour_id")
  public TakableAbsenceBehaviour takableAbsenceBehaviour;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "complation_behaviour_id")
  public ComplationAbsenceBehaviour complationAbsenceBehaviour;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "next_group_to_check_id")
  public GroupAbsenceType nextGroupToCheck;

  @OneToMany(mappedBy = "nextGroupToCheck", fetch = FetchType.LAZY)
  public Set<GroupAbsenceType> previousGroupChecked = Sets.newHashSet();

  public boolean automatic = false;

  public boolean initializable = false;

  /**
   * Label.
   *
   * @return label
   */
  public String getLabel() {
    return computeChainDescription();
  }

  /**
   * La stringa che rappresenta la catena cui appartiene il gruppo.
   *
   * @return chainDescription
   */
  public String computeChainDescription() {
    if (!Strings.isNullOrEmpty(this.chainDescription)) {
      return this.chainDescription;
    } else {
      return this.description;
    }
  }

  /**
   * Il primo gruppo della catena (quando ho un modo univoco di raggiungerlo).
   *
   * @return primo gruppo
   */
  public GroupAbsenceType firstOfChain() {
    if (this.previousGroupChecked.isEmpty()) {
      return this; 
    }
    if (this.previousGroupChecked.size() == 1) {
      return this.previousGroupChecked.iterator().next().firstOfChain();
    }
    return this;
  }

  /**
   * Enumerato che gestisce il tipo di periodo temporale.
   *
   * @author dario
   *
   */
  public enum PeriodType {
    
    always(0, null, null), year(0, null, null), month(0, null, null),
    child1_0_3(1, 0, 3), child1_0_6(1, 0, 6), child1_0_12(1, 0, 12), 
    child1_6_12(1, 6, 12), child1_3_12(1, 3, 12),
    
    child2_0_3(2, 0, 3), child2_0_6(2, 0, 6), child2_0_12(2, 0, 12), child2_6_12(2, 6, 12), 
    child2_3_12(2, 3, 12),
    
    child3_0_3(3, 0, 3), child3_0_6(3, 0, 6), child3_0_12(3, 0, 12), child3_6_12(3, 6, 12), 
    child3_3_12(3, 3, 12),
    
    child4_0_3(4, 0, 3), child4_0_6(4, 0, 6), child4_0_12(4, 0, 12), child4_6_12(4, 6, 12), 
    child4_3_12(4, 3, 12);
    
    
    
    public Integer childNumber;
    public Integer fromYear;
    public Integer toYear;
    
    PeriodType(Integer childNumber, Integer fromYear, Integer toYear) {
      this.childNumber = childNumber;
      this.fromYear = fromYear;
      this.toYear = toYear;
    }
    
    public boolean isChildPeriod() {
      return childNumber > 0;
    }
    
    public Integer getChildNumber() {
      return childNumber;
    }
    
    /**
     * L'intervallo figlio.
     *
     * @param birthDate data di nascita
     * @return intervallo
     */
    public DateInterval getChildInterval(LocalDate birthDate) {
      if (fromYear == null || toYear == null) {
        return null;
      }
      LocalDate from = birthDate.plusYears(fromYear);
      LocalDate to = birthDate.plusYears(toYear).minusDays(1);
      return new DateInterval(from, to);
    }
  }
  
  /**
   * Enumerato che determina la tipologia di gruppo (se gestito da un pattern).
   *
   * @author dario
   *
   */
  public enum GroupAbsenceTypePattern {
    simpleGrouping,              // semplice raggruppamento senza controlli o automatismi
    programmed,                  
    vacationsCnr,                // custom ferie cnr
    compensatoryRestCnr;         // custom riposi compensativi cnr
    
  }
  
  /**
   * Se esiste fra gli enumerati un corrispondente e se è correttamente modellato.
   *
   * @return absent se la tab non è presente in enum
   */
  public Optional<Boolean> matchEnum() {
    
    for (DefaultGroup defaultGroup : DefaultGroup.values()) {
      if (defaultGroup.name().equals(this.name)) {
        if (defaultGroup.description.equals(this.description) 
            && defaultGroup.chainDescription.equals(this.chainDescription)
            && defaultGroup.category.name().equals(this.category.getName())
            && defaultGroup.priority == this.priority
            && defaultGroup.pattern.equals(this.pattern)
            && defaultGroup.periodType.equals(this.periodType)
            && defaultGroup.takable.name().equals(this.takableAbsenceBehaviour.getName())
            && defaultGroup.automatic == this.automatic
            && defaultGroup.initializable == this.initializable) {
          //campi nullable complation
          if (defaultGroup.complation == null) {
            if (this.complationAbsenceBehaviour != null) {
              return Optional.of(false);
            }
          } else {
            if (this.complationAbsenceBehaviour == null 
                || !defaultGroup.complation.name().equals(
                    this.complationAbsenceBehaviour.getName())) {
              return Optional.of(false);
            }
          }
          //campi nullable next
          if (defaultGroup.nextGroupToCheck == null) {
            if (this.nextGroupToCheck != null) {
              return Optional.of(false);
            }
          } else {
            if (!defaultGroup.nextGroupToCheck.name().equals(this.nextGroupToCheck.name)) {
              return Optional.of(false);
            }
          }

          return Optional.of(true);
        } else {
          return Optional.of(false);
        }
      } 
    }
    return Optional.empty();
    
  }

  @Override
  public String toString() {
    return description;
  }
  
}
