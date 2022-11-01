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
package it.cnr.iit.epas.tests.utils;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Preconditions;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.joda.time.Days;
import org.junit.jupiter.api.Test;

public class DateUtilityTest {

  public static int daysInInterval(org.joda.time.LocalDate begin, org.joda.time.LocalDate end) {

    int days = Days.daysBetween(begin, end).getDays() + 1;
    //controllo compatibilità con vecchio algoritmo.
    if (begin.getYear() == end.getYear()) {
      int oldDays = end.getDayOfYear() - begin.getDayOfYear() + 1;
      Preconditions.checkState(days == oldDays);
    }
    return days;

  }

  @Test
  void daysInIntervalUsingJavaTime() {
    DateInterval dateInterval = new DateInterval(LocalDate.now(), LocalDate.now());
    assertEquals(DateUtility.daysInInterval(dateInterval), 1);

    DateInterval dateInterval2 = new DateInterval(LocalDate.now(), LocalDate.now().plusDays(100));
    assertEquals(DateUtility.daysInInterval(dateInterval2), 101);
    
    
  }

  @Test
  void daysIntervalNewToOld() {
    DateInterval dateInterval = new DateInterval(LocalDate.now(), LocalDate.now());
    assertEquals(DateUtility.daysInInterval(dateInterval), daysInInterval(org.joda.time.LocalDate.now(), org.joda.time.LocalDate.now()));
    
    DateInterval dateInterval2 = new DateInterval(LocalDate.now(), LocalDate.now().plusDays(1));
    assertEquals(DateUtility.daysInInterval(dateInterval2), daysInInterval(org.joda.time.LocalDate.now(), org.joda.time.LocalDate.now().plusDays(1)));
  }

  @Test
  public void toMinute() {
    LocalDateTime aDateTime = LocalDateTime.of(2022,10,19,9,30,00);
    assertEquals(DateUtility.toMinute(aDateTime), 9*60 + 30);
    LocalTime aTime = LocalTime.of(9,30,0);
    assertEquals(DateUtility.toMinute(aTime), 9*60 + 30);
  }
}