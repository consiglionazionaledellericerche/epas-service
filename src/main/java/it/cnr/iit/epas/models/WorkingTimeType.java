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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Modello per le tipologie di orario di lavoro.
 *
 * @author Cristian Lucchesi
 * @author Dario Tagliaferri
 */
@Getter
@Setter
@Entity
@Audited
@Table(name = "working_time_types")
public class WorkingTimeType extends BaseEntity {

  private static final long serialVersionUID = -3443521979786226461L;
  
  //serve per il calcolo della percentuale part time
  public static final int WORKTIME_BASE = 432;

  @Getter
  @NotNull
  @Column(nullable = false)
  //@Unique("office")
  private String description;

  @Getter
  @NotNull
  private Boolean horizontal;

  /**
   * True se il tipo di orario corrisponde ad un "turno di lavoro" false altrimenti.
   */
  private boolean shift = false;

  @Column(name = "meal_ticket_enabled")
  private boolean mealTicketEnabled = true;    //inutile

  @NotAudited
  @OneToMany(mappedBy = "workingTimeType")
  private List<ContractWorkingTimeType> contractWorkingTimeType = Lists.newArrayList();

  //@Required
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "office_id")
  private Office office;

  @Column(name = "disabled")
  private boolean disabled = false;

  @Getter
  @OneToMany(mappedBy = "workingTimeType", fetch = FetchType.EAGER)
  @OrderBy("dayOfWeek")
  private List<WorkingTimeTypeDay> workingTimeTypeDays = new ArrayList<WorkingTimeTypeDay>();
  
  private boolean enableAdjustmentForQuantity = true;

  //@Unique(value = "office, externalId")
  private String externalId;

  @NotAudited
  private LocalDateTime updatedAt;

  @PreUpdate
  @PrePersist
  private void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public String toString() {
    return description;
  }
  
  /**
   * Il tempo a lavoro medio giornaliero.
   *
   * @return tempo medio.
   */
  public int weekAverageWorkingTime() {
    int count = 0;
    int sum = 0;
    for (WorkingTimeTypeDay wttd : this.workingTimeTypeDays) {
      if (wttd.workingTime > 0) {
        sum += wttd.workingTime;
        count++;
      }
    }
    return sum / count;
  }
  
  /**
   * Euristica per capire se il tipo orario è orizzontale.
   *
   * @return esito
   */
  @Transient
  public boolean horizontalEuristic() {

    Integer workingTime = null;
    Integer mealTicketTime = null;
    Integer breakTicketTime = null;
    Integer afternoonThreshold = null;
    Integer afternoonThresholdTime = null;

    boolean equal = true;

    for (WorkingTimeTypeDay wttd : this.workingTimeTypeDays) {

      if (wttd.holiday) {
        continue;
      }

      if (workingTime == null) {
        workingTime = wttd.workingTime;
        mealTicketTime = wttd.mealTicketTime;
        breakTicketTime = wttd.breakTicketTime;
        afternoonThreshold = wttd.ticketAfternoonThreshold;
        afternoonThresholdTime = wttd.ticketAfternoonWorkingTime;
        continue;
      }

      if (!workingTime.equals(wttd.workingTime)) {
        equal = false;
      }
      if (!mealTicketTime.equals(wttd.mealTicketTime)) {
        equal = false;
      }
      if (!breakTicketTime.equals(wttd.breakTicketTime)) {
        equal = false;
      }
      if (!afternoonThreshold.equals(wttd.ticketAfternoonThreshold)) {
        equal = false;
      }
      if (!afternoonThresholdTime.equals(wttd.ticketAfternoonWorkingTime)) {
        equal = false;
      }
    }

    return equal;

  }
  
  /**
   * Calcola il tempo percentuale di part time.
   *
   * @return percentuale
   */
  public int percentEuristic() {
    int average = averageMinutesInWeek();
    if (average == 0) {
      return 100;
    }
    
    int percent = (average * 100) / WORKTIME_BASE;  
    return percent;
  }
  
  /**
   * Ritorna la media dei minuti lavorati in una settimana.
   *
   * @return la media dei minuti lavorati in una settimana.
   */
  public int averageMinutesInWeek() {
    int totalMinutes = 0;
    int totalDays = 0;
    for (WorkingTimeTypeDay workingTimeTypeDay : this.workingTimeTypeDays) {
      if (!workingTimeTypeDay.holiday) {
        totalMinutes += workingTimeTypeDay.workingTime;
      }
      if (workingTimeTypeDay.dayOfWeek != DayOfWeek.SATURDAY.getValue() 
          && workingTimeTypeDay.dayOfWeek != DayOfWeek.SUNDAY.getValue()) {
        totalDays++;
      }
    }
    return totalMinutes / totalDays;
  }

}