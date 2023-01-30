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

package it.cnr.iit.epas.controller;

import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonDayInTroubleDao;
import it.cnr.iit.epas.dto.v4.PersonDayInTroubleDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonDayInTroubleMapper;
import it.cnr.iit.epas.manager.PersonDayInTroubleManager;
import it.cnr.iit.epas.models.PersonDayInTrouble;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rest/v4/admin")
class AdminController {

  private static final String CLEAN_PERSON_DAYS_IN_TROUBLE_API = "/cleanPersonDaysInTrouble";

  private final PersonDao personDao;
  private final PersonDayInTroubleManager personDayInTroubleManager;
  private final PersonDayInTroubleDao personDayInTroubleDao;
  private final PersonDayInTroubleMapper personDayInTroubleMapper;

  @Inject
  AdminController(PersonDao personDao, PersonDayInTroubleManager personDayInTroubleManager,
      PersonDayInTroubleMapper personDayInTroubleMapper,
      PersonDayInTroubleDao personDayInTroubleDao) {
    this.personDao = personDao;
    this.personDayInTroubleManager = personDayInTroubleManager;
    this.personDayInTroubleMapper = personDayInTroubleMapper;
    this.personDayInTroubleDao = personDayInTroubleDao;
  }

  @DeleteMapping(CLEAN_PERSON_DAYS_IN_TROUBLE_API)
  ResponseEntity<List<PersonDayInTroubleDto>> cleanPersonDaysInTrouble(
      @RequestParam("personId") Long personId) {
    log.debug("REST method {} invoked with parameters personId={}",
        CLEAN_PERSON_DAYS_IN_TROUBLE_API, personId);

    val person = personDao.byId(personId);
    if (person.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    val currentPersonDaysInTrouble = 
        personDayInTroubleDao.getPersonDayInTroubleInPeriod(
            person.get(), Optional.empty(), Optional.empty(), Optional.empty());
    log.debug("{} current personDays in trouble found for {}", 
        currentPersonDaysInTrouble.size(), person.get());

    log.debug("Current Thread REST API = {}", Thread.currentThread());
    CompletableFuture<List<PersonDayInTrouble>> deleted = 
        personDayInTroubleManager.cleanPersonDayInTrouble(person.get());
    try {
      return ResponseEntity.ok().body(
          deleted.get().stream()
          .map(pdt -> personDayInTroubleMapper.convert(pdt))
          .collect(Collectors.toList()));
    } catch (InterruptedException | ExecutionException e) {
      log.error("Problem during person days in trouble cleaning for {}",
          person, e);
      return ResponseEntity.internalServerError().build();
    }
  }

}