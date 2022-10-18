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
package it.cnr.iit.epas.models;

import it.cnr.iit.epas.models.base.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;


/**
 * Contiene le informazioni relative alla richiesta/risposta di elaborazione dati delle
 * assenze/competenze/buoni mensa inviati al sistema degli attestati del CNR.
 *
 * @author Cristian Lucchesi
 */
@NoArgsConstructor
@Audited
@Entity
@Table(name = "certificated_data")
public class CertificatedData extends BaseEntity {

  private static final long serialVersionUID = 4909012051833782060L;

  public int year;
  public int month;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "person_id", nullable = false)
  public Person person;

  @Column(name = "cognome_nome")
  public String cognomeNome;

  @Column(name = "matricola")
  public String matricola;

  @Column(name = "absences_sent")
  public String absencesSent = null;

  @Column(name = "competences_sent")
  public String competencesSent = null;

  @Column(name = "meal_ticket_sent")
  public String mealTicketSent = null;

  @Column(name = "traininghours_sent")
  public String trainingHoursSent = null;

  @Column(name = "problems")
  public String problems = null;

  @Column(name = "is_ok")
  public boolean isOk = false;

  /**
   * Costruttore.
   *
   * @param person la persona
   * @param cognomeNome la stringa contenente cognome/nome
   * @param matricola la matricola
   * @param year l'anno
   * @param month il mese
   */
  public CertificatedData(
      Person person, String cognomeNome, String matricola, Integer year, Integer month) {
    this.year = year;
    this.month = month;
    this.person = person;
    this.cognomeNome = cognomeNome;
    this.matricola = matricola;
  }

}
