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
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dto.v4.PersonStampingRecapDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonStampingRecapMapper;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingRecap;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingRecapFactory;
import java.time.YearMonth;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi ai riepiloghi mensili.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@RestController
@RequestMapping("/rest/v4/monthrecaps")
public class MonthRecapController {

  private final PersonDao personDao;
  private final IWrapperFactory wrapperFactory;
  private final PersonStampingRecapFactory stampingRecapFactory;
  private final PersonStampingRecapMapper personStampingRecapMapper;

  @Inject
  MonthRecapController(
      PersonDao personDao, IWrapperFactory wrapperFactory,
      PersonStampingRecapFactory stampingRecapFactory,
      PersonStampingRecapMapper personStampingRecapFactory) {
    this.personDao = personDao;
    this.wrapperFactory = wrapperFactory;
    this.stampingRecapFactory = stampingRecapFactory;
    this.personStampingRecapMapper = personStampingRecapFactory;
  }

  @GetMapping(ApiRoutes.LIST)
  ResponseEntity<PersonStampingRecapDto> show(
      @RequestParam("personId") Long personId, 
      @RequestParam("year") Integer year, 
      @RequestParam("month") Integer month) {
    log.debug("REST method {} invoked with parameters personId={}, year={}, month={}",
        "/rest/v4/monthrecaps" + ApiRoutes.LIST, personId, year, month);
    val person = personDao.byId(personId);

    log.debug("Person {}", person.isEmpty());
    if (person.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    val wrPerson = wrapperFactory.create(person.get());
    if (!wrPerson.isActiveInMonth(YearMonth.of(year, month))) {
      return ResponseEntity.notFound().build();
    }
    PersonStampingRecap psrDto = stampingRecapFactory.create(person.get(), year, month, true);
    return ResponseEntity.ok().body(personStampingRecapMapper.convert(psrDto));
  }
}