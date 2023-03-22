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

package it.cnr.iit.epas.controller.v4;

import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dto.v4.PersonDayDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonDayMapper;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi alla visualizzazione e gestione dei riepilogi
 * giornalieri dei dipendenti.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/persondays")
public class PersonDaysController {

  private final PersonDayDao personDayDao;
  private final PersonDao personDao;
  private final PersonDayMapper personDayMapper;

  /**
   * Costruttore di default per l'injection.
   */
  @Inject
  public PersonDaysController(PersonDayDao personDayDao,
      PersonDao personDao, PersonDayMapper personDayMapper) {
    this.personDayDao = personDayDao;
    this.personDao = personDao;
    this.personDayMapper = personDayMapper;
  }

  @GetMapping(ApiRoutes.LIST)
  ResponseEntity<List<PersonDayDto>> list(
      @RequestParam("personId") Long personId, 
      @RequestParam("year") Integer year, 
      @RequestParam("month") Integer month) {
    log.debug("REST method {} invoked with parameters personId={}, year={}, month={}",
        ApiRoutes.LIST, personId, year, month);
    val person = personDao.byId(personId)
        .orElseThrow(() -> new EntityNotFoundException("Person not found with id = " + personId));
    val personDays = 
        personDayDao.getPersonDayInMonth(person, YearMonth.of(year, month));
    val personDaysDto = 
        personDays.stream().map(personDayMapper::convert).collect(Collectors.toList());
    return ResponseEntity.ok().body(personDaysDto);
  }
}