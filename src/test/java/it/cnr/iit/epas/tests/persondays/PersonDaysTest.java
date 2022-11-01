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
package it.cnr.iit.epas.tests.persondays;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.manager.PersonDayManager;
import it.cnr.iit.epas.manager.configurations.EpasParam.EpasParamValueType.LocalTimeInterval;
import it.cnr.iit.epas.manager.services.PairStamping;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.Stamping;
import it.cnr.iit.epas.models.Stamping.WayType;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PersonDaysTest {

  public static LocalTime startLunch = LocalTime.of(1, 0, 0);
  public static LocalTime endLunch = LocalTime.of(23, 0, 0);

  public static LocalTime startWork = LocalTime.of(0, 0, 0);
  public static LocalTime endWork = LocalTime.of(23, 59, 0);

  public static LocalDate first = LocalDate.of(2016, 1, 2);
  public static LocalDate second = LocalDate.of(2016, 1, 3);

  public static StampTypes lunchST = StampTypes.PAUSA_PRANZO;
  public static StampTypes serviceST = StampTypes.MOTIVI_DI_SERVIZIO;

  @Inject  
  PersonDayManager personDayManager;

  /**
   * Test su un giorno Normale.
   */
  @Test
  public void test() {
    val person = new Person();

    PersonDay personDay = new PersonDay(person, second);

    List<Stamping> stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 9, 30, WayType.in, null, null));
    stampings.add(stampings(personDay, 16, 30, WayType.out, null, null));

    personDay.setStampings(stampings);

    PersonDay previousForProgressive = new PersonDay(person, first, 0, 0, 60);

    personDayManager.updateTimeAtWork(personDay, normalDay(), false, 
        startLunch, endLunch, startWork, endWork, Optional.empty());
    personDayManager.updateDifference(personDay, normalDay(), false,
        startLunch, endLunch, startWork, endWork, Optional.empty());
    personDayManager.updateProgressive(personDay, Optional.ofNullable(previousForProgressive));

    org.assertj.core.api.Assertions.assertThat(
        personDay.getTimeAtWork()).isEqualTo(390);   //6:30 ore
    org.assertj.core.api.Assertions.assertThat(
        personDay.getStampingsTime()).isEqualTo(420); //7:00 ore
    org.assertj.core.api.Assertions.assertThat(
        personDay.getDecurtedMeal()).isEqualTo(30);      //30 minuti
    org.assertj.core.api.Assertions.assertThat(personDay.getDifference()).isEqualTo(-42);
    org.assertj.core.api.Assertions.assertThat(personDay.getProgressive()).isEqualTo(18);
    org.assertj.core.api.Assertions.assertThat(personDay.isTicketAvailable()).isEqualTo(true);
    
  }

  /**
   * Quando la pausa pranzo contiene interamente la fascia pranzo dell'istituto va conteggiata.
   */
  @Test
  public void tagliaferriIsHungry() {
    val person = new Person();
    PersonDay personDay = new PersonDay(person, second);
    List<Stamping> stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 8, 30, WayType.in, null, null));
    stampings.add(stampings(personDay, 11, 30, WayType.out, null, null));

    stampings.add(stampings(personDay, 15, 30, WayType.in, null, null));
    stampings.add(stampings(personDay, 19, 30, WayType.out, null, null));

    personDay.setStampings(stampings);

    personDayManager.updateTimeAtWork(personDay, normalDay(), false, 
        startLunch, endLunch, startWork, endWork, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(
        personDay.getTimeAtWork()).isEqualTo(420);   //7:00 ore
    org.assertj.core.api.Assertions.assertThat(
        personDay.getStampingsTime()).isEqualTo(420); //7:00 ore     
    org.assertj.core.api.Assertions.assertThat(
        personDay.getDecurtedMeal()).isEqualTo(0);      //00 minuti
    org.assertj.core.api.Assertions.assertThat(
        personDay.isTicketAvailable()).isEqualTo(true);
    
  }

  /**
   * Quando una persona dispone di una coppia di timbrature valide <br> 
   * (cioè che contribuiscono a calcolare il tempo a lavoro)<br> 
   * in cui almeno una delle due timbrature è taggata con 
   * StampTypes.MOTIVI_DI_SERVIZIO_FUORI_SEDE... <br>
   * <u>Allora:</u><br>
   * All'interno di tale coppia possono esserci tutte
   * e sole timbrature di servizio. 
   * L'ordine delle timbrature di servizio in questo caso non è più vincolante. 
   * Esse contribuiscono esclusivamente a segnalare la presenza in sede o meno della persona. 
   */
  @Test
  public void mazzantiIsInServiceOutSite() {

    //coppia valida con dentro una timbratura di servizio ok
    PersonDay personDay = new PersonDay(null, second);
    List<Stamping> stamps = Lists.newArrayList();
    stamps.add(stampings(personDay, 8, 30, WayType.in, StampTypes.LAVORO_FUORI_SEDE, null));
    stamps.add(stampings(personDay, 15, 30, WayType.in, StampTypes.MOTIVI_DI_SERVIZIO, null));
    stamps.add(stampings(personDay, 19, 30, WayType.out, null, null));
    personDayManager.setValidPairStampings(personDay.getStampings());
    org.assertj.core.api.Assertions.assertThat(personDayManager.allValidStampings(personDay));

    //coppia valida con dentro timbrature di servizio con ordine sparso ok 
    personDay = new PersonDay(null, second);
    stamps = Lists.newArrayList();
    stamps.add(stampings(personDay, 8, 30, WayType.in, StampTypes.LAVORO_FUORI_SEDE, null));
    stamps.add(stampings(personDay, 14, 30, WayType.out, StampTypes.MOTIVI_DI_SERVIZIO, null));
    stamps.add(stampings(personDay, 15, 30, WayType.in, StampTypes.MOTIVI_DI_SERVIZIO, null));
    stamps.add(stampings(personDay, 16, 30, WayType.in, StampTypes.MOTIVI_DI_SERVIZIO, null));
    stamps.add(stampings(personDay, 19, 30, WayType.out, null, null));
    personDayManager.setValidPairStampings(personDay.getStampings());
    org.assertj.core.api.Assertions.assertThat(personDayManager.allValidStampings(personDay));

    //coppia non valida 
    personDay = new PersonDay(null, second);
    stamps = Lists.newArrayList();
    stamps.add(stampings(personDay, 8, 30, WayType.in, StampTypes.LAVORO_FUORI_SEDE, null));
    stamps.add(stampings(personDay, 15, 30, WayType.in, null, null));
    stamps.add(stampings(personDay, 19, 30, WayType.out, null, null));
    personDayManager.setValidPairStampings(personDay.getStampings());
    org.assertj.core.api.Assertions.assertThat(!personDayManager.allValidStampings(personDay));

  }
  
  @Test
  public void consideredGapLunchPairsOutOfSite() {

    org.assertj.core.api.Assertions.assertThat(
        StampTypes.LAVORO_FUORI_SEDE.isGapLunchPairs()).isEqualTo(true);
    org.assertj.core.api.Assertions.assertThat(
        StampTypes.PAUSA_PRANZO.isGapLunchPairs()).isEqualTo(true);

    val person = new Person();
    PersonDay personDay = new PersonDay(person, second);
    List<Stamping> stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 8, 30, WayType.in, null, null));
    stampings.add(stampings(personDay, 11, 30, WayType.out, null, null));

    stampings.add(stampings(personDay, 15, 30, WayType.in, StampTypes.LAVORO_FUORI_SEDE, null));
    stampings.add(stampings(personDay, 19, 30, WayType.out, null, null));

    personDay.setStampings(stampings);

    personDayManager.updateTimeAtWork(personDay, normalDay(), false, 
        startLunch, endLunch, startWork, endWork, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(
        personDay.getTimeAtWork()).isEqualTo(420);     //7:00 ore
    org.assertj.core.api.Assertions.assertThat(
        personDay.getStampingsTime()).isEqualTo(420);  //7:00 ore     
    org.assertj.core.api.Assertions.assertThat(
        personDay.getDecurtedMeal()).isEqualTo(0);      //00 minuti
    org.assertj.core.api.Assertions.assertThat(
        personDay.isTicketAvailable()).isEqualTo(true);

    // # anche le coppie che hanno due causali diverse ma che hanno il parametro gapLunchPairs true

    personDay = new PersonDay(person, second);
    stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 8, 30, WayType.in, null, null));
    stampings.add(stampings(personDay, 11, 30, WayType.out, StampTypes.PAUSA_PRANZO, null));
        
    stampings.add(stampings(personDay, 15, 30, WayType.in, StampTypes.LAVORO_FUORI_SEDE, null));
    stampings.add(stampings(personDay, 19, 30, WayType.out, null, null));
    
    personDay.setStampings(stampings);

    personDayManager.updateTimeAtWork(personDay, normalDay(), false, 
        startLunch, endLunch, startWork, endWork, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(
        personDay.getTimeAtWork()).isEqualTo(420);     //7:00 ore
    org.assertj.core.api.Assertions.assertThat(
        personDay.getStampingsTime()).isEqualTo(420);  //7:00 ore     
    org.assertj.core.api.Assertions.assertThat(
        personDay.getDecurtedMeal()).isEqualTo(0);      //00 minuti
    org.assertj.core.api.Assertions.assertThat(
        personDay.isTicketAvailable()).isEqualTo(true);

  }

  /**
   * Le pause pranzo da considerare sono tutte quelle che hanno:
   * #1 Uscita pr Ingresso pr
   * Uscita pr Ingresso 
   * Uscita    Ingrssso pr
   * Uscita    Ingresso    (e sono in istituto non di servizio). 
   */
  @Test
  public void consideredGapLunchPairs() { 

    PersonDay personDay = new PersonDay(null, second);

    org.assertj.core.api.Assertions.assertThat(
        lunchST.isGapLunchPairs()).isEqualTo(true);
    org.assertj.core.api.Assertions.assertThat(
        StampTypes.MOTIVI_PERSONALI.isGapLunchPairs()).isEqualTo(false);

    // #1
    List<Stamping> stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 8, 00, WayType.in, null, null));
    stampings.add(stampings(personDay, 13, 00, WayType.out, lunchST, null));
    stampings.add(stampings(personDay, 14, 00, WayType.in, lunchST, null));
    stampings.add(stampings(personDay, 17, 00, WayType.out, null, null));
    
    personDay.setStampings(stampings);
    List<PairStamping> gapLunchPair = 
        personDayManager.getGapLunchPairs(personDay, startLunch, endLunch, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(gapLunchPair.size()).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(gapLunchPair.get(0).timeInPair).isEqualTo(60);

    stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 13, 00, WayType.out, lunchST, null));
    stampings.add(stampings(personDay, 14, 00, WayType.in, lunchST, null));
    stampings.add(stampings(personDay, 17, 00, WayType.out, null, null));
    personDay.setStampings(stampings);

    List<PairStamping> validPairs = personDayManager.getValidPairStampings(personDay.getStampings());

    gapLunchPair = personDayManager
        .getGapLunchPairs(personDay, startLunch, endLunch, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(validPairs.size()).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(validPairs.get(0).timeInPair).isEqualTo(180);
    org.assertj.core.api.Assertions.assertThat(gapLunchPair.size()).isEqualTo(0);


    // #2
    stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 8, 00, WayType.in, null, null));
    stampings.add(stampings(personDay, 13, 00, WayType.out, lunchST, null));
    stampings.add(stampings(personDay, 14, 00, WayType.in, null, null));
    stampings.add(stampings(personDay, 17, 00, WayType.out, null, null));
    personDay.setStampings(stampings);
    gapLunchPair = personDayManager
        .getGapLunchPairs(personDay, startLunch, endLunch, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(gapLunchPair.size()).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(gapLunchPair.get(0).timeInPair).isEqualTo(60);

    stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 13, 00, WayType.out, lunchST, null));
    stampings.add(stampings(personDay, 14, 00, WayType.in, null, null));
    stampings.add(stampings(personDay, 17, 00, WayType.out, null, null));
    personDay.setStampings(stampings);

    validPairs = personDayManager.getValidPairStampings(personDay.getStampings());
    gapLunchPair = personDayManager
        .getGapLunchPairs(personDay, startLunch, endLunch, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(validPairs.size()).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(validPairs.get(0).timeInPair).isEqualTo(180);
    org.assertj.core.api.Assertions.assertThat(gapLunchPair.size()).isEqualTo(0);

    // #3
    stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 8, 00, WayType.in, null, null));
    stampings.add(stampings(personDay, 13, 00, WayType.out, null, null));
    stampings.add(stampings(personDay, 14, 00, WayType.in, lunchST, null));
    stampings.add(stampings(personDay, 17, 00, WayType.out, null, null));
    personDay.setStampings(stampings);

    gapLunchPair = personDayManager
        .getGapLunchPairs(personDay, startLunch, endLunch, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(gapLunchPair.size()).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(gapLunchPair.get(0).timeInPair).isEqualTo(60);

    stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 13, 00, WayType.out, null, null));
    stampings.add(stampings(personDay, 14, 00, WayType.in, lunchST, null));
    stampings.add(stampings(personDay, 17, 00, WayType.out, null, null));
    personDay.setStampings(stampings);

    validPairs = personDayManager.getValidPairStampings(personDay.getStampings());
    gapLunchPair = personDayManager
        .getGapLunchPairs(personDay, startLunch, endLunch, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(validPairs.size()).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(validPairs.get(0).timeInPair).isEqualTo(180);
    org.assertj.core.api.Assertions.assertThat(gapLunchPair.size()).isEqualTo(0);
    
    // # L'ingresso post pranzo deve essere coerente.
    stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 8, 00, WayType.in, null, null));
    stampings.add(stampings(personDay, 13, 00, WayType.out, lunchST, null));
    stampings.add(stampings(personDay, 14, 00, WayType.in, StampTypes.MOTIVI_PERSONALI, null));
    stampings.add(stampings(personDay, 17, 00, WayType.out, null, null));
    personDay.setStampings(stampings);
    
    validPairs = personDayManager.getValidPairStampings(personDay.getStampings());
    gapLunchPair = personDayManager
        .getGapLunchPairs(personDay, startLunch, endLunch, Optional.empty());
    
    org.assertj.core.api.Assertions.assertThat(validPairs.size()).isEqualTo(2);
    org.assertj.core.api.Assertions.assertThat(gapLunchPair.size()).isEqualTo(0);
       
    stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 8, 00, WayType.in, null, null));
    stampings.add(stampings(personDay, 12, 30, WayType.out, lunchST, null));
    stampings.add(stampings(personDay, 13, 00, WayType.in, StampTypes.MOTIVI_PERSONALI, null));
    stampings.add(stampings(personDay, 13, 30, WayType.out, StampTypes.MOTIVI_PERSONALI, null));
    stampings.add(stampings(personDay, 14, 00, WayType.in, null, null));
    stampings.add(stampings(personDay, 17, 00, WayType.out, null, null));
    personDay.setStampings(stampings);

    // # Il test che secondo Daniele fallisce
    LocalTime startLunch = LocalTime.of(12, 0, 0);
    LocalTime endLunch = LocalTime.of(15, 0, 0);

    validPairs = personDayManager.getValidPairStampings(personDay.getStampings());
    gapLunchPair = personDayManager
        .getGapLunchPairs(personDay, startLunch, endLunch, Optional.empty());

    org.assertj.core.api.Assertions.assertThat(gapLunchPair.size()).isEqualTo(0);
    
    
  }
  
  /**
   * Il test verifica il funzionamento del meccanismo di stima del tempo al
   * lavoro uscendo in questo momento.
   */
  @Test
  public void estimatedTimeAtWorkToday() {
    
    val person = new Person();
    PersonDay previousForProgressive = new PersonDay(person, first, 0, 0, 60);

    //Caso base una timbratura di ingresso
    PersonDay personDay = new PersonDay(person, second);
    
    List<Stamping> stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 9, 30, WayType.in, null, null));

    LocalDateTime exitingTime = LocalDateTime.of(second.getYear(), second.getMonthValue(), 
        second.getDayOfMonth(), 16, 30);
    //final LocalDateTime time18 = new LocalDateTime(second).withHourOfDay(18);

    personDayManager.queSeraSera(personDay, exitingTime, 
        Optional.ofNullable(previousForProgressive), normalDay(), false,
        new LocalTimeInterval(startLunch, endLunch), new LocalTimeInterval(startWork, endWork));

    org.assertj.core.api.Assertions.assertThat(
        personDay.getTimeAtWork()).isEqualTo(390);   //6:30 ore
    org.assertj.core.api.Assertions.assertThat(
        personDay.getStampingsTime()).isEqualTo(420); //7:00 ore     
    org.assertj.core.api.Assertions.assertThat(
        personDay.getDecurtedMeal()).isEqualTo(30);      //30 minuti
    org.assertj.core.api.Assertions.assertThat(
        personDay.getDifference()).isEqualTo(-42);
    org.assertj.core.api.Assertions.assertThat(
        personDay.getProgressive()).isEqualTo(18);
    org.assertj.core.api.Assertions.assertThat(
        personDay.isTicketAvailable()).isEqualTo(true);

    //Caso con uscita per pranzo
    personDay = new PersonDay(person, second);
    stampings = Lists.newArrayList();
    stampings.add(stampings(personDay, 9, 00, WayType.in, null, null));       //4 ore mattina
    stampings.add(stampings(personDay, 13, 00, WayType.out, null, null));     //pausa pranzo 1 ora
    stampings.add(stampings(personDay, 14, 00, WayType.in, null, null));

    exitingTime = LocalDateTime.of(second.getYear(), second.getMonthValue(),  //4 ore pom. 
        second.getDayOfMonth(), 18, 00);

    LocalTime startLunch12 = LocalTime.of(12, 0, 0);
    LocalTime endLunch15 = LocalTime.of(15, 0, 0);
    personDayManager.queSeraSera(personDay, exitingTime, 
        Optional.ofNullable(previousForProgressive), normalDay(), false,
        new LocalTimeInterval(startLunch12, endLunch15), new LocalTimeInterval(startWork, endWork));

    org.assertj.core.api.Assertions.assertThat(personDay.getTimeAtWork()).isEqualTo(480);   //8 ore
    org.assertj.core.api.Assertions.assertThat(personDay.isTicketAvailable()).isEqualTo(true);

  }

  /**
   * Supporto alla creazione di un WorkingTimeType da non mockare.
   * @return WorkingTimeTypeDay di default (quelle Normale).
   */
  public WorkingTimeTypeDay normalDay() {
    WorkingTimeTypeDay wttd = new WorkingTimeTypeDay();
    wttd.breakTicketTime = 30;
    wttd.mealTicketTime = 360;
    wttd.workingTime = 432;
    wttd.ticketAfternoonThreshold = null;
    wttd.holiday = false;
    return wttd;
  }

  /**
   * Supporto alla creazione di una stamping da non mockare.
   */
  public Stamping stampings(PersonDay personDay, int hour, int minute, 
      WayType way, StampTypes stampType, String stampingZone) {
    LocalDateTime time = LocalDateTime.of(personDay.getDate().getYear(), 
        personDay.getDate().getMonthValue(), personDay.getDate().getDayOfMonth(), hour, minute);
    Stamping stamping = new Stamping(personDay, time);
    stamping.setWay(way);
    stamping.setStampType(stampType);
    stamping.setStampingZone(stampingZone);
    return stamping;
  }

}