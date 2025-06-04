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
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dto.v4.PersonShowDto;
import it.cnr.iit.epas.dto.v4.PersonShowExtendedDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonShowExtendedMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonShowMapper;
import it.cnr.iit.epas.helpers.TemplateUtility;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.repo.PersonRepository;
import it.cnr.iit.epas.security.SecureUtils;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi alla visualizzazione e gestione dei dati personali
 * di una persona.
 *
 * @author Cristian Lucchesi
 *
 */
@SecurityRequirements(
    value = { 
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION), 
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(
    name = "Person Info Controller", 
    description = "Visualizzazione delle informazioni della persona correntemente autenticata")
@Slf4j
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/personinfo")
public class PersonInfo {

  private PersonRepository repo;
  private PersonShowMapper mapper;
  private PersonShowExtendedMapper mapperExtend;
  private SecureUtils secureUtils;
  private TemplateUtility templateUtility;
  
  /**
   * Costruttore di default per l'injection.
   */
  @Inject
  PersonInfo(PersonRepository repo, PersonShowMapper mapper,
      SecureUtils securityUtils, PersonShowExtendedMapper mapperExtend,
      TemplateUtility templateUtility) {
    this.repo = repo;
    this.mapper = mapper;
    this.secureUtils = securityUtils;
    this.mapperExtend = mapperExtend;
    this.templateUtility = templateUtility;
  }

  @Operation(
      summary = "Mostra le informazioni della persona collegata all'utente autenticato.",
      description = "Questo endpoint è utilizzabile da tutti gli utenti autenticati.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituite le informazioni relative alla persona autenticata."),
      @ApiResponse(responseCode = "401", 
          description = "Autenticazione non presente", content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Utente che ha effettuato la richiesta non ha associato nessuna persona.",
            content = @Content)
  })
  @GetMapping
  ResponseEntity<PersonShowDto> show() {
    Optional<User> user = secureUtils.getCurrentUser();
    log.debug("UserInfo::show user = {}", user.orElse(null));
    if (!user.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    long personId = user.get().getPerson().getId();
    val entity = repo.findById(personId)
        .orElseThrow(() -> 
        new EntityNotFoundException("Person not found for user with id = "  + user.get().getId()));
    return ResponseEntity.ok().body(mapper.convert(entity));
  }

  @Operation(
      summary = "Mostra le informazioni della persona collegata all'utente autenticato.",
      description = "Questo endpoint è utilizzabile da tutti gli utenti autenticati.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituite le informazioni relative alla persona autenticata."),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Utente che ha effettuato la richiesta non ha associato nessuna persona.",
          content = @Content)
  })
  @GetMapping("/extend")
  ResponseEntity<PersonShowExtendedDto> extend() {
    Optional<User> user = secureUtils.getCurrentUser();
    log.debug("UserInfo::show user = {}", user.orElse(null));
    if (!user.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    Person person = user.get().getPerson();
    boolean isAvailable = templateUtility.isAvailable(person);
    long personId = person.getId();
    val entity = repo.findById(personId)
        .orElseThrow(() ->
            new EntityNotFoundException("Person not found for user with id = " 
                + user.get().getId()));
    PersonShowExtendedDto dto = mapperExtend.convert(entity);
    dto.setAvailable(isAvailable);
    return ResponseEntity.ok().body(dto);
  }

}