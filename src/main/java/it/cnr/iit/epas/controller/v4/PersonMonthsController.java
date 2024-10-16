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
import com.google.common.base.Verify;
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
import it.cnr.iit.epas.dao.PersonMonthRecapDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.dao.wrapper.IWrapperContractMonthRecap;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dto.v4.PersonMonthRecapCreateDto;
import it.cnr.iit.epas.dto.v4.PersonMonthRecapDto;
import it.cnr.iit.epas.dto.v4.PersonMonthsDto;
import it.cnr.iit.epas.dto.v4.TrainingHoursDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonMonthRecapMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonMonthsMapper;
import it.cnr.iit.epas.dto.v4.mapper.TrainingHoursMapper;
import it.cnr.iit.epas.manager.PersonMonthsManager;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonMonthRecap;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi alla gestione dei PersonMonths.
 *
 * @author Cristian Lucchesi
 */
@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)})
@Tag(
    name = "PersonMonths controller",
    description = "Visualizzazione dei riepiloghi orari e formazione dei dipendenti.")
@Transactional
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/personmonths")
public class PersonMonthsController {

  private final IWrapperFactory wrapperFactory;
  private final PersonMonthsManager personMonthsManager;
  private final PersonMonthsMapper personMonthsMapper;
  private final PersonMonthRecapMapper personMonthRecapMapper;
  private final TrainingHoursMapper trainingHoursMapper;
  private final PersonMonthRecapDao personMonthRecapDao;
  private final SecurityRules rules;
  private final SecureUtils securityUtils;
  private final Validator validator;

  @Operation(
      summary = "Visualizzazione del riepilogo orario del dipendente.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "la propria situazione oraria, oppure dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i dati dei riepiloghi orari"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati del riepilogo delle ore.",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Riepilogo ore non trovato con l'anno fornito",
          content = @Content)
  })
  @GetMapping("/hourRecap")
  ResponseEntity<List<PersonMonthsDto>> hourRecap(
      @NotNull @RequestParam("year") Integer year) {
    log.debug("REST method {} invoked with parameters year={}",
        "/rest/v4/personmonths/hourRecap", year);

    Optional<User> user = securityUtils.getCurrentUser();

    if (!user.isPresent() || user.get().getPerson() == null) {
      //flash.error("Accesso negato.");
      //renderTemplate("Application/indexAdmin.html");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    if (year > LocalDate.now().getYear()) {
      //flash.error("Impossibile richiedere riepilogo anno futuro.");
      //renderTemplate("Application/indexAdmin.html");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    Person person = user.get().getPerson();
    Optional<Contract> contract = wrapperFactory.create(person).getCurrentContract();

    List<PersonMonthsDto> pmListDto = Lists.newArrayList();
    Preconditions.checkState(contract.isPresent());
    List<IWrapperContractMonthRecap> recaps = Lists.newArrayList();
    IWrapperContractMonthRecap wp;
    YearMonth actual = YearMonth.of(year, 1);
    YearMonth last = YearMonth.of(year, 12);
    IWrapperContract con = wrapperFactory.create(contract.get());
    while (!actual.isAfter(last)) {
      Optional<ContractMonthRecap> recap = con.getContractMonthRecap(actual);
      if (recap.isPresent()) {
        wp = wrapperFactory.create(recap.get());
        PersonMonthsDto pmDto = personMonthsMapper.convert(wp);
        pmListDto.add(pmDto);
      }
      actual = actual.plusMonths(1);
    }
    return ResponseEntity.ok().body(pmListDto);
  }

  @Operation(
      summary = "Visualizzazione del riepilogo delle ore di formazione annuale del dipendente.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "la propria situazione delle ore di formazione, oppure dagli utenti con il ruolo "
          + "'Amministratore del personale' della sede a cui appartiene la persona, oppure dagli "
          + "utenti con il ruolo di sistema 'Developer' e/o 'Admin'.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti i dati dei riepiloghi delle ore di formazione"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " i dati del riepilogo delle ore di formazione.",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Riepilogo ore di formazione non trovato con l'anno fornito",
          content = @Content)
  })
  @GetMapping("/trainingHours")
  ResponseEntity<TrainingHoursDto> trainingHours(
      @NotNull @RequestParam("year") Integer year) {
    log.debug("REST method {} invoked with parameters year={}",
        "/rest/v4/personmonths/trainingHours", year);

    Person person = securityUtils.getCurrentUser().get().getPerson();

    List<PersonMonthRecap> personMonthRecapList = personMonthRecapDao
        .getPersonMonthRecapInYearOrWithMoreDetails(person, year,
            Optional.empty(), Optional.empty());

    LocalDate today = LocalDate.now();
    List<PersonMonthRecapDto> dtoList = new ArrayList<>();
    personMonthRecapList.forEach(pmr -> dtoList.add(personMonthRecapMapper.convert(pmr)));

    TrainingHoursDto dto = new TrainingHoursDto();
    dto.setPerson(trainingHoursMapper.convert(person));
    dto.setToday(today);
    dto.setPersonMonthRecaps(dtoList);
    return ResponseEntity.ok().body(dto);
  }

  @DeleteMapping("/trainingHours" + ApiRoutes.DELETE)
  ResponseEntity<Map<String, String>> trainingHours(
      @NotNull @PathVariable("id") Long idTraining) {
    log.debug("REST method {} invoked with parameters idTraining={}",
        "/rest/v4/personmonths/trainingHours", idTraining);

    Map<String, String> response = new HashMap<>();
    PersonMonthRecap pm = personMonthRecapDao.getPersonMonthRecapById(idTraining);
    if (pm == null) {
      throw new EntityNotFoundException("Ore di formazioni inesistenti. Operazione annullata.");
    }

    personMonthRecapDao.delete(pm);

    log.info("Eliminata trainingHour {}", pm);
    response.put("message", "Ore di formazione eliminate con successo.");
    return ResponseEntity.ok().body(response);
  }

  /**
   * Inserimento delle ore di formazioni
   */
  @Operation(
      summary = "Inserimento delle ore di formazione.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui inserire le ore di formazione e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alla formazione")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Inserite le ore di formazione"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a eseguire "
              + "l'aggiornamento delle ore di formazione'",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content),
      @ApiResponse(responseCode = "409",
          description = "Ore formative già inserite per quell'intervallo di tempo e quella persona.",
          content = @Content),
      @ApiResponse(responseCode = "412",
          description = "Errore di validazione dati inseriti.",
          content = @Content)
  })
  @PostMapping("/trainingHours")
  public ResponseEntity<Map<String, String>> insert(
      @NotNull @RequestBody @Valid PersonMonthRecapCreateDto personMonthRecapCreateDto) {
    log.debug("PersonMonthsController::insert PersonMonthRecapCreateDto = {}",
        personMonthRecapCreateDto);

    Map<Integer, String> resultMap = saveTrainingHours(personMonthRecapCreateDto);
    Map<String, String> response = new HashMap<>();
    response.put("message", "Ore di formazione aggiornate con successo.");

    Integer statusCode = resultMap.keySet().iterator().next();
    String message = resultMap.get(statusCode);

    if (statusCode == 404) {
      return ResponseEntity.notFound().build();
    }
    if (statusCode == 400) {
      return ResponseEntity.badRequest().build();
    }
    if (statusCode == 403) {
      response.put("message", message);
      return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(response);
    }
    if (statusCode == 409) {
      response.put("message", "Ore formative già presenti.");
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    response.put("message", "Ore formative salvate con successo.");
    return ResponseEntity.ok().body(response);
  }


  /**
   * Aggiornamento delle ore di formazioni
   */
  @Operation(
      summary = "Aggiornamento delle ore di formazione.",
      description = "Questo endpoint è utilizzabile dagli utenti con ruolo "
          + "'Gestore Assenze', 'Amministratore Personale' o "
          + "'Amministratore Personale sola lettura' della sede a "
          + "appartiene la persona di cui aggiornare le ore di formazione e dagli utenti con il ruolo "
          + "di sistema 'Developer' e/o 'Admin' oppure dall'utente relativo alla formazione")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Aggiornate le ore di formazione"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a eseguire "
              + "l'aggiornamento delle ore di formazione'",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Persona non trovata con l'id e/o il codice fiscale fornito",
          content = @Content),
      @ApiResponse(responseCode = "409",
          description = "Ore formative già inserite per quell'intervallo di tempo e quella persona.",
          content = @Content),
      @ApiResponse(responseCode = "412",
          description = "Errore di validazione dati inseriti.",
          content = @Content)
  })
  @PostMapping("/trainingHours" + ApiRoutes.UPDATE)
  public ResponseEntity<Map<String, String>> update(
      @NotNull @RequestBody @Valid PersonMonthRecapCreateDto personMonthRecapCreateDto) {
    log.debug("PersonMonthsController::update PersonMonthRecapCreateDto = {}",
        personMonthRecapCreateDto);

    Map<Integer, String> resultMap = saveTrainingHours(personMonthRecapCreateDto);
    Map<String, String> response = new HashMap<>();
    response.put("message", "Ore di formazione aggiornate con successo.");

    Integer statusCode = resultMap.keySet().iterator().next();
    String message = resultMap.get(statusCode);

    if (statusCode == 404) {
      return ResponseEntity.notFound().build();
    }
    if (statusCode == 400) {
      return ResponseEntity.badRequest().build();
    }
    if (statusCode == 403) {
      response.put("message", message);
      return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(response);
    }
    if (statusCode == 409) {
      response.put("message", "Ore formative già presenti.");
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    response.put("message", "Ore formative salvate con successo.");
    return ResponseEntity.ok().body(response);
  }

  private Map<Integer, String> saveTrainingHours(
      PersonMonthRecapCreateDto personMonthRecapCreateDto) {
    Map<Integer, String> resultMap = new HashMap<>();

    Long personMonthSituationId = personMonthRecapCreateDto.getId();

    Integer year = personMonthRecapCreateDto.getYear();
    Integer month = personMonthRecapCreateDto.getMonth();
    Integer begin = personMonthRecapCreateDto.getBegin();
    Integer end = personMonthRecapCreateDto.getEnd();
    Integer trainingHours = personMonthRecapCreateDto.getTrainingHours();;

    User user = securityUtils.getCurrentUser().get();
    Person person = user.getPerson();

    if (personMonthSituationId != null) {
      PersonMonthRecap pm = personMonthRecapDao.getPersonMonthRecapById(personMonthSituationId);
      Verify.verify(pm.isEditable());
      Errors errors = checkErrorsInUpdate(validator, trainingHours, pm);
      if (errors.hasErrors()) {
        LocalDate dateFrom = LocalDate.of(year, month, begin);
        LocalDate dateTo = LocalDate.of(year, month, end);
        resultMap.put(403, "Ci sono errori di validazione");
      }
      pm.setTrainingHours(trainingHours);
      personMonthRecapDao.save(pm);
      resultMap.put(200, "Ore di formazione aggiornate.");
      return resultMap;
    }

    PersonMonthRecap pm = new PersonMonthRecap(person, year, month);
    LocalDate beginDate = LocalDate.of(year, month, begin);
    pm.setHoursApproved(false);
    pm.setTrainingHours(trainingHours);
    pm.setFromDate(beginDate);
    pm.setToDate(LocalDate.of(year, month, end));

    Errors errors = checkErrors(validator, pm, begin, end, year, month, trainingHours);
    if (errors.hasErrors()) {
      resultMap.put(403, "Ci sono errori di validazione.");
    }
    if (!personMonthsManager.checkIfAlreadySent(person, year, month).getResult()) {
      resultMap.put(403, "Le ore di formazione per il mese selezionato sono già state approvate.");
    }
    personMonthsManager.saveTrainingHours(person, year, month, begin, end, false, trainingHours);
    resultMap.put(200, String.format("Salvate %d ore di formazione", trainingHours));
    return resultMap;
  }


  /**
   * metodo privato che aggiunge al validation eventuali errori riscontrati nel passaggio
   * dei parametri.
   *
   * @param begin il giorno di inizio della formazione
   * @param end il giorno di fine della formazione
   * @param year l'anno di formazione
   * @param month il mese di formazione
   * @param value la quantità di ore di formazione
   */
  private static Errors checkErrors(Validator validator, PersonMonthRecap pm, Integer begin, Integer end, Integer year,
      Integer month, Integer value) {

    Errors errors = new BeanPropertyBindingResult(pm,"personReperibilityDay");

    if (!errors.hasErrors()) {
      if (begin == null) {
        errors.rejectValue("begin", "Richiesto", "Il valore è obbligatorio");
      }
      if (end == null) {
        errors.rejectValue("end", "Richiesto", "Il valore è obbligatorio");
      }
      int endMonth = LocalDate.of(year, month, 1).lengthOfMonth();
      if (begin > endMonth) {
        errors.rejectValue("begin", "Mese",
            "deve appartenere al mese selezionato");
      }
      if (end > endMonth) {
        errors.rejectValue("end", "Mese",
            "deve appartenere al mese selezionato");
      }
      if (begin > end) {
        errors.rejectValue("begin", "Intervallo",
            "inizio intervallo  non valido");
      }

      if (value > 24 * (end - begin + 1) && end - begin >= 0) {
        errors.rejectValue("value", "valore.troppo.alto",
            "valore troppo alto");
      }
    }
      return errors;
  }

  /**
   * aggiunge al validation l'eventuale errore relativo al quantitativo orario che può superare
   * le ore possibili prendibili per quel giorno.
   *
   * @param value il quantitativo di ore di formazione
   * @param pm il personMonthRecap da modificare con le ore passate come parametro
   */
  private static Errors checkErrorsInUpdate(Validator validator, Integer value, PersonMonthRecap pm) {

    Errors errors = new BeanPropertyBindingResult(pm,"personReperibilityDay");
    validator.validate(pm, errors);

    if (!errors.hasErrors()) {
      if (value > 24 * (pm.getToDate().getDayOfMonth() - pm.getFromDate().getDayOfMonth() + 1)) {
        errors.rejectValue("value", "valore.troppo.alto", "Il valore inserito è troppo alto");
      }
    }

    return errors;
  }

}