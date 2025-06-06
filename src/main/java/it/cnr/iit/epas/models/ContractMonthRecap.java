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

import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.envers.Audited;

/**
 * Riepilogo mensile di un contratto.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "contract_month_recap",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"year", "month", "contract_id"})})
@Audited
public class ContractMonthRecap extends BaseEntity {

  private static final long serialVersionUID = 5381901476391668672L;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_id")
  public Contract contract;

  @Column
  public int year;

  @Column
  public int month;

  //***************************************************************************/
  // MODULO RECAP ASSENZE
  // **************************************************************************/

  @Column(name = "abs_rc_usati")
  public Integer recoveryDayUsed = 0;        //numeroRiposiCompensativi

  //***************************************************************************/
  // * FONTI DELL'ALGORITMO RESIDUI
  // **************************************************************************/

  @Column(name = "s_r_bp_init")
  public int buoniPastoDaInizializzazione = 0;

  @Column(name = "s_r_bp")
  public int buoniPastoDalMesePrecedente = 0;

  @Column(name = "s_bp_consegnati")
  public int buoniPastoConsegnatiNelMese = 0;

  @Getter
  @Column(name = "s_bd_usati")
  public int buoniPastoUsatiNelMese = 0;

  @Column(name = "s_r_ac_initmese")
  public int initResiduoAnnoCorrenteNelMese = 0;    //per il template (se sourceContract è del mese)

  @Column(name = "s_r_ap")
  public int initMonteOreAnnoPassato = 0;        //dal precedente recap ma è utile salvarlo

  @Column(name = "s_r_ac")
  public int initMonteOreAnnoCorrente = 0;    //dal precedente recap ma è utile salvarlo

  @Column(name = "s_pf")
  public int progressivoFinaleMese = 0;            //person day

  /**
   * Questo campo ha due scopi: <br> 1) Il progressivo finale positivo da visualizzare nel template.
   * <br> 2) Il tempo disponibile per straordinari. <br> TODO: Siccome i due valori potrebbero
   * differire (esempio turnisti), decidere se splittarli in due campi distinti.
   */
  @Column(name = "s_pfp")
  public int progressivoFinalePositivoMese = 0;


  @Column(name = "s_r_ap_usabile")
  public boolean possibileUtilizzareResiduoAnnoPrecedente = true;

  @Column(name = "s_s1")
  public int straordinariMinutiS1Print = 0;    //per il template

  @Column(name = "s_s2")
  public int straordinariMinutiS2Print = 0;    //per il template

  @Column(name = "s_s3")
  public int straordinariMinutiS3Print = 0;    //per il template

  @Column(name = "s_rc_min")
  public int riposiCompensativiMinutiPrint = 0;    //per il template
  
  @Column(name = "s_91ce_min")
  public int riposiCompensativiChiusuraEnteMinutiPrint = 0;    //per il template

  @Column(name = "s_ol")
  public int oreLavorate = 0;                // riepilogo per il template

  //***************************************************************************/
  // DECISIONI DELL'ALGORITMO
  // **************************************************************************/

  @Column(name = "d_pfn_ap")
  public int progressivoFinaleNegativoMeseImputatoAnnoPassato = 0;
  @Column(name = "d_pfn_ac")
  public int progressivoFinaleNegativoMeseImputatoAnnoCorrente = 0;
  @Column(name = "d_pfn_pfp")
  public int progressivoFinaleNegativoMeseImputatoProgressivoFinalePositivoMese = 0;

  @Column(name = "d_rc_ap")
  public int riposiCompensativiMinutiImputatoAnnoPassato = 0;
  @Column(name = "d_rc_ac")
  public int riposiCompensativiMinutiImputatoAnnoCorrente = 0;
  @Column(name = "d_rc_pfp")
  public int riposiCompensativiMinutiImputatoProgressivoFinalePositivoMese = 0;
  
  @Column(name = "d_91ce_ap")
  public int riposiCompensativiChiusuraEnteMinutiImputatoAnnoPassato = 0;
  @Column(name = "d_91ce_ac")
  public int riposiCompensativiChiusuraEnteMinutiImputatoAnnoCorrente = 0;
  @Column(name = "d_91ce_pfp")
  public int riposiCompensativiChiusuraEnteMinutiImputatoProgressivoFinalePositivoMese = 0;

  
  @Column(name = "d_r_ap")
  public Integer remainingMinutesLastYear = 0;

  @Column(name = "d_r_ac")
  public Integer remainingMinutesCurrentYear = 0;

  @Column(name = "d_r_bp")
  public Integer remainingMealTickets = 0; //buoniPastoResidui


  //***************************************************************************/
  // DI SUPPORTO (VALORIZZATI PER POI ESSERE IMPUTATI)
  // **************************************************************************/

  @Transient
  public int straordinariMinuti = 0;    //competences (di appoggio deducibile dalle imputazioni)

  @Transient
  public int riposiCompensativiMinuti = 0;    //absences  (di appoggio deducibile dalle imputazioni)
  // in charts è usato... capire cosa contiene alla fine e fixare
  
  @Transient
  public int riposiCompensativiChiusuraEnteMinuti = 0;

  //person day  // (di appoggio deducibile dalle imputazioni)
  @Transient
  public int progressivoFinaleNegativoMese = 0;

  //**************************************************************************
  // DI SUPPORTO (VALORIZZATI PER POI ESSERE SCORPORATI)
  // ************************************************************************/

  @Transient
  public int progressivoFinalePositivoMeseAux = 0;    //person day
  // forse è usato... capire cosa contiene alla fine e fixare

  //**************************************************************************
  // TRANSIENTI DA METTERE NEL WRAPPER
  //*************************************************************************/

  @Transient
  public Person person;
  @Transient
  public Optional<ContractMonthRecap> mesePrecedente;
  @Transient
  public int qualifica;
  @Transient
  public IWrapperContract wrContract;

  @Transient
  public int getStraordinarioMinuti() {
    return this.straordinariMinutiS1Print + this.straordinariMinutiS2Print
            + this.straordinariMinutiS3Print;
  }

  /**
   * Stringa di descrizione del contratto. 
   */
  @Transient
  public String getContractDescription() {
    LocalDate beginMonth = LocalDate.of(this.year, this.month, 1);
    LocalDate endMonth = DateUtility.endOfMonth(beginMonth);
    DateInterval monthInterval = new DateInterval(beginMonth, endMonth);
    LocalDate endContract = this.contract.getEndDate();
    if (this.contract.getEndContract() != null) {
      endContract = this.contract.getEndContract();
    }
    if (DateUtility.isDateIntoInterval(endContract, monthInterval)) {
      return "(contratto scaduto in data " + endContract + ")";
    }
    return "";
  }

  @Transient
  public int getPositiveResidualInMonth() {

    return this.progressivoFinalePositivoMese;
  }

  /**
   * Verifica se è l'ultimo mese prima della scadenza del contratto.
   */
  @Transient
  public boolean expireInMonth() {
    if (this.contract.getEndDate() != null 
        && this.contract.getEndDate().isBefore(
            DateUtility.endOfMonth(LocalDate.of(year, month, 1)))) {
      return true;
    }
    return false;
  }
  
  /**
   * Clean dell'oggetto persistito pre ricomputazione.
   */
  public void clean() {
    //MODULO RECAP ASSENZE

    this.recoveryDayUsed = 0;        //numeroRiposiCompensativi

    //FONTI DELL'ALGORITMO RESIDUI

    this.buoniPastoDaInizializzazione = 0;
    this.buoniPastoDalMesePrecedente = 0;
    this.buoniPastoConsegnatiNelMese = 0;
    this.buoniPastoUsatiNelMese = 0;
    this.initResiduoAnnoCorrenteNelMese = 0;
    this.initMonteOreAnnoPassato = 0;
    this.initMonteOreAnnoCorrente = 0;
    this.progressivoFinaleMese = 0;
    this.progressivoFinalePositivoMese = 0;
    this.possibileUtilizzareResiduoAnnoPrecedente = true;
    this.straordinariMinutiS1Print = 0;
    this.straordinariMinutiS2Print = 0;
    this.straordinariMinutiS3Print = 0;
    this.riposiCompensativiMinutiPrint = 0;
    this.oreLavorate = 0;

    //DECISIONI DELL'ALGORITMO

    this.progressivoFinaleNegativoMeseImputatoAnnoPassato = 0;
    this.progressivoFinaleNegativoMeseImputatoAnnoCorrente = 0;
    this.progressivoFinaleNegativoMeseImputatoProgressivoFinalePositivoMese = 0;
    this.riposiCompensativiMinutiImputatoAnnoPassato = 0;
    this.riposiCompensativiMinutiImputatoAnnoCorrente = 0;
    this.riposiCompensativiMinutiImputatoProgressivoFinalePositivoMese = 0;
    this.riposiCompensativiChiusuraEnteMinutiImputatoAnnoCorrente = 0;
    this.riposiCompensativiChiusuraEnteMinutiImputatoAnnoPassato = 0;
    this.riposiCompensativiChiusuraEnteMinutiImputatoProgressivoFinalePositivoMese = 0;
    this.remainingMinutesLastYear = 0;
    this.remainingMinutesCurrentYear = 0;
    this.remainingMealTickets = 0;

  }

}
