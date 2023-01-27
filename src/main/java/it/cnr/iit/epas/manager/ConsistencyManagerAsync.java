/*
 * Copyright (C) 2023  Consiglio Nazionale delle Ricerche
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

import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.models.Person;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


/**
* Metodi async del Manager per la gestione della consistenza tra timbrature ed assenze.
*
 *@author Cristian Lucchesi
 */
@Slf4j
@Service
public class ConsistencyManagerAsync {

  private final PersonDao personDao;
  private final ConsistencyManagerUtils consistencyManagerUtils;
  private final PersonDayInTroubleManager personDayInTroubleManager;

  @Inject
  ConsistencyManagerAsync(
      PersonDao personDao, ConsistencyManagerUtils consistencyManagerUtils,
      PersonDayInTroubleManager personDayInTroubleManager) {
    this.personDao = personDao;
    this.consistencyManagerUtils = consistencyManagerUtils;
    this.personDayInTroubleManager = personDayInTroubleManager;
  }

  /**
   * Ricalcola i riepiloghi mensili del contratto a partire dalla data from.
   * Metodo Asincrono!
   *
   * @param personId id della persona
   * @param from data dalla quale effettuare i ricalcoli
   */
  @Async
  public CompletableFuture<Void> updatePersonRecaps(Long personId, LocalDate from) {
    consistencyManagerUtils.updatePersonSituationEngine(
        personId, from, Optional.<LocalDate>empty(), true);
    return CompletableFuture.allOf();
  }

  /**
   * Aggiorna la situazione della persona a partire dalla data from.
   * Metodo Asincrono!
   *
   * @param personId id della persona
   * @param from data dalla quale effettuare i ricalcoli
   */
  @Async
  public CompletableFuture<Void> updatePersonSituation(Long personId, LocalDate from) {
    consistencyManagerUtils.updatePersonSituationEngine(
        personId, from, Optional.<LocalDate>empty(), false);
    return CompletableFuture.allOf();
  }

  /**
   * Ricalcolo della situazione di una persona dal mese e anno specificati ad oggi.
   *
   * @param personId id della persona di cui effettuare il ricalcolo
   * @param fromDate dalla data
   * @param onlyRecap se si vuole aggiornare solo i riepiloghi
   */
  @Async
  public CompletableFuture<Void> fixPersonSituation(
      Long personId, LocalDate fromDate, boolean onlyRecap) {
    
    final Person person = personDao.getPersonById(personId);

    if (onlyRecap) {
      updatePersonRecaps(personId, fromDate);
    } else {
      updatePersonSituation(personId, fromDate);
    }

    personDayInTroubleManager.cleanPersonDayInTrouble(person);
    log.debug("Elaborata la persona ... {}", person);
    return CompletableFuture.allOf();
  }
}