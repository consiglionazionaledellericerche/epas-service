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
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dto.v4.ConfigurationsPersonShowDto;
import it.cnr.iit.epas.dto.v4.PersonConfigurationDto;
import it.cnr.iit.epas.dto.v4.mapper.ConfigurationsPersonMapper;
import it.cnr.iit.epas.helpers.TemplateUtility;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonConfiguration;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    name = "Configurations controller",
    description = "Visualizzazione delle configurazioni dei dipendenti.")
@Transactional
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/configurations")
class ConfigurationsController {
  private final SecurityRules rules;
  private final PersonDao personDao;
  private final ConfigurationManager configurationManager;
  private final ConfigurationsPersonMapper configurationsPersonMapper;
  private final TemplateUtility templateUtility;

  @Operation(
      summary = "Visualizzazione delle configurazioni di una persona.",
      description = "Questo endpoint è utilizzabile dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin per visualizzare "
          + "le configurazioni di una persona")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti le configurazioni di una persona"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati delle configurazioni",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Configurazioni non trovati con l'id della persona fornito",
          content = @Content)
  })
  @GetMapping("/personShow")
  ResponseEntity<ConfigurationsPersonShowDto> personShow(@RequestParam("personId") Long personId) {
    log.debug("REST method {} invoked with parameters personId ={}",
        "/rest/v4/configurations/personShow", personId);

    Person person = personDao.getPersonById(personId);

    if (person == null) {
      return ResponseEntity.notFound().build();
    }

    rules.checkifPermitted(person.getOffice());

    log.debug("Person {}", person);

    List<PersonConfiguration> currentConfiguration = configurationManager
        .getPersonConfigurationsByDate(person, LocalDate.now());

    List<PersonConfigurationDto> pcdto = Lists.newArrayList();
    for (PersonConfiguration conf : currentConfiguration) {
      EpasParam ep = (EpasParam) conf.getType();
      val edto = configurationsPersonMapper.convert(ep);
      val cdto = configurationsPersonMapper.convert(conf);
      cdto.setEpasParams(edto);
      val valore = (boolean) templateUtility.getObjectInConfiguration(ep, (String) conf.getValue());
      cdto.setValore(valore);
      pcdto.add(cdto);
    }

    boolean enableCovid = templateUtility.enableCovid();
    boolean enableSmartworking = templateUtility.enableSmartworking();

    ConfigurationsPersonShowDto dto = new ConfigurationsPersonShowDto();

    dto.setEnableCovid(enableCovid);
    dto.setEnableSmartworking(enableSmartworking);
    dto.setPersonConfigurations(pcdto);

    return ResponseEntity.ok().body(dto);
  }

}