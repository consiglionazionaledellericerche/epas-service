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

package it.cnr.iit.epas.utils;

import com.google.common.base.Preconditions;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.NoArgsConstructor;

/**
 * Classe di utilità per la gestione dei tempi e orari.
 * 
 */
@NoArgsConstructor
public class TimeInterval {
  
  private LocalTime begin;
  private LocalTime end;

  /**
   * Un TimeInterval con begin obbligatorio.
   *
   * @param begin ora inizio intervallo.
   * @param end da fine intervallo.
   */
  public static TimeInterval withBegin(LocalTime begin, Optional<LocalTime> end) {
    TimeInterval timeInterval = new TimeInterval();
    Preconditions.checkArgument(begin != null);
    timeInterval.begin = begin;
    if (end.isPresent()) {
      Preconditions.checkArgument(!begin.isAfter(end.get()));
      timeInterval.end = end.get();
    } else {
      timeInterval.end = LocalTime.of(23, 59, 59);
    }
    return timeInterval;
  }

  /**
   * Costruisce il dateInterval. <br>
   * - Se begin è null viene impostata MIN_DATE. <br>
   * - Se end è null viene impostata MAX_DATE. <br>
   * Se begin è successiva a end vengono invertite.
   *
   * @param begin data inizio intervallo, se null impostata a MIN_DATE
   * @param end data fine intervallo, se null impostata a MAX_DATE
   */
  public static TimeInterval build(LocalTime begin, LocalTime end) {

    if (begin == null && end == null) {
      begin = LocalTime.of(0, 0, 0);
      end = LocalTime.of(23, 59, 59);
    } else if (begin == null) {
      begin = LocalTime.of(0, 0, 0);
    } else if (end == null) {
      end = LocalTime.of(23, 59, 59);
    }

    //Non applico il riferimento ma costruisco nuovi oggetti
    LocalTime beginCopy = LocalTime.from(begin);
    LocalTime endCopy = LocalTime.from(end);

    TimeInterval timeInterval = new TimeInterval();
    
    if (begin.isAfter(end)) {
      timeInterval.begin = endCopy;
      timeInterval.end = beginCopy;
    } else {
      timeInterval.begin = beginCopy;
      timeInterval.end = endCopy;
    }

    return timeInterval;
  }


  /**
   * Questo costruttore è confondente. <br>
   * Sia date1 che date2 quando nulle vengono sostituite con MAX_DATE. <br>
   * se date1 è null e date2 è valorizzato, crea un intorno [date2, MAX_DATE]
   * il chè non è molto intuitivo.
   */
  public TimeInterval(LocalTime time1, LocalTime time2) {

    if (time1 == null && time2 == null) {
      time1 = LocalTime.of(0, 0, 0);
      time2 = LocalTime.of(23, 59, 59);
    } else if (time1 == null) {
      time1 = LocalTime.of(23, 59, 59);
    } else if (time2 == null) {
      time2 = LocalTime.of(23, 59, 59);
    }

    //Non applico il riferimento ma costruisco nuovi oggetti
    LocalTime time1Copy = LocalTime.from(time1);
    LocalTime time2Copy = LocalTime.from(time2);

    if (time1.isAfter(time2)) {
      this.begin = time2Copy;
      this.end = time1Copy;
    } else {
      this.begin = time1Copy;
      this.end = time2Copy;
    }
  }

  public LocalTime getBegin() {
    return begin;
  }

  public LocalTime getEnd() {
    return end;
  }

  public long hoursInInterval() {
    return ChronoUnit.HOURS.between(begin, end);
  }
  
  public long minutesInInterval() {
    return ChronoUnit.MINUTES.between(begin, end);
  }

  @Override
  public String toString() {
    return "[" + this.begin.toString() + "," + this.getEnd().toString() + "]";
  }
}