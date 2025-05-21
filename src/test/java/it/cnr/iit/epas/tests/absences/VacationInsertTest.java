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

package it.cnr.iit.epas.tests.absences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableSet;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.AbsenceService.InsertReport;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import it.cnr.iit.epas.models.absences.definitions.DefaultAbsenceType;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import it.cnr.iit.epas.models.enumerate.VacationCode;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import it.cnr.iit.epas.tests.db.h2support.base.H2AbsenceSupport;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import javax.inject.Inject;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest
class VacationInsertTest {
  
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
  public void contractVacationPeriods() {
    absenceService.enumInitializator();

    Person person = h2Examples.normalEmployee(LocalDate.of(2014, 3, 17),
        Optional.of(LocalDate.of(2019, 3, 16)));
    
    assertEquals(person.getContracts().size(), 1);
    val contract = person.getContracts().get(0);
    assertEquals(contract.getVacationPeriods().size(), 2);
    val vacationPeriod26 = contract.getVacationPeriods().get(0);
    assertNotNull(vacationPeriod26);
    assertEquals(VacationCode.CODE_26_4, vacationPeriod26.getVacationCode());

    val vacationPeriod28 = contract.getVacationPeriods().get(1);
    assertNotNull(vacationPeriod28);
    assertEquals(VacationCode.CODE_28_4, vacationPeriod28.getVacationCode());
    val vacationPeriods = contract.getExtendedVacationPeriods();
    assertNotNull(vacationPeriods);

  }

  /**
   * Issue #258.
   */
  @Test
  public void bucciCase() {
    
    absenceService.enumInitializator();
    
    Person person = h2Examples.normalEmployee(LocalDate.of(2014, 3, 17),
        Optional.of(LocalDate.of(2019, 3, 16)));
    
    //le ferie del 2015 utilizzate
    val absences2016 = h2AbsenceSupport.multipleAllDayInstances(person, DefaultAbsenceType.A_31, 
        ImmutableSet.of(
            LocalDate.of(2016, 1, 4),
            LocalDate.of(2016, 1, 5),
            LocalDate.of(2016, 3, 24),
            LocalDate.of(2016, 3, 25),
            LocalDate.of(2016, 3, 29),
            LocalDate.of(2016, 5, 11),
            LocalDate.of(2016, 7, 13),
            LocalDate.of(2016, 7, 25),
            LocalDate.of(2016, 7, 26),
            LocalDate.of(2016, 7, 27)
            ));
    assertEquals(10, absences2016.size());
    
    val absences2015 = h2AbsenceSupport.multipleAllDayInstances(person, DefaultAbsenceType.A_32, 
        ImmutableSet.of(
            LocalDate.of(2015, 7, 29),
            LocalDate.of(2015, 7, 30),
            LocalDate.of(2015, 7, 31),
            LocalDate.of(2015, 8, 3),
            LocalDate.of(2015, 8, 4),
            LocalDate.of(2015, 8, 5),
            LocalDate.of(2015, 8, 6),
            LocalDate.of(2015, 8, 7),
            LocalDate.of(2015, 8, 26),
            LocalDate.of(2015, 8, 27),
            LocalDate.of(2015, 8, 28),
            LocalDate.of(2015, 8, 31),
            LocalDate.of(2015, 12, 24),
            LocalDate.of(2015, 12, 30),
            LocalDate.of(2015, 12, 31)
            ));
    assertEquals(15, absences2015.size());

    GroupAbsenceType vacationGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();
    assertNotNull(vacationGroup);

    JustifiedType allDay = absenceComponentDao
        .getOrBuildJustifiedType(JustifiedTypeName.all_day);
    assertNotNull(allDay);
    assertEquals(allDay.getName(), JustifiedTypeName.all_day);

    AbsenceType absenceType = absenceComponentDao
        .absenceTypeByCode(DefaultAbsenceType.A_32.getCode()).get();
    assertNotNull(absenceType);

    LocalDate today = LocalDate.of(2015, 8, 24);
    
    InsertReport insertReport = absenceService.insert(person, vacationGroup, today, null, 
        absenceType, allDay, null, null, false, null);
    
    assertEquals(insertReport.howManySuccess(), 1);
  }
  
}