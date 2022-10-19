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
package it.cnr.iit.epas.db.h2support;

import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.QualificationDao;
import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.db.h2support.base.H2WorkingTimeTypeSupport;
import it.cnr.iit.epas.db.h2support.base.WorkingTimeTypeDefinitions.WorkingDefinition;
import it.cnr.iit.epas.manager.ContractManager;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.WorkingTimeType;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.springframework.stereotype.Component;

/**
 * Costruzione rapida di entity per test standard.
 *
 * @author Alessandro Martelli
 * @author Cristian Lucchesi
 */
@Component
public class H2Examples {

  public static final long DEFAULT_PERSON_QUALIFICATION = 4L;
  
  private final H2WorkingTimeTypeSupport h2WorkingTimeTypeSupport;
  private final ConfigurationManager configurationManager;
  private final ContractManager contractManager;
  private final UserDao userDao;
  private final PersonDao personDao;
  private final OfficeDao officeDao;
  private final QualificationDao qualificationDao;
  private final ContractDao contractDao;

  /**
   * Injection. 
   */
  @Inject
  public H2Examples(H2WorkingTimeTypeSupport h2WorkingTimeTypeSupport, 
      ConfigurationManager configurationManager, ContractManager contractManager,
      UserDao userDao, PersonDao personDao, OfficeDao officeDao,
      QualificationDao qualificationDao, ContractDao contractDao) {
    this.h2WorkingTimeTypeSupport = h2WorkingTimeTypeSupport;
    this.configurationManager = configurationManager;
    this.contractManager = contractManager;
    this.userDao = userDao;
    this.personDao = personDao;
    this.officeDao = officeDao;
    this.qualificationDao = qualificationDao;
    this.contractDao = contractDao;
  }

  /**
   * Costruisce e persiste il contratto.
   *
   * @param person person
   * @param beginDate data inizio
   * @param endDate data fine
   * @param endContract terminazione
   * @return persisted entity
   */
  @Transactional
  public Contract buildContract(Person person, LocalDate beginDate, Optional<LocalDate> endDate, 
      Optional<LocalDate> endContract, WorkingTimeType workingTimeType) {
    Contract contract = new Contract();
    contract.person = person;
    contract.setBeginDate(beginDate);
    if (endDate.isPresent()) {
      contract.setEndDate(endDate.get());
    }
    if (endContract.isPresent()) {
      contract.setEndContract(endContract.get());
    }
    contractManager.properContractCreate(contract, Optional.of(workingTimeType), false);
    return contract;
  }

  /**
   * Costruisce e persiste una persona.
   *
   * @param office office
   * @param username username
   * @return persisted entity
   */
  @Transactional
  public Person createPerson(Office office, String username) {

    User user = new User();
    user.setUsername(username);
    user.password = "UnaPasswordQualsiasi";
    userDao.persist(user);
    Person person = new Person();
    person.setName("Name " + username);
    person.setSurname("Surname " + username);
    person.setEmail(String.format("%s@example.com", UUID.randomUUID()));
    person.setBeginDate(LocalDate.now());
    person.setUser(user);
    person.setOffice(office);
    person.setQualification(qualificationDao.findById(DEFAULT_PERSON_QUALIFICATION).get());
    personDao.persist(person);
    configurationManager.updateConfigurations(person);
    return person;
  }
  
  /**
   * Costruisce e persiste una sede.
   *
   * @param beginDate inizio sede
   * @param name nome 
   * @param codeId codeId
   * @param code code
   * @return persisted entity
   */
  @Transactional
  public Office buildOffice(LocalDate beginDate, String name, String codeId, String code) {

    Office office = new Office();
    office.setName(name);
    office.setBeginDate(beginDate);
    office.setCodeId(codeId);
    office.setCode(code);
    officeDao.persist(office);
    configurationManager.updateConfigurations(office);
    return office;
  }

  /**
   * Istanza di un dipendente con orario Normale.
   *
   * @param beginContract inizio contratto
   * @return mocked entity
   */
  @Transactional
  public Person normalEmployee(LocalDate beginContract, 
      Optional<LocalDate> expireContract) {

    final String name = "normalUndefinedEmployee" + beginContract + UUID.randomUUID();
    Office office = buildOffice(beginContract, name, name, name);
    WorkingTimeType normal = h2WorkingTimeTypeSupport.getWorkingTimeType(WorkingDefinition.Normal);
    Person person = createPerson(office, name);
    Contract contract = 
        buildContract(person, beginContract, expireContract, Optional.empty(), normal);
    contractDao.refresh(contract);
    personDao.refresh(person);
    
    return person;
  }
  
  /**
   * Istanza di un dipendente con orario PartTime 50.
   *
   * @param beginContract inizio contratto
   * @return mocked entity
   */
  @Transactional
  public Person partTime50Employee(LocalDate beginContract) {

    final String name = "partTime50UndefinedEmployee" + beginContract + UUID.randomUUID();
    Office office = buildOffice(beginContract, name, name, name);
    
    WorkingTimeType normal = h2WorkingTimeTypeSupport
        .getWorkingTimeType(WorkingDefinition.PartTime50);
    Person person = createPerson(office, name);
    Contract contract = 
        buildContract(person, beginContract, Optional.empty(), Optional.empty(), normal);
    contractDao.refresh(contract);
    personDao.refresh(person);
    
    return person;
  }

}