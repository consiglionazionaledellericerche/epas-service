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
import it.cnr.iit.epas.controller.v4.utils.PersonFinder;
import it.cnr.iit.epas.dao.CompetenceCodeDao;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.dto.v4.CompetenceCodeDto;
import it.cnr.iit.epas.dto.v4.CompetenceDto;
import it.cnr.iit.epas.dto.v4.CompetencesDto;
import it.cnr.iit.epas.dto.v4.mapper.CompetencesMapper;
import it.cnr.iit.epas.manager.CompetenceManager;
import it.cnr.iit.epas.manager.recaps.competences.PersonMonthCompetenceRecap;
import it.cnr.iit.epas.manager.recaps.competences.PersonMonthCompetenceRecapFactory;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonCompetenceCodes;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
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
    name = "Competence controller",
    description = "Visualizzazione delle informazioni sulle competenze dei dipendenti.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/competences")
class CompetencesController {
  private final WrapperFactory wrapperFactory;
  private final SecurityRules rules;
  private final PersonFinder personFinder;
  private final CompetenceManager competenceManager;
  private final CompetenceDao competenceDao;
  private final CompetenceCodeDao competenceCodeDao;
  private final PersonMonthCompetenceRecapFactory personMonthCompetenceRecapFactory;
  private final CompetencesMapper competencesMapper;

  @Operation(
      summary = "Visualizzazione delle competenze.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "le proprie competenze, oppure dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i dati delle competenze"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati delle competenze",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Competenze non trovati con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping(ApiRoutes.LIST)
  ResponseEntity<CompetencesDto> show(
      @RequestParam("personId") Optional<Long> personId,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @NotNull @RequestParam("year") Integer year,
      @NotNull @RequestParam("month") Integer month) {
    log.debug("REST method {} invoked with parameters year={}, month={}, personId ={}",
        "/rest/v4/competences" + ApiRoutes.LIST, year, month, personId);

    Person person = personFinder.getPerson(personId, fiscalCode)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(person);

    log.debug("Person {}", person);

    LocalDate today = LocalDate.now();
    if (year > today.getYear() || today.getYear() == year && month > today.getMonth().getValue()) {
      return ResponseEntity.badRequest().build();
    }

    Optional<Contract> contract = wrapperFactory
        .create(person).getCurrentContract();

    if (!contract.isPresent()) {
      return ResponseEntity.badRequest().build();
      //Stampings.stampings(year, month);
    }
    log.debug("contract {}", contract);

    List<PersonCompetenceCodes> pccList = competenceCodeDao
        .listByPerson(person,
            Optional.ofNullable(LocalDate.now().withMonth(month).withYear(year)));
    List<CompetenceCode> codeListIds = Lists.newArrayList();
    for (PersonCompetenceCodes pcc : pccList) {
      codeListIds.add(pcc.getCompetenceCode());
    }

    List<Competence> competenceList = competenceDao.getCompetences(Optional.ofNullable(person),
        year, Optional.ofNullable(month), codeListIds);
    Map<CompetenceCode, String> map = competenceManager.createMapForCompetences(competenceList);

    Optional<PersonMonthCompetenceRecap> personMonthCompetenceRecap = 
        personMonthCompetenceRecapFactory.create(
        contract.get(), month, year);

    List<CompetenceCodeDto> competenceCodeListDto = Lists.newArrayList();

    for (java.util.Map.Entry<CompetenceCode, String> entry : map.entrySet()) {
      CompetenceCodeDto cptCodeDto = new CompetenceCodeDto();
      cptCodeDto.setCode(entry.getKey().getCode());
      cptCodeDto.setDescription(entry.getKey().getDescription());
      cptCodeDto.setValue(entry.getValue());
      competenceCodeListDto.add(cptCodeDto);
    }

    CompetencesDto cptDto = new CompetencesDto();
    cptDto.setPersonMonthCompetenceRecap(
        competencesMapper.convertPersonMonthCompetenceRecap(personMonthCompetenceRecap.get()));

    cptDto.setCompetencesCode(competenceCodeListDto);
    List<CompetenceDto> tp = Lists.newArrayList();
    for (Competence cp : competenceList) {
      tp.add(competencesMapper.convertCompetence(cp));
    }
    cptDto.setCompetences(tp);
    cptDto.setMonth(month);
    cptDto.setYear(year);
    cptDto.setPersonId(person.getId());

    return ResponseEntity.ok().body(cptDto);
  }

}