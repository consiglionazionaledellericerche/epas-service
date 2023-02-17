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
import it.cnr.iit.epas.controller.exceptions.EntityNotFoundException;
import it.cnr.iit.epas.controller.exceptions.ValidationException;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dto.v4.ContractCreateDto;
import it.cnr.iit.epas.dto.v4.ContractDto;
import it.cnr.iit.epas.dto.v4.ContractUpdateDto;
import it.cnr.iit.epas.dto.v4.OfficeShowDto;
import it.cnr.iit.epas.dto.v4.OfficeUpdateDto;
import it.cnr.iit.epas.dto.v4.mapper.ContractShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
import it.cnr.iit.epas.manager.ContractManager;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Transactional
@Slf4j
@RestController
@RequestMapping("/rest/v4/contracts")
public class ContractController {

  private final ContractDao contractDao;
  private final ContractManager contractManager;
  private final ContractShowMapper mapper;
  private final EntityToDtoConverter entityToDtoConverter;
  private final SecurityRules rules;

  @Inject
  ContractController(ContractDao contractDao, ContractShowMapper mapper,
      ContractManager contractManager, EntityToDtoConverter entityToDtoConverter,
      SecurityRules rules) {
    this.contractDao = contractDao;
    this.mapper = mapper;
    this.contractManager = contractManager;
    this.entityToDtoConverter = entityToDtoConverter;
    this.rules = rules;
  }

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
  ResponseEntity<ContractDto> show(@NotNull @PathVariable("id") Long id) {
    log.debug("ContractController::show id = {}", id);
    val office = contractDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Contract not found"));
    if (!rules.check(office)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok().body(mapper.convert(office));
  }

  @Operation(
      summary = "Creazione di un ufficio.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore anagrafica' della sede da visualizzare e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
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
  ResponseEntity<ContractDto> create(@NotNull @Valid @RequestBody ContractCreateDto contractDto) {
    log.debug("ContractController::create contractDto = {}", contractDto);
    val contract = entityToDtoConverter.createEntity(contractDto);
    if (!contractManager.properContractCreate(contract, Optional.empty(), true)) {
      throw new ValidationException("Contract create parameters are invalid");
    }
    log.info("Creato contratto {}", contract);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.convert(contract));
  }

//  @Operation(
//      summary = "Aggiornamento di un contratto.",
//      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
//          + "'Amministratore tecnico' della sede da modificare e dagli utenti con il ruolo "
//          + "di sistema 'Developer' e/o 'Admin'.")
//  @ApiResponses(value = {
//      @ApiResponse(responseCode = "200", description = "Contratto aggiornato correttamente"),
//      @ApiResponse(responseCode = "400", description = "Dati per l'aggiornamento del contratto"
//          + " non validi (es. data fine prima di data inizio)", content = @Content), 
//      @ApiResponse(responseCode = "401", description = "Autenticazione non presente", 
//          content = @Content), 
//      @ApiResponse(responseCode = "403", description = "Utente che ha effettuato la richiesta "
//          + "non autorizzato a modificare il contratto", 
//          content = @Content), 
//      @ApiResponse(responseCode = "404", 
//          description = "Persona associata al contratto non trovata con i parametri forniti", 
//          content = @Content)
//  })
//  @Transactional
//  @PostMapping(ApiRoutes.UPDATE)
//  ResponseEntity<ContractDto> update(
//      @NotNull @Valid @RequestBody ContractUpdateDto contractDto) {
//    log.debug("OfficeController::update officeDto = {}", contractDto);
//    val contract = entityToDtoConverter.updateEntity(contractDto);
//    if (!rules.check(contract)) {
//      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//    }
//    contractManager.properContractUpdate(contract, LocalDate.now());
//    log.info("Aggiornato ufficio, i nuovi dati sono {}", contract);
//    return ResponseEntity.ok().body(mapper.convert(contract));
//  }

  @PutMapping(ApiRoutes.ID_REGEX + "/endContract")
  ResponseEntity<ContractDto> endContract(
      @PathVariable("id") Long id,
      @RequestParam("endContract") LocalDate endContract) {
    log.debug("ContractController::endContract id = {}", id);
    val entity = contractDao.byId(id)
        .orElseThrow(() -> new EntityNotFoundException("Contratto non trovato con id passato"));
    entity.setEndContract(endContract);
    contractManager.properContractUpdate(entity, LocalDate.now().minusDays(1), false);
    return ResponseEntity.ok().body(mapper.convert(entity));
  }

}