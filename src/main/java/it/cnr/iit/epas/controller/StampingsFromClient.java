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

  @PutMapping("/create")
  ResponseEntity<StampingDto> create(@NotNull StampingFromClientDto stampingFromClientDto) {

    val stampingFromClient = stampingFromClientDto.convert();

    // Badge number not present (404)
    if (!stampingManager.linkToPerson(stampingFromClient).isPresent()) {
      return ResponseEntity.notFound().build();
    }

    // Controllo timbratura con data troppo vecchia
    if (stampingManager.isTooFarInPast(stampingFromClient.getDateTime())) {
      log.info("Ignorata timbratura con data troppo nel passato: {}", stampingFromClient);
      return ResponseEntity.badRequest().build();
    }

    val stamping = stampingManager.createStampingFromClient(stampingFromClient, true);

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