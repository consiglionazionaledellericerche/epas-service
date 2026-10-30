/*
 * Copyright (C) 2026  Consiglio Nazionale delle Ricerche
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.GeneralSettingDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dto.v4.configurations.ConfigurationOwnerDto;
import it.cnr.iit.epas.dto.v4.configurations.ConfigurationSettingsDto;
import it.cnr.iit.epas.dto.v4.configurations.RecomputationDto;
import it.cnr.iit.epas.dto.v4.configurations.RecomputationRequestDto;
import it.cnr.iit.epas.manager.configurations.ConfigurationRecomputationManager;
import it.cnr.iit.epas.security.NoCheck;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servizi per epas-configurations-service, che gestisce le configurazioni di sedi e persone.
 *
 * <p>Gli endpoint sono riservati ai client di servizio configurati in
 * epas.security.configuration-client-ids (vedi SecurityConfig), per questo non passano dalle
 * regole drools.</p>
 */
@NoCheck
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION)
@Tag(name = "Configuration support controller",
    description = "Servizi riservati a epas-configurations-service.")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + ConfigurationSupportController.PATH)
public class ConfigurationSupportController {

  public static final String PATH = "/configurations";

  private final PersonDao personDao;
  private final OfficeDao officeDao;
  private final GeneralSettingDao generalSettingDao;
  private final ConfigurationRecomputationManager recomputationManager;

  @Operation(summary = "Periodo di validità della sede.")
  @GetMapping("/offices/{officeId}")
  @Transactional
  public ResponseEntity<ConfigurationOwnerDto> office(@PathVariable Long officeId) {
    val office = officeDao.byId(officeId)
        .orElseThrow(() -> new EntityNotFoundException("Office not found"));
    val dto = new ConfigurationOwnerDto();
    dto.setId(office.getId());
    dto.setBeginDate(office.getBeginDate());
    dto.setEndDate(office.calculatedEnd());
    return ResponseEntity.ok(dto);
  }

  @Operation(summary = "Periodo di validità della persona e sua sede.")
  @GetMapping("/people/{personId}")
  @Transactional
  public ResponseEntity<ConfigurationOwnerDto> person(@PathVariable Long personId) {
    val person = personDao.byId(personId)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    val dto = new ConfigurationOwnerDto();
    dto.setId(person.getId());
    dto.setBeginDate(person.getBeginDate());
    dto.setEndDate(person.calculatedEnd());
    dto.setOfficeId(person.getOffice().getId());
    return ResponseEntity.ok(dto);
  }

  @Operation(summary = "Impostazioni generali usate dalle configurazioni.")
  @GetMapping("/generalSettings")
  public ResponseEntity<ConfigurationSettingsDto> generalSettings() {
    val dto = new ConfigurationSettingsDto();
    dto.setEnableAbsenceTopLevelAuthorization(
        generalSettingDao.generalSetting().isEnableAbsenceTopLevelAuthorization());
    return ResponseEntity.ok(dto);
  }

  @Operation(summary = "Avvia in background il ricalcolo dopo una modifica di configurazione.")
  @PostMapping("/recomputations")
  public ResponseEntity<RecomputationDto> requestRecomputation(
      @Valid @RequestBody RecomputationRequestDto request) {
    recomputationManager.checkOwner(request);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(recomputationManager.start(request));
  }

  @Operation(summary = "Stato di un ricalcolo. Lo stato è in memoria e dura un giorno.")
  @GetMapping("/recomputations/{id}")
  public ResponseEntity<RecomputationDto> recomputation(@PathVariable String id) {
    return ResponseEntity.of(recomputationManager.status(id));
  }
}
