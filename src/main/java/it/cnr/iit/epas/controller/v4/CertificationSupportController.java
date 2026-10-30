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
import it.cnr.iit.epas.dto.v4.certifications.CertificationOfficeDto;
import it.cnr.iit.epas.dto.v4.certifications.CertificationPersonDto;
import it.cnr.iit.epas.dto.v4.certifications.CertificationRowDto;
import it.cnr.iit.epas.dto.v4.certifications.CertificationSettingsDto;
import it.cnr.iit.epas.manager.certifications.MonthCertificationManager;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.security.NoCheck;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servizi per epas-certifications-service, che comunica con Attestati.
 *
 * <p>Gli endpoint sono riservati ai client di servizio configurati in
 * epas.security.certification-client-ids (vedi SecurityConfig), per questo non passano dalle
 * regole drools.</p>
 */
@NoCheck
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION)
@Tag(name = "Certification support controller",
    description = "Servizi riservati a epas-certifications-service.")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + CertificationSupportController.PATH)
public class CertificationSupportController {

  public static final String PATH = "/certifications";

  private final PersonDao personDao;
  private final OfficeDao officeDao;
  private final GeneralSettingDao generalSettingDao;
  private final MonthCertificationManager monthCertificationManager;

  @Operation(summary = "Informazioni sulla persona per gli attestati.")
  @GetMapping("/people/{personId}")
  @Transactional
  public ResponseEntity<CertificationPersonDto> person(@PathVariable Long personId) {
    return ResponseEntity.ok(toDto(findPerson(personId)));
  }

  @Operation(summary = "Informazioni sulla sede per gli attestati.")
  @GetMapping("/offices/{officeId}")
  @Transactional
  public ResponseEntity<CertificationOfficeDto> office(@PathVariable Long officeId) {
    val office = findOffice(officeId);
    val dto = new CertificationOfficeDto();
    dto.setId(office.getId());
    dto.setName(office.getName());
    dto.setCodeId(office.getCodeId());
    return ResponseEntity.ok(dto);
  }

  @Operation(summary = "Persone della sede da considerare per gli attestati del mese.")
  @GetMapping("/offices/{officeId}/people")
  @Transactional
  public ResponseEntity<List<CertificationPersonDto>> peopleInOffice(@PathVariable Long officeId,
      @RequestParam int year, @RequestParam int month) {
    val begin = LocalDate.of(year, month, 1);
    val end = begin.withDayOfMonth(begin.lengthOfMonth());
    return ResponseEntity.ok(personDao.list(Optional.empty(), Set.of(findOffice(officeId)), false,
        begin, end, true).list().stream().map(this::toDto).toList());
  }

  @Operation(summary = "Righe dell'attestato del mese calcolate da ePAS.")
  @GetMapping("/people/{personId}/monthCertifications")
  @Transactional
  public ResponseEntity<List<CertificationRowDto>> monthCertifications(
      @PathVariable Long personId, @RequestParam int year, @RequestParam int month) {
    return ResponseEntity.ok(
        monthCertificationManager.monthCertifications(findPerson(personId), year, month));
  }

  @Operation(summary = "Impostazioni generali relative ad Attestati.")
  @GetMapping("/generalSettings")
  public ResponseEntity<CertificationSettingsDto> generalSettings() {
    val settings = generalSettingDao.generalSetting();
    val dto = new CertificationSettingsDto();
    dto.setEnableSsoForAttestati(settings.isEnableSsoForAttestati());
    dto.setTimeoutAttestati(settings.getTimeoutAttestati());
    return ResponseEntity.ok(dto);
  }

  private CertificationPersonDto toDto(Person person) {
    val dto = new CertificationPersonDto();
    dto.setId(person.getId());
    dto.setFullname(person.getFullname());
    dto.setNumber(person.getNumber());
    if (person.getOffice() != null) {
      dto.setOfficeId(person.getOffice().getId());
      dto.setOfficeCodeId(person.getOffice().getCodeId());
    }
    return dto;
  }

  private Person findPerson(Long personId) {
    return personDao.byId(personId)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));
  }

  private Office findOffice(Long officeId) {
    return officeDao.byId(officeId)
        .orElseThrow(() -> new EntityNotFoundException("Office not found"));
  }
}
