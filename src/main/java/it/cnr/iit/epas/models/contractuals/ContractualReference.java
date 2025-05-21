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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.base.PeriodModel;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.URL;

/**
 * Allegato o indirizzo web di documento amministrativo.
 *
 * @author Cristian Lucchesi
 * @author Dario Tagliaferri
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "contractual_references")
public class ContractualReference extends PeriodModel {
  
  private static final long serialVersionUID = 53012052329220325L;

  @NotNull
  private String name;

  @URL
  private String url;
  
  private String filename;

  //FIXME: da implementare prima del passaggio a spring boot
  //  public Blob file;

  @ManyToMany(mappedBy = "contractualReferences")
  private List<ContractualClause> contractualClauses = Lists.newArrayList();

  //FIXME: da implementare prima del passaggio a spring boot
  //  @Transient
  //  public long getLength() {
  //    return file == null ? 0 : file.length();
  //  }
  
  //FIXME: da implementare prima del passaggio a spring boot
  //  @PreRemove
  //  private void onDelete() {
  //    if (file != null && file.getFile() != null) {
  //      file.getFile().delete();  
  //    }    
  //  }

  @Override
  public String toString() {
    return name;
  }
}