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
import it.cnr.iit.epas.dto.v4.YearsDropDownDto;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.repo.UserRepository;
import it.cnr.iit.epas.security.SecureUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi al recupero delle informazioni iniziali ad. esempio gli
 * anni per popolare la dropbox
 *
 * @author
 */
@SecurityRequirements(
    value = { 
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION), 
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(
    name = "Request Init Controller", 
    description = "Informazioni utili per la nuova UI di ePAS")
@Slf4j
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/init")
public class RequestInit {

  //  private final PersonDao personDao;
  private UserRepository repo;
  private SecureUtils securityUtils;

  /**
   * Costruttore di default per l'injection.
   */
  @Inject
  RequestInit(UserRepository repo, SecureUtils securityUtils) {
    this.repo = repo;
    this.securityUtils = securityUtils;
  }

  @Operation(
      summary = "Lista degli anni di utilizzo di ePAS da parte dell'utente corrente.",
      description = "Questo endpoint è utilizzabile da tutti gli utenti autenticati.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituite le informazioni relative agli anni in cui la persona "
              + "autenticata ha usato il sistema."),
      @ApiResponse(responseCode = "401", 
          description = "Autenticazione non presente", content = @Content), 
      @ApiResponse(responseCode = "404", 
          description = "Utente che ha effettuato la richiesta non ha associato nessuna persona.",
            content = @Content)
  })
  @GetMapping("/yearsdropdown")
  ResponseEntity<YearsDropDownDto> yearsDropDown() {
    Optional<User> user = securityUtils.getCurrentUser();
    log.debug("UserInfo::yearsDropDown user = {}", user.orElse(null));

    if (!user.isPresent()) {
      return ResponseEntity.badRequest().build();
    }

    long personId = user.get().getId();
    log.debug("UserInfo::yearsDropDown personId = {}", personId);

    val entity = repo.findById(personId)
        .orElseThrow(() ->
            new EntityNotFoundException(
                "Person not found for user with id = " + user.get().getId()));
    log.debug("UserInfo::yearsDropDown user entity = {}", entity);

    val person = entity.getPerson();
    log.debug("UserInfo::yearsDropDown user person = {}", person);

    List<Integer> years = Lists.newArrayList();
    int minYear = LocalDate.now().getYear();

    for (Contract contract : person.getContracts()) {
      Office office = contract.person.getOffice();

      if (office.getBeginDate().getYear() < minYear) {
        minYear = office.getBeginDate().getYear();
      }
    }
    // Oltre alle sedi amminisitrate anche gli anni della propria sede per le viste dipendente.
    if (user.get().getPerson() != null && user.get().getPerson().getOffice() != null) {
      //if (office.getBeginDate().getYear() < minYear) {
      minYear = user.get().getPerson().getOffice().getBeginDate().getYear();
    }
    for (int i = minYear; i <= LocalDate.now().plusYears(1).getYear(); i++) {
      years.add(i);
    }

    YearsDropDownDto dto = new YearsDropDownDto();
    dto.setYears(years);

    return ResponseEntity.ok().body(dto);
  }
}