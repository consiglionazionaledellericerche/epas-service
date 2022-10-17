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
package it.cnr.iit.epas.models.dto;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.WorkingTimeType;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * Rappresenta le informazioni di un'orario di lavoro di tipo orizzontale.
 */
@Component
public class HorizontalWorkingTime {

  private final Provider<EntityManager> emp;
  
  @Inject
  public HorizontalWorkingTime(Provider<EntityManager> emp) {
    this.emp = emp;
    this.holidays = Lists.newArrayList();
    this.holidays.add(LocalDate.now().withDayOfWeek(
            DateTimeConstants.SATURDAY).dayOfWeek().getAsText());
    this.holidays.add(LocalDate.now().withDayOfWeek(
            DateTimeConstants.SUNDAY).dayOfWeek().getAsText());
  }

  /**
   * Ore lavorative.
   */
  @Min(1)
  @Max(DateTimeConstants.HOURS_PER_DAY - 1)
  public int workingTimeHour = 7;

  /**
   * Frazione orario di minuti lavorativi da sommare alle ore lavorative FIXME: perché non fare un
   * campo unico con i minuti lavorativi del giorno?.
   */
  @Min(0)
  @Max(DateTimeConstants.MINUTES_PER_HOUR - 1)
  public int workingTimeMinute = 12;

  public List<String> holidays;

  public boolean mealTicketEnabled = true;
  
  public boolean reproportionAbsenceCodesEnabled = true;

  @Min(1)
  @Max(23)
  public int mealTicketTimeHour = 6;
  @Min(0)
  @Max(59)
  public int mealTicketTimeMinute = 0;
  @Min(30)
  public int breakTicketTime = 30;

  public boolean afternoonThresholdEnabled = false;

  @Min(1)
  @Max(23)
  public int ticketAfternoonThresholdHour = 13;
  @Min(0)
  @Max(59)
  public int ticketAfternoonThresholdMinute = 30;
  @Min(0)
  public int ticketAfternoonWorkingTime = 1;

  @NotNull
  public String name;
  public String externalId;
  
  /**
   * Dal tipo orario ricava il pattern originario.
   */
  //FIXME: da correggere prima del passaggio a spring boot
//  public HorizontalWorkingTime(final WorkingTimeType wtt) {
//
//    this.name = wtt.description;
//    this.holidays = Lists.newArrayList();
//    this.reproportionAbsenceCodesEnabled = wtt.enableAdjustmentForQuantity;
//
//    for (WorkingTimeTypeDay wttd : wtt.workingTimeTypeDays) {
//
//      if (wttd.holiday) {
//        this.holidays.add(holidayName(wttd.dayOfWeek));
//        continue;
//      }
//
//      this.workingTimeHour =
//              wttd.workingTime / DateTimeConstants.SECONDS_PER_MINUTE;
//      this.workingTimeMinute =
//              wttd.workingTime % DateTimeConstants.SECONDS_PER_MINUTE;
//
//      if (wttd.mealTicketTime > 0) {
//        this.mealTicketEnabled = true;
//        this.mealTicketTimeHour =
//                wttd.mealTicketTime / DateTimeConstants.SECONDS_PER_MINUTE;
//        this.mealTicketTimeMinute =
//                wttd.mealTicketTime % DateTimeConstants.SECONDS_PER_MINUTE;
//        this.breakTicketTime = wttd.breakTicketTime;
//      } else {
//        this.mealTicketEnabled = false;
//      }
//
//      if (wttd.ticketAfternoonThreshold > 0) {
//        this.afternoonThresholdEnabled = true;
//        this.ticketAfternoonThresholdHour =
//                wttd.ticketAfternoonThreshold
//                        /
//                        DateTimeConstants.SECONDS_PER_MINUTE;
//
//        this.ticketAfternoonThresholdMinute =
//                wttd.ticketAfternoonThreshold
//                        %
//                        DateTimeConstants.SECONDS_PER_MINUTE;
//        this.ticketAfternoonWorkingTime =
//                wttd.ticketAfternoonWorkingTime;
//      } else {
//        this.afternoonThresholdEnabled = false;
//      }
//    }
//  }

  private static final String holidayName(final int dayOfWeek) {

    return LocalDate.now().withDayOfWeek(dayOfWeek).dayOfWeek().getAsText();
  }

  /**
   * Dal pattern orizzontale costruisce il tipo orario con ogni giorno di lavoro e persiste i dati.
   */
  public final void buildWorkingTimeType(final Office office) {

    WorkingTimeType wtt = new WorkingTimeType();

    wtt.setHorizontal(true);
    wtt.setDescription(this.name);
    wtt.setOffice(office);
    wtt.setDisabled(false);
    wtt.setExternalId(this.externalId);
    wtt.setEnableAdjustmentForQuantity(this.reproportionAbsenceCodesEnabled);

    emp.get().persist(wtt);
    //wtt.save();

    for (int i = 0; i < DateTimeConstants.DAYS_PER_WEEK; i++) {

      WorkingTimeTypeDay wttd = new WorkingTimeTypeDay();
      wttd.dayOfWeek = i + 1;
      wttd.workingTime =
              this.workingTimeHour * DateTimeConstants.SECONDS_PER_MINUTE
                      + this.workingTimeMinute;
      wttd.holiday = isHoliday(wttd);

      if (this.mealTicketEnabled) {
        wttd.mealTicketTime =
                this.mealTicketTimeHour
                        *
                        DateTimeConstants.SECONDS_PER_MINUTE
                        +
                        this.mealTicketTimeMinute;
        wttd.breakTicketTime = this.breakTicketTime;

        if (this.afternoonThresholdEnabled) {
          wttd.ticketAfternoonThreshold =
                  this.ticketAfternoonThresholdHour
                          *
                          DateTimeConstants.SECONDS_PER_MINUTE
                          +
                          this.ticketAfternoonThresholdMinute;
          wttd.ticketAfternoonWorkingTime =
                  this.ticketAfternoonWorkingTime;
        }
      }

      wttd.workingTimeType = wtt;
      emp.get().persist(wttd);
      //wttd.save();

    }
  }

  private final boolean isHoliday(final WorkingTimeTypeDay wttd) {

    return this.holidays.contains(LocalDate.now()
            .withDayOfWeek(wttd.dayOfWeek).dayOfWeek().getAsText());
  }

  /**
   * TODO: Impostare un global binder.
   */
  public final void setHolidays(final String value) {
    this.holidays = Lists.newArrayList((Splitter.on(",")
            .trimResults()
            .omitEmptyStrings()
            .split(value)));
  }


}
