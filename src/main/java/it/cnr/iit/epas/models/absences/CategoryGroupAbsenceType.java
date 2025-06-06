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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.absences.definitions.DefaultCategoryType;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.contractuals.ContractualClause;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Associazione tra tipologie di gruppi di assenze e le tab in cui mostrarle
 * nell'interfaccia di gestione delle assenze.
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "category_group_absence_types")
public class CategoryGroupAbsenceType extends BaseEntity 
    implements Comparable<CategoryGroupAbsenceType> {

  private static final long serialVersionUID = 4580659910825885894L;

  public String name;

  public String description;
  
  public int priority;

  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
  public Set<GroupAbsenceType> groupAbsenceTypes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_tab_id")
  public CategoryTab tab;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contractual_clause_id")
  public ContractualClause contractualClause;

  @Override
  public int compareTo(CategoryGroupAbsenceType obj) {
    return name.compareTo(obj.name);
  }
  
  /**
   * Un modo (semplificabile) per ordinare i gruppi della categoria per priorità, prendendoli 
   * (se ci sono) dallo heap.
   *
   * @param onlyFirstOfChain se voglio solo i primi della catena.
   */
  @Transient
  public List<GroupAbsenceType> orderedGroupsInCategory(boolean onlyFirstOfChain) {
    
    SortedMap<Integer, Set<GroupAbsenceType>> setByPriority = 
        Maps.newTreeMap();
    
    //ogni gruppo lo inserisco con quelli della stessa priorità
    for (GroupAbsenceType group : this.groupAbsenceTypes) {
      if (onlyFirstOfChain && !group.getPreviousGroupChecked().isEmpty()) {
        continue;
      }
      Set<GroupAbsenceType> prioritySet = setByPriority.get(group.getPriority());
      if (prioritySet == null) {
        prioritySet = Sets.newHashSet();
        setByPriority.put(group.getPriority(), prioritySet);
      }
      prioritySet.add(group);
    }
    //lista unica ordinata 
    List<GroupAbsenceType> orderedGroupInCateogory = Lists.newArrayList();
    for (Set<GroupAbsenceType> set : setByPriority.values()) {
      orderedGroupInCateogory.addAll(set);
    }
    return orderedGroupInCateogory;
  }
  
  /**
   * Se esiste fra gli enumerati un corrispondente e se è correttamente modellato.
   *
   * @return absent se la categoria non è presente in enum
   */
  public Optional<Boolean> matchEnum() {
    
    for (DefaultCategoryType defaultCategory : DefaultCategoryType.values()) {
      if (defaultCategory.name().equals(this.name)) {
        if (defaultCategory.description.equals(this.description)
            && defaultCategory.priority == this.priority
            && defaultCategory.categoryTab.name().equals(this.tab.getName())) {
          return Optional.of(true);
        } else {
          return Optional.of(false);
        }
      } 
    }
    
    return Optional.empty();
  }
  
  /**
   * Calcola la lista di tutti i codici prendibili in ogni groupAbsenceType di questa
   * categoria.
   *
   * @return la lista di tutti i codici prendibili in questa categoria.
   */
  @Transient
  public Set<AbsenceType> getAbsenceTypes() {
    return groupAbsenceTypes.stream()
        .flatMap(gat -> gat.getTakableAbsenceBehaviour().getTakableCodes().stream())
        .collect(Collectors.toSet());
  }

  /**
   * To String.
   */
  public String toString() {
    return this.description;
  }

}