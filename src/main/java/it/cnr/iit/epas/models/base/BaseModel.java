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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
//import dao.wrapper.IWrapperFactory;
//import dao.wrapper.IWrapperModel;
import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
//import org.hibernate.annotations.TypeDef;
//import org.hibernate.annotations.TypeDefs;
//import org.jadira.usertype.dateandtime.joda.PersistentYearMonthAsString;
//import org.joda.time.YearMonth;
import lombok.Getter;
import lombok.Setter;


/**
 * Default base class per sovrascrivere la generazione delle nuove chiavi primarie.
 *
 * @author Marco Andreini
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseModel implements Serializable {

  private static final long serialVersionUID = 4849404810311166199L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  //@NotAudited
  @Version
  private Integer version;

  @Transient
  public Long getId() {
    return id;
  }

  @Transient
  public String getLabel() {
    return toString();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).toString();
  }

  /**
   * Costruisce una istanza del wrapper se esiste.
   *
   * @param wrapperFactory wrapperFactory
   * @return wrapper model
   */
//  @Transient
//  public IWrapperModel<?> getWrapper(IWrapperFactory wrapperFactory) {
//    if (this instanceof Person) {
//      return wrapperFactory.create((Person) this);
//    }
//    return null;
//  }
}
