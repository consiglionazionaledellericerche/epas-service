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

import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * Problema su un'assenza.
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@Audited
@Entity
@Table(name = "absence_troubles")
public class AbsenceTrouble extends BaseEntity {
  
  private static final long serialVersionUID = -5066077912284859060L;

  @Enumerated(EnumType.STRING)
  private AbsenceProblem trouble;

  @ManyToOne//(fetch = FetchType.LAZY)
  @JoinColumn(name = "absence_id", nullable = false, updatable = false)
  private Absence absence;

  /**
   * Tipo di problemi sulle assenze che si possono verificare.
   */
  public enum AbsenceProblem {
    
    //Ignorata dal controllo
    IgnoredOutOfContract(false),
    IgnoredBeforeInitialization(false),
    
    //Generici
    TwoSameCodeSameDay(false),
    AllDayAlreadyExists(false),
    NotOnHoliday(false),
    IncompatibilyTypeSameDay(false),
    WeekEndContinuityBroken(false),
    UngrantedAbsence(false),
    MinimumTimeViolated(false),
    MaximumTimeExceed(false),
    Expired(false),
    
    //Gruppo
    LimitExceeded(false),
    CompromisedTwoComplation(false),           //due completamenti nello stesso giorno
    CompromisedTakableComplationGroup(false),  //assenze successive
    
    //Figli
    NoChildExist(false),
    
    //Implementazione
    ImplementationProblem(true),
    
    //Warnings
    ForceInsert(false, true),
    InReperibility(false, true),
    InShift(false, true),
    InReperibilityOrShift(false, true),
    Migration661(false, true);

    public boolean isImplementationProblem;
    
    public boolean isWarning = false;
    
    private AbsenceProblem(boolean isImplementationProblem, boolean isWarning) {
      this.isImplementationProblem = isImplementationProblem;
      this.isWarning = isWarning;
    }
    
    private AbsenceProblem(boolean isImplementationProblem) {
      this.isImplementationProblem = isImplementationProblem;
      this.isWarning = false;
    }

  }

}
