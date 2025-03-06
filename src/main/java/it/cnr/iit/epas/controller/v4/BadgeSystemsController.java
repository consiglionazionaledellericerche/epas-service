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
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.GeneralSettingDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.dto.v4.BadgeDto;
import it.cnr.iit.epas.dto.v4.BadgesShowDto;
import it.cnr.iit.epas.dto.v4.mapper.BadgeSystemMapper;
import it.cnr.iit.epas.dto.v4.mapper.ContractShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.EntityToDtoConverter;
import it.cnr.iit.epas.dto.v4.mapper.OfficeShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonChildrenMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonVacationMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonWrapperShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.ShowCurrentContractWttMapper;
import it.cnr.iit.epas.manager.PersonManager;
import it.cnr.iit.epas.models.Badge;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.security.SecurityRules;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)})
@Tag(
    name = "Badge System controller",
    description = "Badge dei dipendenti.")
@Slf4j
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/badgesystems")
public class BadgeSystemsController {
  private final SecurityRules rules;
  private final PersonDao personDao;
  private final BadgeSystemMapper badgeSystemMapper;
  private final PersonShowMapper personMapper;

  @Inject
  BadgeSystemsController(SecurityRules rules, PersonDao personDao, BadgeSystemMapper badgeSystemMapper, PersonShowMapper personMapper) {
    this.personDao = personDao;
    this.personMapper = personMapper;
    this.badgeSystemMapper = badgeSystemMapper;
    this.rules = rules;
  }


  @Operation(
      summary = "Visualizzazione dei badge della persona.",
      description = "Questo endpoint è utilizzabile dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin per visualizzare "
          + "le configurazioni di una persona")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i badge della persona"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati dei badge della persona",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Badge non trovati con l'id della persona fornito",
          content = @Content)
  })
  @GetMapping("/personBadges")
  ResponseEntity<BadgesShowDto> personBadges(@RequestParam("personId") Long personId) {
    log.debug("REST method {} invoked with parameters personId ={}",
        "/rest/v4/badgesystems/personBadges", personId);

    Person person = personDao.getPersonById(personId);

    if (person == null) {
      return ResponseEntity.notFound().build();
    }

    rules.checkifPermitted(person.getOffice());

    BadgesShowDto dto = new BadgesShowDto();
    List<BadgeDto> badgesDto = Lists.newArrayList();

    for (Badge badge : person.getBadges()) {
      badgesDto.add(badgeSystemMapper.convert(badge));
    }
    dto.setBadges(badgesDto);
    dto.setPerson(personMapper.convert(person));

    return ResponseEntity.ok().body(dto);
  }

}