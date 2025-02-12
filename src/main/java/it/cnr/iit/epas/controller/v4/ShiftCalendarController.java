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

import com.google.common.collect.ImmutableList;
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
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.CompetenceCodeDao;
import it.cnr.iit.epas.dao.GeneralSettingDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.ShiftDao;
import it.cnr.iit.epas.dao.ShiftTypeMonthDao;
import it.cnr.iit.epas.dto.v4.PersonReperibilityDayDto;
import it.cnr.iit.epas.dto.v4.RecapReperibilityDto;
import it.cnr.iit.epas.dto.v4.ReperibilityCalendarCreateDto;
import it.cnr.iit.epas.dto.v4.ReperibilityEventDto;
import it.cnr.iit.epas.dto.v4.ReperibilityTypeDropDownDto;
import it.cnr.iit.epas.dto.v4.ShiftTypeDropDownDto;
import it.cnr.iit.epas.dto.v4.mapper.ShiftMapper;
import it.cnr.iit.epas.manager.ConsistencyManager;
import it.cnr.iit.epas.manager.ShiftManager2;
import it.cnr.iit.epas.manager.recaps.reperibilitycalendar.ReperibilityCalendarRecap;
import it.cnr.iit.epas.models.OrganizationShiftSlot;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonReperibility;
import it.cnr.iit.epas.models.PersonReperibilityDay;
import it.cnr.iit.epas.models.PersonReperibilityType;
import it.cnr.iit.epas.models.PersonShiftShiftType;
import it.cnr.iit.epas.models.ReperibilityTypeMonth;
import it.cnr.iit.epas.models.ShiftType;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import it.cnr.iit.epas.models.dto.ReperibilityEvent;
import it.cnr.iit.epas.models.dto.ShiftEvent;
import it.cnr.iit.epas.models.enumerate.EventColor;
import it.cnr.iit.epas.models.enumerate.ShiftSlot;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)})
@Tag(
    name = "Shift Calendar controller",
    description = "Generazione e gestione dei calendari dei turni.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/shiftcalendar")
class ShiftCalendarController {

  private final SecurityRules rules;
  private final PersonFinder personFinder;
  private ShiftMapper shiftMapper;

  private final ShiftManager2 shiftManager2;
  private final ShiftDao shiftDao;
  private final AbsenceDao absenceDao;
  private final PersonDao personDao;

  private final ShiftTypeMonthDao shiftTypeMonthDao;
  private final CompetenceCodeDao competenceCodeDao;
  private final GeneralSettingDao generalSettingDao;
  private final ConsistencyManager consistencyManager;
  private static String holidayCode = "T3";
  private static String nightCode = "T2";

  @Operation(
      summary = "Visualizzazione delle informazioni del calendario dei turni.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "le informazioni necessarie per creare il calendariodi reperibilità.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti le informazioni del calendario dei turni"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati dei turni",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Reperibilità non trovate con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping("/show")
  ResponseEntity<ShiftTypeDropDownDto> show(
      @RequestParam("personId") Optional<Long> personId,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("activityId") Optional<Integer> activityId,
      @RequestParam("date") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> date) {
    log.debug("REST method {} invoked with parameters date={}, activityId={}, personId ={}",
        "/rest/v4/shiftcalendar/show", date, activityId, personId);

    Person person = personFinder.getPerson(personId, fiscalCode)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(person);

    log.debug("Person {}", person);

    final LocalDate currentDate = date.orElse(LocalDate.now());

    final List<ShiftType> activities = shiftManager2.getUserActivities();
    log.debug("userActivities.size() = {}", activities.size());

    final ShiftType activitySelected = activityId.isPresent() ? activities.get(activityId.get())
        : activities.size() > 0 ? activities.get(0) : null;

    rules.checkifPermitted(activitySelected);

    ShiftTypeDropDownDto dto = new ShiftTypeDropDownDto();

    dto.setActivitySelected(shiftMapper.convert(activitySelected));
    dto.setActivities(activities.stream()
        .map(ab -> shiftMapper.convert(ab))
        .collect(Collectors.toList()));
    dto.setCurrentDate(currentDate);
    boolean editable = false;//isEditable(activitySelected.getId(), currentDate);
    dto.setEditable(editable);
    return ResponseEntity.ok().body(dto);
  }

//  @Operation(
//      summary = "Visualizzazione delle informazioni del calendario di reperibilità.",
//      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
//          + "le informazioni necessarie per creare il calendariodi reperibilità.")
//  @ApiResponses(value = {
//      @ApiResponse(responseCode = "200",
//          description = "Restituiti le informazioni del calendario di reperibilità"),
//      @ApiResponse(responseCode = "401",
//          description = "Autenticazione non presente", content = @Content),
//      @ApiResponse(responseCode = "403",
//          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
//              + " i dati delle reperibilità",
//          content = @Content),
//      @ApiResponse(responseCode = "404",
//          description = "Reperibilità non trovate con l'id e/o il codice fiscale fornito",
//          content = @Content)
//  })
//  @GetMapping("/reperibilityPeople")
//  ResponseEntity<List<ReperibilityEventDto>> shiftPeople(
//      @RequestParam("personId") Optional<Long> personId,
//      @RequestParam("fiscalCode") Optional<String> fiscalCode,
//      @NotNull @RequestParam("activityId") Long activityId,
//      @NotNull @RequestParam("start") @DateTimeFormat(iso = ISO.DATE) LocalDate start,
//      @NotNull @RequestParam("end") @DateTimeFormat(iso = ISO.DATE) LocalDate end) {
//    log.debug(
//        "REST method {} invoked with parameters start={}, end={}, activityId={}, personId ={}",
//        "/rest/v4/reperibilitycalendar/shiftPeople", start, end, activityId, personId);
//
//    Optional<ShiftType> activity = shiftDao.getShiftTypeById(activityId);
//    if (activity.isPresent()) {
//      rules.checkifPermitted(activity.get());
//      final List<PersonShiftShiftType> people = shiftManager2
//          .shiftWorkers(activity.get(), start, end);
//
//      int index = 0;
//
//      final List<ShiftEvent> shiftWorkers = new ArrayList<>();
//      final List<ShiftEvent> jolly = new ArrayList<>();
//
//      for (PersonShiftShiftType personShift : people) {
//        // lenght-1 viene fatto per escludere l'ultimo valore che è dedicato alle assenze
//        final EventColor eventColor = EventColor.values()[index % (EventColor.values().length - 1)];
//        final Person person = personShift.getPersonShift().getPerson();
//
//        final ShiftEvent event = ShiftEvent.builder()
//            .allDay(true)
//            .title(person.fullName())
//            .personId(person.id)
//            .eventColor(eventColor)
//            .color(eventColor.backgroundColor)
//            .textColor(eventColor.textColor)
//            .borderColor(eventColor.borderColor)
//            .className("removable")
//            .mobile(person.getMobile())
//            .email(person.getEmail())
//            .build();
//
//        if (personShift.isJolly()) {
//          jolly.add(event);
//        } else {
//          shiftWorkers.add(event);
//        }
//        index++;
//      }
//      List<ShiftSlot> slotList = null;
//      List<OrganizationShiftSlot> organizationSlotList = null;
//      if (activity.get().getShiftTimeTable() != null) {
//        slotList = shiftManager2.getSlotsFromTimeTable(activity.get().getShiftTimeTable());
//      } else {
//        organizationSlotList =
//            Lists.newArrayList(
//                activity.get().getOrganizaionShiftTimeTable().getOrganizationShiftSlot());
//      }
//
//      shiftWorkers.sort(Comparator.comparing(ShiftEvent::getTitle));
//      jolly.sort(Comparator.comparing(ShiftEvent::getTitle));
//
//      //render(shiftWorkers, jolly, slotList, organizationSlotList);
//
//
//    return ResponseEntity.ok().body(shiftWorkers.stream()
//        .map(ev -> reperibilityEventMapper.convert(ev))
//        .collect(Collectors.toList()));
//  }
//
//  @Operation(
//      summary = "Visualizzazione delle informazioni del calendario di reperibilità.",
//      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
//          + "le informazioni necessarie per creare il calendariodi reperibilità.")
//  @ApiResponses(value = {
//      @ApiResponse(responseCode = "200",
//          description = "Restituiti le informazioni del calendario di reperibilità"),
//      @ApiResponse(responseCode = "401",
//          description = "Autenticazione non presente", content = @Content),
//      @ApiResponse(responseCode = "403",
//          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
//              + " i dati delle reperibilità",
//          content = @Content),
//      @ApiResponse(responseCode = "404",
//          description = "Reperibilità non trovate con l'id e/o il codice fiscale fornito",
//          content = @Content)
//  })
//  @GetMapping("/events")
//  ResponseEntity<List<ReperibilityEventDto>> events(
//      @RequestParam("personId") Optional<Long> personId,
//      @RequestParam("fiscalCode") Optional<String> fiscalCode,
//      @NotNull @RequestParam("reperibility") Long reperibilityId,
//      @NotNull @RequestParam("start") @DateTimeFormat(iso = ISO.DATE) LocalDate start,
//      @NotNull @RequestParam("end") @DateTimeFormat(iso = ISO.DATE) LocalDate end) {
//    log.debug(
//        "REST method {} invoked with parameters start={}, end={}, reperibility={}, personId ={}",
//        "/rest/v4/reperibilitycalendar/events", start, end, reperibilityId, personId);
//
//    List<ReperibilityEvent> events = new ArrayList<>();
//    PersonReperibilityType reperibility =
//        reperibilityDao.getPersonReperibilityTypeById(reperibilityId);
//
//    log.debug("PersonReperibilityType reperibility {}   rules.check(reperibility)>> {}",
//        reperibility, rules.check(reperibility));
//
//    if (reperibility != null && rules.check(reperibility)) {
//      List<PersonReperibility> people =
//          reperibilityManager2.reperibilityWorkers(reperibility, start, end);
//
//      log.debug("PersonReperibilityType people {} ", people);
//
//      int index = 0;
//
//      // prende i turni associati alle persone attive in quel turno
//      for (PersonReperibility personReperibility : people) {
//        final Person person = personReperibility.getPerson();
//        final EventColor eventColor = EventColor.values()[index % (EventColor.values().length - 1)];
//        events.addAll(reperibilityEvents(reperibility, person, start, end, eventColor));
//        events.addAll(absenceEvents(person, start, end));
//        index++;
//      }
//    }
//
//    log.debug("events {}", events);
//    return ResponseEntity.ok().body(events.stream()
//        .map(ev -> reperibilityEventMapper.convert(ev))
//        .collect(Collectors.toList()));
//  }
//
//
//  @Operation(
//      summary =
//          "Inserisce un nuovo slot di turno per l'attività al turnista passati come parametro.",
//      description =
//          "Questo endpoint è utilizzabile dalle persone autenticate per inserire un nuovo "
//              + "slot di turno per l'attività al turnista passati come parametro.")
//  @ApiResponses(value = {
//      @ApiResponse(responseCode = "200",
//          description = "Aggiunte le informazioni del calendario di reperibilità"),
//      @ApiResponse(responseCode = "401",
//          description = "Autenticazione non presente", content = @Content),
//      @ApiResponse(responseCode = "403",
//          description = "Utente che ha effettuato la richiesta non autorizzato a inserire"
//              + " i dati delle reperibilità",
//          content = @Content),
//      @ApiResponse(responseCode = "404",
//          description = "Tipo di Reperibilità non trovata",
//          content = @Content),
//      @ApiResponse(responseCode = "409",
//          description = "Reperibilità già esistente in quel giorno",
//          content = @Content)
//  })
//  @PutMapping(ApiRoutes.CREATE)
//  ResponseEntity<PersonReperibilityDayDto> newReperibility(
//      @NotNull @Valid @RequestBody ReperibilityCalendarCreateDto reperibilityDto) {
//    log.debug(
//        "REST method PUT /rest/v4/reperibilitycalendar invoked with dto={}",
//        reperibilityDto);
//
//    val reperibilityId = reperibilityDto.getReperibilityId();
//    val personId = reperibilityDto.getPersonId();
//    val date = reperibilityDto.getDate();
//
//    PersonReperibilityType reperibilityType = reperibilityDao.getPersonReperibilityTypeById(
//        reperibilityId);
//
//    final ReperibilityTypeMonth reperibilityTypeMonth =
//        reperibilityTypeMonthDao.byReperibilityTypeAndDate(reperibilityType, date).orElse(null);
//
//    if (reperibilityType != null) {
//      if (rules.check(reperibilityType) && rules.check(reperibilityTypeMonth)) {
//        log.debug(
//            "HO PASSATO LA PARTE rules.check(reperibilityType) && rules.check(reperibilityTypeMonth) ");
//        Person person = personDao.getPersonById(personId);
//        if (person == null) {
//          return ResponseEntity.notFound().build();
//        } else {
//          PersonReperibilityDay personReperibilityDay = new PersonReperibilityDay();
//          personReperibilityDay.setDate(date);
//          personReperibilityDay.setReperibilityType(reperibilityType);
//          personReperibilityDay.setPersonReperibility(reperibilityDao
//              .getPersonReperibilityByPersonAndType(person, reperibilityType));
//
//          Errors errors = new BeanPropertyBindingResult(personReperibilityDay,
//              "personReperibilityDay");
//          validator.validate(personReperibilityDay, errors);
//
//          Optional<String> error;
//
//          if (errors.hasErrors()) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).build();
//          } else {
//            // Validation successful, save the entity
//            error = reperibilityManager2.reperibilityPermitted(personReperibilityDay);
//          }
//          log.debug(
//              "HO PASSATO LA PARTE errors.hasErrors() ");
//
//          if (error.isPresent()) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).build();
//          } else {
//            reperibilityManager2.save(personReperibilityDay);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                .body(personReperibilityDayMapper.convert(personReperibilityDay));
//          }
//        }
//      } else {  // Le Drools non danno il grant
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//      }
//
//    } else { // Il ReperibilityType specificato non esiste
//      return ResponseEntity.notFound().build();
//    }
//  }
//
//  @Operation(
//      summary = "Cambio di reperibilità.",
//      description =
//          "Questo endpoint è utilizzabile dalle persone autenticate per cambiare i turni nel"
//              + " calendariodi reperibilità.Controlla se il turno passato come parametro può "
//              + "essere salvato in un dato giorno ed eventualmente lo salva, altrimenti "
//              + "restituisce un errore")
//  @ApiResponses(value = {
//      @ApiResponse(responseCode = "200",
//          description = "Salvato nel calendario di reperibilità il cambio turno"),
//      @ApiResponse(responseCode = "401",
//          description = "Autenticazione non presente", content = @Content),
//      @ApiResponse(responseCode = "403",
//          description = "Utente che ha effettuato la richiesta non autorizzato a modificare"
//              + " i dati delle reperibilità",
//          content = @Content),
//      @ApiResponse(responseCode = "404",
//          description = "Reperibilità non trovate con l'id e/o il codice fiscale fornito",
//          content = @Content),
//      @ApiResponse(responseCode = "409",
//          description = "Reperibilità già esistente in quel giorno",
//          content = @Content)
//  })
//  @PatchMapping(ApiRoutes.PATCH)
//  ResponseEntity<List<ReperibilityEventDto>> changeReperibility(
//      @NotNull @PathVariable("id") Long personReperibilityDayId,
//      @NotNull @RequestParam("newDate") @DateTimeFormat(iso = ISO.DATE) LocalDate newDate) {
//    log.debug(
//        "REST method PATCH /rest/v4/reperibilitycalendar/patch/{}?newDate={} "
//            + "invoked with parameters",
//        personReperibilityDayId, newDate);
//
//    final Optional<PersonReperibilityDay> prd =
//        reperibilityDao.getPersonReperibilityDayById(personReperibilityDayId);
//    if (prd.isPresent()) {
//      final ReperibilityTypeMonth reperibilityTypeMonth =
//          reperibilityTypeMonthDao.byReperibilityTypeAndDate(
//              prd.get().getReperibilityType(), newDate).orElse(null);
//
//      if (rules.check(prd.get().getReperibilityType()) && rules.check(reperibilityTypeMonth)) {
//        prd.get().setDate(newDate);
//
//        log.debug("Richiesta cambio esistente reperibilità prd {}",
//            prd.get().getPersonReperibility());
//
//        Optional<String> error = reperibilityManager2.reperibilityPermitted(prd.get());
//        log.debug("Richiesta cambio esistente reperibilità error {}", error);
//        if (error.isPresent()) {
//          return ResponseEntity.status(HttpStatus.CONFLICT).build();
//        } else {
//          //salva il turno modificato
//          log.debug("Richiesta cambio esistente reperibilità prd {}", prd.get().getDate());
//          reperibilityManager2.save(prd.get());
//          return ResponseEntity.ok().build();
//        }
//      } else {
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//      }
//
//    } else {
//      return ResponseEntity.notFound().build();
//    }
//  }
//
//
//  @Operation(
//      summary = "Eliminazione reperibilità.",
//      description = "Questo endpoint è utilizzabile dalle persone autenticate per effettuare "
//          + "l'eliminazione di un turno di reperibilità ")
//  @ApiResponses(value = {
//      @ApiResponse(responseCode = "200",
//          description = "Reperibilità eliminata con successo"),
//      @ApiResponse(responseCode = "401",
//          description = "Autenticazione non presente", content = @Content),
//      @ApiResponse(responseCode = "403",
//          description = "Utente che ha effettuato la richiesta non autorizzato a eliminare"
//              + " i dati delle reperibilità",
//          content = @Content),
//      @ApiResponse(responseCode = "404",
//          description = "Reperibilità non trovate con l'id",
//          content = @Content)
//  })
//  @DeleteMapping(ApiRoutes.DELETE)
//  ResponseEntity<List<ReperibilityEventDto>> deleteReperibility(
//      @NotNull @PathVariable("id") Long reperibilityId) {
//    log.debug("REST method DELETE /rest/v4/reperibilitycalendar/{} invoked with parameters ",
//        reperibilityId);
//
//    final Optional<PersonReperibilityDay> prd =
//        reperibilityDao.getPersonReperibilityDayById(reperibilityId);
//    if (!prd.isPresent()) {
//      return ResponseEntity.notFound().build();
//    }
//
//    final ReperibilityTypeMonth reperibilityTypeMonth =
//        reperibilityTypeMonthDao.byReperibilityTypeAndDate(prd.get().getReperibilityType(),
//            prd.get().getDate()).orElse(null);
//
//    if (rules.check(prd.get().getReperibilityType()) && rules.check(reperibilityTypeMonth)) {
//
//      reperibilityManager2.delete(prd.get());
//
//      return ResponseEntity.ok().body(null);
//    } else {
//      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//    }
//  }
//
//  @Operation(
//      summary = "Calcola le ore di turno effettuate in quel periodo per ciascuna persona "
//          + "dell'attività.",
//      description = "Questo endpoint è utilizzabile dalle persone autenticate per calcolare "
//          + "le ore di turno effettuate in quel periodo per ciascuna persona dell'attività")
//  @ApiResponses(value = {
//      @ApiResponse(responseCode = "200",
//          description = "Ore di turno calcolate con successo."),
//      @ApiResponse(responseCode = "401",
//          description = "Autenticazione non presente", content = @Content),
//      @ApiResponse(responseCode = "403",
//          description = "Utente che ha effettuato la richiesta non autorizzato a calcolare"
//              + " i dati delle reperibilità",
//          content = @Content),
//      @ApiResponse(responseCode = "404",
//          description = "Reperibilità non trovate con l'id",
//          content = @Content)
//  })
//  @GetMapping("/recap")
//  ResponseEntity<RecapReperibilityDto> recap(
//      @NotNull @RequestParam("reperibility") Long reperibilityId,
//      @NotNull @RequestParam("start") @DateTimeFormat(iso = ISO.DATE) LocalDate start,
//      @NotNull @RequestParam("end") @DateTimeFormat(iso = ISO.DATE) LocalDate end) {
//    log.debug(
//        "REST method {} invoked with parameters start={}, end={}, reperibility={}",
//        "/rest/v4/reperibilitycalendar/recap", start, end, reperibilityId);
//
//    PersonReperibilityType reperibility =
//        reperibilityDao.getPersonReperibilityTypeById(reperibilityId);
//
//    if (reperibility == null) {
//      return ResponseEntity.notFound().build();
//    }
//    log.debug("getPersonReperibilityTypeById reperibilityId {} reperibility>> {}",
//        reperibilityId, reperibility);
//
//    rules.checkifPermitted(reperibility);
//    ReperibilityCalendarRecap reperibilityCalendarRecap =
//        reperibilityCalendarRecapFactory.create(reperibility, start, end);
//    return ResponseEntity.ok().body(reperibilityRecapMapper.convert(reperibilityCalendarRecap));
//  }
//
//  /**
//   * Verifica se il calendario è modificabile o meno nella data richiesta.
//   *
//   * @param reperibilityId id dell'attività da verificare
//   * @param start          data relativa al mese da controllare
//   * @return true se l'attività è modificabile nella data richiesta, false altrimenti.
//   */
//  private boolean isEditable(long reperibilityId, LocalDate start) {
//
//    PersonReperibilityType reperibilityType =
//        reperibilityDao.getPersonReperibilityTypeById(reperibilityId);
//    if (reperibilityType == null) {
//      return false;
//    }
//    final ReperibilityTypeMonth reperibilityTypeMonth =
//        reperibilityTypeMonthDao.byReperibilityTypeAndDate(reperibilityType, start).orElse(null);
//
//    return rules.check(reperibilityType) && rules.check(reperibilityTypeMonth);
//
//  }
//
//  /**
//   * DTO che modellano le assenze della persona nel periodo.
//   *
//   * @param person Persona della quale recuperare le assenze
//   * @param start  data iniziale del periodo
//   * @param end    data finale del periodo
//   * @return Una lista di DTO che modellano le assenze di quella persona nell'intervallo specificato
//   * da renderizzare nel fullcalendar.
//   */
//  private List<ReperibilityEvent> absenceEvents(Person person,
//      LocalDate start, LocalDate end) {
//
//    final List<JustifiedTypeName> types = ImmutableList
//        .of(JustifiedTypeName.all_day, JustifiedTypeName.assign_all_day,
//            JustifiedTypeName.complete_day_and_add_overtime);
//
//    List<Absence> absences = absenceDao.filteredByTypes(person, start, end, types,
//        Optional.ofNullable(false), Optional.empty());
//    List<ReperibilityEvent> events = new ArrayList<>();
//    ReperibilityEvent event = null;
//
//    for (Absence abs : absences) {
//
//      /*
//       * Per quanto riguarda gli eventi 'allDay':
//       *
//       * La convenzione del fullcalendar è quella di avere il parametro end = null
//       * nel caso di eventi su un singolo giorno, mentre nel caso di evento su più giorni il
//       * parametro end assume il valore del giorno successivo alla fine effettiva
//       * (perchè ne imposta l'orario alla mezzanotte).
//       */
//      if (event == null
//          || event.getEnd() == null && !event.getStart().plusDays(1)
//          .equals(abs.getPersonDay().getDate())
//          || event.getEnd() != null && !event.getEnd().equals(abs.getPersonDay().getDate())) {
//
//        event = ReperibilityEvent.builder()
//            .allDay(true)
//            .title("Assenza di " + abs.getPersonDay().getPerson().fullName())
//            .start(abs.getPersonDay().getDate())
//            .editable(false)
//            .color(EventColor.RED.backgroundColor)
//            .textColor(EventColor.RED.textColor)
//            .borderColor(EventColor.RED.borderColor)
//            .build();
//
//        events.add(event);
//      } else {
//        event.setEnd(abs.getPersonDay().getDate().plusDays(1));
//      }
//
//    }
//    return events;
//  }
//
//  /**
//   * Carica la lista delle reperibilità di un certo tipo associati ad una determinata persona in un
//   * intervallo di tempo.
//   *
//   * @param reperibility attività di reperibilità
//   * @param person       persona associata ai turni
//   * @param start        data inizio intervallo di tempo
//   * @param end          data fine intervallo di tempo
//   * @param color        colore da utilizzare per il rendering degli eventi restituiti
//   * @return Una lista di DTO da serializzare in Json per renderizzarli nel fullcalendar.
//   */
//  private List<ReperibilityEvent> reperibilityEvents(PersonReperibilityType reperibility,
//      Person person, LocalDate start, LocalDate end, EventColor color) {
//
//    return reperibilityDao.getPersonReperibilityDaysByPeriodAndType(start,
//            end, reperibility, person).stream()
//        .map(personReperibilityDay -> {
//          final ReperibilityEvent event = ReperibilityEvent.builder()
//
//              .personReperibilityDayId(personReperibilityDay.getId())
//              .title(person.fullName())
//              .start(personReperibilityDay.getDate())
//              .end(personReperibilityDay.getDate())
//              .durationEditable(false)
//              .color(color.backgroundColor)
//              .textColor(color.textColor)
//              .borderColor(color.borderColor)
//              .className("removable")
//              .personId(person.getId())
//              .build();
//
//          return event;
//        }).collect(Collectors.toList());
//  }
}