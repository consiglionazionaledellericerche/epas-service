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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.envers.Audited;


/**
 * I gruppi servono per descrivere comportamenti e limiti comuni a più
 * codici di competenza.
 *
 * @author Dario Tagliaferri
 */
@Audited
@Entity
@Table(name = "competence_code_groups",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"label"})})
public class CompetenceCodeGroup extends BaseEntity {

  private static final long serialVersionUID = 6486248571013912369L;

  @OneToMany(mappedBy = "competenceCodeGroup")
  public List<CompetenceCode> competenceCodes = Lists.newArrayList();

  @NotNull
  public String label;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "limit_type")
  public LimitType limitType;

  @Column(name = "limit_value")
  public Integer limitValue;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "limit_unit")
  public LimitUnit limitUnit;

}