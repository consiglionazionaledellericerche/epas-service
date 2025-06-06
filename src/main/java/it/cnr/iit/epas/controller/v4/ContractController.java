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
import it.cnr.iit.epas.controller.exceptions.ValidationException;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.dto.v4.ContractCreateDto;
import it.cnr.iit.epas.dto.v4.ContractShowDto;
import it.cnr.iit.epas.dto.v4.ContractUpdateDto;
import it.cnr.iit.epas.dto.v4.mapper.ContractShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
import it.cnr.iit.epas.manager.ContractManager;
import it.cnr.iit.epas.manager.PeriodManager;
import it.cnr.iit.epas.manager.recaps.recomputation.RecomputeRecap;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.security.SecurityRules;
import it.cnr.iit.epas.utils.DateInterval;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
 * Metodi REST per la gestione delle informazioni sui contratti.
 */
@SecurityRequirements(
    value = { 
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION), 
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(name = "Contracts Controller", description = "Gestione delle informazioni dei contratti")
@Transactional
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/contracts")
public class ContractController {

  private final ContractDao contractDao;
  private final ContractManager contractManager;
  private final ContractShowMapper mapper;
  private final EntityToDtoConverter entityToDtoConverter;
  private final PeriodManager periodManager;
  private final WrapperFactory wrapperFactory;
  private final SecurityRules rules;

  @Operation(
      summary = "Visualizzazione delle informazioni di un contratto.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede da visualizzare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituiti i dati del contratto"),
      @ApiResponse(responseCode = "401", 
          description = "Autenticazione non presente", content = @Content), 
      @ApiResponse(responseCode = "403", 
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati del contratto",
            content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Contratto non trovato con l'id fornito",
          content = @Content)
  })
  @GetMapping(ApiRoutes.SHOW)
  ResponseEntity<ContractShowDto> show(@NotNull @PathVariable("id") Long id) {
    log.debug("ContractController::show id = {}", id);
    val contract = contractDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

    rules.checkifPermitted(contract.getPerson().getOffice());

    return ResponseEntity.ok().body(mapper.convert(contract));
  }

  @Operation(
      summary = "Creazione di un contratto.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede associata alla persona del contratto e dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Contratto creato correttamente"),
      @ApiResponse(responseCode = "400", description = "Dati per la creazione del contratto"
          + " non validi (es. data fine prima di data inizio)", content = @Content), 
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a creare nuovi contratti", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Persona associata al contratto non trovata con i parametri forniti", 
          content = @Content)
  })
  @Transactional
  @PutMapping(ApiRoutes.CREATE)
  ResponseEntity<ContractShowDto> create(
      @NotNull @Valid @RequestBody ContractCreateDto contractDto) {
    log.debug("ContractController::create contractDto = {}", contractDto);
    val contract = entityToDtoConverter.createEntity(contractDto);
    if (!contractManager.properContractCreate(contract, Optional.empty(), true)) {
      throw new ValidationException("Contract create parameters are invalid");
    }
    log.info("Creato contratto {}", contract);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.convert(contract));
  }

  @Operation(
      summary = "Aggiornamento di un contratto.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede a cui appartiene la persona di cui modificare"
          + "il contratto e dagli utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Contratto aggiornato correttamente"),
      @ApiResponse(responseCode = "400", description = "Dati per l'aggiornamento del contratto"
          + " non validi (es. data fine prima di data inizio)", content = @Content), 
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a modificare il contratto", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Contratto non trovato con i parametri forniti", 
          content = @Content)
  })
  @Transactional
  @PostMapping(ApiRoutes.UPDATE)
  ResponseEntity<ContractShowDto> update(
      @NotNull @Valid @RequestBody ContractUpdateDto contractDto) {
    log.debug("ContractController::update contractDto = {}", contractDto);
    val contract = 
        contractDao.byId(contractDto.getId())
          .orElseThrow(() -> 
            new EntityNotFoundException("Contract non found with id = " + contractDto.getId()));

    rules.checkifPermitted(contract.getPerson().getOffice());

    IWrapperContract wrappedContract = wrapperFactory.create(contract);
    final DateInterval previousInterval = wrappedContract.getContractDatabaseInterval();

    entityToDtoConverter.updateEntity(contractDto);

    DateInterval newInterval = wrappedContract.getContractDatabaseInterval();
    RecomputeRecap recomputeRecap = periodManager.buildTargetRecap(previousInterval, newInterval,
        wrappedContract.initializationMissing());

    contractManager.properContractUpdate(
        contract, Optional.ofNullable(recomputeRecap.recomputeFrom).orElse(LocalDate.now()), false);

    log.info("Aggiornato contratto, i nuovi dati sono {}", contract);
    return ResponseEntity.ok().body(mapper.convert(contract));
  }

  @Operation(
      summary = "Eliminazione di un contratto.", 
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede da modificare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Contratto eliminato correttamente"),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato ad eliminatre il contratto", 
          content = @Content), 
      @ApiResponse(responseCode = "422", 
          description = "Informazioni importanti associate al contratto, impossibile eliminarlo", 
          content = @Content)
  })
  @Transactional
  @DeleteMapping(ApiRoutes.DELETE)
  ResponseEntity<Void> delete(@NotNull @PathVariable("id") Long id) {
    log.debug("ContractController::delete id = {}", id);
    val contract = contractDao.byId(id)
          .orElseThrow(() -> new EntityNotFoundException("Contract not found with id = " + id));
    checkIfIsPossibileToDelete(contract);

    contractDao.delete(contract);
    log.info("Eliminato contratto {}", contract);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Se è presente un contratto precedente che termina il giorno prima dell'inizio del "
          + "contratto attuale allora importa il contratto come continuativo del precedente.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede a cui appartiene la persona di cui modificare"
          + "il contratto e dagli utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Contratto aggiornato correttamente"),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a modificare il contratto", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Persona associata al contratto non trovata con i parametri forniti", 
          content = @Content),
      @ApiResponse(responseCode = "422", description = "Contratto precedente non trovato o non"
          + " immediatamente precedente all'attuale.", content = @Content),
  })
  @Transactional
  @PutMapping(ApiRoutes.ID_REGEX + "/linkPreviousContract")
  ResponseEntity<ContractShowDto> linkPreviousContract(@NotNull @PathVariable("id") Long id) {
    log.debug("ContractController::linkPreviousContract id = {}", id);
    val contract = contractDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Contract not found"));
    
    rules.checkifPermitted(contract.getPerson().getOffice());

    if (!contractManager.applyPreviousContractLink(contract, true)) {
      throw new InvalidOperationOnCurrentStateException(
          "No suitable previous contract found to link.");
    }
    return ResponseEntity.ok().body(mapper.convert(contract));
  }

  @Operation(
      summary = "Se è presente un contratto precedente collegato al contratto indicato per id "
          + "allora toglie il collagamento con il predente contratto.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede a cui appartiene la persona di cui modificare"
          + "il contratto e dagli utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Contratto aggiornato correttamente"),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a modificare il contratto", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Persona associata al contratto non trovata con i parametri forniti", 
          content = @Content),
      @ApiResponse(responseCode = "422", description = "Nessun contratto precedente collegato al "
          + "contratto indicato per id", content = @Content),
  })
  @Transactional
  @PutMapping(ApiRoutes.ID_REGEX + "/unlinkPreviousContract")
  ResponseEntity<ContractShowDto> unlinkPreviousContract(@NotNull @PathVariable("id") Long id) {
    log.debug("ContractController::unlinkPreviousContract id = {}", id);
    val contract = contractDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Contract not found"));
    
    rules.checkifPermitted(contract.getPerson().getOffice());

    if (!contractManager.applyPreviousContractLink(contract, true)) {
      throw new InvalidOperationOnCurrentStateException("Linked previous contract not found.");
    }
    contractManager.applyPreviousContractLink(contract, true);
    return ResponseEntity.ok().body(mapper.convert(contract));
  }

  @Operation(
      summary = "Imposta la data di fine contratto.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede a cui appartiene la persona di cui modificare"
          + "il contratto e dagli utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Data fine contratto aggiornata correttamente"),
      @ApiResponse(responseCode = "401", description = "Autenticazione non presente", 
          content = @Content), 
      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
          + "non autorizzato a modificare il contratto", 
          content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Persona associata al contratto non trovata con i parametri forniti", 
          content = @Content)
  })
  @Transactional
  @PutMapping(ApiRoutes.ID_REGEX + "/endContract")
  ResponseEntity<ContractShowDto> endContract(
      @PathVariable("id") Long id,
      @RequestParam("endContract") LocalDate endContract) {
    log.debug("ContractController::endContract id = {}", id);
    val entity = contractDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Contratto not found with requested id"));
    entity.setEndContract(endContract);
    contractManager.properContractUpdate(entity, LocalDate.now().minusDays(1), false);
    return ResponseEntity.ok().body(mapper.convert(entity));
  }

  /**
   * Verifica le condizioni per cui non è possibile cancellare un contratto.
   * Solleva un eccezzione InvalidOperationOnCurrentStateException se non è 
   * possibile cancellarlo.
   */
  private void checkIfIsPossibileToDelete(Contract contract) 
      throws InvalidOperationOnCurrentStateException {
    if (!contract.getMealTickets().isEmpty()) {
      throw new InvalidOperationOnCurrentStateException(
          String.format("Impossibile eliminare l'ufficio, "
          + "sono presenti %d buoni pasto associati al contratto", 
          contract.getMealTickets().size()));
    }
  }
}