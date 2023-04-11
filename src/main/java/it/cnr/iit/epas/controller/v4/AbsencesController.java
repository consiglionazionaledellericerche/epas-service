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
import it.cnr.iit.epas.controller.exceptions.ValidationException;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.AbsenceTypeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.dto.v4.AbsenceAddedDto;
import it.cnr.iit.epas.dto.v4.AbsenceShowDto;
import it.cnr.iit.epas.dto.v4.AbsenceShowTerseDto;
import it.cnr.iit.epas.dto.v4.mapper.AbsenceMapper;
import it.cnr.iit.epas.manager.AbsenceManager;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Metodi REST per la gestione delle informazioni sui contratti.
 */
@SecurityRequirements(
    value = { 
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION), 
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(
    name = "Absences Controller", 
    description = "Gestione e visualizzazione delle informazioni delle assenze")
@Transactional
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/absences")
public class AbsencesController {

  private final AbsenceDao absenceDao;
  private final PersonDao personDao;
  private final AbsenceTypeDao absenceTypeDao;
  private final AbsenceMapper absenceMapper;
  private final AbsenceManager absenceManager;
  private final AbsenceService absenceService;
  private final WrapperFactory wrapperFactory;
  private final SecurityRules rules;

  /**
   * Visualizzazione delle informazioni di un'assenza.
   */
  @Operation(
      summary = "Visualizzazione delle informazioni di un'assenza.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona associata all'assenza e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituiti i dati dell'assenza"),
      @ApiResponse(responseCode = "401", 
      description = "Autenticazione non presente", content = @Content), 
      @ApiResponse(responseCode = "403", 
      description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
          + " i dati dell'assenza",
          content = @Content), 
      @ApiResponse(responseCode = "404", 
      description = "Assenza non trovata con l'id fornito",
      content = @Content)
  })
  @GetMapping(ApiRoutes.SHOW)
  public ResponseEntity<AbsenceShowDto> show(@NotNull @PathVariable("id") Long id) {
    log.debug("AbsenceController::show id = {}", id);
    val absence = absenceDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Absence not found"));

    rules.checkifPermitted(absence.getPersonDay().getPerson());

    return ResponseEntity.ok().body(absenceMapper.convert(absence));
  }

  /**
   * Elenco delle assenza in un periodo.
   */
  @Operation(
      summary = "Visualizzazione della lista delle assenza di una persona.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituito l'elenco delle assenze"),
      @ApiResponse(responseCode = "401", 
      description = "Autenticazione non presente", content = @Content), 
      @ApiResponse(responseCode = "403", 
      description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
          + " l'elenco delle assenze",
          content = @Content), 
      @ApiResponse(responseCode = "404", 
      description = "Persona non trovata con l'id e/o il codice fiscale fornito",
      content = @Content)
  })
  @GetMapping("/absencesInPeriod")
  public ResponseEntity<List<AbsenceShowTerseDto>> absencesInPeriod(
      @RequestParam("id") Optional<Long> id, 
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
      @RequestParam("beginDate") LocalDate beginDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
      @RequestParam("endDate") Optional<LocalDate> endDate) {
    log.debug("AbsenceController::absencesInPeriod id = {}", id);
    val person = personDao.byIdOrFiscalCode(id, fiscalCode)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(person);

    val absences = absenceDao.absenceInPeriod(person, beginDate, endDate);

    return ResponseEntity.ok().body(
        absences.stream()
        .map(ab -> absenceMapper.convertTerse(ab))
        .collect(Collectors.toList()));
  }

  /**
   * Report con la simulazione delle assenze inserite.
   */
  public ResponseEntity<List<AbsenceAddedDto>> checkAbsence(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam @NotNull @NotEmpty String absenceCode,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam("begin") @NotNull LocalDate begin,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam("end") @NotNull LocalDate end,
      @RequestParam("hours") Integer hours, Integer minutes) {

    log.debug("AbsenceController::insertReport id = {}, fiscalCode = {}, "
        + "absenceCode = {}, beginDate = {}", 
        id, fiscalCode, absenceCode, begin);
    val person = personDao.byIdOrFiscalCode(id, fiscalCode)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    rules.checkifPermitted(person);
    
    if (begin.isAfter(end)) {
      throw new ValidationException(
          String.format("La data di inizio ( {} ) non può essere successiva"
              + "a quella di fine ( {} )", begin, end));
    }
    Optional<Contract> contract = wrapperFactory
        .create(person).getCurrentContract();
    Optional<ContractMonthRecap> recap = wrapperFactory.create(contract.get())
        .getContractMonthRecap(YearMonth.of(end.getYear(),
            end.getMonthValue()));

    if (!recap.isPresent()) {
      throw new ValidationException(String.format(
      "Non esistono riepiloghi per %s da cui prender le informazioni per il calcolo",
          person.getFullname()));
    }

    val absenceType = absenceTypeDao.getAbsenceTypeByCode(absenceCode);
    val justifiedType = absenceType.get().getJustifiedTypesPermitted().iterator().next();
    val groupAbsenceType = absenceType.get().defaultTakableGroup(); 
    val report = 
        absenceService.insert(person, groupAbsenceType, begin, end, absenceType.get(),
        justifiedType, hours, minutes, false, absenceManager);

    val list = report.insertTemplateRows.stream()
        .map(AbsenceAddedDto::build)
        .collect(Collectors.toList());
    return ResponseEntity.ok().body(list);
  }

  /**
   * Metodo REST per l'inserimento della assenze. 
   */
  public ResponseEntity<List<AbsenceShowDto>> insertAbsence(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam @NotNull @NotEmpty String absenceCode,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam("begin") @NotNull LocalDate begin,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam("end") @NotNull LocalDate end,
      @RequestParam("hours") Integer hours, Integer minutes) {

    log.debug("AbsenceController::insertAbsence id = {}, fiscalCode = {}, "
        + "absenceCode = {}, beginDate = {}", 
        id, fiscalCode, absenceCode, begin);
    val person = personDao.byIdOrFiscalCode(id, fiscalCode)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    rules.checkifPermitted(person);
    
    if (begin.isAfter(end)) {
      throw new ValidationException(
          String.format("La data di inizio ( {} ) non può essere successiva"
              + "a quella di fine ( {} )", begin, end));
    }
    Optional<Contract> contract = wrapperFactory
        .create(person).getCurrentContract();
    Optional<ContractMonthRecap> recap = wrapperFactory.create(contract.get())
        .getContractMonthRecap(YearMonth.of(end.getYear(),
            end.getMonthValue()));

    if (!recap.isPresent()) {
      throw new ValidationException(String.format(
      "Non esistono riepiloghi per %s da cui prender le informazioni per il calcolo",
          person.getFullname()));
    }

    val absenceType = absenceTypeDao.getAbsenceTypeByCode(absenceCode);
    val justifiedType = absenceType.get().getJustifiedTypesPermitted().iterator().next();
    val groupAbsenceType = absenceType.get().defaultTakableGroup(); 
    val report = 
        absenceService.insert(person, groupAbsenceType, begin, end, absenceType.get(),
        justifiedType, hours, minutes, false, absenceManager);

    val list = report.insertTemplateRows.stream()
        .map(AbsenceAddedDto::build)
        .collect(Collectors.toList());

    List<Absence> absences = 
        absenceManager.saveAbsences(report, person, begin, null, justifiedType, groupAbsenceType);

    return null;
  }
  

}