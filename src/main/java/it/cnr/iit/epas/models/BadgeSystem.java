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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Gruppi di badge.
 *
 * @author Alessandro Martelli
 */
@Getter
@Setter
@Entity
@Table(name = "badge_systems", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@Audited
public class BadgeSystem extends BaseEntity {

  private static final long serialVersionUID = -2530079366642292082L;

  @NotNull
  private String name;

  private String description;

  @OrderBy("code ASC")
  @OneToMany(mappedBy = "badgeSystem")
  private Set<Badge> badges = Sets.newHashSet();

  @ManyToMany(mappedBy = "badgeSystems")
  private List<BadgeReader> badgeReaders = Lists.newArrayList();

  @ManyToOne
  private Office office;

  private boolean enabled = true;

  @Override
  public String toString() {
    return this.name;
  }
}