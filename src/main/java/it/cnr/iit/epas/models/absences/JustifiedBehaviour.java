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

package it.cnr.iit.epas.models.absences;

import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Modellazione delle tipologie di giustificazione dell'orario dell'orario
 * di lavoro da parte delle varie tipologie di assenza.
 *
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "justified_behaviours")
public class JustifiedBehaviour extends BaseEntity {

  private static final long serialVersionUID = -3532986170397408935L;

  /**
   * Enumerato per la gestione del comportamento di un'assenza.
   *
   */
  public enum JustifiedBehaviourName {
    minimumTime,
    maximumTime,
    takenPercentageTime,
    no_overtime,
    reduce_overtime;
  }

  @Enumerated(EnumType.STRING)
  public JustifiedBehaviourName name;
  
  @OneToMany(mappedBy = "justifiedBehaviour")
  public Set<AbsenceTypeJustifiedBehaviour> absenceTypesJustifiedBehaviours = Sets.newHashSet();

  @Override
  public String toString() {
    return this.name.name();
  }

}