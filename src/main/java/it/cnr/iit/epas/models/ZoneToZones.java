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

import it.cnr.iit.epas.models.base.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Associazione tra zone di timbratura.
 */
@Entity
@Table(name = "zone_to_zones")
public class ZoneToZones extends BaseEntity {

  private static final long serialVersionUID = 1252197401101094698L;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "zone_base_id", updatable = false)
  public Zone zoneBase;
  
  @NotNull
  @ManyToOne
  @JoinColumn(name = "zone_linked_id", updatable = false)
  public Zone zoneLinked;
  
  @NotNull
  @javax.validation.constraints.Min(1)
  public int delay;  

  @Override
  public String toString() {
    return String.format(
        "Zone[%d] - zone.name = %s, zoneLinked.name= %s, delay = %d",
         getId(), zoneBase.name, zoneLinked.name, delay);
  }
}