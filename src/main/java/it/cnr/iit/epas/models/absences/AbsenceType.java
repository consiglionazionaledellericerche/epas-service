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

import com.google.common.base.Joiner;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.Qualification;
import it.cnr.iit.epas.models.absences.GroupAbsenceType.GroupAbsenceTypePattern;
import it.cnr.iit.epas.models.absences.JustifiedBehaviour.JustifiedBehaviourName;
import it.cnr.iit.epas.models.absences.definitions.DefaultAbsenceType;
import it.cnr.iit.epas.models.absences.definitions.DefaultAbsenceType.Behaviour;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.enumerate.MealTicketBehaviour;
import it.cnr.iit.epas.models.enumerate.QualificationMapping;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;

/**
 * Tipologia di assenza.
 */
@Getter
@Setter
@Entity
@Table(name = "absence_types")
@Audited
public class AbsenceType extends BaseEntity {

  private static final long serialVersionUID = 7157167508454574329L;

  @ManyToMany
  private List<Qualification> qualifications = Lists.newArrayList();

  @NotNull
  public String code;

  @Column(name = "certification_code")
  private String certificateCode;

  private String description;

  private LocalDate validFrom;

  private LocalDate validTo;

  @Column(name = "internal_use")
  private boolean internalUse = false;

  @Column(name = "considered_week_end")
  private boolean consideredWeekEnd = false;

  //  @Getter
  //  @Column(name = "time_for_mealticket")
  //  public boolean timeForMealTicket = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "meal_ticket_behaviour")
  private MealTicketBehaviour mealTicketBehaviour;

  private Integer justifiedTime;

  private boolean toUpdate = true;

  @ManyToMany
  @JoinTable(name = "absence_types_justified_types", 
      joinColumns = { @JoinColumn(name = "absence_types_id") }, 
      inverseJoinColumns = { @JoinColumn(name = "justified_types_id") })
  private Set<JustifiedType> justifiedTypesPermitted = Sets.newHashSet();

  @OneToMany(mappedBy = "absenceType")
  private Set<AbsenceTypeJustifiedBehaviour> justifiedBehaviours = Sets.newHashSet();

  private Integer replacingTime;

  @ManyToOne
  @JoinColumn(name = "replacing_type_id")
  private JustifiedType replacingType;

  @OneToMany(mappedBy = "absenceType")
  @LazyCollection(LazyCollectionOption.EXTRA)
  private Set<Absence> absences = Sets.newHashSet();

  @ManyToMany(mappedBy = "takenCodes")
  private Set<TakableAbsenceBehaviour> takenGroup = Sets.newHashSet();

  @ManyToMany(mappedBy = "takableCodes")
  private Set<TakableAbsenceBehaviour> takableGroup = Sets.newHashSet();

  @ManyToMany(mappedBy = "complationCodes")
  private Set<ComplationAbsenceBehaviour> complationGroup = Sets.newHashSet();

  @ManyToMany(mappedBy = "replacingCodes")
  private Set<ComplationAbsenceBehaviour> replacingGroup = Sets.newHashSet();

  /**
   * Eventuale documentazione specifica del codice da mostrare ai dipendenti ed
   * agli amministratori del personale.
   */
  private String documentation; 

  /**
   * per il controllo della prendibilità della reperibilità sul giorno di assenza.
   */
  @Column(name = "reperibility_compatible")
  private boolean reperibilityCompatible;

  private boolean isRealAbsence = true;

  private String externalId;

  // Metodi
  
  /**
   * Descrizione limitata a 60 caratteri.
   *
   * @return short description
   */
  @Transient
  public String getShortDescription() {
    if (description != null && description.length() > 60) {
      return description.substring(0, 60) + "...";
    }
    return description;
  }
  
  
  /**
   * La validità.
   *
   * @return dateInterval
   */
  @Transient
  public DateInterval validity() {
    return DateInterval.build(this.validFrom, this.validTo);
  }
  
  /**
   * Se il codice è scaduto.
   *
   * @return esito
   */
  @Transient
  public boolean isExpired() {
    return isExpired(LocalDate.now());
  }
  
  /**
   * Se il codice è scaduto alla data.
   *
   * @return esito
   */
  @Transient
  public boolean isExpired(LocalDate date) {
    return !DateUtility.isDateIntoInterval(date, validity());
  }

  @Override
  public String toString() {
    return Joiner.on(" - ").skipNulls().join(code, description);
  }
  
  /**
   * Se fra i tipi giustificativi c'è quello all day.
   *
   * @return esito
   */
  @Transient
  public boolean isAllDayPermitted() {
    for (JustifiedType justifiedType : this.justifiedTypesPermitted) {
      if (justifiedType.getName().equals(JustifiedType.JustifiedTypeName.all_day)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Se fra i tipi giustificativi c'è quello absence type minutes.
   *
   * @return esito
   */  
  @Transient
  public boolean isAbsenceTypeMinutesPermitted() {
    for (JustifiedType justifiedType : this.justifiedTypesPermitted) {
      if (justifiedType.getName().equals(JustifiedType.JustifiedTypeName.absence_type_minutes)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Se fra i tipi giustificativi c'è quello specified minutes.
   *
   * @return esito
   */
  @Transient
  public boolean isSpecifiedMinutesPermitted() {
    for (JustifiedType justifiedType : this.justifiedTypesPermitted) {
      if (justifiedType.getName().equals(JustifiedType.JustifiedTypeName.specified_minutes)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Se fra i tipi giustificativi c'è quello nothing.
   *
   * @return esito
   */
  @Transient
  public boolean isNothingPermitted() {
    for (JustifiedType justifiedType : this.justifiedTypesPermitted) {
      if (justifiedType.getName().equals(JustifiedType.JustifiedTypeName.nothing)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Se il tipo ha quel comportamento.
   *
   * @param behaviour comportamento
   * @return il payload del comportamento se esiste
   */
  public Optional<AbsenceTypeJustifiedBehaviour> getBehaviour(JustifiedBehaviourName behaviour) {
    for (AbsenceTypeJustifiedBehaviour entity : this.justifiedBehaviours) {
      if (entity.getJustifiedBehaviour().getName().equals(behaviour)) {
        return Optional.of(entity);
      }
    }
    return Optional.empty();
  }

  /**
   * Se il codice di assenza è utilizzabile per tutte le qualifiche del mapping.
   *
   * @param mapping mapping
   * @return esito
   */
  @Transient
  public boolean isQualificationMapping(QualificationMapping mapping) {
    Set<Integer> set = ContiguousSet.create(mapping.getRange(), 
        DiscreteDomain.integers());
    Set<Integer> actuals = Sets.newHashSet();
    for (Qualification qualification : qualifications) {
      actuals.add(qualification.getQualification());
    }
    for (Integer item : set) {
      if (!actuals.contains(item)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * I gruppi coinvolti dal tipo assenza.
   *
   * @param onlyProgrammed non filtrare i soli programmati
   * @return entity set
   */
  public Set<GroupAbsenceType> involvedGroupAbsenceType(boolean onlyProgrammed) {

    //TODO: da fare la fetch perchè è usato in tabellone timbrature per ogni codice assenza.
    
    Set<GroupAbsenceType> groups = Sets.newHashSet();
    for (TakableAbsenceBehaviour behaviour : this.takableGroup) {
      groups.addAll(behaviour.getGroupAbsenceTypes());
    }
    for (TakableAbsenceBehaviour behaviour : this.takenGroup) {
      groups.addAll(behaviour.getGroupAbsenceTypes());
    }
    for (ComplationAbsenceBehaviour behaviour : this.complationGroup) {
      groups.addAll(behaviour.getGroupAbsenceTypes());
    }
    for (ComplationAbsenceBehaviour behaviour : this.replacingGroup) {
      groups.addAll(behaviour.getGroupAbsenceTypes());
    }
    if (!onlyProgrammed) {
      return groups;
    }
    Set<GroupAbsenceType> filteredGroup = Sets.newHashSet();
    for (GroupAbsenceType groupAbsenceType : groups) {
      if (groupAbsenceType.getPattern().equals(GroupAbsenceTypePattern.programmed)) {
        filteredGroup.add(groupAbsenceType);
      }
    }
    return filteredGroup;
  }
  
  /**
   * I gruppi coinvolti dal tipo assenza nella parte taken.
   *
   * @param onlyProgrammed non filtrare i soli programmati
   * @return entity set
   */
  public Set<GroupAbsenceType> involvedGroupTaken(boolean onlyProgrammed) {

    //TODO: da fare la fetch perchè è usato in tabellone timbrature per ogni codice assenza.
    
    Set<GroupAbsenceType> groups = Sets.newHashSet();
    for (TakableAbsenceBehaviour behaviour : this.takableGroup) {
      groups.addAll(behaviour.getGroupAbsenceTypes());
    }
    for (TakableAbsenceBehaviour behaviour : this.takenGroup) {
      groups.addAll(behaviour.getGroupAbsenceTypes());
    }
    Set<GroupAbsenceType> filteredGroup = Sets.newHashSet();
    for (GroupAbsenceType groupAbsenceType : groups) {
      if (groupAbsenceType.getPattern().equals(GroupAbsenceTypePattern.programmed)) {
        filteredGroup.add(groupAbsenceType);
      }
    }
    return filteredGroup;
  }
  
  /**
   * Se il codice è coinvolto solo in gruppi semplici.
   *
   * @return esito
   */
  public boolean onlySimpleGroupInvolved() {
    for (GroupAbsenceType group : involvedGroupAbsenceType(false)) {
      if (group.getPattern() == GroupAbsenceTypePattern.simpleGrouping) {
        continue;
      }
      return false;
    }
    return true;
  }
  
  /**
   * Il gruppo con priorità più alta di cui il tipo è takable.
   *
   * @return gruppo
   */
  public GroupAbsenceType defaultTakableGroup() {
    GroupAbsenceType groupSelected = null;
    for (TakableAbsenceBehaviour behaviour : this.takableGroup) {   //o uno o due...
      for (GroupAbsenceType group : behaviour.getGroupAbsenceTypes()) {  //quasi sempre 1
        if (group.isAutomatic() || group.getName().equals(DefaultGroup.FERIE_CNR_DIPENDENTI.name())
            || group.getName().equals(DefaultGroup.RIPOSI_CNR_DIPENDENTI.name()) 
            || group.getName().equals(DefaultGroup.LAVORO_FUORI_SEDE.name())
            || group.getName().equals(DefaultGroup.G_OA_DIPENDENTI.name())) {
          //TODO: questi gruppi (anche in groups permitted) vanno taggati
          continue;
        }
        if (groupSelected == null) {
          groupSelected = group;
          continue;
        }
        if (groupSelected.getPriority() > group.getPriority()) {
          groupSelected = group;
        }
      }
    }
    return groupSelected;
  }
  
  /**
   * Se esiste fra gli enumerati un corrispondente e se è correttamente modellato.
   *
   * @return absent se il completamento non è presente in enum
   */
  public Optional<Boolean> matchEnum() {
    for (DefaultAbsenceType defaultType : DefaultAbsenceType.values()) {
      if (defaultType.getCode().equals(this.code)) {
        if (defaultType.certificationCode.equals(this.certificateCode)
            && defaultType.description.equals(this.description)
            && defaultType.internalUse == this.internalUse
            && defaultType.justifiedTime.equals(this.justifiedTime)
            && defaultType.consideredWeekEnd == this.consideredWeekEnd
            && defaultType.mealTicketBehaviour == this.mealTicketBehaviour
            && defaultType.replacingTime.equals(this.replacingTime)
            ) {
          //Tipi permessi
          if (defaultType.justifiedTypeNamesPermitted.size() 
              != this.justifiedTypesPermitted.size()) {
            return Optional.of(false); 
          }
          for (JustifiedType justifiedType : this.justifiedTypesPermitted) {
            if (!defaultType.justifiedTypeNamesPermitted.contains(justifiedType.getName())) {
              return Optional.of(false);
            }
          }
          
          //Behaviours
          if (defaultType.behaviour.size() 
              != this.justifiedBehaviours.size()) {
            return Optional.of(false); 
          }
          for (AbsenceTypeJustifiedBehaviour behaviour : this.justifiedBehaviours) {
            boolean equal = false;
            for (Behaviour defaultBehaviour : defaultType.behaviour) { 
              if (defaultBehaviour.name.equals(behaviour.getJustifiedBehaviour().getName()) 
                  && safeEqual(defaultBehaviour.data, behaviour.getData())) {
                equal = true;
              }
            }
            if (!equal) {
              return Optional.of(false);
            }
          }
          
          //replecing type nullable
          if (defaultType.replacingType == null) {
            if (this.replacingType != null) {
              return Optional.of(false);
            }
          } else {
            if (this.replacingType == null 
                || !defaultType.replacingType.equals(this.replacingType.getName())) {
              return Optional.of(false);
            }
          }
          //valid from nullable
          if (defaultType.validFrom == null) {
            if (this.validFrom != null) {
              return Optional.of(false);
            }
          } else {
            if (!defaultType.validFrom.equals(this.validFrom)) {
              return Optional.of(false);
            }
          }
          //valid to nullable
          if (defaultType.validTo == null) {
            if (this.validTo != null) {
              return Optional.of(false);
            }
          } else {
            if (!defaultType.validTo.equals(this.validTo)) {
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
  
  /**
   * Controlla se due interi sono uguali.
   *
   * @param a intero
   * @param b intero
   * @return true se due interi sono uguali, false altrimenti.
   */
  public static boolean safeEqual(Integer a, Integer b) {
    if (a == null && b == null) {
      return true;
    }
    if (a != null && b != null && a.equals(b)) {
      return true;
    }
    return false;
  } 
  
}
