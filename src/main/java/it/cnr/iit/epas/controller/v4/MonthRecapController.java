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
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.controller.v4.utils.PersonFinder;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dto.v4.PersonStampingRecapDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonStampingRecapMapper;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingRecap;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingRecapFactory;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.security.SecurityRules;
import jakarta.validation.constraints.NotNull;
import java.time.YearMonth;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi ai riepiloghi mensili.
 *
 * @author Cristian Lucchesi
 *
 */
@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)})
@Tag(
    name = "MonthRecap controller",
    description = "Visualizzazione dei riepiloghi mensili dei dipendenti.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/monthrecaps")
public class MonthRecapController {

  private final IWrapperFactory wrapperFactory;
  private final PersonStampingRecapFactory stampingRecapFactory;
  private final PersonStampingRecapMapper personStampingRecapMapper;
  private final SecurityRules rules;
  private final PersonFinder personFinder;

  @Operation(
      summary = "Visualizzazione dei riepiloghi mensili dei dipendenti.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "la propria situazione mensile, oppure dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i dati dei riepiloghi mensili"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati delle ferie e dei permessi",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Ferie e permessi non trovati con l'id fornito",
          content = @Content)
  })
  @GetMapping(ApiRoutes.LIST)
  ResponseEntity<PersonStampingRecapDto> show(
      @RequestParam("personId") Optional<Long> personId,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @NotNull @RequestParam("year") Integer year,
      @NotNull @RequestParam("month") Integer month) {
    log.debug("REST method {} invoked with parameters personId={}, year={}, month={}",
        "/rest/v4/monthrecaps" + ApiRoutes.LIST, personId, year, month);

    Person person = 
        personFinder.getPerson(personId, fiscalCode)
          .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(person);

    val wrPerson = wrapperFactory.create(person);
    if (!wrPerson.isActiveInMonth(YearMonth.of(year, month))) {
      return ResponseEntity.notFound().build();
    }

    PersonStampingRecap psrDto = stampingRecapFactory.create(person, year, month, true);
    return ResponseEntity.ok().body(personStampingRecapMapper.convert(psrDto));
  }

}