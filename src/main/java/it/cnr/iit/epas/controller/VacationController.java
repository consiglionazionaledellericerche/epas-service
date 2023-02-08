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
package it.cnr.iit.epas.controller;

import it.cnr.iit.epas.controller.utils.ApiRoutes;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dto.v4.AbsenceSubPeriodDto;
import it.cnr.iit.epas.dto.v4.PersonVacationDto;
import it.cnr.iit.epas.dto.v4.PersonVacationSummaryDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonVacationMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonVacationSummaryMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonVacationSummarySubperiodMapper;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationRecap;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationRecapFactory;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationSummary;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationSummaryFactory;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationSummarySubperiod;
import it.cnr.iit.epas.manager.recaps.personvacation.PersonVacationSummarySubperiodFactory;
import it.cnr.iit.epas.manager.services.absences.model.AbsencePeriod;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary.TypeSummary;
import java.time.YearMonth;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rest/v4/vacations")
public class VacationController {

  private final PersonDao personDao;
  private final IWrapperFactory wrapperFactory;
  private final PersonVacationRecapFactory personvacationFactory;
  private final PersonVacationMapper personVacationMapper;
  private final PersonVacationSummaryFactory personVacationSummaryFactory;
  private final PersonVacationSummaryMapper personVacationSummaryMapper;
  private final PersonVacationSummarySubperiodFactory personVacationSummarySubperiodFactory;
  private final PersonVacationSummarySubperiodMapper personVacationSummarySubperiodMapper;

  @Inject
  public VacationController(
      PersonDao personDao, IWrapperFactory wrapperFactory,
      PersonVacationRecapFactory personvacationFactory,
      PersonVacationMapper personVacationMapper,
      PersonVacationSummaryFactory personVacationSummaryFactory,
      PersonVacationSummaryMapper personVacationSummaryMapper,
      PersonVacationSummarySubperiodFactory personVacationSummarySubperiodFactory,
      PersonVacationSummarySubperiodMapper personVacationSummarySubperiodMapper) {
    this.personDao = personDao;
    this.wrapperFactory = wrapperFactory;
    this.personvacationFactory = personvacationFactory;
    this.personVacationMapper = personVacationMapper;
    this.personVacationSummaryFactory = personVacationSummaryFactory;
    this.personVacationSummaryMapper = personVacationSummaryMapper;
    this.personVacationSummarySubperiodFactory = personVacationSummarySubperiodFactory;
    this.personVacationSummarySubperiodMapper = personVacationSummarySubperiodMapper;
  }

  @GetMapping(ApiRoutes.LIST)
  public ResponseEntity<PersonVacationDto> show(
      @RequestParam("personId") Long personId, 
      @RequestParam("year") Integer year,
      @RequestParam("month") Integer month) {
    log.debug("REST method {} invoked with parameters personId={}, year={}, month={}",
        "/rest/v4/vacations" + ApiRoutes.LIST, personId, year, month);
    val person = personDao.byId(personId);

    log.debug("Person {}", person.isEmpty());
    if (person.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    val wrPerson = wrapperFactory.create(person.get());
    if (!wrPerson.isActiveInMonth(YearMonth.of(year, month))) {
      return ResponseEntity.notFound().build();
    }

    PersonVacationRecap psrDto = personvacationFactory.create(person.get(), year);
    return ResponseEntity.ok().body(personVacationMapper.convert(psrDto));
  }

  @GetMapping("/summary")
  public ResponseEntity<PersonVacationSummaryDto> summary(
      @RequestParam("personId") Long personId,
      @RequestParam("contractId") Long contractId,
      @RequestParam("year") Integer year,
      @RequestParam("month") Integer month,
      @RequestParam("type") TypeSummary typeSummary) {
    log.debug("REST method {} invoked with parameters personId={}, contractId={}, year={}, type={}",
        "/rest/v4/vacations/summary", personId, contractId, year, typeSummary);

    val person = personDao.byId(personId);

    val wrPerson = wrapperFactory.create(person.get());
    if (!wrPerson.isActiveInMonth(YearMonth.of(year, month))) {
      return ResponseEntity.notFound().build();
    }

    PersonVacationSummary psrDto = personVacationSummaryFactory.create(person.get(), year, contractId, typeSummary);
    log.debug("psrDto  {} -------- total={}", psrDto, psrDto.vacationSummary.total());
    return ResponseEntity.ok().body(personVacationSummaryMapper.convert(psrDto));
  }

  @GetMapping("/summary/subperiod")
  public ResponseEntity<AbsenceSubPeriodDto> subperiod(
      @RequestParam("vacationSummary") VacationSummary vacationSummary,
      @RequestParam("period") AbsencePeriod period) {
    log.debug("REST method {} invoked with parameters period={}", "/rest/v4/vacations/summary/subperiod", period);

    PersonVacationSummarySubperiod psrDto = personVacationSummarySubperiodFactory.create(vacationSummary, period);
    log.debug("psrDto  {} -------- total={}", psrDto);
    return ResponseEntity.ok().body(personVacationSummarySubperiodMapper.convert(psrDto));
  }

}