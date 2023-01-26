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

package it.cnr.iit.epas.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.QualificationDao;
import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.val;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test elementari sul salvataggio di alcune entity di base
 * e sull'inizializzazione degli entity per la gestione
 * delle assenze.
 *
 * @author Cristian Lucchesi
 */
@Transactional
@SpringBootTest
class EntityAndEnunInitializationTests {

  @Inject
  private PersonDao personDao;
  @Inject
  private UserDao userDao;
  @Inject
  private OfficeDao officeDao;
  @Inject
  private QualificationDao qualificationDao;
  @Inject
  private WrapperFactory wrapperFactory;
  @Inject
  private H2Examples examples;
  @Inject
  private AbsenceService absenceService;

  @Order(1)
  @Test
  void buildOffice() {
    String name = UUID.randomUUID().toString();
    Office office = new Office();
    office.setName(name);
    office.setBeginDate(LocalDate.now());
    office.setCodeId(name);
    office.setCode(name);
    officeDao.persist(office);
    assertNotNull(office.getId());
  }
  
  @Order(2)
  @Test
  void buildPerson() {
    String username = UUID.randomUUID().toString();
    User user = new User();
    user.setUsername(username);
    user.setPassword("UnaPasswordQualsiasi");
    userDao.persist(user);
    Person person = new Person();
    person.setName("Name " + username);
    person.setSurname("Surname " + username);
    person.setEmail(String.format("%s@example.com", UUID.randomUUID()));
    person.setBeginDate(LocalDate.now());
    person.setUser(user);
    person.setOffice(officeDao.allEnabledOffices().get(0));
    person.setQualification(
        qualificationDao.findById(H2Examples.DEFAULT_PERSON_QUALIFICATION).get());
    personDao.persist(person);
    assertNotNull(person.getId());
  }

  @Order(3)
  @Test
  void initializeEpasEnums() {
    absenceService.enumInitializator();
    val person = examples.normalEmployee(LocalDate.now(), Optional.empty());
    assertNotNull(person.getId()); 
  }

  @Test
  void currentContract() {
    val person = examples.normalEmployee(LocalDate.now(), Optional.empty());
    assertEquals(1, person.getContracts().size());
    assertTrue(wrapperFactory.create(person).getCurrentContract().isPresent());
  }
}