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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Classe di utilità per la gestione delle date.
 *
 */
public class DateUtility {

  public static final int DECEMBER = 12;
  public static final int JANUARY = 1;
  static final int MINUTE_IN_HOUR = 60;
  static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 1);

  /**
   * Ritorna l'ultimo giorno del mese relativo alla data passata.
   */
  public static final LocalDate endOfMonth(LocalDate date) {
    return date.withDayOfMonth(date.getMonth().length(date.isLeapYear()));
  }
  
  /**
   * Metodo che calcola la data della pasqua nell'anno passato come parametro.
   *
   * @param year l'anno di riferimento
   * @return la data in cui cade la pasqua.
   */
  public static final LocalDate findEaster(int year) {
    if (year <= 1582) {
      throw new IllegalArgumentException("Algorithm invalid before April 1583");
    }

    int golden = (year % 19) + 1; /* E1: metonic cycle */
    int century = (year / 100) + 1; /* E2: e.g. 1984 was in 20th C */
    int z;
    int x = (3 * century / 4) - 12; /* E3: leap year correction */
    z = ((8 * century + 5) / 25) - 5; /* E3: sync with moon's orbit */
    int d;
    d = (5 * year / 4) - x - 10;
    int epact;
    epact = (11 * golden + 20 + z - x) % 30; /* E5: epact */
    if ((epact == 25 && golden > 11) || epact == 24) {
      epact++;
    }
    int n;
    n = 44 - epact;
    n += 30 * (n < 21 ? 1 : 0); /* E6: */
    n += 7 - ((d + n) % 7);

    LocalDate easter = null;
    if (n > 31) /* E7: */ {
      easter = LocalDate.of(year, 4, n - 31);

      return easter; /* April */
    } else {
      easter = LocalDate.of(year, 3, n);

      return easter; /* March */
    }
  }

  /**
   * Festività generale.
   *
   * @param officePatron giorno del patrono
   * @param date data da verificare
   * @return esito
   */
  public static boolean isGeneralHoliday(
      final Optional<MonthDay> officePatron, final LocalDate date) {

    LocalDate easter = findEaster(date.getYear());
    LocalDate easterMonday = easter.plusDays(1);
    if (date.getDayOfMonth() == easter.getDayOfMonth()
            && date.getMonthValue() == easter.getMonthValue()) {
      return true;
    }
    if (date.getDayOfMonth() == easterMonday.getDayOfMonth()
            && date.getMonthValue() == easterMonday.getMonthValue()) {
      return true;
    }
    if ((date.getMonthValue() == 12) && (date.getDayOfMonth() == 25)) {
      return true;
    }
    if ((date.getMonthValue() == 12) && (date.getDayOfMonth() == 26)) {
      return true;
    }
    if ((date.getMonthValue() == 12) && (date.getDayOfMonth() == 8)) {
      return true;
    }
    if ((date.getMonthValue() == 6) && (date.getDayOfMonth() == 2)) {
      return true;
    }
    if ((date.getMonthValue() == 4) && (date.getDayOfMonth() == 25)) {
      return true;
    }
    if ((date.getMonthValue() == 5) && (date.getDayOfMonth() == 1)) {
      return true;
    }
    if ((date.getMonthValue() == 8) && (date.getDayOfMonth() == 15)) {
      return true;
    }
    if ((date.getMonthValue() == 1) && (date.getDayOfMonth() == 1)) {
      return true;
    }
    if ((date.getMonthValue() == 1) && (date.getDayOfMonth() == 6)) {
      return true;
    }
    if ((date.getMonthValue() == 11) && (date.getDayOfMonth() == 1)) {
      return true;
    }

    if (officePatron.isPresent()) {

      return (date.getMonthValue() == officePatron.get().getMonthValue()
              && date.getDayOfMonth() == officePatron.get().getDayOfMonth());
    }

    /*
     * ricorrenza centocinquantenario dell'unità d'Italia.
     */
    if (date.isEqual(LocalDate.of(2011, 3, 17))) {
      return true;
    }

    return false;
  }

  /**
   * Metodo che ritorna la lista dei giorni contenuti nell'intervallo begin-end.
   *
   * @param begin data iniziale.
   * @param end   data finale
   * @return lista di tutti i giorni fisici contenuti nell'intervallo [begin,end] estremi compresi,
   *     escluse le general holiday
   */
  public static List<LocalDate> getGeneralWorkingDays(final LocalDate begin, final LocalDate end) {

    LocalDate day = begin;
    List<LocalDate> generalWorkingDays = new ArrayList<LocalDate>();
    while (!day.isAfter(end)) {
      if (!DateUtility.isGeneralHoliday(Optional.<MonthDay>empty(), day)) {
        generalWorkingDays.add(day);
      }
      day = day.plusDays(1);
    }
    return generalWorkingDays;
  }


  /**
   * Se la data è contenuta nell'intervallo.
   *
   * @param date     data
   * @param interval intervallo
   * @return true se la data ricade nell'intervallo estremi compresi
   */
  public static boolean isDateIntoInterval(final LocalDate date, final DateInterval interval) {
    
    if (interval == null) {
      return false;
    }
    LocalDate dateToCheck = date;
    if (dateToCheck == null) {
      dateToCheck = MAX_DATE;
    }

    if (dateToCheck.isBefore(interval.getBegin()) || dateToCheck.isAfter(interval.getEnd())) {
      return false;
    }
    return true;
  }

  /**
   * L'intervallo di date contenente l'intersezione fra inter1 e inter2.
   *
   * @param inter1 primo intervallo
   * @param inter2 secondo intervallo
   * @return l'intervallo contenente l'intersezione fra inter1 e inter2, null in caso di
   *         intersezione vuota.
   */
  public static DateInterval intervalIntersection(final DateInterval inter1, 
      final DateInterval inter2) {
  
    if (inter1 == null || inter2 == null) {
      return null;
    }
    
    // un intervallo contenuto nell'altro
    if (isIntervalIntoAnother(inter1, inter2)) {
      return new DateInterval(inter1.getBegin(), inter1.getEnd());
    }

    if (isIntervalIntoAnother(inter2, inter1)) {
      return new DateInterval(inter2.getBegin(), inter2.getEnd());
    }

    DateInterval copy1 = new DateInterval(inter1.getBegin(), inter1.getEnd());
    DateInterval copy2 = new DateInterval(inter2.getBegin(), inter2.getEnd());

    // ordino
    if (!inter1.getBegin().isBefore(inter2.getBegin())) {
      DateInterval aux = new DateInterval(inter1.getBegin(), inter1.getEnd());
      copy1 = inter2;
      copy2 = aux;
    }
 
    // fine di inter1 si interseca con inizio di inter2
    if (copy1.getEnd().isBefore(copy2.getBegin())) {
      return null;
    } else {
      return new DateInterval(copy2.getBegin(), copy1.getEnd());
    }
  }
    
  
  /**
   * L'intervallo orario contenente l'intersezione fra inter1 e inter2.
   *
   * @param inter1 primo intervallo
   * @param inter2 secondo intervallo
   * @return l'intervallo contenente l'intersezione fra inter1 e inter2, null in caso di
   *         intersezione vuota.
   */
  public static TimeInterval intervalIntersection(final TimeInterval inter1, 
      final TimeInterval inter2) {
  
    if (inter1 == null || inter2 == null) {
      return null;
    }
    
    // un intervallo contenuto nell'altro
    if (isIntervalIntoAnother(inter1, inter2)) {
      return new TimeInterval(inter1.getBegin(), inter1.getEnd());
    }

    if (isIntervalIntoAnother(inter2, inter1)) {
      return new TimeInterval(inter2.getBegin(), inter2.getEnd());
    }

    TimeInterval copy1 = new TimeInterval(inter1.getBegin(), inter1.getEnd());
    TimeInterval copy2 = new TimeInterval(inter2.getBegin(), inter2.getEnd());

    // ordino
    if (!inter1.getBegin().isBefore(inter2.getBegin())) {
      TimeInterval aux = new TimeInterval(inter1.getBegin(), inter1.getEnd());
      copy1 = inter2;
      copy2 = aux;
    }
 
    // fine di inter1 si interseca con inizio di inter2
    if (copy1.getEnd().isBefore(copy2.getBegin())) {
      return null;
    } else {
      return new TimeInterval(copy2.getBegin(), copy1.getEnd());
    }
  }

  /**
   * Conta il numero di giorni appartenenti all'intervallo estremi compresi.
   *
   * @param inter l'intervallo
   * @return numero di giorni
   */
  public static long daysInInterval(final DateInterval inter) {
    //Attenzione utilizzare sempre ChronoUnit.DAYS.betwee non Period.between,
    //vedi https://www.baeldung.com/java-date-difference
    long days = ChronoUnit.DAYS.between(inter.getBegin(), inter.getEnd()) + 1;

    //controllo compatibilità con vecchio algoritmo.
    if (inter.getBegin().getYear() == inter.getEnd().getYear()) {
      int oldDays = inter.getEnd().getDayOfYear() - inter.getBegin().getDayOfYear() + 1;
      Preconditions.checkState(days == oldDays);
    }

    return days;

  }

  //FIXME: da implementare prima del passaggio a spring boot.
  //  /**
  //   * Conta il numero di mesi appartenenti all'intervallo, estremi compresi.
  //   *
  //   * @param inter intervallo
  //   * @return numero di mesi
  //   */
  //  public static int monthsInInterval(final DateInterval inter) {
  //    return Months.monthsBetween(inter.getBegin(), inter.getEnd()).getMonths() + 1;
  //  }

  /**
   * Se il primo intervallo di date è contenuto nel secondo intervallo.
   *
   * @param first  il primo intervallo
   * @param second il secondo intervallo
   * @return se il primo intervallo di date è contenuto nel secondo intervallo.
   */
  public static boolean isIntervalIntoAnother(final DateInterval first, final DateInterval second) {

    if (first.getBegin().isBefore(second.getBegin()) 
        || first.getEnd().isAfter(second.getEnd())) {
      return false;
    }
    return true;
  }

  /**
   * Se il primo intervallo di orari è contenuto nel secondo intervallo.
   *
   * @param first  il primo intervallo
   * @param second il secondo intervallo
   * @return se il primo intervallo di orari è contenuto nel secondo intervallo.
   */
  public static boolean isIntervalIntoAnother(final TimeInterval first, final TimeInterval second) {

    if (first.getBegin().isBefore(second.getBegin()) 
        || first.getEnd().isAfter(second.getEnd())) {
      return false;
    }
    return true;
  }
  
  /**
   * Se i due inervalli coincidono.
   *
   * @param first first
   * @param second second
   * @return esito
   */
  public static boolean areIntervalsEquals(final DateInterval first, final DateInterval second) {
    if (first.getBegin().isEqual(second.getBegin()) 
        && first.getEnd().isEqual(second.getEnd())) {
      return true;
    }
    return false;
  }

  /**
   * La data massima che equivale a infinito.
   *
   * @return la data infinito
   */
  public static LocalDate setInfinity() {
    return MAX_DATE;
  }


  /**
   * Controlla se la data passata come parametro è molto lontana nel tempo.
   *
   * @param date la data da confrontare
   * @return se la data è molto molto lontana...
   */
  public static boolean isInfinity(final LocalDate date) {
    return date.equals(MAX_DATE);
  }

  /**
   * L'intervallo dell'anno.
   *
   * @param year anno
   * @return l'intervallo
   */
  public static DateInterval getYearInterval(int year) {
    return new DateInterval(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
  }

  /**
   * L'intervallo del mese.
   *
   * @param year anno
   * @param month mese
   * @return intervallo
   */
  public static DateInterval getMonthInterval(int year, int month) {
    return new DateInterval(LocalDate.of(year, month, 1), 
        endOfMonth(LocalDate.of(year, month, 1)));
  }

  /**
   * Trasforma in nome il numero del mese passato come parametro.
   *
   * @param monthNumber mese da formattare.
   * @return il nome del mese con valore monthNumber, null in caso di argomento non valido.
   */
  public static String fromIntToStringMonth(final Integer monthNumber) {
    LocalDate date = LocalDate.of(2022, monthNumber, 1);
    return date.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALY);
    //return date.monthOfYear().getAsText();
  }
  
  /**
   * Trasforma in stringa il numero del mese aggiungendo '""' davanti.
   *
   * @param month il mese da formattare
   * @return il mese se maggiore di 10, con lo 0 davanti se minore di 10.
   */
  public static String checkMonth(int month) {
    if (month < 10) {
      return "0" + month;
    } else {
      return "" + month;
    }
  }

  /**
   * Ritorna la stringa nel formato HH:MM della quantità di minuti passata come parametro.
   *
   * @param minute minuti da formattare.
   * @return stringa contente la formattazione -?HH:MM
   */
  public static String fromMinuteToHourMinute(final int minute) {
    if (minute == 0) {
      return "00:00";
    }
    String string = "";
    int positiveMinute = minute;
    if (minute < 0) {
      string = string + "-";
      positiveMinute = minute * -1;
    }
    int hour = positiveMinute / MINUTE_IN_HOUR;
    int min = positiveMinute % MINUTE_IN_HOUR;

    if (hour < 10) {
      string = string + "0" + hour;
    } else {
      string = string + hour;
    }
    string = string + ":";
    if (min < 10) {
      string = string + "0" + min;
    } else {
      string = string + min;
    }
    return string;
  }

  //FIXME: da implementare prima del passaggio a Spring boot
  //  /**
  //   * Parser della stringa contenente la data in un oggetto LocalDate.
  //   *
  //   * @param date data.
  //   * @param pattern : default dd/MM
  //   * @return effettua il parsing di una stringa che contiene solo giorno e Mese
  //   */
  //  public static LocalDate dayMonth(final String date, final Optional<String> pattern) {
  //
  //    DateTimeFormatter dtf;
  //    if (pattern.isPresent()) {
  //      dtf = DateTimeFormat.forPattern(pattern.get());
  //    } else {
  //      dtf = DateTimeFormat.forPattern("dd/MM");
  //    }
  //    return LocalDate.parse(date, dtf);
  //  }

  /**
   * Ritorna la data del primo giorno del mese.
   *
   * @param yearMonth il mese da considerare.
   * @return il primo giorno del mese da considerare formato LocalDate.
   */
  public static LocalDate getMonthFirstDay(final YearMonth yearMonth) {
    return LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1);
  }

  /**
   * Ritorna la data dell'ultimo giorno del mese.
   *
   * @param yearMonth il mese da considerare.
   * @return l'ultimo giorno del mese da considerare formato LocalDate.
   */
  public static LocalDate getMonthLastDay(final YearMonth yearMonth) {
    return endOfMonth(LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1));
  }

  /**
   * Ritorna la quantità di minuti trascorsi dall'inizio del giorno all'ora.
   *
   * @param time ora.
   * @return il numero di minuti trascorsi dall'inizio del giorno all'ora.
   */
  public static int toMinute(final LocalDateTime time) {
    return toMinute(time.toLocalTime());
  }
  
  
  /**
   * Il tempo dalla mezzanotte.
   *
   * @param time orario
   * @return tempo
   */
  public static int toMinute(final LocalTime time) {
    int dateToMinute = 0;
    if (time != null) {
      int hour = time.get(ChronoField.CLOCK_HOUR_OF_DAY);
      int minute =  time.get(ChronoField.MINUTE_OF_HOUR);
      dateToMinute = (MINUTE_IN_HOUR * hour) + minute;
    }
    return dateToMinute;
  }

  /**
   * Ritorna la differenza in minuti tra due orari.
   *
   * @param begin orario di ingresso.
   * @param end   orario di uscita.
   * @return minuti lavorati.
   */
  public static Integer getDifferenceBetweenLocalTime(final LocalTime begin, final LocalTime end) {

    int timeToMinute = 0;
    if (end != null && begin != null) {
      int hourBegin = begin.getHour();
      int minuteBegin = begin.getMinute();
      int hourEnd = end.getHour();
      int minuteEnd = end.getMinute();
      timeToMinute =
              ((MINUTE_IN_HOUR * hourEnd + minuteEnd) - (MINUTE_IN_HOUR * hourBegin + minuteBegin));
    }

    return timeToMinute;
  }

}