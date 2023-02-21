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
import it.cnr.iit.epas.controller.exceptions.EntityNotFoundException;
import it.cnr.iit.epas.controller.exceptions.InvalidOperationOnCurrentStateException;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dto.v4.PersonCreateDto;
import it.cnr.iit.epas.dto.v4.PersonShowDto;
import it.cnr.iit.epas.dto.v4.PersonUpdateDto;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
import it.cnr.iit.epas.dto.v4.mapper.PersonShowMapper;
import it.cnr.iit.epas.manager.PersonManager;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.security.SecurityRules;
import javax.inject.Inject;
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
@RequestMapping("/rest/v4/people")
public class PersonController {

  private final PersonDao personDao;
  private final PersonShowMapper personMapper;
  private final PersonManager personManager;
  private final EntityToDtoConverter entityToDtoConverter;
  private final SecurityRules rules;

  @Inject
  PersonController(PersonDao personRepository, PersonShowMapper personMapper,
      EntityToDtoConverter entityToDtoConverter,
      PersonManager personManager, SecurityRules rules) {
    this.personDao = personRepository;
    this.personMapper = personMapper;
    this.entityToDtoConverter = entityToDtoConverter;
    this.personManager = personManager;
    this.rules = rules;
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
    if (!rules.check(person)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
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

    if (!rules.check(person.getOffice())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

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
    if (!rules.check(person)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
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
    checkIfIsPossibileToDelete(person);

    personDao.delete(person);
    log.info("Eliminata persona {}", person);
    return ResponseEntity.ok().build();
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