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
import it.cnr.iit.epas.dao.MealTicketDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dto.v4.MealTicketRecapDto;
import it.cnr.iit.epas.dto.v4.MealTicketRecapShowDto;
import it.cnr.iit.epas.dto.v4.mapper.MealTicketRecapMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonShowTerseMapper;
import it.cnr.iit.epas.manager.services.mealtickets.IMealTicketsService;
import it.cnr.iit.epas.manager.services.mealtickets.MealTicketRecap;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi ai riepiloghi dei buoni pasto dipendente.
 *
 * @author Cristian Lucchesi
 *
 */
@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)})
@Tag(
    name = "MealTickets controller",
    description = "Visualizzazione dei riepiloghi mensili dei dipendenti.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/mealtickets")
public class MealTicketsController {

  private final IWrapperFactory wrapperFactory;
  private final IMealTicketsService mealTicketService;
  private final SecurityRules rules;
  private final PersonFinder personFinder;
  private final PersonDao personDao;
  private final MealTicketDao mealTicketDao;
  private final MealTicketRecapMapper mealTicketRecapMapper;
  private final PersonShowTerseMapper personShowTerseMapper;

  @Operation(
      summary = "Visualizzazione del riepilogo dei buoni pasto dei dipendenti.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "la propria situazione dei buoni pasto, oppure dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i dati del riepilogo dei buoni pasto"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati dei buoni pasto",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "situazione di buoni pasto non trovata",
          content = @Content)
  })
  @GetMapping(ApiRoutes.LIST)
  ResponseEntity<MealTicketRecapShowDto> show(
      @RequestParam("personId") Optional<Long> personId,
      @RequestParam("fiscalCode") Optional<String> fiscalCode) {
    log.debug("REST method {} invoked with parameters personId={}",
        "/rest/v4/mealtickets" + ApiRoutes.LIST, personId);

    Person person = 
        personFinder.getPerson(personId, fiscalCode)
          .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(person);

    MealTicketRecap recapPrevious = null; // TODO: nella vista usare direttamente optional

    Optional<Contract> contract = wrapperFactory.create(person).getCurrentContract();
    Preconditions.checkState(contract.isPresent());

    // riepilogo contratto corrente
    MealTicketRecap recap = mealTicketService.create(contract.get()).orElse(null);
    Preconditions.checkNotNull(recap);

    //riepilogo contratto precedente
    Contract previousContract = personDao.getPreviousPersonContract(contract.get());
    if (previousContract != null) {
      Optional<MealTicketRecap> previousRecap = mealTicketService.create(previousContract);
      if (previousRecap.isPresent()) {
        recapPrevious = previousRecap.get();
      }
    }


    //TODO mettere nel default.
    Integer ticketNumberFrom = 1;
    Integer ticketNumberTo = 22;

    MealTicketRecapShowDto dto = new MealTicketRecapShowDto();
    MealTicketRecapDto recapDto = mealTicketRecapMapper.convert(recap);

    LocalDate deliveryDate = LocalDate.now();
    LocalDate today = LocalDate.now();
    LocalDate expireDate = mealTicketDao.getFurtherExpireDateInOffice(person.getOffice());
    recapDto.setBlockMealTicketReceivedDeliveryDesc(
        mealTicketRecapMapper.convert(recap.getBlockMealTicketReceivedDeliveryDesc()));
    dto.setDeliveryDate(deliveryDate);
    dto.setToday(today);
    dto.setPerson(personShowTerseMapper.convert(person));
    dto.setRecap(recapDto);
    dto.setRecapPrevious(mealTicketRecapMapper.convert(recapPrevious));
    dto.setExpireDate(expireDate);
    dto.setTicketNumberFrom(ticketNumberFrom);
    dto.setTicketNumberTo(ticketNumberTo);

    return ResponseEntity.ok().body(dto);
  }

}