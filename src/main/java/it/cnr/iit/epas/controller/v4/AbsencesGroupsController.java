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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.controller.v4.utils.PersonFinder;
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.AbsenceTypeDao;
import it.cnr.iit.epas.dao.CategoryTabDao;
import it.cnr.iit.epas.dao.GroupAbsenceTypeDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.dto.v4.AbsenceFormDto;
import it.cnr.iit.epas.dto.v4.AbsenceFormSaveDto;
import it.cnr.iit.epas.dto.v4.AbsenceFormSaveResponseDto;
import it.cnr.iit.epas.dto.v4.AbsenceFormSimulationDto;
import it.cnr.iit.epas.dto.v4.AbsenceFormSimulationResponseDto;
import it.cnr.iit.epas.dto.v4.AbsenceGroupsDto;
import it.cnr.iit.epas.dto.v4.AbsencePeriodTerseDto;
import it.cnr.iit.epas.dto.v4.AbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.DayInPeriodDto;
import it.cnr.iit.epas.dto.v4.GroupAbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.PeriodChainDto;
import it.cnr.iit.epas.dto.v4.TemplateRowDto;
import it.cnr.iit.epas.dto.v4.mapper.AbsenceFormMapper;
import it.cnr.iit.epas.dto.v4.mapper.AbsenceFormSimulationResponseMapper;
import it.cnr.iit.epas.dto.v4.mapper.AbsenceGroupsMapper;
import it.cnr.iit.epas.manager.AbsenceManager;
import it.cnr.iit.epas.manager.recaps.absencegroups.AbsenceGroupsRecap;
import it.cnr.iit.epas.manager.recaps.absencegroups.AbsenceGroupsRecapFactory;
import it.cnr.iit.epas.manager.services.absences.AbsenceForm;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.AbsenceService.InsertReport;
import it.cnr.iit.epas.manager.services.absences.model.AbsencePeriod;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod.TemplateRow;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.CategoryGroupAbsenceType;
import it.cnr.iit.epas.models.absences.CategoryTab;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Metodi REST per la gestione dei gruppi di assenza.
 */
@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(
    name = "Absences Groups Controller",
    description = "Gestione e visualizzazione deli gruppi di assenza.")
@Transactional
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/absencesGroups")
public class AbsencesGroupsController {

  private final AbsenceGroupsRecapFactory absenceGroupsRecapFactory;
  private final AbsenceGroupsMapper absenceGroupsMapper;
  private final AbsenceFormMapper absenceFormMapper;
  private final AbsenceFormSimulationResponseMapper absenceFormSimulationResponseMapper;
  private final AbsenceService absenceService;
  private final CategoryTabDao categoryTabDao;
  private final OfficeDao officeDao;
  private final PersonDao personDao;
  private final AbsenceDao absenceDao;
  private final GroupAbsenceTypeDao groupAbsenceTypeDao;
  private final AbsenceTypeDao absenceTypeDao;
  private final AbsenceComponentDao absenceComponentDao;
  private final AbsenceManager absenceManager;
  private final PersonFinder personFinder;
  private final SecurityRules rules;
  private SecureUtils secureUtils;

  // Mappa che associa i nomi dei target alle loro classi
  private static final Map<String, Class<?>> targetFromClassMap = new HashMap<>();
  private static final Map<String, Class<?>> targetToClassMap = new HashMap<>();
  private static final Map<String,Map<String,Object>> targetDaoMap = new HashMap<>();
  private static final Map<String,List<String>> targetMethodMap = new HashMap<>();

  private void init() {
    targetFromClassMap.put("Office", Person.class);
    targetFromClassMap.put("Person", Absence.class);

    // Popolazione della mappa che associa i nomi dei target alle loro classi
    targetToClassMap.put("Office", Office.class);
    targetToClassMap.put("Person", Person.class);

    // Popolazione della mappa che associa i nomi dei target ai loro rispettivi DAO
    Map<String, Object> officeDaoActions = new HashMap<>();
    officeDaoActions.put("byId", personDao);
    targetDaoMap.put("Office", officeDaoActions);

    Map<String, Object> personDaoActions = new HashMap<>();
    personDaoActions.put("byId", absenceDao);
    targetDaoMap.put("Person", personDaoActions);

    List<String> officeMethods = new ArrayList<>();
    officeMethods.add("getOffice");
    targetMethodMap.put("Office", officeMethods);

    List<String> personMethods = new ArrayList<>();
    personMethods.add("getPersonDay");
    personMethods.add("getPerson");
    targetMethodMap.put("Person", personMethods);
  }

  private Object findEntityById(Object dao, Class<?> targetClass, String methodToCall, Long id) throws Exception {
    Method findByIdMethod = dao.getClass().getMethod(methodToCall, Long.class);
    Optional<?> optionalEntity = (Optional<?>) findByIdMethod.invoke(dao, id);
    if (optionalEntity.isEmpty()) {
      throw new EntityNotFoundException(targetClass.getSimpleName() + " not found with id: " + id);
    }
    return optionalEntity.get();
  }
  private Object entityApplyMethod(Object target, String methodToCall) throws Exception {
    // Usa riflessione per invocare il metodo di ricerca per ID sul DAO corretto
    return target.getClass().getMethod(methodToCall).invoke(target);
  }

  /**
   * Visualizzazione delle informazioni di un'assenza.
   */
  @Operation(
      summary = "Verifica se l'utente corrente ha l'accesso ad un certo endpoint REST relativo alle assenze.")
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
      @RequestParam("path") String path,
      @RequestParam("target") Optional<String> target,
      @RequestParam("id") Optional<Long> id) throws Exception {

    init();
    Object targetFromObject = null;
    Object targetToObject = null;

    log.debug("GeneralizedController::secureCheck method= {}, path = {}, id = {}, target={}",
        method, path, id, target);

    if (target.isPresent()) {
      String targetType = target.get();
      Class<?> targetFromClass = targetFromClassMap.get(targetType);
      Class<?> targetToClass = targetToClassMap.get(targetType);
      Map<String,Object> daoAction = targetDaoMap.get(targetType);
      String action = new ArrayList<>(daoAction.keySet()).get(0);
      log.debug("GeneralizedController::secureCheck targetType={} targetFromClass= {}, targetToClass = {}, daoAction={}",
          targetType, targetFromClass, targetToClass, action);

      if (targetFromClass == null || targetToClass == null || daoAction == null) {
        throw new IllegalArgumentException("Invalid target type: " + targetType);
      }

      if (id.isPresent()) {
        Long entityId = id.get();
        targetFromObject = findEntityById(daoAction.get(action), targetFromClass, action, entityId);

        log.debug("GeneralizedController::secureCheck targetFromObject={}",targetFromObject);
        log.debug("GeneralizedController::secureCheck targetToObject={}",targetToObject);


        if (targetFromObject == null) {
          throw new EntityNotFoundException(targetType + " not found");
        }
      }

      targetToObject = targetFromObject;
      List<String> targetMethods = targetMethodMap.get(targetType);
      for (String methodToApply : targetMethods) {
        log.debug("GeneralizedController::secureCheck targetToObject={}",targetToObject);
        targetToObject = entityApplyMethod(targetToObject, methodToApply);
      }
    }

    log.debug("GeneralizedController::secureCheck targetFromObject={}",targetFromObject);
    log.debug("GeneralizedController::secureCheck targetToObject={}",targetToObject);

    //Le drools sulle assenze non controllano come target l'assenza ma la person
    return ResponseEntity.ok(rules.check(method, path, targetToObject));
  }

  /**
   * Verifica se l'utente corrente ha l'accesso ad un certo endpoint REST relativo alle assenze e
   * con target Office.
   */
  @Operation(
      summary = "Verifica se l'utente corrente ha l'accesso ad un certo endpoint REST relativo alle assenze.")
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
  @GetMapping("/secureCheckOffice")
  public ResponseEntity<Boolean> secureCheckOffice(
      @RequestParam("method") String method,
      @RequestParam("path") String path, @RequestParam("personId") Optional<Long> personId) {
    Person person = null;
    log.debug("AbsencesGroups::secureCheck method= {}, path = {}, id = {}", method, path,
        personId.get());
    if (personId.isPresent()) {
      person = personDao.byId(personId.get())
          .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    }

    Office office = person.getOffice();
    return ResponseEntity.ok(rules.check(method, path, office));
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
  @GetMapping("/groupStatus")
  public ResponseEntity<AbsenceGroupsDto> groupStatus(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("groupAbsenceTypeId") Long groupAbsenceTypeId,
      @RequestParam("from") String from) {
    log.debug("AbsencesGroupsController::groupStatus groupAbsenceTypeId = {} from = {}",
        groupAbsenceTypeId, from);
    LocalDate fromLocalDate = LocalDate.parse(from);
    Person person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(person);

    AbsenceGroupsRecap psrDto = absenceGroupsRecapFactory.create(person, groupAbsenceTypeId,
        fromLocalDate);

    SortedMap<LocalDate, DayInPeriodDto> newDaysInPeriod = Maps.newTreeMap();
    List<AbsencePeriodTerseDto> newPeriodDto = Lists.newArrayList();

    for (AbsencePeriod sp : psrDto.periodChain.periods) {
      AbsencePeriodTerseDto aspDto = absenceGroupsMapper.convertAbsencePeriodTerse(sp);
      for (DayInPeriod dp : sp.daysInPeriod.values()) {
        DayInPeriodDto dpDto = absenceGroupsMapper.convertDayInPeriod(dp);
        List<TemplateRowDto> tp = Lists.newArrayList();
        for (TemplateRow tr : dp.allTemplateRows()) {
          tp.add(absenceGroupsMapper.convertTemplateRow(tr));
        }
        dpDto.setRowRecap(tp);
        newDaysInPeriod.put(dp.getDate(), dpDto);
      }
      aspDto.setDaysInPeriod(newDaysInPeriod);
      newPeriodDto.add(aspDto);
    }

    AbsenceGroupsDto absGroupDto = absenceGroupsMapper.convert(psrDto);
    PeriodChainDto newPeriodChain = absGroupDto.getPeriodChain();
    newPeriodChain.setPeriods(newPeriodDto);
    absGroupDto.setPeriodChain(newPeriodChain);

    List<GroupAbsenceTypeDto> groupAbsenceTypeDto = Lists.newArrayList();
    for (GroupAbsenceType gr : psrDto.categorySwitcher.groups()) {
      groupAbsenceTypeDto.add(absenceGroupsMapper.convertGroupAbsenceType(gr));
    }
    absGroupDto.setGroups(groupAbsenceTypeDto);

    return ResponseEntity.ok().body(absGroupDto);
  }

  /**
   * Recupero le informazioni per costruire la form dell'inserimento assenza.
   */
  @Operation(
      summary = "Recupera le informazioni per costruire la form dell'inserimento assenza.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituite le informazioni per popolare la form dell'inserimento assenza."),
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
  @GetMapping("/groupsForCategory")
  public ResponseEntity<AbsenceFormDto> getGroupsForCategory(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("from") String from,
      @RequestParam("switchGroup") Optional<Boolean> switchGroup,
      @RequestParam("to") Optional<String> to,
      @RequestParam("category") Optional<String> category,
      @RequestParam("groupAbsenceTypeName") Optional<String> groupAbsenceTypeName,
      @RequestParam("absenceTypeCode") Optional<String> absenceTypeCode,
      @RequestParam("justifiedTypeName") Optional<String> justifiedTypeName,
      @RequestParam("hours") Optional<Integer> hours,
      @RequestParam("minutes") Optional<Integer> minutes
  ) {
    log.debug("AbsencesGroupsController::groupsForCategory id = {} "
            + "from = {} "
            + "to = {} "
            + "category={}  "
            + "groupAbsenceTypeName={}  "
            + "absenceTypeName={}  "
            + "justifiedTypeName={}  "
            + "hours={}  "
            + "minutes={}  "
            + "switchGroup= {}",
        id, from, to, category, groupAbsenceTypeName, absenceTypeCode, justifiedTypeName, hours,
        minutes, switchGroup);

    LocalDate fromLocalDate = LocalDate.parse(from);

    LocalDate toLocalDate = null;
    if (to.isPresent()) {
      toLocalDate = LocalDate.parse(to.get());
    }

    Person person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(person);

    CategoryTab categoryTabFind = null;
    for (CategoryTab ct : categoryTabDao.findAll()) {
      if (ct.name.equals(category.orElse(null))) {
        categoryTabFind = ct;
        break;
      }
    }

    GroupAbsenceType groupAbsenceType = null;
    for (GroupAbsenceType gat : groupAbsenceTypeDao.findAll()) {
      if (gat.name.equals(groupAbsenceTypeName.orElse(null))) {
        groupAbsenceType = gat;
        break;
      }
    }

    AbsenceType absenceType = null;
    for (AbsenceType at : absenceTypeDao.findAll()) {
      if (at.code.equals(absenceTypeCode.orElse(null))) {
        absenceType = at;
        break;
      }
    }

    JustifiedType justifiedType = null;
    if (justifiedTypeName.orElse(null) != null) {
      justifiedType = absenceComponentDao.getOrBuildJustifiedType(
          JustifiedType.JustifiedTypeName.valueOf(justifiedTypeName.get()));
    }

    AbsenceForm absenceForm = absenceService.buildAbsenceForm(person, fromLocalDate,
        categoryTabFind, toLocalDate, null, groupAbsenceType, switchGroup.orElse(false),
        absenceType,
        justifiedType, hours.orElse(null), minutes.orElse(null), false, false);

    AbsenceFormDto absFormDto = absenceFormMapper.convert(absenceForm);

    List<GroupAbsenceType> groups = null;
    List<GroupAbsenceTypeDto> groupsDto = Lists.newArrayList();
    groups = absenceForm.groupsForCategory(absenceForm.groupSelected.category);
    for (GroupAbsenceType gat : groups) {
      groupsDto.add(absenceFormMapper.convert(gat));
    }
    absFormDto.setGroups(groupsDto);

    Map<String, List<GroupAbsenceTypeDto>> groupsByCategory =
        Maps.newHashMap();
    for (CategoryGroupAbsenceType cgr : absenceForm.categories()) {
      List<GroupAbsenceTypeDto> groupsListDto = Lists.newArrayList();
      for (GroupAbsenceType gr : absenceForm.groupsForCategory(cgr)) {
        groupsListDto.add(absenceFormMapper.convert(gr));
      }
      groupsByCategory.put(cgr.getLabel(), groupsListDto);
    }
    absFormDto.setGroupsByCategory(groupsByCategory);

    List<String> justifiedTypes = Lists.newArrayList();
    for (JustifiedType jt : absenceForm.justifiedTypes) {
      justifiedTypes.add(jt.getLabel());
    }
    absFormDto.setJustifiedTypes(justifiedTypes);

    return ResponseEntity.ok().body(absFormDto);
  }

  /**
   * Simula l'inserimento dell'assenza.
   */
  @Operation(
      summary = "simula l'inserimento dell'assenza.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituisce le informazioni associate alla simulazione dell'inserimento di un'assenza"),
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
  @PostMapping("/simulateInsert")
  public ResponseEntity<AbsenceFormSimulationResponseDto> simulateInsert(
      @NotNull @Valid @RequestBody AbsenceFormSimulationDto simulationDto) {

    Optional<Long> id = simulationDto.getIdPerson();
    Optional<String> fiscalCode = simulationDto.getFiscalCode();
    LocalDate dateFrom = LocalDate.parse(simulationDto.getFrom());
    LocalDate dateTo = LocalDate.parse(simulationDto.getTo());
    LocalDate recoveryDate = null;
    if (simulationDto.getRecoveryDate() != null) {
      recoveryDate = LocalDate.parse(simulationDto.getRecoveryDate());
    }
    String categoryTabName = simulationDto.getCategoryTabName();
    String groupAbsenceTypeName = simulationDto.getGroupAbsenceTypeName();
    String absenceTypeName = simulationDto.getAbsenceTypeCode();
    String justifiedTypeName = simulationDto.getJustifiedTypeName();
    Integer hours = simulationDto.getHours();
    Integer minutes = simulationDto.getMinutes();
    boolean forceInsert = simulationDto.isForceInsert();
    boolean switchGroup = simulationDto.isSwitchGroup();

    Person person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    rules.checkifPermitted(person);

    CategoryTab categoryTab = null;
    for (CategoryTab ct : categoryTabDao.findAll()) {
      if (ct.name.equals(categoryTabName)) {
        categoryTab = ct;
        break;
      }
    }
    AbsenceType absenceType = null;
    for (AbsenceType at : absenceTypeDao.findAll()) {
      if (at.code.equals(absenceTypeName)) {
        absenceType = at;
        break;
      }
    }

    GroupAbsenceType groupAbsenceType = null;
    for (GroupAbsenceType gat : groupAbsenceTypeDao.findAll()) {
      if (gat.name.equals(groupAbsenceTypeName)) {
        groupAbsenceType = gat;
        break;
      }
    }

    JustifiedType justifiedType = null;
    if (justifiedTypeName != null) {
      justifiedType = absenceComponentDao.getOrBuildJustifiedType(
          JustifiedType.JustifiedTypeName.valueOf(justifiedTypeName));
    }

    log.debug("AbsenceController::simulateInsert person = {} from={} to={}, absenceType={}",
        person, dateFrom, dateTo, absenceType);

    AbsenceForm absenceForm =
        absenceService.buildAbsenceForm(person, dateFrom, categoryTab,
            dateTo, recoveryDate, groupAbsenceType, switchGroup, absenceType,
            justifiedType, hours, minutes, false, false);

    log.debug("AbsenceController::simulateInsert justifiedType={}",
        absenceForm.justifiedTypeSelected);

    InsertReport insertReport = absenceService.insert(person,
        absenceForm.groupSelected,
        absenceForm.from, absenceForm.to,
        absenceForm.absenceTypeSelected, absenceForm.justifiedTypeSelected,
        absenceForm.hours, absenceForm.minutes, forceInsert, absenceManager);

    AbsenceFormSimulationResponseDto dto = absenceFormSimulationResponseMapper.convert(
        insertReport);

    return ResponseEntity.ok().body(dto);
  }

  /**
   * Salva l'assenza.
   */
  @Operation(
      summary = "salva l'assenza.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituisce l'id della persona, mese e anno dell'assenza e se è stata inserita o meno."),
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
  @PostMapping("/save")
  public ResponseEntity<AbsenceFormSaveResponseDto> saveAbsenceForm(
      @NotNull @Valid @RequestBody AbsenceFormSaveDto absenceFormSaveDto) {

    Optional<Long> id = absenceFormSaveDto.getIdPerson();
    Optional<String> fiscalCode = absenceFormSaveDto.getFiscalCode();
    LocalDate dateFrom = LocalDate.parse(absenceFormSaveDto.getFrom());
    LocalDate dateTo = LocalDate.parse(absenceFormSaveDto.getTo());
    LocalDate recoveryDate = null;
    if (absenceFormSaveDto.getRecoveryDate() != null) {
      recoveryDate = LocalDate.parse(absenceFormSaveDto.getRecoveryDate());
    }
    String groupAbsenceTypeName = absenceFormSaveDto.getGroupAbsenceTypeName();
    String absenceTypeName = absenceFormSaveDto.getAbsenceTypeCode();
    String justifiedTypeName = absenceFormSaveDto.getJustifiedTypeName();
    Integer hours = absenceFormSaveDto.getHours();
    Integer minutes = absenceFormSaveDto.getMinutes();
    boolean forceInsert = absenceFormSaveDto.isForceInsert();

    Person person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    rules.checkifPermitted(person);
    Preconditions.checkNotNull(person);
    Preconditions.checkNotNull(dateFrom);

    AbsenceType absenceType = null;
    for (AbsenceType at : absenceTypeDao.findAll()) {
      if (at.code.equals(absenceTypeName)) {
        absenceType = at;
        break;
      }
    }

    if (!absenceTypeDao.isPersistent(absenceType)) {
      absenceType = null;
    }

    GroupAbsenceType groupAbsenceType = null;
    for (GroupAbsenceType gat : groupAbsenceTypeDao.findAll()) {
      if (gat.name.equals(groupAbsenceTypeName)) {
        groupAbsenceType = gat;
        break;
      }
    }
    Preconditions.checkNotNull(groupAbsenceType);

    JustifiedType justifiedType = absenceComponentDao.getOrBuildJustifiedType(
        JustifiedType.JustifiedTypeName.valueOf(justifiedTypeName));

    Preconditions.checkNotNull(justifiedType);

    log.debug(
        "AbsenceController::saveAbsenceForm person = {} from={} to={}, absenceType={} groupAbsenceType={} justifiedType={}",
        person, dateFrom, dateTo, absenceType, groupAbsenceType, justifiedType);

    InsertReport insertReport = absenceService.insert(person, groupAbsenceType, dateFrom, dateTo,
        absenceType, justifiedType, hours, minutes, forceInsert, absenceManager);

    log.info("Richiesto inserimento assenze per {}. "
            + "Codice/Tipo {}, dal {} al {} (ore:{}, minuti:{})",
        person.getFullname(), absenceType != null ? absenceType.getCode() : groupAbsenceType,
        dateFrom, dateTo, hours, minutes);

    absenceManager.saveAbsences(insertReport, person, dateFrom, recoveryDate,
        justifiedType, groupAbsenceType);

    log.info("Effettuato inserimento assenze per {}. "
            + "Codice/Tipo {}, dal {} al {} (ore:{}, minuti:{})",
        person.getFullname(), absenceType != null ? absenceType.getCode() : groupAbsenceType,
        dateFrom, dateTo, hours, minutes);

    AbsenceFormSaveResponseDto absDto = new AbsenceFormSaveResponseDto();
    absDto.setIdPerson(person.getId());
    absDto.setYear(dateFrom.getYear());
    absDto.setMonth(dateFrom.getMonthValue());
    absDto.setSavedSuccessfully(true);

    return ResponseEntity.ok().body(absDto);
  }

  /**
   * Restituisce tutti i codici assenza.
   */
  @Operation(
      summary = "Restituisce tutti i codici assenza.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti tutti i codici assenza."),
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
  @GetMapping("/findCode")
  public ResponseEntity<Set<AbsenceTypeDto>> findCode(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("from") String from) {
    log.debug("AbsencesGroupsController::findCode id = {} from = {} ", id, from);
    LocalDate fromDate = LocalDate.parse(from);

    Person person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    log.debug("AbsenceController::absencesInPeriod person = {}", person);

    rules.checkifPermitted(person);

    HashMap<String, String> categoryTab = new HashMap<String, String>();
    for (CategoryTab ct : categoryTabDao.findAll()) {
      log.debug("CategoryTab name {}", ct.name);
      categoryTab.put(ct.getLabel(), ct.name);
    }

    //    AbsenceForm absenceForm = absenceService.buildAbsenceForm(person, fromDate, null, null, null,
//        null, true, null, null, null, null, false, false);
    Set<AbsenceTypeDto> allTakableDto = Sets.newHashSet();
    for (GroupAbsenceType group : absenceComponentDao.allGroupAbsenceType(false)) {
      for (AbsenceType abt : group.getTakableAbsenceBehaviour().getTakableCodes()) {
        if (abt.defaultTakableGroup() == null) {
          log.debug("Il defaultTakable è null per {}", abt.getCode());
          abt.defaultTakableGroup();
        }
      }

      AbsenceTypeDto dto = null;
      //TODO eventualmente controllo prendibilità della persona alla data (figli, l 104 etc.)
      for (AbsenceType abst : group.getTakableAbsenceBehaviour().getTakableCodes()) {
        dto = absenceFormMapper.convert(abst);
        dto.setCategoryTabName(Optional.ofNullable(abst.defaultTakableGroup().category.tab.name));
        allTakableDto.add(dto);
        log.debug("defaultTakableGroup defaultTakableGroup {}",
            abst.defaultTakableGroup().category.tab.name);
      }
    }

    return ResponseEntity.ok().body(allTakableDto);
  }
}