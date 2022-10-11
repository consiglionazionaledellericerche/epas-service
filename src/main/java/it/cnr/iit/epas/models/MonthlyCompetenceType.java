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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.base.BaseEntity;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import org.hibernate.envers.Audited;

/**
 * Rappresenta le possibili tipologie di competenze mensili.
 */
@Audited
@Table(name = "monthly_competence_type")
@Entity
public class MonthlyCompetenceType extends BaseEntity {
  
  private static final long serialVersionUID = -298105801035472529L;
  
  public String name;
  
  @OneToMany(mappedBy = "monthlyCompetenceType")
  public List<PersonReperibilityType> personReperibilityTypes = Lists.newArrayList();
  
  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "workdays_code", nullable = false)
  public CompetenceCode workdaysCode;  
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "holidays_code", nullable = false)
  public CompetenceCode holidaysCode;
  
  /**
   * Transiente che ritorna i codici associati all'attività.
   *
   * @return la lista di codici di competenza feriale e festivo per l'attività.
   */
  @Transient
  public List<CompetenceCode> getCodesForActivity() {
    List<CompetenceCode> list = Lists.newArrayList();
    list.add(workdaysCode);
    list.add(holidaysCode);
    return list;
  }
  
  @Override
  public String toString() {
    return name;
  }
}
