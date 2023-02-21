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

package it.cnr.iit.epas.controller.v4;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dto.v4.AbsencePeriodSummaryDto;
import it.cnr.iit.epas.dto.v4.AbsenceSubPeriodDto;
import it.cnr.iit.epas.dto.v4.PersonVacationDto;
import it.cnr.iit.epas.dto.v4.PersonVacationSummaryDto;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
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
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.repo.UserRepository;
import it.cnr.iit.epas.security.SecureUtils;
import java.time.YearMonth;
import java.util.Optional;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rest/v4/vacations")
public class VacationController {

  private UserRepository repo;
  private final PersonVacationRecapFactory personvacationFactory;
  private final PersonVacationMapper personVacationMapper;
  private final PersonVacationSummaryFactory personVacationSummaryFactory;
  private final PersonVacationSummaryMapper personVacationSummaryMapper;
  private final PersonVacationSummarySubperiodFactory personVacationSummarySubperiodFactory;
  private final PersonVacationSummarySubperiodMapper personVacationSummarySubperiodMapper;
  private SecureUtils securityUtils;
  private final EntityToDtoConverter entityToDtoConverter;

  @Inject
  public VacationController(UserRepository repo,
      PersonVacationRecapFactory personvacationFactory,
      PersonVacationMapper personVacationMapper,
      PersonVacationSummaryFactory personVacationSummaryFactory,
      PersonVacationSummaryMapper personVacationSummaryMapper,
      PersonVacationSummarySubperiodFactory personVacationSummarySubperiodFactory,
      PersonVacationSummarySubperiodMapper personVacationSummarySubperiodMapper,
      SecureUtils securityUtils, EntityToDtoConverter entityToDtoConverter) {
    this.repo = repo;
    this.personvacationFactory = personvacationFactory;
    this.personVacationMapper = personVacationMapper;
    this.personVacationSummaryFactory = personVacationSummaryFactory;
    this.personVacationSummaryMapper = personVacationSummaryMapper;
    this.personVacationSummarySubperiodFactory = personVacationSummarySubperiodFactory;
    this.personVacationSummarySubperiodMapper = personVacationSummarySubperiodMapper;
    this.securityUtils = securityUtils;
    this.entityToDtoConverter = entityToDtoConverter;
  }

  @Operation(
      summary = "Visualizzazione delle informazioni delle ferie e permessi.",
      description = "Questo endpoint è utilizzabile dagli utenti autenticati"
          + " e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i dati delle ferie e dei permessi"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati delle ferie e dei permessi",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Ferie e permessi non trovati con l'id fornito",
          content = @Content)
  })
  @GetMapping(ApiRoutes.LIST)
  public ResponseEntity<PersonVacationDto> show(
      @RequestParam("year") Integer year,
      @RequestParam("month") Integer month) {
    log.debug("REST method {} invoked with parameters year={}, month={}",
        "/rest/v4/vacations" + ApiRoutes.LIST, year, month);

    Optional<Person> person = getPerson();

   if (!person.isPresent()) {
      return ResponseEntity.notFound().build();
    }

    PersonVacationRecap psrDto = personvacationFactory.create(person.get(), year);
    return ResponseEntity.ok().body(personVacationMapper.convert(psrDto));
  }

  @Operation(
      summary = "Recupera il dettaglio di un summary/permesso per valorizzare il contenuto della modale",
      description = "Questo endpoint è utilizzabile dagli utenti autenticati"
          + " e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i dati delle ferie e dei permessi"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati delle ferie e dei permessi",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Ferie e permessi non trovati con l'id fornito",
          content = @Content)
  })
  @GetMapping("/summary")
  public ResponseEntity<PersonVacationSummaryDto> summary(
      @RequestParam("contractId") Long contractId,
      @RequestParam("year") Integer year,
      @RequestParam("month") Integer month,
      @RequestParam("type") TypeSummary typeSummary) {
    log.debug("REST method {} invoked with parameters contractId={}, year={}, type={}",
        "/rest/v4/vacations/summary", contractId, year, typeSummary);

    Optional<Person> person = getPerson();

    if (!person.isPresent()) {
      return ResponseEntity.notFound().build();
    }

    PersonVacationSummary psrDto = personVacationSummaryFactory.create(person.get(), year,
        contractId, typeSummary);
    log.debug("psrDto  {} -------- total={}", psrDto, psrDto.vacationSummary.total());
    return ResponseEntity.ok().body(personVacationSummaryMapper.convert(psrDto));
  }

  @GetMapping("/summary/subperiod")
  public ResponseEntity<AbsenceSubPeriodDto> subperiod(
        @RequestBody AbsencePeriodSummaryDto periodSummaryDto) {
    log.debug("REST method {} invoked ","/rest/v4//summary/subperiod");

    AbsencePeriod period = personVacationSummarySubperiodMapper.createPeriodFromDto(
        periodSummaryDto);
    VacationSummary summary = personVacationSummarySubperiodMapper.createSummaryFromDto(periodSummaryDto);
    log.debug("summary={}", summary.absencePeriod.subPeriods);
    PersonVacationSummarySubperiod psrDto = personVacationSummarySubperiodFactory.create(summary,
        period);
    log.debug("psrDto  {}", psrDto);
    return ResponseEntity.ok().body(personVacationSummarySubperiodMapper.convert(psrDto));
  }


  /**
   * Restituisce la Person associata all'utente autenticato.
   * @return person
   */
  private Optional<Person> getPerson(){
    Optional<User> user = securityUtils.getCurrentUser();
    log.debug("UserInfo::show user = {}", user.orElse(null));
    if (!user.isPresent()) {
      return Optional.empty();
    }
    long personId = user.get().getId();
    Optional<User> entity = repo.findById(personId);

    log.debug("personId::show user = {} {}", personId, entity);

    if (entity.isEmpty()) {
      return Optional.empty();
    }

    return Optional.ofNullable(entity.get().getPerson());
  }
}