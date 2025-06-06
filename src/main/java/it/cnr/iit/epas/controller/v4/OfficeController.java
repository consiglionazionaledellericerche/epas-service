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
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dto.v4.OfficeCreateDto;
import it.cnr.iit.epas.dto.v4.OfficeShowDto;
import it.cnr.iit.epas.dto.v4.OfficeShowTerseDto;
import it.cnr.iit.epas.dto.v4.OfficeUpdateDto;
import it.cnr.iit.epas.dto.v4.UserShowTerseDto;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
import it.cnr.iit.epas.dto.v4.mapper.OfficeShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.UserShowMapper;
import it.cnr.iit.epas.manager.OfficeManager;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.security.SecurityRules;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
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
 * Controller con i metodi REST relativi alla visualizzazione e gestione degli uffici.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@SecurityRequirements(
    value = { 
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION), 
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(name = "Offices Controller", description = "Gestione delle informazioni degli uffici")
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/offices")
public class OfficeController {

  private final OfficeDao officeDao;
  private final OfficeManager officeManager;
  private final OfficeShowMapper officeMapper;
  private final UserShowMapper userShowMapper;
  private final EntityToDtoConverter entityToDtoConverter;
  private final SecurityRules rules;

  @Inject
  OfficeController(OfficeDao officeDao, OfficeShowMapper officeMapper,
      OfficeManager officeManager,
      UserShowMapper userShowMapper, EntityToDtoConverter entityToDtoConverter,
      SecurityRules rules) {
    this.officeDao = officeDao;
    this.officeManager = officeManager;
    this.officeMapper = officeMapper;
    this.userShowMapper = userShowMapper;
    this.entityToDtoConverter = entityToDtoConverter;
    this.rules = rules;
  }

  @Operation(
      summary = "Visualizzazione delle informazioni di un ufficio.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Amministratore tecnico' della sede da visualizzare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituiti i dati dell'ufficio."),
      @ApiResponse(responseCode = "401", 
          description = "Autenticazione non presente.", content = @Content), 
      @ApiResponse(responseCode = "403", 
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati dell'ufficio.",
            content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Ufficio non trovato con l'id fornito.",
          content = @Content)
  })
  @GetMapping(ApiRoutes.SHOW)
  ResponseEntity<OfficeShowDto> show(@NotNull @PathVariable("id") Long id) {
    log.debug("OfficeController::show id = {}", id);
    val office = officeDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Office not found with id = " + id));

    rules.checkifPermitted(office);

    return ResponseEntity.ok().body(officeMapper.convert(office));
  }

  @Operation(
      summary = "Ricerca di un ufficio e visualizzazione delle sue informazioni.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Amministratore tecnico' della sede cercata e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ufficio trovato e visualizzato."),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente.", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a visualizzare i dati dell'ufficio.", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Ufficio non trovato con i parametri forniti.", content = @Content)
  })
  @GetMapping("/search")
  ResponseEntity<OfficeShowDto> search(
      @RequestParam("id") Optional<Long> id, @RequestParam("code") Optional<String> code,
      @RequestParam("codeId") Optional<String> codeId) {
    log.debug("OfficeController::search id = {}, code = {}, codeId = {}",
        id.or(null), code.or(null), codeId.or(null));

    if ((id == null || id.isEmpty()) && (code == null || code.isEmpty()) 
        && (codeId == null || codeId.isEmpty())) {
      return ResponseEntity.badRequest().build();
    }

    val office = officeDao
        .byIdOrCodeOrCodeId(id.orElse(null), code.orElse(null), codeId.orElse(null))
        .orElseThrow(() -> new EntityNotFoundException("Office not found"));

    rules.checkifPermitted(office);

    return ResponseEntity.ok().body(officeMapper.convert(office));
  }

  @Operation(
      summary = "Visualizzazione di tutti gli uffici presenti.",
      description = "Questo endpoint è utilizzabile solo dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restitutita la lista degli ufficio presenti."),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente.", 
          content = @Content), 
      @ApiResponse(responseCode = "403", 
          description = "Autenticazione non presente o utente che ha effettuato la richiesta "
            + "non autorizzato a visualizzare i dati degli uffici.", content = @Content), 
  })
  @GetMapping(ApiRoutes.ALL)
  ResponseEntity<List<OfficeShowTerseDto>> all(
      @RequestParam(name = "enabled") Optional<Boolean> enabled) {
    log.debug("OfficeController::all enabled = {}", enabled);
    List<Office> offices = officeDao.allOffices(enabled);
    return ResponseEntity.ok().body(
        offices.stream()
          .map(office -> officeMapper.convertTerse(office))
          .collect(Collectors.toList()));
  }

  @Operation(
      summary = "Creazione di un ufficio.",
      description = "Questo endpoint è utilizzabile solo dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ufficio creato correttamente."),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente.", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a creare nuovi uffici.", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Istituto associato all'ufficio non trovato con i parametri forniti.", 
          content = @Content)
  })
  @Transactional
  @PutMapping(ApiRoutes.CREATE)
  ResponseEntity<OfficeShowDto> create(@NotNull @Valid @RequestBody OfficeCreateDto officeDto) {
    log.debug("OfficeController::create officeDto = {}", officeDto);
    val office = entityToDtoConverter.createEntity(officeDto);
    officeManager.save(office);
    log.info("Creato ufficio {}", office);
    return ResponseEntity.status(HttpStatus.CREATED).body(officeMapper.convert(office));
  }

  @Operation(
      summary = "Aggiornamento di un ufficio.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Amministratore tecnico' della sede da modificare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ufficio aggiornato correttamente."),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente.", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a modificare i dati dell'ufficio.", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Istituto associato all'ufficio non trovato con i parametri forniti.", 
          content = @Content)
  })
  @Transactional
  @PostMapping(ApiRoutes.UPDATE)
  ResponseEntity<OfficeShowDto> update(@NotNull @Valid @RequestBody OfficeUpdateDto officeDto) {
    log.debug("OfficeController::update officeDto = {}", officeDto);
    val office = entityToDtoConverter.updateEntity(officeDto);
    rules.checkifPermitted(office);
    officeManager.save(office);
    log.info("Aggiornato ufficio, i nuovi dati sono {}", office);
    return ResponseEntity.ok().body(officeMapper.convert(office));
  }

  @Operation(
      summary = "Eliminazione di un ufficio.", 
      description = "Questo endpoint è utilizzabile solo dagli utenti con il ruolo "
          + "di sistema Developer e/o Admin.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ufficio eliminato correttamente."),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente.", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato ad eliminare l'ufficio.", 
          content = @Content), 
      @ApiResponse(responseCode = "422", 
          description = "Informazioni importanti associate all'ufficio, impossibile eliminarlo.", 
          content = @Content)
  })
  @Transactional
  @DeleteMapping(ApiRoutes.DELETE)
  ResponseEntity<Void> delete(@NotNull @PathVariable("id") Long id) {
    log.debug("OfficeController::delete id = {}", id);
    val office = officeDao.byId(id).orElseThrow(() -> new EntityNotFoundException());
    checkIfIsPossibileToDelete(office);

    officeDao.delete(office);
    log.info("Eliminato ufficio {}", office);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Visualizzazione di tutti gli utenti associati ad un ufficio.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Amministratore tecnico' della sede da visualizzare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Ufficio trovato e utenti visualizzati"),
      @ApiResponse(responseCode = "403", 
        description = "Autenticazione non presente o utente che ha effettuato la richiesta "
            + "non autorizzato a visualizzare i dati dell'ufficio",
         content = @Content), 
      @ApiResponse(responseCode = "404", description = "Ufficio non trovato con l'id fornito",
          content = @Content)
  })
  @GetMapping(ApiRoutes.SHOW + "/users")
  ResponseEntity<List<UserShowTerseDto>> users(
      @NotNull @PathVariable("id") Long id,
      @RequestParam(name = "enabled") Optional<Boolean> enabled) {
    log.debug("OfficeController::users id = {}", id);
    val office = officeDao.byId(id).orElseThrow(() -> new EntityNotFoundException());
    rules.checkifPermitted(office);
    var users = office.getUsers();
    if (enabled.isPresent() && enabled.get().equals(Boolean.TRUE)) {
      users = users.stream().filter(user -> !user.isDisabled()).collect(Collectors.toList());
    } else if (enabled.isPresent() && enabled.get().equals(Boolean.FALSE)) {
      users = users.stream().filter(user -> user.isDisabled()).collect(Collectors.toList());
    }
    return ResponseEntity.ok().body(
        office.getUsers().stream()
          .map(user -> userShowMapper.convertTerse(user))
          .collect(Collectors.toList()));
  }

  /**
   * Verifica le condizioni per cui non è possibile cancellare un ufficio.
   * Solleva un eccezzione InvalidOperationOnCurrentStateException se non è 
   * possibile cancellarlo.
   */
  private void checkIfIsPossibileToDelete(Office office) 
      throws InvalidOperationOnCurrentStateException {
    if (!office.getPersons().isEmpty()) {
      throw new InvalidOperationOnCurrentStateException(
          String.format("Impossibile eliminare l'ufficio, "
          + "sono presenti %d persona/e associate all'ufficio", office.getPersons().size()));
    }
  }
}