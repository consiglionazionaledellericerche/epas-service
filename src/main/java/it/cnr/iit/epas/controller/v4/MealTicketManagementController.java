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
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.config.client.MealTicketServiceClient;
import it.cnr.iit.epas.controller.exceptions.ValidationException;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestione dei buoni pasto e delle card per gli utenti: verifica i permessi dell'utente sulla sede
 * interessata e inoltra la richiesta a epas-meal-ticket-service con le credenziali di servizio.
 *
 * <p>Il percorso non inizia con /mealtickets perché la regola IsEmployee abilita ai dipendenti
 * tutte le GET con quel prefisso.</p>
 */
@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)})
@Tag(
    name = "MealTicket management controller",
    description = "Gestione dei buoni pasto e delle card da parte degli amministratori.")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + MealTicketManagementController.PATH)
public class MealTicketManagementController {

  public static final String PATH = "/mealticketmanagement";

  private final MealTicketServiceClient client;
  private final SecurityRules rules;
  private final SecureUtils secureUtils;
  private final OfficeDao officeDao;
  private final PersonDao personDao;
  private final ContractDao contractDao;

  @Operation(summary = "Riepilogo dei buoni pasto di un contratto e del suo precedente.")
  @GetMapping("/contracts/{contractId}/recap")
  ResponseEntity<byte[]> contractRecap(@PathVariable Long contractId) {
    checkContract(contractId);
    return client.forward(HttpMethod.GET, "/mealtickets/contracts/{contractId}/recap",
        Map.of("contractId", contractId), null);
  }

  @Operation(summary = "Riepiloghi dei buoni pasto dei dipendenti di una sede nel mese.")
  @GetMapping("/offices/{officeId}/recap")
  ResponseEntity<byte[]> officeRecap(@PathVariable Long officeId,
      @RequestParam int year, @RequestParam int month) {
    checkOffice(officeId);
    return client.forward(HttpMethod.GET, "/mealtickets/offices/{officeId}/recap",
        Map.of("officeId", officeId, "year", year, "month", month), null);
  }

  @Operation(summary = "Inserimento di un blocchetto; l'amministratore è l'utente corrente.")
  @PostMapping("/contracts/{contractId}/blocks")
  ResponseEntity<byte[]> createBlock(@PathVariable Long contractId,
      @RequestBody Map<String, Object> dto) {
    checkContract(contractId);
    return client.forward(HttpMethod.POST, "/mealtickets/contracts/{contractId}/blocks",
        Map.of("contractId", contractId), withCurrentAdmin(dto));
  }

  @Operation(summary = "Riconsegna alla sede centrale di una porzione di blocchetto.")
  @PutMapping("/contracts/{contractId}/blocks/{codeBlock}/return")
  ResponseEntity<byte[]> returnBlock(@PathVariable Long contractId,
      @PathVariable String codeBlock, @RequestParam int first, @RequestParam int last,
      @RequestParam(defaultValue = "false") boolean undo) {
    checkContract(contractId);
    return client.forward(HttpMethod.PUT,
        "/mealtickets/contracts/{contractId}/blocks/{codeBlock}/return",
        Map.of("contractId", contractId, "codeBlock", codeBlock, "first", first, "last", last,
            "undo", undo), null);
  }

  @Operation(summary = "Eliminazione di una porzione di blocchetto.")
  @DeleteMapping("/contracts/{contractId}/blocks/{codeBlock}")
  ResponseEntity<byte[]> deleteBlock(@PathVariable Long contractId,
      @PathVariable String codeBlock, @RequestParam int first, @RequestParam int last) {
    checkContract(contractId);
    return client.forward(HttpMethod.DELETE,
        "/mealtickets/contracts/{contractId}/blocks/{codeBlock}",
        Map.of("contractId", contractId, "codeBlock", codeBlock, "first", first, "last", last),
        null);
  }

  @Operation(summary = "Conversione di un blocchetto da cartaceo a elettronico o viceversa.")
  @PutMapping("/contracts/{contractId}/blocks/{codeBlock}/convert")
  ResponseEntity<byte[]> convertBlock(@PathVariable Long contractId,
      @PathVariable String codeBlock) {
    checkContract(contractId);
    return client.forward(HttpMethod.PUT,
        "/mealtickets/contracts/{contractId}/blocks/{codeBlock}/convert",
        Map.of("contractId", contractId, "codeBlock", codeBlock), null);
  }

  @Operation(summary = "Trasferimento dei buoni avanzati dal contratto precedente.")
  @PostMapping("/contracts/{contractId}/legacy")
  ResponseEntity<byte[]> legacy(@PathVariable Long contractId) {
    checkContract(contractId);
    return client.forward(HttpMethod.POST, "/mealtickets/contracts/{contractId}/legacy",
        Map.of("contractId", contractId), null);
  }

  /**
   * Come in ePAS gli utenti di sistema cercano in tutte le sedi; gli altri nella sede indicata
   * oppure, se assente, nella propria.
   */
  @Operation(summary = "Ricerca di un blocchetto.")
  @GetMapping("/blocks")
  ResponseEntity<byte[]> findBlocks(@RequestParam String code,
      @RequestParam Optional<Long> officeId) {
    User user = currentUser();
    Map<String, Object> variables = new HashMap<>(Map.of("code", code));
    if (officeId.isPresent()) {
      checkOffice(officeId.get());
      variables.put("officeId", officeId.get());
    } else if (!user.isSystemUser()) {
      if (user.getPerson() == null) {
        throw new AccessDeniedException("Utente senza una sede di appartenenza");
      }
      Office office = user.getPerson().getOffice();
      rules.checkifPermitted(office);
      variables.put("officeId", office.getId());
    }
    return client.forward(HttpMethod.GET, "/mealtickets/blocks", variables, null);
  }

  @Operation(summary = "Blocchi della sede che possono essere riconsegnati.")
  @GetMapping("/offices/{officeId}/returned")
  ResponseEntity<byte[]> returned(@PathVariable Long officeId,
      @RequestParam(defaultValue = "") String code) {
    checkOffice(officeId);
    return client.forward(HttpMethod.GET, "/mealtickets/offices/{officeId}/returned",
        Map.of("officeId", officeId, "code", code), null);
  }

  @Operation(summary = "Tipologia di blocchetto associata ai buoni maturati nel mese.")
  @GetMapping("/contracts/{contractId}/composition")
  ResponseEntity<byte[]> whichBlock(@PathVariable Long contractId,
      @RequestParam int year, @RequestParam int month) {
    checkContract(contractId);
    return client.forward(HttpMethod.GET, "/mealtickets/contracts/{contractId}/composition",
        Map.of("contractId", contractId, "year", year, "month", month), null);
  }

  @Operation(summary = "Persone di una sede con i riepiloghi del mese attuale e le card attive.")
  @GetMapping("/cards/offices/{officeId}")
  ResponseEntity<byte[]> officeCards(@PathVariable Long officeId) {
    checkOffice(officeId);
    return client.forward(HttpMethod.GET, "/mealticketcards/offices/{officeId}",
        Map.of("officeId", officeId), null);
  }

  /**
   * Come in ePAS servono i permessi sulla sede della persona e su quella che consegna la card.
   */
  @Operation(summary = "Associazione di una nuova card alla persona.")
  @PostMapping("/cards")
  ResponseEntity<byte[]> createCard(@RequestBody Map<String, Object> dto) {
    checkPerson(numericField(dto, "personId"));
    if (dto.get("deliveryOfficeId") != null) {
      checkOffice(numericField(dto, "deliveryOfficeId"));
    }
    return client.forward(HttpMethod.POST, "/mealticketcards", Map.of(), dto);
  }

  @Operation(summary = "Informazioni sulla card.")
  @GetMapping("/cards/{id}")
  ResponseEntity<byte[]> showCard(@PathVariable Long id) {
    return withCardPermission(id, () ->
        client.forward(HttpMethod.GET, "/mealticketcards/{id}", Map.of("id", id), null));
  }

  @Operation(summary = "Rimozione di una card senza buoni associati.")
  @DeleteMapping("/cards/{id}")
  ResponseEntity<byte[]> deleteCard(@PathVariable Long id) {
    return withCardPermission(id, () ->
        client.forward(HttpMethod.DELETE, "/mealticketcards/{id}", Map.of("id", id), null));
  }

  @Operation(summary = "Situazione dei buoni elettronici sul contratto indicato.")
  @GetMapping("/cards/contracts/{contractId}")
  ResponseEntity<byte[]> contractCard(@PathVariable Long contractId) {
    checkContract(contractId);
    return client.forward(HttpMethod.GET, "/mealticketcards/contracts/{contractId}",
        Map.of("contractId", contractId), null);
  }

  @Operation(summary = "Assegna alla card i buoni elettronici del contratto attuale senza card.")
  @PostMapping("/cards/{id}/assignOrphans")
  ResponseEntity<byte[]> assignOrphans(@PathVariable Long id) {
    return withCardPermission(id, () -> client.forward(HttpMethod.POST,
        "/mealticketcards/{id}/assignOrphans", Map.of("id", id), null));
  }

  @Operation(summary = "Carica i buoni elettronici sulla card; l'amministratore è l'utente "
      + "corrente.")
  @PostMapping("/cards/{id}/tickets")
  ResponseEntity<byte[]> tickets(@PathVariable Long id, @RequestBody Map<String, Object> dto) {
    return withCardPermission(id, () -> client.forward(HttpMethod.POST,
        "/mealticketcards/{id}/tickets", Map.of("id", id), withCurrentAdmin(dto)));
  }

  private ResponseEntity<byte[]> withCardPermission(Long cardId,
      Supplier<ResponseEntity<byte[]>> action) {
    Optional<Long> ownerId = client.cardOwnerId(cardId);
    if (ownerId.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
    checkPerson(ownerId.get());
    return action.get();
  }

  private void checkOffice(Long officeId) {
    Office office = officeDao.getOfficeById(officeId);
    if (office == null) {
      throw new EntityNotFoundException("Office not found");
    }
    rules.checkifPermitted(office);
  }

  private void checkPerson(Long personId) {
    Person person = personDao.byId(personId)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    rules.checkifPermitted(person.getOffice());
  }

  private void checkContract(Long contractId) {
    Person person = contractDao.byId(contractId)
        .orElseThrow(() -> new EntityNotFoundException("Contract not found"))
        .getPerson();
    rules.checkifPermitted(person.getOffice());
  }

  private User currentUser() {
    return secureUtils.getCurrentUser()
        .orElseThrow(() -> new AccessDeniedException("Utente non riconosciuto"));
  }

  /**
   * L'amministratore che esegue l'operazione è sempre l'utente corrente, mai quello indicato
   * nella richiesta.
   */
  private Map<String, Object> withCurrentAdmin(Map<String, Object> dto) {
    Person admin = currentUser().getPerson();
    if (admin == null) {
      throw new ValidationException("L'utente non è associato a una persona");
    }
    Map<String, Object> result = new HashMap<>(dto);
    result.put("adminId", admin.getId());
    return result;
  }

  private Long numericField(Map<String, Object> dto, String name) {
    if (dto.get(name) instanceof Number value) {
      return value.longValue();
    }
    throw new ValidationException("Il campo " + name + " è obbligatorio");
  }
}
