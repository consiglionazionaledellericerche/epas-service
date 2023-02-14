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

package it.cnr.iit.epas.models.base;

import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.NotAudited;

/**
 * Default base class per sovrascrivere la generazione delle nuove chiavi primarie.
 *
 * @author Cristian Lucchesi
 */
@ToString
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

  private static final long serialVersionUID = 4849404810311166199L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotAudited
  @Version
  private Integer version;

  @Transient
  public String getLabel() {
    return toString();
  }

  /**
   * Due entity sono uguali se sono lo stesso oggetto o se hanno lo stesso id.
   * Idee prelevate dal Play1.
   */
  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (this == other) {
      return true;
    }

    Long key = this.getId();
    if (key == null) {
      return false;
    }

    if (!this.getClass().isAssignableFrom(other.getClass())) {
      return false;
    }

    return key.equals(((BaseEntity) other).getId());
  }

}