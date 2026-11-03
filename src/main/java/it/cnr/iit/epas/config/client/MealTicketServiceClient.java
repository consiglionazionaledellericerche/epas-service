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
package it.cnr.iit.epas.config.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Inoltra le richieste a epas-meal-ticket-service, dopo che epas-service ha verificato i permessi
 * dell'utente.
 */
@Slf4j
@RequiredArgsConstructor
public class MealTicketServiceClient {

  private static final String BASE_PATH = "/rest/v4";

  private final RestClient restClient;

  @JsonIgnoreProperties(ignoreUnknown = true)
  record CardOwner(Long personId) {}

  /**
   * Inoltra una richiesta e ne restituisce la risposta. Le variabili non presenti nel percorso
   * sono inviate come parametri della query, così da essere sempre codificate.
   *
   * <p>Gli errori del servizio dovuti alla richiesta sono restituiti al chiamante. Quelli dovuti
   * alle credenziali di servizio o al servizio stesso non dipendono dall'utente: diventano 502.
   * </p>
   */
  public ResponseEntity<byte[]> forward(HttpMethod method, String path,
      Map<String, ?> variables, Object body) {
    try {
      RestClient.RequestBodySpec request = restClient.method(method)
          .uri(builder -> {
            builder.path(BASE_PATH + path);
            variables.keySet().stream()
                .filter(name -> !path.contains("{" + name + "}"))
                .forEach(name -> builder.queryParam(name, "{" + name + "}"));
            return builder.build(variables);
          });
      if (body != null) {
        request.contentType(MediaType.APPLICATION_JSON).body(body);
      }
      return request.exchange((req, response) -> {
        HttpStatusCode status = response.getStatusCode();
        if (status.is5xxServerError() || status.isSameCodeAs(HttpStatus.UNAUTHORIZED)
            || status.isSameCodeAs(HttpStatus.FORBIDDEN)) {
          log.warn("epas-meal-ticket-service ha risposto {} a {} {}", status, method, path);
          return ResponseEntity.status(HttpStatus.BAD_GATEWAY).<byte[]>build();
        }
        var result = ResponseEntity.status(status);
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType != null) {
          result.contentType(contentType);
        }
        return result.body(response.getBody().readAllBytes());
      });
    } catch (RestClientException e) {
      log.warn("Chiamata a epas-meal-ticket-service fallita: {} {}", method, path, e);
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
  }

  /**
   * La persona a cui è associata la card, necessaria per verificare i permessi sulla card.
   *
   * @throws EntityNotFoundException se la card non esiste.
   * @return vuoto se non è stato possibile ottenere la risposta dal servizio.
   */
  public Optional<Long> cardOwnerId(Long cardId) {
    try {
      return Optional.ofNullable(restClient.get()
          .uri(BASE_PATH + "/mealticketcards/{id}", cardId)
          .retrieve()
          .body(CardOwner.class))
          .map(CardOwner::personId);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
        throw new EntityNotFoundException("Card not found");
      }
      log.warn("Lettura della card {} fallita con stato {}", cardId, e.getStatusCode());
      return Optional.empty();
    } catch (RestClientException e) {
      log.warn("Lettura della card {} fallita", cardId, e);
      return Optional.empty();
    }
  }
}
