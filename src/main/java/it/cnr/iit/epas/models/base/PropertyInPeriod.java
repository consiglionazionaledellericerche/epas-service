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

package it.cnr.iit.epas.models.base;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Classe base per le proprietà in un determinato periodo.
 *
 * @author Cristian Lucchesi
 *
 */
@MappedSuperclass
@Slf4j
public abstract class PropertyInPeriod extends PeriodModel implements IPropertyInPeriod {

  private static final long serialVersionUID = 2434367290313120345L;


  /**
   * Contiene l'informazione se all'interno del periodo vi è la prima data da ricalcolare.
   */
  @Transient
  @Getter
  @Setter
  public LocalDate recomputeFrom;

  /**
   * Costruisce una nuova istanza del periodo dello stesso tipo e con lo stesso valore di this.
   */
  public PropertyInPeriod newInstance() {
    Class<?> superClass = this.getClass();
    PropertyInPeriod obj = null;
    try {
      obj = (PropertyInPeriod) superClass.newInstance();
      obj.setOwner(this.getOwner());
      obj.setValue(this.getValue());
      obj.setType(this.getType());
      return (PropertyInPeriod) obj;
    } catch (InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      log.error("Impossibile creare una nuova istanza di {}", this.toString());
    }
    return null;

  }
}
