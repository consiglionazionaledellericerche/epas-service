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

import com.google.common.base.MoreObjects;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;


/**
 * Modello della Timbratura.
 *
 * @author Cristian Lucchesi
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "stampings")
public class Stamping extends BaseEntity implements Comparable<Stamping> {

  private static final long serialVersionUID = -2422323948436157747L;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "personDay_id", nullable = false, updatable = false)
  private PersonDay personDay;

  @Column(name = "stamp_type")
  @Enumerated(EnumType.STRING)
  private StampTypes stampType;

  @ManyToOne(optional = true)
  @JoinColumn(name = "stamp_modification_type_id")
  private StampModificationType stampModificationType;

  @NotNull
  @Column(nullable = false)
  private LocalDateTime date;

  @NotNull
  @Enumerated(EnumType.STRING)
  private WayType way;

  //@As(binder = NullStringBinder.class)
  private String note;

  //@As(binder = NullStringBinder.class)
  //@CheckWith(StringIsValid.class)
  private String place;

  //@As(binder = NullStringBinder.class)
  //@CheckWith(StringIsValid.class)
  private String reason;

  /**
   * questo campo booleano consente di determinare se la timbratura è stata effettuata dall'utente
   * all'apposita macchinetta (valore = false) o se è stato l'amministratore a settare l'orario di
   * timbratura poichè la persona in questione non ha potuto effettuare la timbratura (valore =
   * true).
   */
  @Column(name = "marked_by_admin")
  private boolean markedByAdmin;

  /**
   * con la nuova interpretazione delle possibilità del dipendente, questo campo viene settato a
   * true quando è il dipendente a modificare la propria timbratura.
   */
  @Column(name = "marked_by_employee")
  private boolean markedByEmployee;

  /**
   * con la nuova interpretazione del telelavoro per i livelli I-III, quando un dipendente si 
   * inserisce una timbratura in telelavoro, questa deve essere inserita anche sul suo cartellino, 
   * diventando a tutti gli effetti una timbratura che concorre alla generazione del residuo 
   * giornaliero.
   */
  @Column(name = "marked_by_telework")
  private boolean markedByTelework;

  /**
   * questo nuovo campo si è reso necessario per la sede centrale per capire da quale lettore 
   * proviene la timbratura così da poter applicare un algoritmo che giustifichi le timbrature 
   * di uscita/ingresso consecutive dei dipendenti se provenienti da lettori diversi e appartenenti 
   * a un collegamento definito.e all'interno della tolleranza definita per quel collegamento.
   */
  @Column(name = "stamping_zone")
  private String stampingZone;

  /**
   * true, cella bianca; false, cella gialla.
   */
  @Transient
  private boolean valid;
  @Transient
  private int pairId = 0;

  /**
   * true, la cella fittizia di uscita adesso.
   */
  @Transient
  private boolean exitingNow;

  @Transient
  public boolean isValid() {
    return valid;
  }

  @Transient
  public boolean isIn() {
    return way == WayType.in;
  }

  @Transient
  public boolean isOut() {
    return way == WayType.out;
  }
  

  /**
   * Verifica se è lavoro fuori sede.
   *
   * @return @see StampTypes::isOffSiteWork
   */
  @Transient
  public boolean isOffSiteWork() {
    return stampType != null && stampType.isOffSiteWork();
  }
  
  /**
   * costruttore di default implicitamente utilizzato dal play(controllers).
   */
  Stamping() {
  }

  /**
   * Costruttore.
   *
   * @param personDay personDay
   * @param time      time
   */
  public Stamping(PersonDay personDay, LocalDateTime time) {
    // FIXME se necessito di una stamping senza personDay (ex. per uscita in questo momento)
    // questo costruttore mi impedisce di costruirla. Per adesso permetto di passare personDay null.
    date = time;
    if (personDay != null) {
      this.personDay = personDay;
      personDay.getStampings().add(this);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("id", getId())
        .add("personDay.id", personDay.getId())
        .add("way", way)
        .add("date", date)
        .add("stampType", stampType)
        .add("stampModificationType", stampModificationType)
        .toString();

  }

  /**
   * Comparator Stamping.
   */
  @Override
  public int compareTo(final Stamping compareStamping) {
    return date.compareTo(compareStamping.date);
  }

  /**
   * Orario formattato come HH:mm.
   *
   * @return orario della timbratura formattato come HH:mm.
   */
  @Transient
  public String formattedHour() {
    if (this.date != null) {
      return DateTimeFormatter.ofPattern("HH:mm").format(date);
    } else {
      return "";
    }
  }

  /**
   * Rappresentazione compatta della timbratura.
   *
   * @return Una rappresentazione compatta della timbratura.
   */
  @Transient
  public String getLabel() {
    String output = formattedHour();
    output += way == WayType.in ? " Ingr." : " Usc.";
    output += stampType != null ? " (" + stampType.getIdentifier() + ")" : "";
    return output;
  }

  /**
   * Fondamentale per far funzionare alcune drools.
   *
   * @return Restituisce il proprietario della timbratura.
   */
  public Person getOwner() {
    return personDay.getPerson();
  }

  /**
   * Utile per effettuare i controlli temporali sulle drools.
   *
   * @return il mese relativo alla data della timbratura.
   */
  public YearMonth getYearMonth() {
    return YearMonth.of(date.getYear(), date.getMonthValue());
  }

  /**
   * Ingresso/Uscita.
   */
  public enum WayType {
    in("in"),
    out("out");

    public String description;

    WayType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return this.description;
    }
  }

}
