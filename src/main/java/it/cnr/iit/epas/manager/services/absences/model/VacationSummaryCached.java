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

package it.cnr.iit.epas.manager.services.absences.model;

import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary.TypeSummary;
import it.cnr.iit.epas.models.Contract;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Versione cachata del riepilogo.
 *
 * @author Alessandro Martelli
 */
public class VacationSummaryCached implements Serializable {

  private static final long serialVersionUID = -8968069510648138668L;

  public boolean exists = true;
  public TypeSummary type;
  public int year;
  public LocalDate date; 
  public Contract contract;
  
  public long total;
  public long postPartum;
  public long accrued;
  public long used;
  public long usable;
  public boolean expired;
  public long usableTotal;
  public boolean isContractLowerLimit;
  public LocalDate lowerLimit;
  public boolean isContractUpperLimit;
  public LocalDate upperLimit;
  
  /**
   * Costruttore. Se il vacationSummary è null significa che il riepilogo non esiste:
   * Setto exists = false;
   */
  public VacationSummaryCached(VacationSummary vacationSummary, Contract contract, int year, 
      LocalDate date, TypeSummary type) {
    
    if (vacationSummary == null) {
      this.exists = false;
      this.type = type;
      this.year = year;
      this.date = date;
      this.contract = contract;
      //this.contract.merge();
    } else {
      this.type = vacationSummary.type;
      this.year = vacationSummary.year;
      this.date = vacationSummary.date;
      this.contract = vacationSummary.contract;
      //this.contract.merge();
      this.total = vacationSummary.total();
      this.postPartum = vacationSummary.postPartum().size();
      this.accrued = vacationSummary.accrued();
      this.used = vacationSummary.used();
      this.usable = vacationSummary.usable();
      this.expired = vacationSummary.expired();
      this.usableTotal = vacationSummary.usableTotal();
      this.isContractLowerLimit = vacationSummary.isContractLowerLimit();
      this.lowerLimit = vacationSummary.lowerLimit();
      this.isContractUpperLimit = vacationSummary.isContractUpperLimit();
      this.upperLimit = vacationSummary.upperLimit();
    }
  }
}