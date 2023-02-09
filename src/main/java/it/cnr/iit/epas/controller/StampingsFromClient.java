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

package it.cnr.iit.epas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.dto.v4.StampingDto;
import it.cnr.iit.epas.dto.v4.StampingFromClientDto;
import it.cnr.iit.epas.dto.v4.mapper.StampingDtoMapper;
import it.cnr.iit.epas.manager.StampingManager;
import it.cnr.iit.epas.models.exports.StampingFromClient;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequestMapping("/rest/v4/stampingsfromclient")
@RestController
class StampingsFromClient {

  private final StampingManager stampingManager;
  private final StampingDtoMapper stampingDtoMapper;

  @Inject
  StampingsFromClient(StampingManager stampingManager, StampingDtoMapper stampingDtoMapper) {
    this.stampingManager = stampingManager;
    this.stampingDtoMapper = stampingDtoMapper;
  }

  @Operation(
      summary = "Inserisce una timbratura ricevuta nel formato utilizzato dai client di ePAS")
  @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "timbratura inserita correttamente"),
      @ApiResponse(responseCode = "400", description = "dati timbratura non corretti oppure"
          + " data troppo vecchia nel passato (limite definito da una configurazione generale"
          + " del sistema"),
      @ApiResponse(responseCode = "403", 
        description = "autenticazione non presente o utente (sorgente timbratura) che ha effettuato"
            + " la richiesta non autorizzato ad inserire timbrature per il dipendente indicato"
            + " nel json"), 
      @ApiResponse(responseCode = "404", description = "dipendente indicato nel json non trovato"),
      @ApiResponse(responseCode = "409", description = "timbratura già presente")
  })
  @PutMapping("/create")
  ResponseEntity<StampingDto> create(
      @NotNull @RequestBody StampingFromClientDto stampingFromClientDto) {

    log.debug("Ricevuta richiesta creazione timbratura -> {}", stampingFromClientDto);

    if (stampingFromClientDto == null) {
      log.info("Ricevuta richiesta di creazione timbratura senza informazioni json nel body");
      return ResponseEntity.badRequest().build();
    }

    val stampingFromClient = stampingFromClientDto.convert();
    if (stampingFromClient.isEmpty()) {
      log.info("Ricevuta richiesta di creazione timbratura con informazioni incomplete"
          + " o errate nel body");
      return ResponseEntity.badRequest().build();
    }

    // Badge number not present (404)
    if (!stampingManager.linkToPerson(stampingFromClient.get()).isPresent()) {
      return ResponseEntity.notFound().build();
    }

    // Controllo timbratura con data troppo vecchia
    if (stampingManager.isTooFarInPast(stampingFromClient.get().getDateTime())) {
      log.info("Ignorata timbratura con data troppo nel passato: {}", stampingFromClient);
      return ResponseEntity.badRequest().build();
    }

    val stamping = stampingManager.createStampingFromClient(stampingFromClient.get(), true);

    // Stamping already present (409)
    if (!stamping.isPresent()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    // Success (200)
    return ResponseEntity.ok().body(stampingDtoMapper.convert(stamping.get()));
  }

  /**
   * Inserimento timbratura senza ricalcolo.
   */
  @PutMapping("/createNotRecompute")
  public ResponseEntity<StampingDto> createNotRecompute(
      @NotNull StampingFromClient stampingFromClient) {

    // Badge number not present (404)
    if (!stampingManager.linkToPerson(stampingFromClient).isPresent()) {
      return ResponseEntity.notFound().build();
    }

    // Controllo timbratura con data troppo vecchia
    if (stampingManager.isTooFarInPast(stampingFromClient.getDateTime())) {
      log.info("Ignorata timbratura con data troppo nel passato: {}", stampingFromClient);
      return ResponseEntity.badRequest().build();
    }

    val stamping = stampingManager.createStampingFromClient(stampingFromClient, false);

    // Stamping already present (409)
    if (!stamping.isPresent()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    // Success (200)
    return ResponseEntity.ok().body(stampingDtoMapper.convert(stamping.get()));
  }
}