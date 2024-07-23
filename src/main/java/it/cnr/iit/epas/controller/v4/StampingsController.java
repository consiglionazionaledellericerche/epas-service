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
import it.cnr.iit.epas.dto.v4.StampingFormDto;
import it.cnr.iit.epas.dto.v4.ZoneDto;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
import it.cnr.iit.epas.dto.v4.mapper.PersonShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.StampingFormDtoMapper;
import it.cnr.iit.epas.manager.PersonDayManager;
import it.cnr.iit.epas.manager.StampingManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.BadgeReader;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.Stamping;
import it.cnr.iit.epas.models.Stamping.WayType;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.models.Zone;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  private final UserDao userDao;
  private final PersonDao personDao;
  private final StampingDao stampingDao;
//  private final StampingsHistoryDao stampingsHistoryDao;
  private final EntityToDtoConverter entityToDtoConverter;
  private final StampingManager stampingManager;
  private final PersonDayManager personDayManager;
  private final SecurityRules rules;
  private final SecureUtils securityUtils;
  private final WrapperFactory wrapperFactory;

  private final StampingFormDtoMapper stampingFormDtoMapper;

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
  @PutMapping("/insert")
  public ResponseEntity<String> insert(@NotNull @RequestBody @Valid StampingCreateDto stampingCreateDto) {
    log.debug("StampingsController::insert stampingCreateDto = {}",stampingCreateDto);

    Long personId = stampingCreateDto.getPersonId();
    Person person = personDao.getPersonById(personId);
    if (person == null) {
      return ResponseEntity.notFound().build();
    }

    LocalDate stampingDate = stampingCreateDto.getDate();
    String time = stampingCreateDto.getTime();
    Preconditions.checkState(!stampingDate.isAfter(LocalDate.now()));

    Stamping stamping = new Stamping();
    stamping.setDate(stampingManager.deparseStampingDateTime(stampingDate, time));

    String zone = stampingCreateDto.getZone();
    String way = stampingCreateDto.getWay();
    String stampType = stampingCreateDto.getStampType();
    String note = stampingCreateDto.getNote();

    if (way == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    WayType wayType = WayType.valueOf(way);
    stamping.setWay(wayType);

    StampTypes stampTypeEnum = StampTypes.valueOf(stampType);
    stamping.setStampType(stampTypeEnum);

    stamping.setNote(note);

    boolean newInsert = !stampingDao.isPersistent(stamping);

    final PersonDay personDay = personDayManager.getOrCreateAndPersistPersonDay(person, stampingDate);
    stamping.setPersonDay(personDay);
    // non è usato il costruttore con la add, quindi aggiungiamo qui a mano:
    personDay.getStampings().add(stamping);
    log.debug("StampingsController::insert stamping = {}",stamping.toString());

    rules.checkifPermitted(stamping);
    final User currentUser = securityUtils.getCurrentUser().get();

    stamping.setStampingZone(zone);

    String result = stampingManager
        .persistStamping(stamping, person, currentUser, newInsert, false);

    if (!Strings.isNullOrEmpty(result)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    return ResponseEntity.ok().body(result);
  }

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
  @GetMapping("/stampingForm")
  public ResponseEntity<StampingFormDto> stampingForm(
      @RequestParam("personId") Long personId,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestParam("date") LocalDate date
  ) {
    log.debug("StampingsController::stampingForm personId = {}  date = {}",personId, date);
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
        .flatMap(br ->  br.getZones().stream().filter(z -> z != null)).collect(Collectors.toList());

    log.debug("StampingsController::stampingForm badgeReaders = {}  zones = {}",badgeReaders, zones);

    StampingFormDto dto = new StampingFormDto();
    List<String> offsiteStrings = offsite.stream()
        .map(StampTypes::name)
        .collect(Collectors.toList());
    List<ZoneDto> zonesDto = Lists.newArrayList();
    for (Zone zone : zones){
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
        .create(securityUtils.getCurrentUser().get().getPerson());
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

    if (autocertification == true  && insertOffsite == true) {
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
}
