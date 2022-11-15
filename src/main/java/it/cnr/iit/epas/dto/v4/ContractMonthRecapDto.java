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
package it.cnr.iit.epas.dto.v4;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO del riepilogo mensile di un contratto.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractMonthRecapDto extends BaseModelDto {

  //private ContractDto contract;

  private int year;

  private int month;

  //***************************************************************************/
  // MODULO RECAP ASSENZE
  // **************************************************************************/

  private Integer recoveryDayUsed = 0;        //numeroRiposiCompensativi

  //***************************************************************************/
  // * FONTI DELL'ALGORITMO RESIDUI
  // **************************************************************************/

  private int buoniPastoDaInizializzazione = 0;

  private int buoniPastoDalMesePrecedente = 0;

  private int buoniPastoConsegnatiNelMese = 0;

  private int buoniPastoUsatiNelMese = 0;

  private int initResiduoAnnoCorrenteNelMese = 0;    //per il template (se sourceContract è del mese)

  private int initMonteOreAnnoPassato = 0;        //dal precedente recap ma è utile salvarlo

  private int initMonteOreAnnoCorrente = 0;    //dal precedente recap ma è utile salvarlo

  private int progressivoFinaleMese = 0;            //person day

  /**
   * Questo campo ha due scopi: <br> 1) Il progressivo finale positivo da visualizzare nel template.
   * <br> 2) Il tempo disponibile per straordinari. <br> TODO: Siccome i due valori potrebbero
   * differire (esempio turnisti), decidere se splittarli in due campi distinti.
   */
  private int progressivoFinalePositivoMese = 0;

  private boolean possibileUtilizzareResiduoAnnoPrecedente = true;

  private int straordinariMinutiS1Print = 0;    //per il template

  private int straordinariMinutiS2Print = 0;    //per il template

  private int straordinariMinutiS3Print = 0;    //per il template

  private int riposiCompensativiMinutiPrint = 0;    //per il template

  private int riposiCompensativiChiusuraEnteMinutiPrint = 0;    //per il template

  private int oreLavorate = 0;                // riepilogo per il template

  //***************************************************************************/
  // DECISIONI DELL'ALGORITMO
  // **************************************************************************/

  private int progressivoFinaleNegativoMeseImputatoAnnoPassato = 0;

  private int progressivoFinaleNegativoMeseImputatoAnnoCorrente = 0;

  private int progressivoFinaleNegativoMeseImputatoProgressivoFinalePositivoMese = 0;

  private int riposiCompensativiMinutiImputatoAnnoPassato = 0;

  private int riposiCompensativiMinutiImputatoAnnoCorrente = 0;

  private int riposiCompensativiMinutiImputatoProgressivoFinalePositivoMese = 0;

  private int riposiCompensativiChiusuraEnteMinutiImputatoAnnoPassato = 0;

  private int riposiCompensativiChiusuraEnteMinutiImputatoAnnoCorrente = 0;
  private int riposiCompensativiChiusuraEnteMinutiImputatoProgressivoFinalePositivoMese = 0;

  private Integer remainingMinutesLastYear = 0;

  private Integer remainingMinutesCurrentYear = 0;

  private Integer remainingMealTickets = 0; //buoniPastoResidui

  private  int qualifica;

  private int straordinarioMinuti;

  private int positiveResidualInMonth;

  /**
   * Verifica se è l'ultimo mese prima della scadenza del contratto.
   */
  private boolean expireInMonth;

  private int residualLastYearInit;
  
  private boolean hasResidualLastYear;

}