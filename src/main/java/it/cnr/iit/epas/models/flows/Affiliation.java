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

package it.cnr.iit.epas.models.flows;

import com.google.common.collect.Range;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;


/**
 * Rappresenta l'affiliazione di una persona ad un gruppo.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Audited
@Entity
public class Affiliation extends BaseEntity {

  private static final long serialVersionUID = -8101378323853245726L;

  @NotNull
  //@CheckWith(AffiliationCheck.class)
  @ManyToOne
  private Group group;

  @NotNull
  @ManyToOne
  private Person person;
  
  //@Range(min = 0.0, max = 100)
  private BigDecimal percentage = BigDecimal.valueOf(100);

  @NotNull
  private LocalDate beginDate;

  private LocalDate endDate;

  //Il controllo di unicità degli externalId dovrebbe essere sugli office
  //non su group
  //@Unique(value = "group, externalId")
  private String externalId;

  @NotAudited
  private LocalDateTime updatedAt;

  @PreUpdate
  @PrePersist
  private void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Il Range che comprende le date di inizio e fine dell'assegnazione.
   */
  public Range<LocalDate> getRange() {
    if (endDate != null) {
      return Range.closed(beginDate, endDate);
    }
    return Range.atLeast(beginDate);
  }
  
  /**
   * Verifica di appartenenza a questa affiliazione.
   *
   * @return true se la data passata è compresa nelle data di validità 
   *     di questa affiliazione, false altrimenti.
   */
  public boolean contains(LocalDate date) {
    return getRange().contains(date);
  }

  /**
   * Verifica di sovrapposizione con il range di questa affiliazione.
   *
   * @return true se il range passato si sovrappone a quello definito
   *     questa affiliazione.
   */
  public boolean overlap(Range<LocalDate> otherRange) {
    return getRange().isConnected(otherRange);
  }

  /**
   * Verifica di sovrapposizione tra due affiliazioni.
   */
  public boolean overlap(Affiliation otherAffiliation) {
    return overlap(otherAffiliation.getRange()) && group.equals(otherAffiliation.getGroup());
  }

  /**
   * Verificare se l'affiliazione è attiva nella data corrente.
   *
   * @return true se l'affiliazione è attiva nella data corrente, false altrimenti.
   */
  public boolean isActive() {
    return beginDate.isBefore(LocalDate.now()) 
        && (endDate == null || LocalDate.now().isBefore(endDate));
  }
}