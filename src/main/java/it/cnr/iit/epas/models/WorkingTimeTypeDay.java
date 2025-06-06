/*
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
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;


/**
 * Per ogni giorno della settimana ci sono riportate le informazioni necessarie all'utilizzo di
 * questa tipologia di orario nel giorno specificato.
 *
 * @author Cristian Lucchesi
 * @author Dario Tagliaferri
 */
@ToString
@Audited
@Entity
@Table(name = "working_time_type_days")
public class WorkingTimeTypeDay extends BaseEntity {

  private static final long serialVersionUID = 4622948996966018754L;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "working_time_type_id", nullable = false)
  public WorkingTimeType workingTimeType;

  @Getter
  @NotNull
  @Min(1)
  @Max(7)
  public int dayOfWeek;

  /**
   * tempo di lavoro giornaliero espresso in minuti.
   */
  @Getter
  @NotNull
  public Integer workingTime;

  /**
   * booleano per controllo se il giorno in questione è festivo o meno.
   */
  @Getter
  public boolean holiday = false;

  /**
   * tempo di lavoro espresso in minuti che conteggia se possibile usufruire del buono pasto.
   */
  @Getter
  @NotNull
  public Integer mealTicketTime = 0;

  @Getter
  @NotNull
  public Integer breakTicketTime = 0;

  /**
   * La soglia pomeridiana dopo la quale è necessario effettuare lavoro per avere diritto al buono
   * pasto.
   */
  @Getter
  public Integer ticketAfternoonThreshold = 0;

  /**
   * La quantità di lavoro dopo la soglia pomeridiana necessaria per avere diritto al buono pasto.
   */
  @Getter
  public Integer ticketAfternoonWorkingTime = 0;


  // Campi non utilizzati

  public Integer timeSlotEntranceFrom;
  public Integer timeSlotEntranceTo;
  public Integer timeSlotExitFrom;
  public Integer timeSlotExitTo;

  /**
   * tempo inizio pausa pranzo.
   */
  public Integer timeMealFrom;

  /**
   * tempo fine pausa pranzo.
   */
  public Integer timeMealTo;

  @NotAudited
  public LocalDateTime updatedAt;

  @PreUpdate
  @PrePersist
  private void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * True se è ammesso il calcolo del buono pasto per la persona, false altrimenti (il campo
   * mealTicketTime che rappresenta il tempo minimo di lavoro per avere diritto al buono pasto è
   * pari a zero).
   */
  @Transient
  public boolean mealTicketEnabled() {

    if (this.holiday) {
      return false;
    }
    if (this.mealTicketTime > 0) {
      return true;
    } else {
      return false;
    }
  }
  

}
