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

package it.cnr.iit.epas.models.flows;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.base.MutableModel;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.hibernate.envers.Audited;

/**
 * Rappresenta un gruppo di Persone.
 */
@Getter
@Audited
@Entity
@Table(name = "groups")
public class Group extends MutableModel {

  private static final long serialVersionUID = -5169540784395404L;

  //@Unique(value = "office, name")
  private String name;

  private String description;

  private boolean sendFlowsEmail;

  @ManyToOne
  @JoinColumn(name = "office_id", nullable = false)
  private Office office;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "manager", nullable = false)
  private Person manager;

  @OneToMany(mappedBy = "group")
  private List<Affiliation> affiliations = Lists.newArrayList();

  //  @As(binder = NullStringBinder.class)
  //  @Unique(value = "office, externalId")
  private String externalId;

  private LocalDate endDate;

  /**
   * Verificat se un gruppo è sempre attivo alla data attuale.
   *
   * @return true se il gruppo non ha una data di fine passata.
   */
  public boolean isActive() {
    return endDate == null || endDate.isAfter(LocalDate.now());
  }
  
  /**
   * La lista delle persone che appartengono al gruppo
   * ad una certa data.
   */
  @Transient
  public List<Person> getPeople(LocalDate date) {
    return affiliations.stream()
        .filter(a -> !a.getBeginDate().isAfter(date) 
            && (a.getEndDate() == null || a.getEndDate().isAfter(date)))
        .map(a -> a.getPerson())
        .collect(Collectors.toList());
  }

  /**
   * La lista delle persone che appartengono al gruppo
   * alla data odierna.
   */
  @Transient
  public List<Person> getPeople() {
    return getPeople(LocalDate.now());
  }
  
  public String getLabel() {
    return name;
  }
  
}