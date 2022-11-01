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
package it.cnr.iit.epas.tests.absences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.definitions.DefaultAbsenceType;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import it.cnr.iit.epas.tests.db.h2support.base.H2AbsenceSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class VacationSituationTest {

  public static final LocalDate EXPIRE_DATE_LAST_YEAR = LocalDate.of(2016, 8, 31);
  public static final LocalDate EXPIRE_DATE_CURRENT_YEAR = LocalDate.of(2017, 8, 31);

  @Inject 
  private H2Examples h2Examples;
  @Inject 
  private H2AbsenceSupport h2AbsenceSupport;
  @Inject
  private AbsenceService absenceService;
  @Inject
  private AbsenceComponentDao absenceComponentDao;

  @Test
  @Transactional
  public void vacationsTestBase() {
    
    absenceService.enumInitializator();

    GroupAbsenceType vacationGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();

    final LocalDate today = LocalDate.of(2016, 9, 1);

    //un tempo determinato
    Person person = h2Examples.normalEmployee(LocalDate.of(2009, 2, 01), Optional.empty());

    h2AbsenceSupport.absence(DefaultAbsenceType.A_31, 
        LocalDate.of(2016, 1, 1), Optional.empty(), 0, person);
    h2AbsenceSupport.absence(DefaultAbsenceType.A_37, 
        LocalDate.of(2016, 9, 1), Optional.empty(), 0, person);
    h2AbsenceSupport.absence(DefaultAbsenceType.A_32, 
        LocalDate.of(2016, 9, 12), Optional.empty(), 0, person);
    h2AbsenceSupport.absence(DefaultAbsenceType.A_94, 
        LocalDate.of(2016, 9, 13), Optional.empty(), 0, person);

    VacationSituation vacationSituation = absenceService.buildVacationSituation(
        person.getContracts().get(0), 2016, vacationGroup, Optional.of(today), false);

    assertTrue(vacationSituation.lastYear.expired());
    assertEquals(vacationSituation.lastYear.total(), 28);
    assertEquals(vacationSituation.lastYear.used(), 2);
    assertEquals(vacationSituation.lastYear.usableTotal(), 26);
    assertEquals(vacationSituation.lastYear.usable(), 0);
    
    assertFalse(vacationSituation.currentYear.expired());
    assertEquals(vacationSituation.currentYear.total(), 28);
    assertEquals(vacationSituation.currentYear.used(), 1);
    assertEquals(vacationSituation.currentYear.usableTotal(), 27);
    assertEquals(vacationSituation.currentYear.usable(), 27);
    
    assertFalse(vacationSituation.permissions.expired());
    assertEquals(vacationSituation.permissions.total(), 4);
    assertEquals(vacationSituation.permissions.used(), 1);
    assertEquals(vacationSituation.permissions.usableTotal(), 3);
    assertEquals(vacationSituation.permissions.usable(), 3);

    //un tempo determinato
    Person person2 = h2Examples.normalEmployee(LocalDate.of(2011, 10, 1), 
        Optional.of(LocalDate.of(2016, 10, 1)));

    h2AbsenceSupport.absence(DefaultAbsenceType.A_31, 
        LocalDate.of(2016, 1, 1), Optional.empty(), 0, person2);
    h2AbsenceSupport.absence(DefaultAbsenceType.A_37, 
        LocalDate.of(2016, 9, 1), Optional.empty(), 0, person2);
    h2AbsenceSupport.absence(DefaultAbsenceType.A_32, 
        LocalDate.of(2016, 9, 12), Optional.empty(), 0, person2);
    h2AbsenceSupport.absence(DefaultAbsenceType.A_94, 
        LocalDate.of(2016, 9, 13), Optional.empty(), 0, person2);

    VacationSituation vacationSituation2 = absenceService.buildVacationSituation(
        person2.getContracts().get(0), 2016, vacationGroup, Optional.of(today), false);

    assertTrue(vacationSituation2.lastYear.expired());
    assertEquals(vacationSituation2.lastYear.total(), 28);
    assertEquals(vacationSituation2.lastYear.used(), 2);
    assertEquals(vacationSituation2.lastYear.usableTotal(), 26);
    assertEquals(vacationSituation2.lastYear.usable(), 0);

    assertFalse(vacationSituation2.currentYear.expired());
    assertEquals(vacationSituation2.currentYear.total(), 21);
    assertEquals(vacationSituation2.currentYear.used(), 1);
    assertEquals(vacationSituation2.currentYear.usableTotal(), 20);
    assertEquals(vacationSituation2.currentYear.usable(), 20);   // con la vecchia politica era 17

    assertFalse(vacationSituation2.permissions.expired());
    assertEquals(vacationSituation2.permissions.total(), 3);
    assertEquals(vacationSituation2.permissions.used(), 1);
    assertEquals(vacationSituation2.permissions.usableTotal(), 2);
    assertEquals(vacationSituation2.permissions.usable(), 2);
  }

  /**
   * Si assicura che fino al primo anno di contratto il dipendente può usufruire soltanto delle
   * ferie maturate.
   * Dal secondo anno può usufruire di tutte le ferie.
   */
  @Test
  @Transactional
  public void onlyAccruedUntilFirstContractYear() {
    
    absenceService.enumInitializator();

    GroupAbsenceType vacationGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();

    final LocalDate contractBegin = LocalDate.of(2017, 7, 1);

    Person person = h2Examples.normalEmployee(contractBegin, Optional.empty());

    // riepilogo all'ultimo giorno del primo anno, posso prendere solo quelle maturate
    LocalDate today = contractBegin.plusYears(1).minusDays(1);
    VacationSituation vacationSituation = absenceService.buildVacationSituation(
        person.getContracts().get(0), 2018, vacationGroup, Optional.of(today), false);

    assertEquals(vacationSituation.currentYear.total(), 26);
    assertEquals(vacationSituation.currentYear.usable(), 13);

    // riepilogo al primo giorno del secondo anno, posso prenderle tutte
    today = contractBegin.plusYears(1);
    vacationSituation = absenceService.buildVacationSituation(
        person.getContracts().get(0), 2018, vacationGroup, Optional.of(today), false);

    assertEquals(vacationSituation.currentYear.total(), 26);
    assertEquals(vacationSituation.currentYear.usable(), 26);

  }
  
  /**
   * Cambiando piano ferie nel corso dell'anno 2015 per quell'anno disponeva di soli 25 giorni.
   * Quindi gliene diamo uno in più che viene maturata immediatamente.
   * Il test costruisce il recap al primo giorno del 2015 e dimostra che in quel momento taverniti
   * ha un giorno di ferie immediatamente maturato, e 26 totali (subito prendibili perchè è un tempo
   * indeterminato).
   * Piano Ferie 26+4
   * Intervallo  Dal 01/01/2015 al 29/10/2015
   * Giorni considerati  302
   * Giorni calcolati    21
   * Piano Ferie 28+4
   * Intervallo  Dal 30/10/2015 al 31/12/2015
   * Giorni considerati  63
   * Giorni calcolati    4
   */
  @Test
  @Transactional
  public void theCuriousCaseOfMariaTaverniti() {

    absenceService.enumInitializator();

    GroupAbsenceType vacationGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();

    //un tempo determinato
    Person person = h2Examples.normalEmployee(LocalDate.of(2012, 10, 30), Optional.empty());

    final LocalDate today = LocalDate.of(2015, 1, 1); //recap date

    VacationSituation vacationSituation = absenceService.buildVacationSituation(
        person.getContracts().get(0), 2015, vacationGroup, Optional.of(today), false);

    assertEquals(vacationSituation.currentYear.total(), 26);
    assertEquals(vacationSituation.currentYear.accrued(), 1);
    assertEquals(vacationSituation.currentYear.usableTotal(), 26);

  }

  /**
   * Quando il cambio di piano durante l'anno porta ad avere un numero superiore di ferie
   * rispetto al valore massimo fra i piani ferie.
   * Si adotta l'aggiustamento.
   */
  @Test
  @Transactional
  public void tooLucky() {

    absenceService.enumInitializator();

    GroupAbsenceType vacationGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();

    //un tempo determinato
    Person person = h2Examples.normalEmployee(LocalDate.of(2013, 4, 17), Optional.empty());

    final LocalDate today = LocalDate.of(2016, 1, 1); //recap date

    VacationSituation vacationSituation = absenceService.buildVacationSituation(
        person.getContracts().get(0), 2016, vacationGroup, Optional.of(today), false);

    assertEquals(vacationSituation.currentYear.total(), 28);
    assertEquals(vacationSituation.currentYear.accrued(), 0);
    assertEquals(vacationSituation.currentYear.usableTotal(), 28);

  }

  /**
   * Il source contract va utilizzato in modo appropriato nel caso in cui si debba costruire 
   * il riepilogo per l'anno successivo l'inizializzazione.
   * Esempio con data inizializzazione nel 2016 
   * A) Se voglio costruire il riepilogo dell'anno 2016 il bind è automatico. 
   * ferie da inizializzazione 2015 = contract.getSourceVacationLastYearUsed()      
   * ferie da inizializzazione 2016 = contract.getSourceVacationCurrentYearUsed() 
   * permessi da inizializzazione 2016 = contact.getSourcePermissionCurrentYearUsed()
   * B) Se voglio costruire il riepilogo dell'anno 2017 il bind corretto è 
   * ferie da inizializzazione 2016 = contract.getSourceVacationCurrentYearUsed()
   * (gli altri campi sono inutili)                  
   */
  @Test
  @Transactional
  public void initializationShouldWorksTheNextYear() {

    //Esempio Pinna IMM - Lecce
    
    absenceService.enumInitializator();

    //un tempo determinato
    Person person = h2Examples.normalEmployee(LocalDate.of(2001, 1, 16), Optional.empty());
    Contract contract = person.getContracts().get(0);
    contract.sourceDateVacation = LocalDate.of(2016, 10, 31);
    contract.sourceVacationLastYearUsed = 28;
    contract.sourceVacationCurrentYearUsed = 5;
    contract.sourcePermissionUsed = 4;

    GroupAbsenceType vacationGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();
    
    VacationSituation vacationSituation = absenceService.buildVacationSituation(
        contract, 2016, vacationGroup, Optional.of(LocalDate.of(2016, 1, 1)), false);

    assertEquals(vacationSituation.currentYear.usable(), 23);

    VacationSituation vacationSituation2 = absenceService.buildVacationSituation(
        contract, 2017, vacationGroup, Optional.of(LocalDate.of(2017, 1, 1)), false);

    assertEquals(vacationSituation2.lastYear.usable(), 23);
    
  }

}