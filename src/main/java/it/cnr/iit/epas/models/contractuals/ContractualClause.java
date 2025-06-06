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

package it.cnr.iit.epas.models.contractuals;

import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.absences.CategoryGroupAbsenceType;
import it.cnr.iit.epas.models.base.PeriodModel;
import it.cnr.iit.epas.models.enumerate.ContractualClauseContext;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Istituto contrattuale.
 *
 * <p>
 *Contiene la documentazione delle varie disposizioni contrattuali
 * raggruppate per tipologia di assenza (GroupAbsenceType).
 * </p>
 *
 * @author Cristian Lucchesi
 * @author Dario Tagliaferri
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "contractual_clauses")
public class ContractualClause extends PeriodModel {

  private static final long serialVersionUID = -1933483982513717538L;

  /**
   * Esempio: Permessi retribuiti (art. 72)
   */
  @NotNull
  //@Unique
  private String name;

  /**
   * Tempi di fruizione.
   */
  private String fruitionTime;

  /**
   * Caratteristiche Giuridico Economiche.
   */
  private String legalAndEconomic;  

  /**
   * Documentazione giustificativa. 
   */
  private String supportingDocumentation;
  
  /**
   * Modalità di richiesta. 
   */
  @Column(name = "how_to_request")
  private String howToRequest;

  /**
   * Altre informazioni. 
   */
  @Column(name = "other_infos")
  private String otherInfos;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ContractualClauseContext context;

  @OneToMany(mappedBy = "contractualClause")
  private Set<CategoryGroupAbsenceType> categoryGroupAbsenceTypes = Sets.newHashSet();

  /**
   * Eventuali allegati o url di documentazione online.
   */
  @ManyToMany
  @JoinTable(name = "contractual_clauses_contractual_references",
      joinColumns = @JoinColumn(name = "contractual_clauses_id"), 
      inverseJoinColumns = @JoinColumn(name = "contractual_references_id"))
  private Set<ContractualReference> contractualReferences = Sets.newHashSet();

}