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
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonReperibilityDayDao;
import it.cnr.iit.epas.dao.ReperibilityTypeMonthDao;
import it.cnr.iit.epas.dto.v4.PersonReperibilityDayDto;
import it.cnr.iit.epas.dto.v4.RecapReperibilityDto;
import it.cnr.iit.epas.dto.v4.ReperibilityCalendarCreateDto;
import it.cnr.iit.epas.dto.v4.ReperibilityEventDto;
import it.cnr.iit.epas.dto.v4.ReperibilityTypeDropDownDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonReperibilityDayMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonReperibilityTypeMapper;
import it.cnr.iit.epas.dto.v4.mapper.ReperibilityEventMapper;
import it.cnr.iit.epas.dto.v4.mapper.ReperibilityRecapMapper;
import it.cnr.iit.epas.manager.ReperibilityManager2;
import it.cnr.iit.epas.manager.recaps.reperibilitycalendar.ReperibilityCalendarRecap;
import it.cnr.iit.epas.manager.recaps.reperibilitycalendar.ReperibilityCalendarRecapFactory;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonReperibility;
import it.cnr.iit.epas.models.PersonReperibilityDay;
import it.cnr.iit.epas.models.PersonReperibilityType;
import it.cnr.iit.epas.models.ReperibilityTypeMonth;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import it.cnr.iit.epas.models.dto.ReperibilityEvent;
import it.cnr.iit.epas.models.enumerate.EventColor;
import it.cnr.iit.epas.repo.PersonReperibilityTypeRepository;
import it.cnr.iit.epas.security.SecurityRules;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
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
    name = "Reperibility Calendar controller",
    description = "Generazione e gestione dei calendari di reperibilità.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/reperibilitycalendar")
class ReperibilityCalendarController {

  private final SecurityRules rules;
  private final PersonFinder personFinder;

  private final ReperibilityManager2 reperibilityManager2;
  private final PersonReperibilityDayDao reperibilityDao;
  private final AbsenceDao absenceDao;
  private final PersonDao personDao;

  private final ReperibilityTypeMonthDao reperibilityTypeMonthDao;
  private final PersonReperibilityTypeMapper personReperibilityTypeMapper;
  private final PersonReperibilityTypeRepository personReperibilityTypeRepository;
  private final ReperibilityEventMapper reperibilityEventMapper;
  private final ReperibilityCalendarRecapFactory reperibilityCalendarRecapFactory;
  private final ReperibilityRecapMapper reperibilityRecapMapper;
  private final PersonReperibilityDayMapper personReperibilityDayMapper;
  private final Validator validator;

  @Operation(
      summary = "Visualizzazione delle informazioni del calendario di reperibilità.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "le informazioni necessarie per creare il calendariodi reperibilità.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti le informazioni del calendario di reperibilità"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati delle reperibilità",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Reperibilità non trovate con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping("/show")
  ResponseEntity<ReperibilityTypeDropDownDto> show(
      @RequestParam("personId") Optional<Long> personId,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @RequestParam("reperibility") Optional<Long> reperibilityId,
      @RequestParam("date") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> date) {
    log.debug("REST method {} invoked with parameters date={}, reperibility={}, personId ={}",
        "/rest/v4/reperibilitycalendar/show", date, reperibilityId, personId);

    Person person = personFinder.getPerson(personId, fiscalCode)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(person);

    log.debug("Person {}", person);

    final LocalDate currentDate = date.orElse(LocalDate.now());

    final List<PersonReperibilityType> reperibilities = reperibilityManager2.getUserActivities();

    reperibilities.forEach(personReperibilityType -> {
      log.debug("Richiesta visualizzazione reperibilità getMonthlyCompetenceType {}", 
          personReperibilityType.getMonthlyCompetenceType().toString());
    });
    log.debug("PersonReperibilityType reperibilities {}", reperibilities);

    if (reperibilities.isEmpty()) {
      log.debug("Richiesta visualizzazione reperibilità ma nessun servizio di "
          + "reperibilità presente");
      return ResponseEntity.badRequest().build();
    }

    final PersonReperibilityType reperibilitySelected =
        reperibilityId.isPresent()
            ? personReperibilityTypeRepository.findById(
            reperibilityId.get()).get() : reperibilities.get(0);

    log.debug("PersonReperibilityType reperibilitySelected {}", 
        reperibilitySelected.getMonthlyCompetenceType().name);
    log.debug("PersonReperibilityType currentDate {}", currentDate);

    ReperibilityTypeDropDownDto dto = new ReperibilityTypeDropDownDto();

    dto.setReperibilitySelected(personReperibilityTypeMapper.convert(reperibilitySelected));
    dto.setReperibilities(reperibilities.stream()
        .map(ab -> personReperibilityTypeMapper.convert(ab))
        .collect(Collectors.toList()));
    dto.setCurrentDate(currentDate);
    boolean editable = isEditable(reperibilitySelected.getId(), currentDate);
    dto.setEditable(editable);
    return ResponseEntity.ok().body(dto);
  }

  @Operation(
      summary = "Visualizzazione delle informazioni del calendario di reperibilità.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "le informazioni necessarie per creare il calendariodi reperibilità.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti le informazioni del calendario di reperibilità"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati delle reperibilità",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Reperibilità non trovate con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping("/events")
  ResponseEntity<List<ReperibilityEventDto>> events(
      @RequestParam("personId") Optional<Long> personId,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @NotNull @RequestParam("reperibility") Long reperibilityId,
      @NotNull @RequestParam("start") @DateTimeFormat(iso = ISO.DATE) LocalDate start,
      @NotNull @RequestParam("end") @DateTimeFormat(iso = ISO.DATE) LocalDate end) {
    log.debug(
        "REST method {} invoked with parameters start={}, end={}, reperibility={}, personId ={}",
        "/rest/v4/reperibilitycalendar/events", start, end, reperibilityId, personId);

    List<ReperibilityEvent> events = new ArrayList<>();
    PersonReperibilityType reperibility =
        reperibilityDao.getPersonReperibilityTypeById(reperibilityId);

    log.debug("PersonReperibilityType reperibility {}   rules.check(reperibility)>> {}",
        reperibility, rules.check(reperibility));

    if (reperibility != null && rules.check(reperibility)) {
      List<PersonReperibility> people =
          reperibilityManager2.reperibilityWorkers(reperibility, start, end);

      log.debug("PersonReperibilityType people {} ", people);

      int index = 0;

      // prende i turni associati alle persone attive in quel turno
      for (PersonReperibility personReperibility : people) {
        final Person person = personReperibility.getPerson();
        final EventColor eventColor = EventColor.values()[index % (EventColor.values().length - 1)];
        events.addAll(reperibilityEvents(reperibility, person, start, end, eventColor));
        events.addAll(absenceEvents(person, start, end));
        index++;
      }
    }

    log.debug("events {}", events);
    return ResponseEntity.ok().body(events.stream()
        .map(ev -> reperibilityEventMapper.convert(ev))
        .collect(Collectors.toList()));
  }

  @Operation(
      summary = "Visualizzazione delle informazioni del calendario di reperibilità.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "le informazioni necessarie per creare il calendariodi reperibilità.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti le informazioni del calendario di reperibilità"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati delle reperibilità",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Reperibilità non trovate con l'id e/o il codice fiscale fornito",
          content = @Content)
  })
  @GetMapping("/reperibilityPeople")
  ResponseEntity<List<ReperibilityEventDto>> reperibilityPeople(
      @RequestParam("personId") Optional<Long> personId,
      @RequestParam("fiscalCode") Optional<String> fiscalCode,
      @NotNull @RequestParam("reperibility") Long reperibilityId,
      @NotNull @RequestParam("start") @DateTimeFormat(iso = ISO.DATE) LocalDate start,
      @NotNull @RequestParam("end") @DateTimeFormat(iso = ISO.DATE) LocalDate end) {
    log.debug(
        "REST method {} invoked with parameters start={}, end={}, reperibility={}, personId ={}",
        "/rest/v4/reperibilitycalendar/reperibilityPeople", start, end, reperibilityId, personId);

    PersonReperibilityType reperibility =
        reperibilityDao.getPersonReperibilityTypeById(reperibilityId);
    if (reperibility == null) {
      return ResponseEntity.notFound().build();
    }
    log.debug("PersonReperibilityType reperibility {}   rules.check(reperibility)>> {}",
        reperibility, rules.check(reperibility));

    rules.checkifPermitted(reperibility);

    final List<ReperibilityEvent> reperibilityWorkers = new ArrayList<>();
    final List<PersonReperibility> people =
        reperibilityManager2.reperibilityWorkers(reperibility, start, end);
    int index = 0;

    for (PersonReperibility personReperibility : people) {
      final EventColor eventColor = EventColor.values()[index % (EventColor.values().length - 1)];
      final Person person = personReperibility.getPerson();
      final ReperibilityEvent event = ReperibilityEvent.builder()
          .allDay(true)
          .title(person.fullName())
          .personId(person.getId())
          .eventColor(eventColor)
          .color(eventColor.backgroundColor)
          .textColor(eventColor.textColor)
          .borderColor(eventColor.borderColor)
          .className("list-group-item fc-event removable")
          .style("color: " + eventColor.textColor + "; background-color:"
              + eventColor.backgroundColor + "; border-color: " + eventColor.borderColor)
          .mobile(person.getMobile())
          .email(person.getEmail())
          .build();
      reperibilityWorkers.add(event);
      index++;
    }
    reperibilityWorkers.sort(Comparator.comparing(ReperibilityEvent::getTitle));


    log.debug("reperibilityWorkers {}", reperibilityWorkers);
    return ResponseEntity.ok().body(reperibilityWorkers.stream()
        .map(ev -> reperibilityEventMapper.convert(ev))
        .collect(Collectors.toList()));
  }

  @Operation(
      summary = 
        "Inserisce un nuovo slot di turno per l'attività al turnista passati come parametro.",
      description =
          "Questo endpoint è utilizzabile dalle persone autenticate per inserire un nuovo "
              + "slot di turno per l'attività al turnista passati come parametro.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Aggiunte le informazioni del calendario di reperibilità"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a inserire"
              + " i dati delle reperibilità",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Tipo di Reperibilità non trovata",
          content = @Content),
      @ApiResponse(responseCode = "409",
          description = "Reperibilità già esistente in quel giorno",
          content = @Content)
  })
  @PutMapping(ApiRoutes.CREATE)
  ResponseEntity<PersonReperibilityDayDto> newReperibility(
      @NotNull @Valid @RequestBody ReperibilityCalendarCreateDto reperibilityDto) {
    log.debug(
        "REST method PUT /rest/v4/reperibilitycalendar invoked with dto={}",
        reperibilityDto);

    val reperibilityId = reperibilityDto.getReperibilityId();
    val personId = reperibilityDto.getPersonId();
    val date = reperibilityDto.getDate();

    PersonReperibilityType reperibilityType = reperibilityDao.getPersonReperibilityTypeById(
        reperibilityId);

    final ReperibilityTypeMonth reperibilityTypeMonth =
        reperibilityTypeMonthDao.byReperibilityTypeAndDate(reperibilityType, date).orElse(null);

    if (reperibilityType != null) {
      if (rules.check(reperibilityType) && rules.check(reperibilityTypeMonth)) {
        Person person = personDao.getPersonById(personId);
        if (person == null) {
          return ResponseEntity.notFound().build();
        } else {
          PersonReperibilityDay personReperibilityDay = new PersonReperibilityDay();
          personReperibilityDay.setDate(date);
          personReperibilityDay.setReperibilityType(reperibilityType);
          personReperibilityDay.setPersonReperibility(reperibilityDao
              .getPersonReperibilityByPersonAndType(person, reperibilityType));

          Errors errors = new BeanPropertyBindingResult(personReperibilityDay,
              "personReperibilityDay");
          validator.validate(personReperibilityDay, errors);

          Optional<String> error;

          if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
          } else {
            // Validation successful, save the entity
            error = reperibilityManager2.reperibilityPermitted(personReperibilityDay);
          }
          if (error.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
          } else {
            reperibilityManager2.save(personReperibilityDay);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(personReperibilityDayMapper.convert(personReperibilityDay));
          }
        }
      } else {  // Le Drools non danno il grant
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

    } else { // Il ReperibilityType specificato non esiste
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(
      summary = "Cambio di reperibilità.",
      description =
          "Questo endpoint è utilizzabile dalle persone autenticate per cambiare i turni nel"
              + " calendariodi reperibilità.Controlla se il turno passato come parametro può "
              + "essere salvato in un dato giorno ed eventualmente lo salva, altrimenti "
              + "restituisce un errore")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Salvato nel calendario di reperibilità il cambio turno"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a modificare"
              + " i dati delle reperibilità",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Reperibilità non trovate con l'id e/o il codice fiscale fornito",
          content = @Content),
      @ApiResponse(responseCode = "409",
          description = "Reperibilità già esistente in quel giorno",
          content = @Content)
  })
  @PatchMapping(ApiRoutes.PATCH)
  ResponseEntity<List<ReperibilityEventDto>> changeReperibility(
      @NotNull @PathVariable("id") Long personReperibilityDayId,
      @NotNull @RequestParam("newDate") @DateTimeFormat(iso = ISO.DATE) LocalDate newDate) {
    log.debug(
        "REST method PATCH /rest/v4/reperibilitycalendar/patch/{}?newDate={} "
        + "invoked with parameters",
        personReperibilityDayId, newDate);

    final Optional<PersonReperibilityDay> prd =
        reperibilityDao.getPersonReperibilityDayById(personReperibilityDayId);
    if (prd.isPresent()) {
      final ReperibilityTypeMonth reperibilityTypeMonth =
          reperibilityTypeMonthDao.byReperibilityTypeAndDate(
              prd.get().getReperibilityType(), newDate).orElse(null);

      if (rules.check(prd.get().getReperibilityType()) && rules.check(reperibilityTypeMonth)) {
        prd.get().setDate(newDate);

        log.debug("Richiesta cambio esistente reperibilità prd {}",
            prd.get().getPersonReperibility());

        Optional<String> error = reperibilityManager2.reperibilityPermitted(prd.get());
        log.debug("Richiesta cambio esistente reperibilità error {}", error);
        if (error.isPresent()) {
          return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
          //salva il turno modificato
          log.debug("Richiesta cambio esistente reperibilità prd {}", prd.get().getDate());
          reperibilityManager2.save(prd.get());
          return ResponseEntity.ok().build();
        }
      } else {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

    } else {
      return ResponseEntity.notFound().build();
    }
  }


  @Operation(
      summary = "Eliminazione reperibilità.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per effettuare "
          + "l'eliminazione di un turno di reperibilità ")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Reperibilità eliminata con successo"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a eliminare"
              + " i dati delle reperibilità",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Reperibilità non trovate con l'id",
          content = @Content)
  })
  @DeleteMapping(ApiRoutes.DELETE)
  ResponseEntity<List<ReperibilityEventDto>> deleteReperibility(
      @NotNull @PathVariable("id") Long reperibilityId) {
    log.debug("REST method DELETE /rest/v4/reperibilitycalendar/{} invoked with parameters ",
        reperibilityId);

    final Optional<PersonReperibilityDay> prd =
        reperibilityDao.getPersonReperibilityDayById(reperibilityId);
    if (!prd.isPresent()) {
      return ResponseEntity.notFound().build();
    }

    final ReperibilityTypeMonth reperibilityTypeMonth =
        reperibilityTypeMonthDao.byReperibilityTypeAndDate(prd.get().getReperibilityType(),
            prd.get().getDate()).orElse(null);

    if (rules.check(prd.get().getReperibilityType()) && rules.check(reperibilityTypeMonth)) {

      reperibilityManager2.delete(prd.get());

      return ResponseEntity.ok().body(null);
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @Operation(
      summary = "Calcola le ore di turno effettuate in quel periodo per ciascuna persona "
          + "dell'attività.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per calcolare "
          + "le ore di turno effettuate in quel periodo per ciascuna persona dell'attività")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Ore di turno calcolate con successo."),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a calcolare"
              + " i dati delle reperibilità",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Reperibilità non trovate con l'id",
          content = @Content)
  })
  @GetMapping("/recap")
  ResponseEntity<RecapReperibilityDto> recap(
      @NotNull @RequestParam("reperibility") Long reperibilityId,
      @NotNull @RequestParam("start") @DateTimeFormat(iso = ISO.DATE) LocalDate start,
      @NotNull @RequestParam("end") @DateTimeFormat(iso = ISO.DATE) LocalDate end) {
    log.debug(
        "REST method {} invoked with parameters start={}, end={}, reperibility={}",
        "/rest/v4/reperibilitycalendar/recap", start, end, reperibilityId);

    PersonReperibilityType reperibility =
        reperibilityDao.getPersonReperibilityTypeById(reperibilityId);
    
    if (reperibility == null) {
      return ResponseEntity.notFound().build();
    }
    log.debug("getPersonReperibilityTypeById reperibilityId {} reperibility>> {}", 
        reperibilityId, reperibility);

    rules.checkifPermitted(reperibility);
    ReperibilityCalendarRecap reperibilityCalendarRecap =
        reperibilityCalendarRecapFactory.create(reperibility, start, end);
    return ResponseEntity.ok().body(reperibilityRecapMapper.convert(reperibilityCalendarRecap));
  }

  /**
   * Verifica se il calendario è modificabile o meno nella data richiesta.
   *
   * @param reperibilityId id dell'attività da verificare
   * @param start          data relativa al mese da controllare
   * @return true se l'attività è modificabile nella data richiesta, false altrimenti.
   */
  private boolean isEditable(long reperibilityId, LocalDate start) {

    PersonReperibilityType reperibilityType =
        reperibilityDao.getPersonReperibilityTypeById(reperibilityId);
    if (reperibilityType == null) {
      return false;
    }
    final ReperibilityTypeMonth reperibilityTypeMonth =
        reperibilityTypeMonthDao.byReperibilityTypeAndDate(reperibilityType, start).orElse(null);

    return rules.check(reperibilityType) && rules.check(reperibilityTypeMonth);

  }

  /**
   * DTO che modellano le assenze della persona nel periodo.
   *
   * @param person Persona della quale recuperare le assenze
   * @param start  data iniziale del periodo
   * @param end    data finale del periodo
   * @return Una lista di DTO che modellano le assenze di quella persona nell'intervallo specificato
   *     da renderizzare nel fullcalendar.
   */
  private List<ReperibilityEvent> absenceEvents(Person person,
      LocalDate start, LocalDate end) {

    final List<JustifiedTypeName> types = ImmutableList
        .of(JustifiedTypeName.all_day, JustifiedTypeName.assign_all_day,
            JustifiedTypeName.complete_day_and_add_overtime);

    List<Absence> absences = absenceDao.filteredByTypes(person, start, end, types,
        Optional.ofNullable(false), Optional.empty());
    List<ReperibilityEvent> events = new ArrayList<>();
    ReperibilityEvent event = null;

    for (Absence abs : absences) {

      /*
       * Per quanto riguarda gli eventi 'allDay':
       *
       * La convenzione del fullcalendar è quella di avere il parametro end = null
       * nel caso di eventi su un singolo giorno, mentre nel caso di evento su più giorni il
       * parametro end assume il valore del giorno successivo alla fine effettiva
       * (perchè ne imposta l'orario alla mezzanotte).
       */
      if (event == null
          || event.getEnd() == null && !event.getStart().plusDays(1)
          .equals(abs.getPersonDay().getDate())
          || event.getEnd() != null && !event.getEnd().equals(abs.getPersonDay().getDate())) {

        event = ReperibilityEvent.builder()
            .allDay(true)
            .title("Assenza di " + abs.getPersonDay().getPerson().fullName())
            .start(abs.getPersonDay().getDate())
            .editable(false)
            .color(EventColor.RED.backgroundColor)
            .textColor(EventColor.RED.textColor)
            .borderColor(EventColor.RED.borderColor)
            .build();

        events.add(event);
      } else {
        event.setEnd(abs.getPersonDay().getDate().plusDays(1));
      }

    }
    return events;
  }

  /**
   * Carica la lista delle reperibilità di un certo tipo associati ad una determinata persona in un
   * intervallo di tempo.
   *
   * @param reperibility attività di reperibilità
   * @param person       persona associata ai turni
   * @param start        data inizio intervallo di tempo
   * @param end          data fine intervallo di tempo
   * @param color        colore da utilizzare per il rendering degli eventi restituiti
   * @return Una lista di DTO da serializzare in Json per renderizzarli nel fullcalendar.
   */
  private List<ReperibilityEvent> reperibilityEvents(PersonReperibilityType reperibility,
      Person person, LocalDate start, LocalDate end, EventColor color) {

    return reperibilityDao.getPersonReperibilityDaysByPeriodAndType(start,
            end, reperibility, person).stream()
        .map(personReperibilityDay -> {
          final ReperibilityEvent event = ReperibilityEvent.builder()

              .personReperibilityDayId(personReperibilityDay.getId())
              .title(person.fullName())
              .start(personReperibilityDay.getDate())
              .end(personReperibilityDay.getDate())
              .durationEditable(false)
              .color(color.backgroundColor)
              .textColor(color.textColor)
              .borderColor(color.borderColor)
              .className("removable")
              .personId(person.getId())
              .build();

          return event;
        }).collect(Collectors.toList());
  }
}