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

import com.google.common.base.MoreObjects;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.enumerate.BlockType;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Buoni pasto.
 */
@Audited
@Entity
@Table(name = "meal_ticket", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"code", "office_id"})})
public class MealTicket extends BaseEntity {

  private static final long serialVersionUID = -963204680918650598L;

  @NotAudited
  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "contract_id", nullable = false)
  public Contract contract;

  public Integer year;

  @NotNull
  public LocalDate date;

  @NotNull
  public String block; /*esempio 5941 3165 01 */
  
  @Enumerated(EnumType.STRING)
  public BlockType blockType;

  public Integer number;

  //@CheckWith(MealTicketInOffice.class)
  //@Unique(value = "code, office")
  public String code; /* concatenzazione block + number */

  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "admin_id", nullable = false)
  public Person admin;

  @NotNull
  @Column(name = "expire_date")
  public LocalDate expireDate;
  
  public boolean returned = false;
  
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "office_id", nullable = false)
  public Office office;

  @Transient
  public Boolean used = null;

  @Override
  public String toString() {

    return MoreObjects.toStringHelper(this)
            .add("id", getId())
            .add("contract", contract.getId())
            .add("code", code)
            .add("person", contract.person.getName() + " " + contract.person.getSurname())
            .add("date", date)
            .add("expire", expireDate).toString();

  }
}
