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

import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.base.MutableModel;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Classe che modella un istituto.
 *
 * @author Alessandro Martelli
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "institutes")
public class Institute extends MutableModel {

  private static final long serialVersionUID = -2062459037430369402L;

  private Long perseoId;
  
  //@Unique
  @NotNull
  private String name;

  /**
   * Codice univoco dell'istituto, per l'IIT è 044.
   */
  //@Unique
  @NotNull
  private String cds;

  /**
   * sigla, ex.: IIT
   */
  //@Unique
  private String code;

  @OneToMany(mappedBy = "institute")
  private Set<Office> seats = Sets.newHashSet();
  
  @Override
  public String getLabel() {
    return this.code;
  }
  
  @Override
  public String toString() {
    return getLabel();
  }

}
