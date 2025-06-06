/*
 * Copyright (C) 2025  Consiglio Nazionale delle Ricerche
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

import com.google.common.collect.Lists;
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
import it.cnr.iit.epas.controller.v4.utils.PersonFinder;
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.AbsenceTypeDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.dto.v4.AbsenceAddedDto;
import it.cnr.iit.epas.dto.v4.AbsenceInMonthDto;
import it.cnr.iit.epas.dto.v4.AbsenceShowDto;
import it.cnr.iit.epas.dto.v4.AbsenceShowTerseDto;
import it.cnr.iit.epas.dto.v4.AbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.mapper.AbsenceGroupMapper;
import it.cnr.iit.epas.dto.v4.mapper.AbsenceMapper;
import it.cnr.iit.epas.manager.AbsenceManager;
import it.cnr.iit.epas.manager.AbsenceManager.AbsenceToDate;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.AbsenceService.InsertReport;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import it.cnr.iit.epas.security.SecurityRules;
import it.cnr.iit.epas.utils.DateUtility;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
  private final AbsenceTypeDao absenceTypeDao;
  private final AbsenceComponentDao absenceComponentDao;
  private final AbsenceMapper absenceMapper;
  private final AbsenceGroupMapper absenceGroupMapper;
  private final AbsenceManager absenceManager;
  private final AbsenceService absenceService;
  private final WrapperFactory wrapperFactory;
  private final PersonFinder personFinder;
  private final SecurityRules rules;

  /**
   * Visualizzazione delle informazioni di un'assenza.
   */
  @Operation(
      summary = "Verifica se l'utente corrente ha l'accesso ad un certo endpoint REST "
          + "relativo alle assenze.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituita l'autorizzazione true/false di accedere all'endpoint indicato"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i permessi di questo controller delle assenze",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Assenza non trovata con l'id fornito",
          content = @Content)
  })
  @GetMapping("/secureCheck")
  public ResponseEntity<Boolean> secureCheck(
      @RequestParam("method") String method, 
      @RequestParam("path") String path, @RequestParam("id") Optional<Long> id) {
    Absence absence = null;
    log.debug("AbsenceController::secureCheck method= {}, path = {}, id = {}", method, path, id);
    Person person = null;
    if (id.isPresent()) {
      absence = absenceDao.byId(id.get())
          .orElseThrow(() -> new EntityNotFoundException("Absence not found"));
      person = absence.getPersonDay().getPerson();
    }
     
    //Le drools sulle assenze non controllano come target l'assenza ma la person
    return ResponseEntity.ok(rules.check(method, path, person));
  }

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
    Absence absence = absenceDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Absence not found"));

    rules.checkifPermitted(absence.getPersonDay().getPerson());
    
    Set<GroupAbsenceType> involvedGroups = absence.absenceType.involvedGroupAbsenceType(true);
    List<Object> objectAll = Lists.newArrayList();

    for (GroupAbsenceType group : involvedGroups) {
      HashMap<String, Object> elem = new HashMap<>();
      elem.put("replacingAbsences", absence.replacingAbsences(group));
      elem.put("id", group.getId());
      elem.put("name", group.name);
      elem.put("description", group.description);
      objectAll.add(elem);
    }

    return ResponseEntity.ok().body(absenceGroupMapper.convert(absence, objectAll));
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
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
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
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("beginDate") LocalDate beginDate,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("endDate") Optional<LocalDate> endDate) {
    log.debug("AbsenceController::absencesInPeriod id = {}", id);
    val person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    log.debug("AbsenceController::absencesInPeriod person = {}", person);

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
  @Operation(
      summary = "Verifica del esito di inserimento di assenze per una persona.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin', oppure dall'utente per cui verrebbero inserite le "
          + "assenze.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituito l'elenco dei dettagli sulle assenze inseribili"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description =
              "Utente che ha effettuato la richiesta non autorizzato a simulare l'inserimento"
                  + " delle assenze per la persona indicata.",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping("/checkAbsence")
  public ResponseEntity<List<AbsenceAddedDto>> checkAbsence(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam @NotNull @NotEmpty String absenceCode,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("begin") @NotNull LocalDate begin,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("end") @NotNull LocalDate end,
      @RequestParam("hours") Optional<Integer> hours,
      @RequestParam("minutes") Optional<Integer> minutes) {

    log.debug("AbsenceController::insertReport id = {}, fiscalCode = {}, "
            + "absenceCode = {}, beginDate = {}",
        id, fiscalCode, absenceCode, begin);

    val person =
        personFinder.getPerson(id, fiscalCode)
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
            justifiedType, hours.orElse(null), minutes.orElse(null), false, absenceManager);

    val list = report.insertTemplateRows.stream()
        .map(AbsenceAddedDto::build)
        .collect(Collectors.toList());
    return ResponseEntity.ok().body(list);
  }

  /**
   * Metodo REST per l'inserimento della assenze.
   */
  @Operation(
      summary = "Metodo per l'nserimento di assenze di una persona.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' della sede a "
          + "appartiene la persona di cui inserire le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin', oppure dall'utente relativo alle "
          + "assenze inserite se la tipologia di assenza è abilitata per l'autoinserimento"
          + "da parte del utente.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Elenco con i dettagli delle assenze inserite"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato ad inserire queste "
              + "assenze",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @PutMapping(ApiRoutes.CREATE)
  public ResponseEntity<List<AbsenceShowDto>> insertAbsence(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam @NotNull @NotEmpty String absenceCode,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("begin") @NotNull LocalDate begin,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("end") @NotNull LocalDate end,
      @RequestParam("hours") Optional<Integer> hours,
      @RequestParam("minutes") Optional<Integer> minutes) {

    log.debug("AbsenceController::insertAbsence id = {}, fiscalCode = {}, "
            + "absenceCode = {}, beginDate = {}",
        id, fiscalCode, absenceCode, begin);
    val person =
        personFinder.getPerson(id, fiscalCode)
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
          "Non esistono riepiloghi per %s da cui prendere le informazioni per il calcolo",
          person.getFullname()));
    }

    val absenceType = absenceTypeDao.getAbsenceTypeByCode(absenceCode);
    val justifiedType = absenceType.get().getJustifiedTypesPermitted().iterator().next();
    val groupAbsenceType = absenceType.get().defaultTakableGroup();
    val report =
        absenceService.insert(person, groupAbsenceType, begin, end, absenceType.get(),
            justifiedType, hours.orElse(null), minutes.orElse(null), false, absenceManager);

    List<Absence> absences =
        absenceManager.saveAbsences(report, person, begin, null, justifiedType, groupAbsenceType);

    return ResponseEntity.ok().body(absences.stream()
        .map(ab -> absenceMapper.convert(ab)).collect(Collectors.toList()));
  }

  /**
   * Metodo REST per l'inserimento della assenze.
   */
  @Operation(
      summary = "Metodo per l'nserimento di assenze di tipo Ferie di una persona con il codice "
          + "di ferie più vantaggioso per il dipendente.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' della sede a "
          + "appartiene la persona di cui inserire le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Elenco con i dettagli delle assenze inserite"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato ad inserire queste "
              + "assenze",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @PutMapping("/insertVacation")
  public ResponseEntity<List<AbsenceShowDto>> insertVacation(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("begin") @NotNull LocalDate begin,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("end") @NotNull LocalDate end,
      @RequestParam("hours") Optional<Integer> hours,
      @RequestParam("minutes") Optional<Integer> minutes) {

    log.debug("AbsenceController::insertVacation id = {}, fiscalCode = {}, "
            + "begin = {}, end = {}",
        id, fiscalCode, begin, end);
    val person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    rules.checkifPermitted(person);

    if (begin.isAfter(end)) {
      throw new ValidationException(
          String.format("La data di inizio ( {} ) non può essere successiva"
              + "a quella di fine ( {} )", begin, end));
    }

    val groupAbsenceType =
        absenceComponentDao.groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();

    AbsenceType absenceType = null;
    LocalDate recoveryDate = null;
    boolean forceInsert = false;

    val justifiedType = absenceComponentDao.getOrBuildJustifiedType(JustifiedTypeName.all_day);

    InsertReport insertReport = absenceService.insert(person, groupAbsenceType, begin, end,
        absenceType, justifiedType, null, null, forceInsert, absenceManager);

    log.debug("Richiesto inserimento assenze per {}. "
            + "Codice/Tipo {}, dal {} al {}",
        person.getFullname(), absenceType != null ? absenceType.getCode() : groupAbsenceType,
        begin, end);

    val absences =
        absenceManager.saveAbsences(insertReport, person, begin, recoveryDate,
            justifiedType, groupAbsenceType);

    log.info("Effettuato inserimento assenze per {}. "
            + "Codice/Tipo {}, dal {} al {}",
        person.getFullname(), absenceType != null ? absenceType.getCode() : groupAbsenceType,
        begin, end);

    return ResponseEntity.ok().body(absences.stream()
        .map(ab -> absenceMapper.convert(ab)).collect(Collectors.toList()));
  }

  /**
   * Cancellazione di un'assenza.
   */
  @Operation(
      summary = "Metodo per la cancellazione di un'assenza.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' della sede a "
          + "appartiene la persona di cui inserire le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Assenza cancellata correttamente."),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato ad eliminare questa "
              + "assenza.",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Assenza non trovata con l'id fornito",
          content = @Content)
  })
  @DeleteMapping(ApiRoutes.DELETE)
  public ResponseEntity<Void> deleteAbsence(@NotNull @PathVariable("id") Long id) {
    log.debug("AbsenceController::deleteAbsence id = {}", id);
    val absence = absenceDao.byId(id
    ).orElseThrow(() -> new EntityNotFoundException("Assenza non trovata con id = " + id));
    absenceDao.delete(absence);
    rules.checkifPermitted(absence.getPersonDay().getPerson());
    log.info("Cancellata assenza {}", absence);
    return ResponseEntity.ok().build();
  }

  /**
   * Cancellazione di un'assenza.
   */
  @Operation(
      summary = "Metodo per la cancellazione di assenze dello stesso tipo all'interno "
          + "di un intervallo temporale.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' della sede a "
          + "appartiene la persona di cui inserire le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Assenze cancellata correttamente."),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato ad eliminare questa "
              + "assenza.",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Assenza non trovata con l'id fornito",
          content = @Content)
  })
  @DeleteMapping("/deleteAbsencesInPeriod")
  public ResponseEntity<Void> deleteAbsencesInPeriod(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam @NotNull @NotEmpty String absenceCode,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("begin") @NotNull LocalDate begin,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("end") @NotNull LocalDate end) {
    log.debug("AbsenceController::deleteAbsencesInPeriod id = {}, fiscalCode = {}, "
            + "absenceCode = {}, begin = {}, end = {}",
        id, fiscalCode, absenceCode, begin, end);
    val person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    rules.checkifPermitted(person);
    val absenceType = absenceTypeDao.getAbsenceTypeByCode(absenceCode)
        .orElseThrow(() ->
            new EntityNotFoundException("AbsenceCode non trovato con codice " + absenceCode));

    val deletedAbsences = absenceManager.removeAbsencesInPeriod(
        person, begin, end, absenceType);

    log.info("Deleted {} absences via REST for {}, code = {}, from {} to {}",
        deletedAbsences, person.getFullname(), absenceCode, begin, end);
    return ResponseEntity.ok().build();
  }

  /**
   * Elenco delle assenze in un mese.
   */
  @Operation(
      summary = "Visualizzazione della lista delle assenza di una persona in un mese.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituito l'elenco delle assenze in un mese"),
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
  @GetMapping("/absenceTypeInMonth")
  public ResponseEntity<List<AbsenceTypeDto>> absenceTypeInMonth(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("year") Integer year,
      @RequestParam("month") Integer month) {
    log.debug("AbsenceController::absenceTypeInMonth year = {} month = {}", year, month);

    val person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    log.debug("AbsenceController::absenceTypeInMonth person = {}", person);

    rules.checkifPermitted(person);

    YearMonth yearMonth = YearMonth.of(year, month);
    Map<AbsenceType, Long> absenceTypeInMonth =
        absenceTypeDao.getAbsenceTypeInPeriod(person,
            DateUtility.getMonthFirstDay(yearMonth),
            Optional.ofNullable(DateUtility.getMonthLastDay(yearMonth)));

    log.debug("AbsenceController::absenceTypeInMonth absenceTypeInMonth = {}", absenceTypeInMonth);

    List<AbsenceTypeDto> dtoList = Lists.newArrayList();

    absenceTypeInMonth.entrySet().forEach((absenceType) -> {
      AbsenceTypeDto dto = new AbsenceTypeDto();
      dto.setCode(absenceType.getKey().code);
      dto.setDescription(absenceType.getKey().getDescription());
      dto.setNumberOfDays(absenceType.getValue().intValue());
      dtoList.add(dto);
    });

    return ResponseEntity.ok().body(dtoList);
  }

  /**
   * Visualizzazione della data in cui si è effettuata l'assenza nel mese.
   */
  @Operation(
      summary = "Visualizzazione della data in cui si è effettuata l'assenza nel mese.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituita la data in cui si è effettuata l'assenza nel mese"),
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
  @GetMapping("/absenceInMonth")
  public ResponseEntity<AbsenceInMonthDto> absenceInMonth(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("code") String code,
      @RequestParam("year") Integer year,
      @RequestParam("month") Integer month) {
    log.debug("AbsenceController::absenceInMonth code= {} year = {} month = {}", code, year, month);

    val person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    log.debug("AbsenceController::absenceTypeInMonth person = {}", person);

    rules.checkifPermitted(person);

    YearMonth yearMonth = YearMonth.of(year, month);
    List<Absence> absences = absenceDao.getAbsenceByCodeInPeriod(
        Optional.ofNullable(person),
        Optional.ofNullable(code),
        DateUtility.getMonthFirstDay(yearMonth),
        DateUtility.getMonthLastDay(yearMonth),
        Optional.empty(),
        false,
        true);

    log.debug("AbsenceController::absenceInMonth absences = {}", absences);

    List<LocalDate> dateAbsences = absences.stream().map(AbsenceToDate.INSTANCE).toList();

    log.debug("AbsenceController::absenceInMonth dateAbsences = {}", dateAbsences);

    AbsenceInMonthDto dto = new AbsenceInMonthDto();
    dto.setCode(code);
    dto.setDateAbsences(dateAbsences);

    return ResponseEntity.ok().body(dto);
  }
}