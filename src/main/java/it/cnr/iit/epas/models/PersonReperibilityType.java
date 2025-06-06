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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Tipo di reperibilità.
 *
 * @author Cristian Lucchesi
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "person_reperibility_types", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"description"}))
public class PersonReperibilityType extends BaseEntity {

  private static final long serialVersionUID = 3234688199593333012L;

  @NotNull
  private String description;

  @OneToMany(mappedBy = "personReperibilityType")
  private List<PersonReperibility> personReperibilities;

  /* responsabile della reperibilità */
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @NotNull
  private Person supervisor;
  
  private boolean disabled;
  
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @NotNull
  private Office office; 
 
  @OneToMany(mappedBy = "personReperibilityType", cascade = CascadeType.REMOVE)
  private Set<ReperibilityTypeMonth> monthsStatus = new HashSet<>();
  
  @ManyToMany
  private List<Person> managers = Lists.newArrayList();
  
  /*Tipo di competenza mensile*/
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @NotNull
  private MonthlyCompetenceType monthlyCompetenceType;

  @Override
  public String toString() {
    return this.description;
  }

  /**
   * Ritorna l'oggetto che contiene l'approvazione della reperibilità alla data.
   *
   * @param date la data da considerare
   * @return l'oggetto che contiene l'approvazione della reperibilità se esistente.
   */
  @Transient
  public Optional<ReperibilityTypeMonth> monthStatusByDate(LocalDate date) {
    final YearMonth requestedMonth = YearMonth.from(date);
    return monthsStatus.stream()
        .filter(reperibilityTypeMonth -> reperibilityTypeMonth
            .getYearMonth().equals(requestedMonth)).findFirst();
  }

  /**
   * Controlla se la reperibilità è stata approvata alla data passata come parametro.
   *
   * @param date la data da verificare
   * @return true se la reperibilità è stata approvata alla data date, false altrimenti.
   */
  @Transient
  public boolean approvedOn(LocalDate date) {
    Optional<ReperibilityTypeMonth> monthStatus = monthStatusByDate(date);
    if (monthStatus.isPresent()) {
      return monthStatus.get().isApproved();
    } else {
      return false;
    }
  }
}
