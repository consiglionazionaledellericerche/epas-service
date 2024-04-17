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
import com.google.common.collect.Maps;
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
import it.cnr.iit.epas.dto.v4.AbsenceFormDto;
import it.cnr.iit.epas.dto.v4.AbsenceGroupsDto;
import it.cnr.iit.epas.dto.v4.AbsencePeriodTerseDto;
import it.cnr.iit.epas.dto.v4.CategoryGroupAbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.CategoryTabDto;
import it.cnr.iit.epas.dto.v4.DayInPeriodDto;
import it.cnr.iit.epas.dto.v4.GroupAbsenceTypeDto;
import it.cnr.iit.epas.dto.v4.PeriodChainDto;
import it.cnr.iit.epas.dto.v4.TemplateRowDto;
import it.cnr.iit.epas.dto.v4.mapper.AbsenceFormMapper;
import it.cnr.iit.epas.dto.v4.mapper.AbsenceGroupsMapper;
import it.cnr.iit.epas.manager.recaps.absencegroups.AbsenceGroupsRecap;
import it.cnr.iit.epas.manager.recaps.absencegroups.AbsenceGroupsRecapFactory;
import it.cnr.iit.epas.manager.services.absences.AbsenceForm;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.model.AbsencePeriod;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod.TemplateRow;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Metodi REST per la gestione dei gruppi di assenza.
 */
@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)
    })
@Tag(
    name = "Absences Groups Controller",
    description = "Gestione e visualizzazione deli gruppi di assenza.")
@Transactional
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/absencesGroups")
public class AbsencesGroupsController {

  private final AbsenceGroupsRecapFactory absenceGroupsRecapFactory;
  private final AbsenceGroupsMapper absenceGroupsMapper;
  private final AbsenceFormMapper absenceFormMapper;
  private final AbsenceService absenceService;
  private final PersonFinder personFinder;
  private final SecurityRules rules;

  /**
   * Elenco delle assenze in un mese.
   */
  @Operation(
      summary = "Visualizzazione della lista delle assenza di una persona in un mese.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituito l'elenco delle assenze in un mese"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " l'elenco delle assenze",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping("/groupStatus")
  public ResponseEntity<AbsenceGroupsDto> groupStatus(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("groupAbsenceTypeId") Long groupAbsenceTypeId,
      @RequestParam("from") String from) {
    log.debug("AbsencesGroupsController::groupStatus groupAbsenceTypeId = {} from = {}",
        groupAbsenceTypeId, from);
    LocalDate fromLocalDate = LocalDate.parse(from);
    Person person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(person);

    AbsenceGroupsRecap psrDto = absenceGroupsRecapFactory.create(person, groupAbsenceTypeId,
        fromLocalDate);

    SortedMap<LocalDate, DayInPeriodDto> newDaysInPeriod = Maps.newTreeMap();
    List<AbsencePeriodTerseDto> newPeriodDto = Lists.newArrayList();

    for (AbsencePeriod sp : psrDto.periodChain.periods) {
      AbsencePeriodTerseDto aspDto = absenceGroupsMapper.convertAbsencePeriodTerse(sp);
      for (DayInPeriod dp : sp.daysInPeriod.values()) {
        DayInPeriodDto dpDto = absenceGroupsMapper.convertDayInPeriod(dp);
        List<TemplateRowDto> tp = Lists.newArrayList();
        for (TemplateRow tr : dp.allTemplateRows()) {
          tp.add(absenceGroupsMapper.convertTemplateRow(tr));
        }
        dpDto.setRowRecap(tp);
        newDaysInPeriod.put(dp.getDate(), dpDto);
      }
      aspDto.setDaysInPeriod(newDaysInPeriod);
      newPeriodDto.add(aspDto);
    }

    AbsenceGroupsDto absGroupDto = absenceGroupsMapper.convert(psrDto);
    PeriodChainDto newPeriodChain = absGroupDto.getPeriodChain();
    newPeriodChain.setPeriods(newPeriodDto);
    absGroupDto.setPeriodChain(newPeriodChain);

    List<GroupAbsenceTypeDto> groupAbsenceTypeDto = Lists.newArrayList();
    for (GroupAbsenceType gr : psrDto.categorySwitcher.groups()) {
      groupAbsenceTypeDto.add(absenceGroupsMapper.convertGroupAbsenceType(gr));
    }
    absGroupDto.setGroups(groupAbsenceTypeDto);

    return ResponseEntity.ok().body(absGroupDto);
  }

  /**
   * Recupero le informazioni per costruire la form dell'inserimento assenza sistemare descrizione
   * sotto.
   */
  @Operation(
      summary = "Visualizzazione della lista delle assenza di una persona in un mese.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui cercare le assenze e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alle assenze")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituito l'elenco delle assenze in un mese"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " l'elenco delle assenze",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping("/absencesForm")
  public ResponseEntity<AbsenceFormDto> getAbsencesForm(
      @RequestParam("id") Optional<Long> id,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("from") String from) {
    log.debug("AbsencesGroupsController::getAbsencesForm id = {} from = {}", id, from);
    LocalDate fromLocalDate = LocalDate.parse(from);

    Person person =
        personFinder.getPerson(id, fiscalCode)
            .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    log.debug("AbsenceController::absencesInPeriod person = {}", person);

    rules.checkifPermitted(person);

    AbsenceForm absenceForm = absenceService.buildAbsenceForm(person, fromLocalDate,
        null, null, null, null, true, null,
        null, null, null, true, false);

    log.debug("absenceForm::tabsVisibile = {}", absenceForm.tabsVisibile);

    AbsenceFormDto absFormDto = absenceFormMapper.convert(absenceForm);
    log.debug("absenceForm::groupsByCategory = {}", absenceForm.groups());
//    Map<CategoryGroupAbsenceTypeDto, List<GroupAbsenceTypeDto>> groupsByCategory =
//        Maps.newHashMap();
//
//    List<GroupAbsenceTypeDto> groupAbsenceTypeDto = Lists.newArrayList();
//    List<GroupAbsenceTypeDto> groupAbsenceTypeDtoTmp;
//
//    CategoryGroupAbsenceTypeDto keyGroupMap;
//    for (GroupAbsenceType gr : absenceForm.groups()) {
//      groupAbsenceTypeDto.add(absenceGroupsMapper.convertGroupAbsenceType(gr));
//      keyGroupMap = absenceFormMapper.convert(gr.category);
//      groupAbsenceTypeDtoTmp = groupsByCategory.get(keyGroupMap);
//      if (groupAbsenceTypeDtoTmp == null) {
//        groupAbsenceTypeDtoTmp = Lists.newArrayList();
//        groupAbsenceTypeDtoTmp.add(absenceGroupsMapper.convertGroupAbsenceType(gr));
//        groupsByCategory.put(keyGroupMap, groupAbsenceTypeDtoTmp);
//      } else {
//        groupAbsenceTypeDtoTmp.add(absenceGroupsMapper.convertGroupAbsenceType(gr));
//        groupsByCategory.replace(keyGroupMap, groupAbsenceTypeDtoTmp);
//      }
//    }
//
//    log.debug("absenceForm::groupsByCategory = {}", groupsByCategory.values());
//    absFormDto.setgroupsByCategory(groupsByCategory);
    return ResponseEntity.ok().body(absFormDto);
  }
}