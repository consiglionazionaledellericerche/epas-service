package it.cnr.iit.epas.controller.v4;

import com.google.common.base.Preconditions;
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
import it.cnr.iit.epas.dao.wrapper.IWrapperPerson;
import it.cnr.iit.epas.dto.v4.StampingCreateDto;
import it.cnr.iit.epas.dto.v4.StampingEditFormDto;
import it.cnr.iit.epas.dto.v4.StampingFormDto;
import it.cnr.iit.epas.dto.v4.ZoneDto;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
import it.cnr.iit.epas.dto.v4.mapper.DtoToEntityMapper;
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
import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.models.Zone;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.google.common.base.Strings;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Validator;

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

  private final UserDao userDao;
  private final PersonDao personDao;
  private final StampingDao stampingDao;
  //  private final StampingsHistoryDao stampingsHistoryDao;
  private final EntityToDtoConverter entityToDtoConverter;
  private final DtoToEntityMapper dtoToEntityMapper;
  private final StampingManager stampingManager;
  private final PersonDayManager personDayManager;
  private final SecurityRules rules;
  private final SecureUtils securityUtils;
  private final WrapperFactory wrapperFactory;
  private final StampingFormDtoMapper stampingFormDtoMapper;
  private final Validator validator;

  /**
   * Recupera le informazioni per costruire la form di inserimento della timbratura
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

    List<StampTypes> offsite = Lists.newArrayList();
    offsite.add(StampTypes.LAVORO_FUORI_SEDE);
    boolean insertOffsite = false;
    boolean insertNormal = true;
    boolean autocertification = false;

    List<BadgeReader> badgeReaders = person.getBadges()
        .stream().map(b -> b.getBadgeReader()).collect(Collectors.toList());
    List<Zone> zones = badgeReaders.stream()
        .flatMap(br -> br.getZones().stream().filter(z -> z != null)).collect(Collectors.toList());

    log.debug("StampingsController::stampingForm badgeReaders = {}  zones = {}", badgeReaders,
        zones);

    StampingFormDto dto = new StampingFormDto();
    List<String> offsiteStrings = offsite.stream()
        .map(StampTypes::name)
        .collect(Collectors.toList());
    List<ZoneDto> zonesDto = Lists.newArrayList();
    for (Zone zone : zones) {
      zonesDto.add(stampingFormDtoMapper.convert(zone));
    }

    User user = securityUtils.getCurrentUser().get();

    if (user.isSystemUser()) {
      dto.setPerson(stampingFormDtoMapper.convert(person));
      dto.setDate(date);
      dto.setOffsite(offsiteStrings);
      dto.setInsertOffsite(insertOffsite);
      dto.setInsertNormal(insertNormal);
      dto.setAutocertification(autocertification);
      dto.setZones(zonesDto);
      return ResponseEntity.ok(dto);
    }

    IWrapperPerson wrperson = wrapperFactory
        .create(user.getPerson());
    if (user.getPerson() != null && user.getPerson().equals(person)) {
      if (userDao.getAllowedStampTypes(user).contains(StampTypes.LAVORO_FUORI_SEDE)) {
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
    dto.setOffsite(offsiteStrings);
    dto.setInsertOffsite(insertOffsite);
    dto.setInsertNormal(insertNormal);
    dto.setAutocertification(autocertification);
    dto.setZones(zonesDto);
    //render(person, date, offsite, insertOffsite, insertNormal, autocertification, zones);

    return ResponseEntity.ok(dto);
  }


  /**
   * Recupera le informazioni per costruire la form di modifica della timbratura
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

    Preconditions.checkNotNull(stamping);

    rules.checkifPermitted(stamping);

    // manca StampingHistoryDao
//    final List<HistoryValue<Stamping>> historyStamping = stampingsHistoryDao
//        .stampings(stamping.getId());

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
    boolean ownStamping = false;
    final Person person = stamping.getPersonDay().getPerson();
    final LocalDate date = stamping.getDate().toLocalDate();
    final String time = stamping.getDate().format(formatter);

    if (stamping.isOffSiteWork()) {
      //render("@editOffSite", stamping, person, date, historyStamping);
    }
    Optional<User> user = securityUtils.getCurrentUser();

    if (user.isPresent() && person.equals(user.get().getPerson())
        && !user.get().hasRoles(Role.PERSONNEL_ADMIN)) {
      ownStamping = true;
    }
    if (stamping.isServiceReasons() && ownStamping) {
      //render("@editServiceReasons", stamping, person, date, historyStamping);
    }

    List<BadgeReader> badgeReaders = person.getBadges()
        .stream().map(b -> b.getBadgeReader()).collect(Collectors.toList());

    List<Zone> zones = badgeReaders.stream()
        .flatMap(br ->  br.getZones().stream().filter(z -> z != null)).collect(Collectors.toList());
    List<ZoneDto> zonesDto = Lists.newArrayList();
    for (Zone zone : zones) {
      zonesDto.add(stampingFormDtoMapper.convert(zone));
    }

    StampingEditFormDto stampingdto = new StampingEditFormDto();
//    render(stamping, person, date, historyStamping, ownStamping, zones);
    stampingdto.setOwnStamping(ownStamping);
    stampingdto.setPersonId(person.getId());
    stampingdto.setDate(date);
    stampingdto.setTime(time);
    stampingdto.setWay(stamping.getWay().name());
    stampingdto.setStampType(stamping.getStampType().name());
    stampingdto.setZones(zonesDto);
    stampingdto.setNote(stamping.getNote());

    return ResponseEntity.ok().body(stampingdto);
  }

  /**
   * Inserimento di una timbratura
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
  @PutMapping(ApiRoutes.CREATE)
  public ResponseEntity<String> create(
      @NotNull @RequestBody @Valid StampingCreateDto stampingCreateDto) {
    log.debug("StampingsController::insert stampingCreateDto = {}", stampingCreateDto);

    Long personId = stampingCreateDto.getPersonId();
    Person person = personDao.getPersonById(personId);
    if (person == null) {
      return ResponseEntity.notFound().build();
    }

    Stamping stamping = entityToDtoConverter.createEntity(stampingCreateDto);
    log.debug("StampingsController::entityToDtoConverter.createEntity = {}", stamping);

    boolean offsite = stampingCreateDto.isOffsite();

    LocalDate stampingDate = stampingCreateDto.getDate();
    String time = stampingCreateDto.getTime();

    if (stamping.getWay() == null) {
      log.debug("StampingsController::stampingDao.isPersistent(stamping) = {}",
          stampingDao.isPersistent(stamping));
//      List<HistoryValue<Stamping>> historyStamping = Lists.newArrayList();
//      if (stamping.isPersistent()) {
//        historyStamping = stampingsHistoryDao.stampings(stamping.id);
//      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


    if (!offsite) {
      Preconditions.checkState(!stampingDate.isAfter(LocalDate.now()));
    } else {
      stamping.setDate(LocalDateTime.now());
      Set<ConstraintViolation<Stamping>> violations = validator.validate(stamping);
      if (!violations.isEmpty()) {
        List<StampTypes> offsiteList = Lists.newArrayList();
        offsiteList.add(StampTypes.LAVORO_FUORI_SEDE);
        boolean disableInsert = false;
        User user = securityUtils.getCurrentUser().get();
        if (user.getPerson() != null) {
          if (person.getOffice().checkConf(EpasParam.WORKING_OFF_SITE, "true")
              && person.checkConf(EpasParam.OFF_SITE_STAMPING, "true")) {
            disableInsert = true;
          }
        }
        return ResponseEntity.badRequest().build();
        //render("@editOffSite", stamping, person, date, time, disableInsert, offsite);
      }
    }

    stamping.setDate(stampingManager.deparseStampingDateTime(stampingDate, time));

    //boolean newInsert = !stampingDao.isPersistent(stamping); // questo non funziona
    boolean newInsert = true;

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
      return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }

    if (!currentUser.isSystemUser() && !currentUser.hasRoles(Role.PERSONNEL_ADMIN)
        && currentUser.getPerson().getId().equals(person.getId())) {
      //stampings(date.getYear(), date.getMonthOfYear());
    }

    return ResponseEntity.ok().body(result);
  }

  @DeleteMapping(ApiRoutes.DELETE)
  public ResponseEntity<Void> delete(@NotNull @PathVariable("id") Long id) {
    log.debug("StampingController::delete id = {}", id);
    val stamping = stampingDao.getStampingById(id);

    if (stamping == null) {
      throw new EntityNotFoundException("Stamping not found");
    }
//    checkIfIsPossibileToDelete(contract);

    stampingDao.delete(stamping);
    log.info("Eliminata timbratura {}", stamping);

    return ResponseEntity.ok().build();
  }
}
