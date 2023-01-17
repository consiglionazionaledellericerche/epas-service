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

package it.cnr.iit.epas.dto.v4;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContractDto extends BaseModelDto {

  private String perseoId;

  private String externalId;

  /**
   * Patch per gestire i contratti con dati mancanti da dcp. E' true unicamente per segnalare tempo
   * determinato senza data fine specificata.
   */
  private boolean isTemporaryMissing;

  /*
   * Quando viene valorizzata la sourceDateResidual, deve essere valorizzata
   * anche la sourceDateMealTicket
   */
  private LocalDate sourceDateResidual;
  private LocalDate sourceDateVacation;
  private LocalDate sourceDateMealTicket;
  private LocalDate sourceDateRecoveryDay;
  private boolean sourceByAdmin = true;

  private Integer sourceVacationLastYearUsed = null;
  private Integer sourceVacationCurrentYearUsed = null;

  private Integer sourcePermissionUsed = null;

  // Valore puramente indicativo per impedire che vengano inseriti i riposi compensativi in minuti
  private Integer sourceRecoveryDayUsed = null;
  private Integer sourceRemainingMinutesLastYear = null;
  private Integer sourceRemainingMinutesCurrentYear = null;
  private Integer sourceRemainingMealTicket = null;

  //data di termine contratto in casi di licenziamento, pensione, morte, ecc ecc...
  private LocalDate endContract;

}
