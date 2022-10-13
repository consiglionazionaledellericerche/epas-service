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
import it.cnr.iit.epas.models.enumerate.CertificationType;
import java.time.YearMonth;
import java.util.Comparator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import org.hibernate.envers.Audited;


/**
 * Contiene le informazioni relative alla richiesta/risposta di elaborazione dati delle
 * assenze/competenze/buoni mensa inviati al nuovo sistema degli attestati del CNR.
 *
 * @author Alessandro Martelli
 */
@Audited
@Entity
@Table(name = "certifications")
public class Certification extends BaseEntity {

  private static final long serialVersionUID = 4909012051833782060L;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "person_id", nullable = false)
  private Person person;

  private int year;

  private int month;

  @Enumerated(EnumType.STRING)
  @Column(name = "certification_type")
  private CertificationType certificationType;

  private String content;

  @Column(name = "problems")
  private String problems = null;

  @Column(name = "warnings")
  private String warnings = null;

  @Column(name = "attestati_id")
  private Integer attestatiId;

  @Transient
  public boolean containProblems() {
    return this.problems != null && !this.problems.isEmpty();
  }

  @Transient
  public boolean containWarnings() {
    return this.warnings != null && !this.warnings.isEmpty();
  }

  /**
   * Una chiave che identifica in modo univoco la certificazione di una persona.
   * es. ABSENCE 92;14;15
   */
  @Transient
  public String aMapKey() {
    return this.certificationType.name() + this.content;
  }

  public static String serializeTrainingHours(int begin, int end, int value) {
    return begin + ";" + end + ";" + value;
  }

  public static String serializeAbsences(String code, int begin, int end) {
    return code + ";" + begin + ";" + end;
  }

  public static String serializeCompetences(String code, int value) {
    return code + ";" + value;
  }

//  @Override
//  public String toString() {
//    return MoreObjects.toStringHelper(PersonCertification.class)
//        .add("person", person.fullName())
//        .add("matricola", person.getNumber())
//        .add("year", year)
//        .add("month", month)
//        .add("key", aMapKey())
//        .toString();
//  }

  /**
   * YearMonth costruito da anno e mese.
   */
  public YearMonth getYearMonth() {
    return YearMonth.of(year, month);
  }

  /**
   * Comparatore.
   *
   * @return un Comparator che compara per fullname poi id.
   */
  public static Comparator<Certification> comparator() {
    return Comparator.comparing(Certification::getYearMonth);

  }

}