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

package it.cnr.iit.epas.manager.services.absences.model;

import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AmountType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Assenza già inserite con le relative informazioni.
 */
@Builder 
@Getter 
@Setter(AccessLevel.PACKAGE)
public class TakenAbsence {
  public Absence absence;
  public AmountType amountType;     //risalibile dal period..
  
  
  public long periodTakableTotal;   //situazione prima della assenza
  public long periodTakenBefore;

  public int takenAmount;
  
  public boolean beforeInitialization; // = false;
  
  public boolean toInsert;          //segnala che è l'assenza da inserire nella chain
  
  /**
   * Se l'assenza non supera i limiti.
   *
   * @return esito
   */
  public boolean canAddTakenAbsence() {
    
    if (beforeInitialization) {
      return true;
    }
    
    if (periodTakableTotal < 0) {
      //TODO: se non c'è limite programmarlo in un booleano
      return true;
    }
    if (periodTakableTotal - periodTakenBefore - takenAmount >= 0) {
      return true;
    }
    return false;
  }
  
  public long periodResidualBefore() {
    return periodTakableTotal - periodTakenBefore;
  }
}
