/*
 * Copyright (C) 2024  Consiglio Nazionale delle Ricerche
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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.StampingDao;
import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.dao.history.HistoryValue;
import it.cnr.iit.epas.dao.history.StampingHistoryDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperPerson;
import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.dto.v4.HistoryValueDto;
import it.cnr.iit.epas.dto.v4.StampTypeDto;
import it.cnr.iit.epas.dto.v4.StampingCreateDto;
import it.cnr.iit.epas.dto.v4.StampingEditFormDto;
import it.cnr.iit.epas.dto.v4.StampingFormDto;
import it.cnr.iit.epas.dto.v4.ZoneDto;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
import it.cnr.iit.epas.dto.v4.mapper.StampingFormDtoMapper;
import it.cnr.iit.epas.manager.PersonDayManager;
import it.cnr.iit.epas.manager.StampingManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.BadgeReader;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.Stamping;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.Zone;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Metodi REST per l'inserimento delle timbrature manuali.
 */
@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(
    name = "stampings Controller",
    description = "Inserimento delle timbrature manuali.")
@Transactional
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/stampings")
public class StampingsController {

  private final PersonDao personDao;
  private final StampingDao stampingDao;
  private final StampingHistoryDao stampingsHistoryDao;

  private final EntityToDtoConverter entityToDtoConverter;
  private final StampingManager stampingManager;
  private final PersonDayManager personDayManager;
  private final SecurityRules rules;
  private final SecureUtils securityUtils;
  private final WrapperFactory wrapperFactory;
  private final StampingFormDtoMapper stampingFormDtoMapper;
  private final Validator validator;

  /**
   * Recupera le informazioni per costruire la form di inserimento della timbratura.
   */
  @Operation(
      summary = "Recupera le informazioni per costruire la form di inserimento della timbratura.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui recuperare le informazioni per costruire"
          + "la form di inserimento timbrature con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle informazioni per "
          + "costruire la form di inserimento timbrature.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituisce le informazioni per la nuova timbratura"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a eseguire "
              + "l'inserimento delle timbrature",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping("/insert")
  public ResponseEntity<StampingFormDto> insertStampingForm(
      @RequestParam("personId") Long personId,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("date") LocalDate date
  ) {
    log.debug("StampingsController::insertStampingForm personId = {}  date = {}", personId, date);
    final Person person = personDao.getPersonById(personId);
    if (person == null) {
      return ResponseEntity.notFound().build();
    }

    Preconditions.checkNotNull(date);
    Preconditions.checkState(!date.isAfter(LocalDate.now()));

    rules.checkifPermitted(person);

    List<StampTypeDto> offsite = Lists.newArrayList();
    StampTypeDto offsiteDto = new StampTypeDto();
    offsiteDto.setName(StampTypes.LAVORO_FUORI_SEDE.name());
    offsiteDto.setDescription(StampTypes.LAVORO_FUORI_SEDE.getDescription());
    offsiteDto.setCode(StampTypes.LAVORO_FUORI_SEDE.getCode());
    offsiteDto.setIdentifier(StampTypes.LAVORO_FUORI_SEDE.getIdentifier());
    offsite.add(offsiteDto);
    boolean insertOffsite = false;
    boolean insertNormal = true;
    boolean autocertification = false;

    List<BadgeReader> badgeReaders = person.getBadges()
        .stream().map(b -> b.getBadgeReader()).collect(Collectors.toList());
    List<Zone> zones = badgeReaders.stream()
        .flatMap(br -> br.getZones().stream().filter(z -> z != null)).collect(Collectors.toList());

    log.debug("StampingsController::stampingForm badgeReaders = {}  zones = {}", badgeReaders,
        zones);

    User user = securityUtils.getCurrentUser().get();

    StampingFormDto dto = new StampingFormDto();

    List<StampTypeDto> stampTypesDto = Lists.newArrayList();

    for (StampTypes stampType : UserDao.getAllowedStampTypes(user)) {
      stampTypesDto.add(stampingFormDtoMapper.convert(stampType));
    }

    List<ZoneDto> zonesDto = Lists.newArrayList();
    for (Zone zone : zones) {
      zonesDto.add(stampingFormDtoMapper.convert(zone));
    }

    if (user.isSystemUser()) {
      dto.setPerson(stampingFormDtoMapper.convert(person));
      dto.setDate(date);
      dto.setOffsite(offsite);
      dto.setInsertOffsite(insertOffsite);
      dto.setInsertNormal(insertNormal);
      dto.setAutocertification(autocertification);
      dto.setZones(zonesDto);
      dto.setStampTypes(stampTypesDto);
      return ResponseEntity.ok(dto);
    }

    IWrapperPerson wrperson = wrapperFactory.create(user.getPerson());
    if (user.getPerson() != null && user.getPerson().equals(person)) {
      if (UserDao.getAllowedStampTypes(user).contains(StampTypes.LAVORO_FUORI_SEDE)) {
        insertOffsite = true;
        insertNormal = false;
      }
    }

    if (user.getPerson() != null && user.getPerson().equals(person)
        && !wrperson.isTechnician()) {
      if (person.getOffice().checkConf(EpasParam.TR_AUTOCERTIFICATION, "true")) {
        autocertification = true;
      }
    }

    if (autocertification == true && insertOffsite == true) {
      insertOffsite = false;
    }

    dto.setPerson(stampingFormDtoMapper.convert(person));
    dto.setDate(date);
    dto.setOffsite(offsite);
    dto.setInsertOffsite(insertOffsite);
    dto.setInsertNormal(insertNormal);
    dto.setAutocertification(autocertification);
    dto.setZones(zonesDto);
    //render(person, date, offsite, insertOffsite, insertNormal, autocertification, zones);

    return ResponseEntity.ok(dto);
  }


  /**
   * Recupera le informazioni per costruire la form di modifica della timbratura.
   */
  @Operation(
      summary = "Recupera le informazioni per costruire la form di modifica della timbratura.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui recuperare le informazioni per costruire"
          + "la form di inserimento timbrature con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle informazioni per "
          + "costruire la form di inserimento timbrature.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituisce le informazioni per la nuova timbratura"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a eseguire "
              + "l'inserimento delle timbrature",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping("/edit")
  public ResponseEntity<StampingEditFormDto> editStampingForm(
      @RequestParam("stampingId") Long stampingId) {
    log.debug("StampingsController::editStampingForm stampingId = {}", stampingId);

    final Stamping stamping = stampingDao.getStampingById(stampingId);
    log.debug("StampingsController::editStampingForm stamping = {}", stamping);
    Preconditions.checkNotNull(stamping);

    rules.checkifPermitted(stamping);

    final List<HistoryValue<Stamping>> historyStamping = stampingsHistoryDao
        .stampings(stamping.getId());

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
    boolean ownStamping = false;
    final Person person = stamping.getPersonDay().getPerson();
    final LocalDate date = stamping.getDate().toLocalDate();
    final String time = stamping.getDate().format(formatter);

    boolean isOffSiteWork = false;
    boolean isServiceReasons = false;
    Optional<User> user = securityUtils.getCurrentUser();

    if (user.isPresent() && person.equals(user.get().getPerson())
        && !user.get().hasRoles(Role.PERSONNEL_ADMIN)) {
      ownStamping = true;
    }

    if (stamping.isOffSiteWork()) {
      //render("@editOffSite", stamping, person, date, historyStamping);
      isOffSiteWork = true;
    } else if (stamping.isServiceReasons() && ownStamping) {
      //render("@editServiceReasons", stamping, person, date, historyStamping);
      isServiceReasons = true;
    }

    List<StampTypeDto> stampTypesDto = Lists.newArrayList();

    for (StampTypes stampType : UserDao.getAllowedStampTypes(user.get())) {
      stampTypesDto.add(stampingFormDtoMapper.convert(stampType));
    }

    List<HistoryValueDto> historyStampingDto = Lists.newArrayList();
    HistoryValueDto historyDto = new HistoryValueDto();
    for (HistoryValue<Stamping> history : historyStamping) {
      historyDto.setFormattedOwner(history.formattedOwner());
      historyDto.setFormattedRevisionDate(history.formattedRevisionDate());
      historyDto.setTypeIsAdd(history.typeIsAdd());
      historyDto.setTypeIsDel(history.typeIsDel());
      historyStampingDto.add(historyDto);
    }

    List<BadgeReader> badgeReaders = person.getBadges()
        .stream().map(b -> b.getBadgeReader()).collect(Collectors.toList());

    List<Zone> zones = badgeReaders.stream()
        .flatMap(br -> br.getZones().stream().filter(z -> z != null)).collect(Collectors.toList());
    List<ZoneDto> zonesDto = Lists.newArrayList();
    for (Zone zone : zones) {
      zonesDto.add(stampingFormDtoMapper.convert(zone));
    }

    StampingEditFormDto stampingdto = new StampingEditFormDto();
//    render(stamping, person, date, historyStamping, ownStamping, zones);
    stampingdto.setOwnStamping(ownStamping);
    stampingdto.setHistoryStamping(historyStampingDto);
    stampingdto.setServiceReasons(isServiceReasons);
    stampingdto.setOffSiteWork(isOffSiteWork);
    stampingdto.setPerson(stampingFormDtoMapper.convert(person));
    stampingdto.setPersonId(person.getId());
    stampingdto.setDate(date);
    stampingdto.setTime(time);
    stampingdto.setWay(stamping.getWay().name());
    stampingdto.setStampTypeOpt(stampingFormDtoMapper.convert(stamping.getStampType()));
    stampingdto.setStampTypes(stampTypesDto);
    stampingdto.setStampType(stamping.getStampType() != null 
        ? stamping.getStampType().name() : null);
    stampingdto.setZones(zonesDto);
    stampingdto.setNote(stamping.getNote());
    stampingdto.setPlace(stamping.getPlace());
    stampingdto.setReason(stamping.getReason());

    return ResponseEntity.ok().body(stampingdto);
  }

  /**
   * Inserimento di una timbratura.
   */
  @Operation(
      summary = "Inserimento di una timbratura.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Inserita la timbratura"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a eseguire "
              + "l'inserimento delle timbrature",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @PostMapping(ApiRoutes.CREATE)
  public ResponseEntity<Map<String, String>> create(
      @NotNull @RequestBody @Valid StampingCreateDto stampingCreateDto) {
    log.debug("StampingsController::insert stampingCreateDto = {}", stampingCreateDto);

    Map<Integer, String> resultMap = manageStamping(stampingCreateDto, true);
    Map<String, String> response = new HashMap<>();
    Integer statusCode = resultMap.keySet().iterator().next();

    if (statusCode == 404) {
      return ResponseEntity.notFound().build();
    }
    if (statusCode == 400) {
      return ResponseEntity.badRequest().build();
    }
    if (statusCode == 409) {
      response.put("message", "Timbratura già presente.");
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    response.put("message", "Timbratura salvata con successo.");
    return ResponseEntity.ok().body(response);
  }

  /**
   * Aggiornamento di una timbratura.
   */
  @Operation(
      summary = "Aggiornamento di una timbratura.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui aggiornare la timbratura e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle timbrature")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Aggiornata la timbratura"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a eseguire "
              + "l'inserimento delle timbrature",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @PostMapping(ApiRoutes.UPDATE)
  public ResponseEntity<Map<String, String>> update(
      @NotNull @RequestBody @Valid StampingCreateDto stampingCreateDto) {
    log.debug("StampingsController::insert stampingCreateDto = {}", stampingCreateDto);

    Map<Integer, String> resultMap = manageStamping(stampingCreateDto, false);
    Map<String, String> response = new HashMap<>();

    Integer statusCode = resultMap.keySet().iterator().next();

    if (statusCode == 404) {
      return ResponseEntity.notFound().build();
    }
    if (statusCode == 400) {
      return ResponseEntity.badRequest().build();
    }
    if (statusCode == 409) {
      response.put("message", "Timbratura già presente.");
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    response.put("message", "Timbratura salvata con successo.");
    return ResponseEntity.ok().body(response);
  }

  /**
   * Effettua il salvataggio dei soli campi reason, place e note di una timbratura per motivi di
   * servizio.
   */
  @Operation(
      summary = "AEffettua il salvataggio dei soli campi reason, place e note di una timbratura per motivi di servizio.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui aggiornare la timbratura e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle timbrature")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Aggiornata la timbratura"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a eseguire "
              + "l'inserimento delle timbrature",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @PostMapping("/saveServiceReasons")
  public ResponseEntity<Map<String, String>> saveServiceReasons(
      @NotNull @RequestBody @Valid StampingCreateDto stampingCreateDto) {
    log.debug("StampingsController::saveServiceReasons stampingCreateDto = {}", stampingCreateDto);
    Map<String, String> response = new HashMap<>();
    Long stampingId = stampingCreateDto.getStampingId();
    Stamping stamping = entityToDtoConverter.createEntity(stampingCreateDto);
    Preconditions.checkNotNull(stampingId);
    Preconditions.checkNotNull(stamping);
    Stamping currentStamping = stampingDao.getStampingById(stampingId);
    Preconditions.checkNotNull(currentStamping);

    currentStamping.setReason(stamping.getReason());
    currentStamping.setPlace(stamping.getPlace());
    currentStamping.setNote(stamping.getNote());
    //currentStamping.save();
    stampingDao.save(currentStamping);
    log.info("Modificata timbratura per motivi di servizio {}", currentStamping);

    final User currentUser = securityUtils.getCurrentUser().get();

    Person person = currentStamping.getPersonDay().getPerson();

    if (!currentUser.isSystemUser() && !currentUser.hasRoles(Role.PERSONNEL_ADMIN)
        && currentUser.getPerson().getId().equals(person.getId())) {
      //stampings(date.getYear(), date.getMonthOfYear());
    }
    response.put("message", "Timbratura salvata con successo.");
    return ResponseEntity.ok().body(response);
  }

  @PostMapping("/saveOffSite")
  public ResponseEntity<Map<String, String>> saveOffSite(
      @NotNull @RequestBody @Valid StampingCreateDto stampingCreateDto) {
    log.debug("StampingsController::saveOffSite stampingCreateDto = {}", stampingCreateDto);
    Map<Integer, String> resultMap = manageStamping(stampingCreateDto, false);
    Map<String, String> response = new HashMap<>();

    Integer statusCode = resultMap.keySet().iterator().next();

    if (statusCode == 404) {
      return ResponseEntity.notFound().build();
    }
    if (statusCode == 400) {
      return ResponseEntity.badRequest().build();
    }
    if (statusCode == 409) {
      response.put("message", "Timbratura già presente.");
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    response.put("message", "Timbratura salvata con successo.");
    return ResponseEntity.ok().body(response);
  }


  @DeleteMapping(ApiRoutes.DELETE)
  public ResponseEntity<Map<String, String>> delete(@NotNull @PathVariable("id") Long id) {
    log.debug("StampingController::delete id = {}", id);

    Map<String, String> response = new HashMap<>();
    Stamping stamping = stampingDao.getStampingById(id);

    if (stamping == null) {
      throw new EntityNotFoundException("Stamping not found");
    }
//    checkIfIsPossibileToDelete(contract);

    stampingDao.delete(stamping);
    log.info("Eliminata timbratura {}", stamping);
    response.put("message", "Timbratura eliminata con successo.");
    return ResponseEntity.ok().body(response);
  }


  private Map<Integer, String> manageStamping(StampingCreateDto stampingCreateDto,
      boolean newInsert) {
    Map<Integer, String> resultMap = new HashMap<>();
    Long personId = stampingCreateDto.getPersonId();
    log.debug("StampingsController::manageStamping  personId = {}", personId);
    Person person = personDao.getPersonById(personId);
    log.debug("StampingsController::manageStamping  person = {}", person);
    if (person == null) {
      resultMap.put(404, null);
      return resultMap;
    }

    Stamping stamping = entityToDtoConverter.createEntity(stampingCreateDto);
    log.debug("StampingsController::entityToDtoConverter.createEntity = {}", stamping);

    boolean offsite = stampingCreateDto.isOffsite();

    LocalDate stampingDate = stampingCreateDto.getDate();
    String time = stampingCreateDto.getTime();

    if (stamping.getWay() == null) {
      log.debug("StampingsController::stampingDao.isPersistent(stamping) = {}",
          stampingDao.isPersistent(stamping));
      resultMap.put(400, null);
      return resultMap;
    }

    if (!offsite) {
      Preconditions.checkState(!stampingDate.isAfter(LocalDate.now()));
    } else {
      stamping.setDate(LocalDateTime.now());
      Set<ConstraintViolation<Stamping>> violations = validator.validate(stamping);
      if (!violations.isEmpty()) {
        List<StampTypes> offsiteList = Lists.newArrayList();
        offsiteList.add(StampTypes.LAVORO_FUORI_SEDE);
        resultMap.put(400, null);
        return resultMap;
      }
    }

    stamping.setDate(stampingManager.deparseStampingDateTime(stampingDate, time));

    if (stampingDao.getStamping(stamping.getDate(), person, stamping.getWay()).isPresent()) {
      log.info("Timbratura delle {} già presente per {} (matricola = {}) ",
          stamping.getDate(), person, person.getNumber());
      newInsert = false;
    }

    log.debug("StampingsController::!stampingDao.isPersistent(stamping) = {}", stamping);

    if (newInsert) {
      final PersonDay personDay = personDayManager.getOrCreateAndPersistPersonDay(person,
          stampingDate);
      stamping.setPersonDay(personDay);
      // non è usato il costruttore con la add, quindi aggiungiamo qui a mano:
      personDay.getStampings().add(stamping);
      log.debug("StampingsController::insert stamping = {}", stamping.toString());
    }

    rules.checkifPermitted(stamping);
    final User currentUser = securityUtils.getCurrentUser().get();
    if (!offsite) {
      String zone = stampingCreateDto.getZone();
      stamping.setStampingZone(zone);
    }

    String result = stampingManager
        .persistStamping(stamping, person, currentUser, newInsert, false);

    if (!Strings.isNullOrEmpty(result)) {
      // Stamping already present (409)
      resultMap.put(409, null);
      return resultMap;
    }

    if (!currentUser.isSystemUser() && !currentUser.hasRoles(Role.PERSONNEL_ADMIN)
        && currentUser.getPerson().getId().equals(person.getId())) {
      //stampings(date.getYear(), date.getMonthOfYear());
    }

    resultMap.put(200, result);

    return resultMap;
  }

}
