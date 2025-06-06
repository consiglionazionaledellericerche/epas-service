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

import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.base.IPropertiesInPeriodOwner;
import it.cnr.iit.epas.models.base.PropertyInPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Singola configurazione associata ad una persona.
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "person_configurations")
public class PersonConfiguration extends PropertyInPeriod {

  private static final long serialVersionUID = 6467506090648831715L;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id")
  private Person person;

  @Enumerated(EnumType.STRING)
  @Column(name = "epas_param")
  private EpasParam epasParam;

  @Column(name = "field_value")
  private String fieldValue;

  @Override
  public IPropertiesInPeriodOwner getOwner() {
    return this.person;
  }

  @Override
  public void setOwner(IPropertiesInPeriodOwner target) {
    this.person = (Person) target;
  }

  @Override
  public Object getType() {
    return this.epasParam;
  }

  @Override
  public void setType(Object value) {
    this.epasParam = (EpasParam) value;
  }

  @Override
  public Object getValue() {
    return this.fieldValue;
  }

  @Override
  public void setValue(Object value) {
    this.fieldValue = (String) value;
  }

  @Override
  public boolean periodValueEquals(Object otherValue) {
    if (otherValue instanceof String) {
      if (this.getValue().equals((String) otherValue)) {
        return true;
      }
    }
    return false;
  }

}
