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

package it.cnr.iit.epas.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.manager.configurations.EpasParam.RecomputationType;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.base.IPropertiesInPeriodOwner;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
//import play.db.jpa.JPA;
//import play.libs.F.Promise;

/**
 * Manager per la gestione della consistenza tra timbrature ed assenze.
 *
 * @author Alessandro Martelli
 */
@Transactional
@Slf4j
@Component
public class ConsistencyManager {

  //private final SecureManager secureManager;

  private final OfficeDao officeDao;
  private final PersonDao personDao;
  private final SecureManager secureManager;
  private final ConsistencyManagerUtils consistencyManagerUtils;
  private final ConsistencyManagerAsync consistencyManagerAsync;
  private final Provider<IWrapperFactory> wrapperFactory;
  private final Provider<EntityManager> emp;

  /**
   * Constructor for injection.
   */
  @Inject
  ConsistencyManager(
      //SecureManager secureManager,
      OfficeDao officeDao, PersonDao personDao,
      SecureManager secureManager,
      ConsistencyManagerUtils consistencyManagerUtils,
      ConsistencyManagerAsync consistencyManagerAsync,
      Provider<IWrapperFactory> wrapperFactory, AbsenceDao absenceDao,
      Provider<EntityManager> emp) {

    //this.secureManager = secureManager;
    this.officeDao = officeDao;
    this.personDao = personDao;
    this.secureManager = secureManager;
    this.consistencyManagerUtils = consistencyManagerUtils;
    this.consistencyManagerAsync = consistencyManagerAsync;
    this.wrapperFactory = wrapperFactory;
    this.emp = emp;
  }

  /**
   * Ricalcolo della situazione di una persona (o tutte) dal mese e anno specificati ad oggi.
   *
   * @param person persona (se absent tutte)
   * @param user utente loggato
   * @param fromDate dalla data
   * @param onlyRecap se si vuole aggiornare solo i riepiloghi
   * @throws ExecutionException  in caso di problemi con i CompletableFuture
   * @throws InterruptedException  in caso di interruzione dei CompletableFuture
   */
  public void fixPersonSituation(Optional<Person> person, Optional<User> user, LocalDate fromDate,
      boolean onlyRecap) throws InterruptedException, ExecutionException {

    Set<Office> offices = user.isPresent() ? secureManager.officesWriteAllowed(user.get())
        : Sets.newHashSet(officeDao.getAllOffices());

    // (0) Costruisco la lista di persone su cui voglio operare
    List<Person> personList = Lists.newArrayList();

    if (person.isPresent() && user.isPresent()) {
      // if(personManager.isAllowedBy(user.get(), person.get()))
      personList.add(person.get());
    } else {
      personList = personDao.list(Optional.<String>empty(), offices, false, fromDate,
          LocalDate.now().minusDays(1), true).list();
    }

    final List<CompletableFuture<Void>> results = Lists.newArrayList();
    for (Person p : personList) {
      results.add(consistencyManagerAsync.fixPersonSituation(p.getId(), fromDate, onlyRecap));
    }
    CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()])).get();
    log.info("Conclusa procedura FixPersonsSituation con parametri!");
  }

  /**
   * Ricalcolo della situazione di una lista di persone dal mese e anno specificati ad oggi.
   *
   * @param personList la lista delle persone da ricalcolare
   * @param fromDate dalla data
   * @param onlyRecap se si vuole aggiornare solo i riepiloghi
   *
   * @throws ExecutionException nel caso di problemi con i CompletableFuture
   * @throws InterruptedException nel caso di interruzione dei CompletableFuture 
   */
  public void fixPersonSituation(
      List<Person> personList, LocalDate fromDate, boolean onlyRecap) 
          throws InterruptedException, ExecutionException {

    final List<CompletableFuture<Void>> results = Lists.newArrayList();

    for (Person p : personList) {
      results.add(consistencyManagerAsync.fixPersonSituation(p.getId(), fromDate, onlyRecap));
    }

    CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()])).get();
    log.info("Conclusa procedura FixPersonsSituation con parametri!");
  }

  /**
   * Ricalcola i riepiloghi mensili del contratto a partire dalla data from.
   *
   * @param personId id della persona
   * @param from data dalla quale effettuare i ricalcoli
   */
  public void updatePersonRecaps(Long personId, LocalDate from) {
    consistencyManagerUtils.updatePersonSituationEngine(
        personId, from, Optional.<LocalDate>empty(), true);
  }

  /**
   * Aggiorna la situazione della persona a partire dalla data from.
   *
   * @param personId id della persona
   * @param from data dalla quale effettuare i ricalcoli
   */
  public Optional<Contract> updatePersonSituation(Long personId, LocalDate from) {
    return consistencyManagerUtils.updatePersonSituationEngine(
        personId, from, Optional.<LocalDate>empty(), false);
  }

  /**
   * Aggiorna la situazione del contratto a partire dalla data from.
   *
   * @param contract contract
   * @param from la data da cui far partire l'aggiornamento.
   */
  public void updateContractSituation(Contract contract, LocalDate from) {

    LocalDate to = wrapperFactory.get().create(contract).getContractDatabaseInterval().getEnd();
    consistencyManagerUtils.updatePersonSituationEngine(
        contract.person.getId(), from, Optional.ofNullable(to), false);
  }

  /**
   * Ricalcola i riepiloghi mensili del contratto a partire dalla data from.
   *
   * @param contract contract
   * @param from la data da cui far partire l'aggiornamento.
   */
  public void updateContractRecaps(Contract contract, LocalDate from) {

    LocalDate to = wrapperFactory.get().create(contract).getContractDatabaseInterval().getEnd();
    consistencyManagerUtils.updatePersonSituationEngine(
        contract.person.getId(), from, Optional.ofNullable(to), true);
  }

  /**
   * Effettua la ricomputazione.
   */
  public void performRecomputation(IPropertiesInPeriodOwner target,
      List<RecomputationType> recomputationTypes, LocalDate recomputeFrom) {

    if (recomputationTypes.isEmpty()) {
      return;
    }
    if (recomputeFrom == null) {
      return;
    }

    List<Person> personToRecompute = Lists.newArrayList();

    if (target instanceof Office) {
      personToRecompute = ((Office) target).getPersons();
    } else if (target instanceof Person) {
      personToRecompute.add((Person) target);
    }

    for (Person person : personToRecompute) {
      if (recomputationTypes.contains(RecomputationType.DAYS)) {
        updatePersonSituation(person.getId(), recomputeFrom);
      } else if (recomputationTypes.contains(RecomputationType.RESIDUAL_HOURS)
          || recomputationTypes.contains(RecomputationType.RESIDUAL_MEALTICKETS)) {
        updatePersonRecaps(person.getId(), recomputeFrom);
      }
      //FIXME: ma servono davvero??
      emp.get().flush();
      emp.get().clear();
      //JPA.em().flush();
      //JPA.em().clear();
    }
  }

}