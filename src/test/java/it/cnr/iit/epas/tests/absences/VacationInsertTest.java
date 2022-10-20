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
import java.util.Optional;
import com.google.common.collect.ImmutableSet;
import javax.inject.Inject;
import javax.transaction.Transactional;
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
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import it.cnr.iit.epas.tests.db.h2support.base.H2AbsenceSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class VacationInsertTest {
  
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
    
  /**
   * Issue #258.
   */
  @Test
  @Transactional
  public void bucciCase() {
    
    absenceService.enumInitializator();
    
    Person person = h2Examples.normalEmployee(LocalDate.of(2014, 3, 17),
        Optional.of(LocalDate.of(2019, 3, 16)));
    
    //le ferie del 2015 utilizzate
    h2AbsenceSupport.multipleAllDayInstances(person, DefaultAbsenceType.A_31, 
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
    
    h2AbsenceSupport.multipleAllDayInstances(person, DefaultAbsenceType.A_32, 
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
    
    GroupAbsenceType vacationGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();
    
    JustifiedType allDay = absenceComponentDao
        .getOrBuildJustifiedType(JustifiedTypeName.all_day);
    
    AbsenceType absenceType = absenceComponentDao
        .absenceTypeByCode(DefaultAbsenceType.A_32.getCode()).get();
    
    LocalDate today = LocalDate.of(2015, 8, 24);
    
    InsertReport insertReport = absenceService.insert(person, vacationGroup, today, null, 
        absenceType, allDay, null, null, false, null);
    
    assertEquals(insertReport.howManySuccess(), 1);
  }
  
}
