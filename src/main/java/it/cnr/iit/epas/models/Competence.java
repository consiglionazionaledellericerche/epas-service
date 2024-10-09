/*
 * Copyright (C) 2024  Consiglio Nazionale delle Ricerche
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

import it.cnr.iit.epas.models.base.BaseEntity;
import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Tabella delle competenze relative alla persona in cui sono memorizzate le competenze in
 * determinate date (espresse attraverso due interi, uno relativo all'anno e uno relativo al mese
 * con relative descrizioni e valori.
 *
 * @author Dario Tagliaferri
 * @author Arianna Del Soldato
 */
@Getter
@Setter
@Entity
@Table(name = "competences")
@Audited
public class Competence extends BaseEntity {

  private static final long serialVersionUID = -36737525666037452L;

  @ManyToOne
  private Person person;

  @NotNull
  @ManyToOne
  public CompetenceCode competenceCode;

  public int year;

  public int month;

  public BigDecimal valueRequested = BigDecimal.ZERO;

  public Integer exceededMins;

  public int valueApproved;


  public String reason;


  /**
   * Costruttore.
   *
   * @param person la persona
   * @param competenceCode il codice di competenza
   * @param year l'anno
   * @param month il mese
   */
  public Competence(
      Person person, CompetenceCode competenceCode, int year, int month) {
    this.person = person;
    this.competenceCode = competenceCode;
    this.year = year;
    this.month = month;
  }

  /**
   * Costruttore.
   *
   * @param person la persona
   * @param competenceCode il codice di competenza
   * @param year l'anno
   * @param month il mese
   * @param valueApproved la quantità
   * @param reason la motivazione
   */
  public Competence(
      Person person, CompetenceCode competenceCode, int year, int month, int valueApproved, String reason) {
    this.person = person;
    this.competenceCode = competenceCode;
    this.year = year;
    this.month = month;
    this.valueApproved = valueApproved;
    this.reason = reason;
  }

  /**
   * Costruttore vuoto.
   */
  public Competence() {
  }

  @Override
  public String toString() {
    return String.format("Competenza %s nel mese %s-%s per %s: %s", competenceCode, month, year,
        person, valueApproved);
  }

}
