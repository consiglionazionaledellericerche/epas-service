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
package it.cnr.iit.epas.tests.mealtickets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import it.cnr.iit.epas.config.client.MealTicketServiceClient;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

/**
 * Inoltro verso un servizio HTTP finto: percorsi codificati e traduzione degli errori.
 */
class MealTicketServiceClientTest {

  private HttpServer server;
  private MealTicketServiceClient client;
  private final AtomicReference<String> rawUri = new AtomicReference<>();
  private final AtomicReference<String> requestBody = new AtomicReference<>();
  private int responseStatus = 200;
  private String responseBody = "{\"personId\":7,\"number\":\"1\"}";

  @BeforeEach
  void setUp() throws IOException {
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext("/", exchange -> {
      rawUri.set(exchange.getRequestURI().getRawPath()
          + "?" + exchange.getRequestURI().getRawQuery());
      requestBody.set(new String(exchange.getRequestBody().readAllBytes(),
          StandardCharsets.UTF_8));
      byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(responseStatus, body.length == 0 ? -1 : body.length);
      if (body.length > 0) {
        exchange.getResponseBody().write(body);
      }
      exchange.close();
    });
    server.start();
    client = new MealTicketServiceClient(RestClient.builder()
        .baseUrl("http://localhost:" + server.getAddress().getPort()).build());
  }

  @AfterEach
  void tearDown() {
    server.stop(0);
  }

  @Test
  void pathAndQueryValuesAreEncoded() {
    var response = client.forward(HttpMethod.GET, "/mealtickets/blocks/{code}",
        Map.of("code", "a/b", "officeId", "1&admin=true"), null);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("/rest/v4/mealtickets/blocks/a%2Fb?officeId=1%26admin%3Dtrue", rawUri.get());
    assertArrayEquals(responseBody.getBytes(StandardCharsets.UTF_8), response.getBody());
  }

  @Test
  void bodyIsSentAsJson() {
    client.forward(HttpMethod.POST, "/mealticketcards", Map.of(), Map.of("personId", 7));
    assertEquals("{\"personId\":7}", requestBody.get());
  }

  @Test
  void requestErrorsReachTheCaller() {
    responseStatus = 400;
    responseBody = "La data di consegna non può essere futura";
    var response = client.forward(HttpMethod.GET, "/mealtickets/blocks", Map.of(), null);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertArrayEquals(responseBody.getBytes(StandardCharsets.UTF_8), response.getBody());
  }

  @Test
  void serviceCredentialsAndServerErrorsBecomeBadGateway() {
    for (int status : new int[] {401, 403, 500}) {
      responseStatus = status;
      assertEquals(HttpStatus.BAD_GATEWAY,
          client.forward(HttpMethod.GET, "/mealtickets/blocks", Map.of(), null).getStatusCode());
    }
  }

  @Test
  void unreachableServiceBecomesBadGateway() {
    server.stop(0);
    assertEquals(HttpStatus.BAD_GATEWAY,
        client.forward(HttpMethod.GET, "/mealtickets/blocks", Map.of(), null).getStatusCode());
    assertEquals(Optional.empty(), client.cardOwnerId(1L));
  }

  @Test
  void cardOwnerIsRead() {
    assertEquals(Optional.of(7L), client.cardOwnerId(1L));
    responseStatus = 404;
    responseBody = "";
    assertThrows(EntityNotFoundException.class, () -> client.cardOwnerId(1L));
  }
}
