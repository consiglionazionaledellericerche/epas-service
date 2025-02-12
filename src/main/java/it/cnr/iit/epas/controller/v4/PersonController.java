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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.exceptions.InvalidOperationOnCurrentStateException;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.GeneralSettingDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dto.v4.ContractShowDto;
import it.cnr.iit.epas.dto.v4.ContractShowTerseDto;
import it.cnr.iit.epas.dto.v4.PersonCreateDto;
import it.cnr.iit.epas.dto.v4.PersonShowDto;
import it.cnr.iit.epas.dto.v4.PersonShowExtendedDto;
import it.cnr.iit.epas.dto.v4.PersonUpdateDto;
import it.cnr.iit.epas.dto.v4.mapper.ContractShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
import it.cnr.iit.epas.dto.v4.mapper.PersonShowExtendedMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonShowMapper;
import it.cnr.iit.epas.manager.PersonManager;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi alla visualizzazione e gestione delle persone.
 *
 * @author Cristian Lucchesi
 *
 */
@SecurityRequirements(
    value = { 
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION), 
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(name = "Persons Controller", description = "Gestione delle informazioni delle persone")
@Slf4j
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/people")
public class PersonController {

  private final PersonDao personDao;
  private final PersonShowMapper personMapper;
  private final PersonShowExtendedMapper personShowExtendedMapper;
  private final ContractShowMapper contractMapper;
  private final PersonManager personManager;
  private final ContractDao contractDao;
  private final OfficeDao officeDao;
  private final GeneralSettingDao generalSettingDao;
  private final EntityToDtoConverter entityToDtoConverter;
  private final SecurityRules rules;

  @Inject
  PersonController(PersonDao personRepository, PersonShowMapper personMapper,
      ContractShowMapper contractMapper, ContractDao contractDao, OfficeDao officeDao,
      EntityToDtoConverter entityToDtoConverter,PersonShowExtendedMapper personShowExtendedMapper,
      PersonManager personManager, SecurityRules rules, GeneralSettingDao generalSettingDao) {
    this.personDao = personRepository;
    this.personMapper = personMapper;
    this.contractMapper = contractMapper;
    this.entityToDtoConverter = entityToDtoConverter;
    this.personShowExtendedMapper = personShowExtendedMapper;
    this.personManager = personManager;
    this.contractDao = contractDao;
    this.officeDao = officeDao;
    this.generalSettingDao = generalSettingDao;
    this.rules = rules;
  }

  @Operation(
      summary = "Visualizzazione della lista delle persone.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della persona da visualizzare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti la lista delle persone."),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente.", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati della persona.",
          content = @Content)
  })
  @GetMapping("/list")
  ResponseEntity<List<PersonShowExtendedDto>> list(
      @RequestParam("officeId") Long officeId,
      @RequestParam("name") String name) {
    log.debug("PersonController::list officeId = {}  name = {}", officeId,name);
    Office office;
    /*if (officeId == null) {
      office = officeDao.getOfficeById(Long.parseLong(session.get("officeSelected")));
    } else {
     */
      office = officeDao.getOfficeById(officeId);
    //}
    //notFoundIfNull(office);
    if (office == null) {
      throw new EntityNotFoundException("Office not found");
    }

    rules.checkifPermitted(office);

    boolean warningInsertPerson = false;// da errore il metodo generalSettingDao.generalSetting().isWarningInsertPerson();

    List<Person> simplePersonList = personDao
        .listFetched(Optional.ofNullable(name), ImmutableSet.of(office), false, null, null, false)
        .list();

    /*List<IWrapperPerson> personList =
        FluentIterable.from(simplePersonList).transform(wrapperFunctionFactory.person()).toList();*/
    List<PersonShowExtendedDto> dto = Lists.newArrayList();
    for (Person person : simplePersonList) {
      dto.add(personShowExtendedMapper.convert(person));
    }
    return ResponseEntity.ok().body(dto);
  }

  @Operation(
      summary = "Visualizzazione delle informazioni di una persona.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della persona da visualizzare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituiti i dati della persona."),
      @ApiResponse(responseCode = "401", 
          description = "Autenticazione non presente.", content = @Content), 
      @ApiResponse(responseCode = "403", 
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati della persona.",
            content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Persona non trovata con l'id fornito.",
          content = @Content)
  })
  @GetMapping(ApiRoutes.SHOW)
  ResponseEntity<PersonShowDto> show(@NotNull @PathVariable("id") Long id) {
    log.debug("PersonController::show id = {}", id);
    val person = personDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Person not found with id = " + id));
    rules.checkifPermitted(person.getOffice());
    return ResponseEntity.ok().body(personMapper.convert(person));
  }

  @Operation(
      summary = "Creazione di una persona.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede da modificare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Persona creato correttamente."),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente.", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a creare nuove persone.", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Office associato alla persona non trovato con i parametri forniti.", 
          content = @Content)
  })
  @Transactional
  @PutMapping(ApiRoutes.CREATE)
  ResponseEntity<PersonShowDto> create(@NotNull @Valid @RequestBody PersonCreateDto personDto) {
    log.debug("PersonController::create personDto = {}", personDto);
    val person = entityToDtoConverter.createEntity(personDto);

    rules.checkifPermitted(person.getOffice());

    personManager.properPersonCreate(person);
    personDao.save(person);
    
    log.info("Creata persona {}", person);
    return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.convert(person));
  }
  
  @Operation(
      summary = "Aggiornamento di una persona.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede da modificare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Persona aggiornata correttamente."),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente.", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a modificare i dati della persona.", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Qualifica associata alla persona non trovata con i parametri forniti.", 
          content = @Content)
  })
  @Transactional
  @PostMapping(ApiRoutes.UPDATE)
  ResponseEntity<PersonShowDto> update(@NotNull @Valid @RequestBody PersonUpdateDto personDto) {
    log.debug("PersonController::update personDto = {}", personDto);
    val person = entityToDtoConverter.updateEntity(personDto);
    rules.checkifPermitted(person.getOffice());
    personDao.save(person);
    log.info("Aggiornato ufficio, i nuovi dati sono {}", person);
    return ResponseEntity.ok().body(personMapper.convert(person));
  }

  @Operation(
      summary = "Eliminazione di una persona.", 
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede da modificare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Persona eliminata correttamente"),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato ad eliminare la persona.", 
          content = @Content), 
      @ApiResponse(responseCode = "422", 
          description = "Informazioni importanti associate alla persona, impossibile eliminarla.", 
          content = @Content)
  })
  @Transactional
  @DeleteMapping(ApiRoutes.DELETE)
  ResponseEntity<Void> delete(@NotNull @PathVariable("id") Long id) {
    log.debug("PersonController::delete id = {}", id);
    val person = personDao.byId(id).orElseThrow(() -> new EntityNotFoundException());

    rules.checkifPermitted(person.getOffice());

    checkIfIsPossibileToDelete(person);

    personDao.delete(person);
    log.info("Eliminata persona {}", person);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Lista di tutti i contratti associati ad persona.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della persona da visualizzare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituiti i contratti associati alla persona."),
      @ApiResponse(responseCode = "401", 
          description = "Autenticazione non presente.", content = @Content), 
      @ApiResponse(responseCode = "403", 
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati della persona.",
            content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Persona non trovata con l'id fornito.",
          content = @Content)
  })
  @GetMapping(ApiRoutes.SHOW + "/contracts")
  ResponseEntity<List<ContractShowTerseDto>> contracts(@NotNull @PathVariable("id") Long id) {
    log.debug("PersonController::contracts person id = {}", id);
    val person = personDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Person not found with id = " + id));
    rules.checkifPermitted(person.getOffice());
    return ResponseEntity.ok().body(person.getContracts()
        .stream().map(contract -> contractMapper.convertTerse(contract))
        .collect(Collectors.toList()));
  }

  @Operation(
      summary = "Contratto attivo di una persona ad una certa data, di default oggi",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della persona da visualizzare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituiti i contratti associati alla persona."),
      @ApiResponse(responseCode = "401", 
          description = "Autenticazione non presente.", content = @Content), 
      @ApiResponse(responseCode = "403", 
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati della persona.",
            content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Persona non trovata con l'id fornito oppure nessun contratto attivo alla "
              + "data indicata", content = @Content)
  })
  @GetMapping(ApiRoutes.SHOW + "/contract")
  ResponseEntity<ContractShowDto> contract(@NotNull @PathVariable("id") Long id,
      @RequestParam("data") Optional<LocalDate> date) {
    log.debug("PersonController::contract person id = {}", id);
    val person = personDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Person not found with id = " + id));
    rules.checkifPermitted(person.getOffice());
    val contractAtDate = date.orElse(LocalDate.now());
    val contract = Optional.ofNullable(contractDao.getContract(contractAtDate, person))
        .orElseThrow(() -> 
            new EntityNotFoundException(
                "Contract not found for person id = " + id + " at date " + contractAtDate));
    return ResponseEntity.ok().body(contractMapper.convert(contract));
  }

  /**
   * Verifica le condizioni per cui non è possibile cancellare una persona.
   * Solleva un eccezzione InvalidOperationOnCurrentStateException se non è 
   * possibile cancellarla.
   */
  private void checkIfIsPossibileToDelete(Person person) 
      throws InvalidOperationOnCurrentStateException {
    if (!person.getContracts().isEmpty()) {
      throw new InvalidOperationOnCurrentStateException(
          String.format("Impossibile eliminare la persona, "
              + "sono presenti %d contratti associati alla persona. "
              + "Cancellare prima i contratti associati.", 
              person.getContracts().size()));
    }
  }
}