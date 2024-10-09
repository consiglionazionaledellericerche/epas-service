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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.PersonMonthRecapDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.dao.wrapper.IWrapperContractMonthRecap;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dto.v4.PersonMonthRecapDto;
import it.cnr.iit.epas.dto.v4.PersonMonthsDto;
import it.cnr.iit.epas.dto.v4.TrainingHoursDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonMonthRecapMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonMonthsMapper;
import it.cnr.iit.epas.dto.v4.mapper.TrainingHoursMapper;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonMonthRecap;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.YearMonth;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi alla gestione dei PersonMonths.
 *
 * @author Cristian Lucchesi
 */
@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)})
@Tag(
    name = "PersonMonths controller",
    description = "Visualizzazione dei riepiloghi orari e formazione dei dipendenti.")
@Transactional
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/personmonths")
public class PersonMonthsController {

  private final IWrapperFactory wrapperFactory;
  private final PersonMonthsMapper personMonthsMapper;
  private final PersonMonthRecapMapper personMonthRecapMapper;
  private final TrainingHoursMapper trainingHoursMapper;
  private final PersonMonthRecapDao personMonthRecapDao;
  private final SecurityRules rules;
  private final SecureUtils securityUtils;

  @Operation(
      summary = "Visualizzazione del riepilogo orario del dipendente.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "la propria situazione oraria, oppure dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i dati dei riepiloghi orari"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati del riepilogo delle ore.",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Riepilogo ore non trovato con l'anno fornito",
          content = @Content)
  })
  @GetMapping("/hourRecap")
  ResponseEntity<List<PersonMonthsDto>> hourRecap(
      @NotNull @RequestParam("year") Integer year) {
    log.debug("REST method {} invoked with parameters year={}",
        "/rest/v4/personmonths/hourRecap", year);

    Optional<User> user = securityUtils.getCurrentUser();

    if (!user.isPresent() || user.get().getPerson() == null) {
      //flash.error("Accesso negato.");
      //renderTemplate("Application/indexAdmin.html");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    if (year > LocalDate.now().getYear()) {
      //flash.error("Impossibile richiedere riepilogo anno futuro.");
      //renderTemplate("Application/indexAdmin.html");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    Person person = user.get().getPerson();
    Optional<Contract> contract = wrapperFactory.create(person).getCurrentContract();

    List<PersonMonthsDto> pmListDto = Lists.newArrayList();
    Preconditions.checkState(contract.isPresent());
    List<IWrapperContractMonthRecap> recaps = Lists.newArrayList();
    IWrapperContractMonthRecap wp;
    YearMonth actual = YearMonth.of(year, 1);
    YearMonth last = YearMonth.of(year, 12);
    IWrapperContract con = wrapperFactory.create(contract.get());
    while (!actual.isAfter(last)) {
      Optional<ContractMonthRecap> recap = con.getContractMonthRecap(actual);
      if (recap.isPresent()) {
        wp = wrapperFactory.create(recap.get());
        PersonMonthsDto pmDto = personMonthsMapper.convert(wp);
        pmListDto.add(pmDto);
      }
      actual = actual.plusMonths(1);
    }
    return ResponseEntity.ok().body(pmListDto);
  }

  @Operation(
      summary = "Visualizzazione del riepilogo delle ore di formazione annuale del dipendente.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "la propria situazione delle ore di formazione, oppure dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i dati dei riepiloghi delle ore di formazione"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati del riepilogo delle ore di formazione.",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Riepilogo ore di formazione non trovato con l'anno fornito",
          content = @Content)
  })
  @GetMapping("/trainingHours")
  ResponseEntity<TrainingHoursDto> trainingHours(
      @NotNull @RequestParam("year") Integer year) {
    log.debug("REST method {} invoked with parameters year={}",
        "/rest/v4/personmonths/trainingHours", year);

    Person person = securityUtils.getCurrentUser().get().getPerson();

    List<PersonMonthRecap> personMonthRecapList = personMonthRecapDao
        .getPersonMonthRecapInYearOrWithMoreDetails(person, year,
            Optional.empty(), Optional.empty());

    LocalDate today = LocalDate.now();
    List<PersonMonthRecapDto> dtoList = new ArrayList<>();
    personMonthRecapList.forEach(pmr -> dtoList.add(personMonthRecapMapper.convert(pmr)));

    TrainingHoursDto dto = new TrainingHoursDto();
    dto.setPerson(trainingHoursMapper.convert(person));
    dto.setToday(today);
    dto.setPersonMonthRecaps(dtoList);

    return ResponseEntity.ok().body(dto);
  }
}