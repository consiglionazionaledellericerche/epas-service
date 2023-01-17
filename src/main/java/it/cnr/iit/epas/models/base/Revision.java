/*
 * Copyright (C) 2023  Consiglio Nazionale delle Ricerche
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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import it.cnr.iit.epas.models.User;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * Definisce il mapping con le revisioni envers.
 *
 * @author Marco Andreini
 */
@Getter
@Setter
@Entity
@RevisionEntity(ExtendedRevisionListener.class)
@Table(name = "revinfo")
public class Revision {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @RevisionNumber
  @Column(name = "rev")
  public int id;

  @RevisionTimestamp
  @Column(name = "revtstmp")
  public long timestamp;
  @ManyToOne(optional = true)
  public User owner;
  // ip address
  public String ipaddress;

  @Transient
  public LocalDateTime getRevisionDate() {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
  }

  @Override
  public boolean equals(Object obj) {

    if (obj instanceof Revision) {
      final Revision other = (Revision) obj;
      return id == other.id;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("id", id)
            .add("date", getRevisionDate())
            .add("owner", owner)
            .add("ipaddress", ipaddress)
            .omitNullValues()
            .toString();
  }
}
