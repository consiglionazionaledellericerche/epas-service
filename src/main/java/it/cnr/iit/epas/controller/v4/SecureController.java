/*
 * Copyright (C) 2024  Consiglio Nazionale delle Ricerche
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

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.security.SecurityService;
import it.cnr.iit.epas.security.SecurityService.EntityType;
import lombok.RequiredArgsConstructor;

/**
 * Metodi REST per la verifica dei permessi.
 */
@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(
    name = "Secure Controller",
    description = "Controllo di sicurezza su path e riferimenti agli oggetti.")
@Transactional
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/secure")
public class SecureController {

  private final SecurityService securityService;

  /**
   * Visualizzazione delle informazioni di accesso ad un controller.
   */
  @Operation(
      summary = "Verifica se l'utente corrente ha l'accesso ad un certo endpoint REST.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituita l'autorizzazione true/false di accedere all'endpoint indicato"),
      @ApiResponse(responseCode = "401",
      description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
      description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
          + " i permessi di questo controller",
          content = @Content),
      @ApiResponse(responseCode = "404",
      description = "Entity non trovata con l'id fornito",
      content = @Content)
  })
  @GetMapping("/check")
  public ResponseEntity<Boolean> secureCheck(
      @RequestParam("method") String method,
      @RequestParam("path") String path,
      @RequestParam("entityType") Optional<EntityType> entityType,
      @RequestParam("targetType") Optional<EntityType> targetType,
      @RequestParam("id") Optional<Long> id) throws Exception {

    return ResponseEntity.ok(
        securityService.secureCheck(method, path, entityType, targetType, id));

  }

}
