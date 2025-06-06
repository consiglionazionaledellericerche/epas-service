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
import it.cnr.iit.epas.models.enumerate.LimitType;
import it.cnr.iit.epas.models.enumerate.LimitUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;


/**
 * Tabella di decodifica dei codici di competenza.
 *
 * @author Dario Tagliaferri
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "competence_codes", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"code", "description"}))
public class CompetenceCode extends BaseEntity {

  private static final long serialVersionUID = 9211205948423608460L;
  
  @NotAudited
  @OneToMany(mappedBy = "workdaysCode")
  public List<MonthlyCompetenceType> workdaysCodes = Lists.newArrayList();
  
  @NotAudited
  @OneToMany(mappedBy = "holidaysCode")
  public List<MonthlyCompetenceType> holidaysCodes = Lists.newArrayList();

  @NotAudited
  @OneToMany(mappedBy = "competenceCode")
  public List<Competence> competence = Lists.newArrayList();

  @NotAudited
  @OneToMany(mappedBy = "competenceCode")
  public List<PersonCompetenceCodes> personCompetenceCodes = Lists.newArrayList();

  @ManyToOne
  @JoinColumn(name = "competence_code_group_id")
  public CompetenceCodeGroup competenceCodeGroup;

  @NotNull
  public String code;

  @Column
  public String codeToPresence;

  @NotNull
  public String description;

  public boolean disabled;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "limit_type")
  public LimitType limitType;

  @Column(name = "limit_value")
  public Integer limitValue;

  @Enumerated(EnumType.STRING)
  @Column(name = "limit_unit")
  public LimitUnit limitUnit;


  @Override
  public String toString() {
    return String.format("%s - %s", code, description);
  }

  @Override
  public String getLabel() {
    return String.format("%s - %s", this.code, this.description);
  }

}