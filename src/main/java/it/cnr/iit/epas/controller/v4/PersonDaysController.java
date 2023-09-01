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
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dto.v4.PersonDayDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonDayMapper;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
 * Controller con i metodi REST relativi alla visualizzazione e gestione dei riepilogi
 * giornalieri dei dipendenti.
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
    name = "PersonDays Controller", 
    description = "Gestione e visualizzazione delle informazioni giornaliere dei dipendenti")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/persondays")
public class PersonDaysController {

  private final PersonDayDao personDayDao;
  private final PersonDayMapper personDayMapper;
  private final PersonFinder personFinder;

  @Operation(
      summary = "Visualizzazione delle informazioni giornaliere di un mese di un dipendente.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona associata all'assenza e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituita la lista dei riepiloghi giornalieri del mese richiesto."),
      @ApiResponse(responseCode = "401", 
      description = "Autenticazione non presente", content = @Content), 
      @ApiResponse(responseCode = "403", 
      description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
          + " i dati giornalieri della persona richiesta",
          content = @Content), 
      @ApiResponse(responseCode = "404", 
      description = "Persona non trovata con il personId o fil fiscalCode forniti",
      content = @Content)
  })
  @GetMapping(ApiRoutes.LIST)
  ResponseEntity<List<PersonDayDto>> list(
      @RequestParam("personId") Optional<Long> personId,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("year") Integer year, 
      @RequestParam("month") Integer month) {
    log.debug("REST method {} invoked with parameters personId={}, fiscalCode = {}, year={}, "
        + "month={}", ApiRoutes.LIST, personId, fiscalCode, year, month);
    val person = personFinder.getPerson(personId, fiscalCode)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    val personDays = 
        personDayDao.getPersonDayInMonth(person, YearMonth.of(year, month));
    val personDaysDto = 
        personDays.stream().map(personDayMapper::convert).collect(Collectors.toList());
    return ResponseEntity.ok().body(personDaysDto);
  }
}