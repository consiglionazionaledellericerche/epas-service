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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dto.v4.AbsencePeriodSummaryDto;
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
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import java.util.Optional;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirements(
    value = { 
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION), 
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)})
@Tag(
    name = "Vacations controller", 
    description = "Visualizzazione delle informazioni sulle ferie dei dipendenti.")
@Slf4j
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/vacations")
class VacationController {

  private final ContractDao contractDao;
  private final PersonDao personDao;
  private final PersonVacationRecapFactory personvacationFactory;
  private final PersonVacationMapper personVacationMapper;
  private final PersonVacationSummaryFactory personVacationSummaryFactory;
  private final PersonVacationSummaryMapper personVacationSummaryMapper;
  private final PersonVacationSummarySubperiodFactory personVacationSummarySubperiodFactory;
  private final PersonVacationSummarySubperiodMapper personVacationSummarySubperiodMapper;
  private final SecurityRules rules;
  private SecureUtils securityUtils;

  @Inject
  VacationController(ContractDao contractDao,
      PersonDao personDao,
      PersonVacationRecapFactory personvacationFactory,
      PersonVacationMapper personVacationMapper,
      PersonVacationSummaryFactory personVacationSummaryFactory,
      PersonVacationSummaryMapper personVacationSummaryMapper,
      PersonVacationSummarySubperiodFactory personVacationSummarySubperiodFactory,
      PersonVacationSummarySubperiodMapper personVacationSummarySubperiodMapper,
      SecurityRules rules, SecureUtils securityUtils) {
    this.contractDao = contractDao;
    this.personDao = personDao;
    this.personvacationFactory = personvacationFactory;
    this.personVacationMapper = personVacationMapper;
    this.personVacationSummaryFactory = personVacationSummaryFactory;
    this.personVacationSummaryMapper = personVacationSummaryMapper;
    this.personVacationSummarySubperiodFactory = personVacationSummarySubperiodFactory;
    this.personVacationSummarySubperiodMapper = personVacationSummarySubperiodMapper;
    this.rules = rules;
    this.securityUtils = securityUtils;
  }

  @Operation(
      summary = "Visualizzazione delle informazioni delle ferie e permessi.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "la propria situazione ferie, oppure dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
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
  ResponseEntity<PersonVacationDto> show(
      @RequestParam("personId") Optional<Long> personId,
      @NotNull @RequestParam("year") Integer year,
      @NotNull @RequestParam("month") Integer month) {
    log.debug("REST method {} invoked with parameters year={}, month={}, personId ={}",
        "/rest/v4/vacations" + ApiRoutes.LIST, year, month, personId);

    Person person = null;
    if (personId.isPresent()) {
      person = personDao.byId(personId.get())
          .orElseThrow(() -> new EntityNotFoundException("Person not found with id = " + personId));
    } else {
      person = getPerson()
          .orElseThrow(() -> new EntityNotFoundException("Person not found using authentication"));
    }

    rules.checkifPermitted(person);

    PersonVacationRecap psrDto = personvacationFactory.create(person, year);
    return ResponseEntity.ok().body(personVacationMapper.convert(psrDto));
  }

  @Operation(
      summary = "Recupera il dettaglio di un summary/permesso per valorizzare il contenuto"
          + " della modale",
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
  ResponseEntity<PersonVacationSummaryDto> summary(
      @NotNull @RequestParam("contractId") Long contractId,
      @NotNull @RequestParam("year") Integer year,
      @NotNull @RequestParam("month") Integer month,
      @NotNull @RequestParam("type") TypeSummary typeSummary) {
    log.debug("REST method {} invoked with parameters contractId={}, year={}, type={}",
        "/rest/v4/vacations/summary", contractId, year, typeSummary);

    val contract = contractDao.byId(contractId)
        .orElseThrow(() -> new EntityNotFoundException(
          "Contract not found with id = " + contractId)); 
    Person person = contract.getPerson();

    rules.checkifPermitted(person);

    PersonVacationSummary psrDto = 
        personVacationSummaryFactory.create(person, year, contract.getId(), typeSummary);
    log.debug("psrDto  {} -------- total={}", psrDto, psrDto.vacationSummary.total());
    return ResponseEntity.ok().body(personVacationSummaryMapper.convert(psrDto));
  }

  @PostMapping("/summary/subperiod")
  ResponseEntity<AbsenceSubPeriodDto> subPeriod(
        @RequestBody AbsencePeriodSummaryDto periodSummaryDto) {
    log.debug("REST method {} invoked ", "/rest/v4//summary/subperiod");

    AbsencePeriod period = personVacationSummarySubperiodMapper.createPeriodFromDto(
        periodSummaryDto);
    log.debug("period {} invoked ", period);

    rules.checkifPermitted(period.person);

    VacationSummary summary =
        personVacationSummarySubperiodMapper.createSummaryFromDto(periodSummaryDto);
    log.debug("summary={}", summary.absencePeriod.subPeriods);
    PersonVacationSummarySubperiod psrDto = personVacationSummarySubperiodFactory.create(summary,
        period);
    log.debug("psrDto  {}", personVacationSummarySubperiodMapper.convert(psrDto));
    return ResponseEntity.ok().body(personVacationSummarySubperiodMapper.convert(psrDto));
  }

  /**
   * Restituisce la Person associata all'utente autenticato.
   */
  private Optional<Person> getPerson() {
    Optional<User> user = securityUtils.getCurrentUser();
    log.debug("VacationController::getPerson user = {}", user.orElse(null));
    if (!user.isPresent()) {
      log.info("Non è presente nessun utente");
      return Optional.empty();
    }
    return Optional.ofNullable(user.get().getPerson());
  }
}