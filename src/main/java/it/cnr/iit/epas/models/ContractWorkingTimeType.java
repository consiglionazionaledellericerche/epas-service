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

import it.cnr.iit.epas.models.base.IPropertiesInPeriodOwner;
import it.cnr.iit.epas.models.base.IPropertyInPeriod;
import it.cnr.iit.epas.models.base.PropertyInPeriod;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Un periodo contrattuale.
 *
 * @author Alessandro Martelli
 */
@Getter
@Setter
@ToString
@Audited
@Entity
@Table(name = "contracts_working_time_types")
public class ContractWorkingTimeType extends PropertyInPeriod implements IPropertyInPeriod {

  private static final long serialVersionUID = 3730183716240278997L;

  @Getter
  @NotNull
  @ManyToOne
  @JoinColumn(name = "contract_id")
  private Contract contract;

  @Getter
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "working_time_type_id")
  private WorkingTimeType workingTimeType;

  @NotAudited
  private LocalDateTime updatedAt;

  private String externalId;

  @PreUpdate
  @PrePersist
  private void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public Object getValue() {
    return this.workingTimeType;
  }

  @Override
  public void setValue(Object value) {
    this.workingTimeType = (WorkingTimeType) value;
  }

  public IPropertiesInPeriodOwner getOwner() {
    return this.contract;
  }

  public void setOwner(IPropertiesInPeriodOwner target) {
    this.contract = (Contract) target;
  }

  @Override
  public boolean periodValueEquals(Object otherValue) {
    if (otherValue instanceof WorkingTimeType) {
      return this.getValue().equals(((WorkingTimeType) otherValue));
    }
    return false;
  }

  @Override
  public Object getType() {
    return this.getClass();
  }

  @Override
  public void setType(Object value) {
    // questo metodo in questo caso non serve, perchè i periods sono tutti dello stesso tipo.
  }
  
  @Override
  public String getLabel() {
    return this.workingTimeType.getDescription();
  }

}