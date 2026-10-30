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

package it.cnr.iit.epas.tests.configurations;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Servizi per epas-configurations-service: accesso riservato al client di servizio e dati dei
 * proprietari delle configurazioni.
 */
@Transactional
@SpringBootTest
@TestPropertySource(properties = "epas.security.oauth2.resourceserverEnabled=true")
class ConfigurationSupportControllerTest {

  private static final String BASE = "/rest/v4/configurations";

  @Inject
  private WebApplicationContext context;
  @Inject
  private H2Examples h2Examples;

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  private static RequestPostProcessor client(String clientId) {
    return jwt().jwt(token -> token.claim("azp", clientId));
  }

  private static RequestPostProcessor configurationsClient() {
    return client("epas-configurations-service");
  }

  @Test
  void onlyConfigurationsClientCanCallTheEndpoints() throws Exception {
    mvc.perform(get(BASE + "/generalSettings").with(configurationsClient()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enableAbsenceTopLevelAuthorization").isBoolean());
    mvc.perform(get(BASE + "/generalSettings").with(client("epas-workflow-service")))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE + "/generalSettings")).andExpect(status().isUnauthorized());
  }

  @Test
  void ownersWithValidity() throws Exception {
    Person person = h2Examples.normalEmployee(LocalDate.of(2016, 1, 1), Optional.empty());

    mvc.perform(get(BASE + "/people/" + person.getId()).with(configurationsClient()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.officeId").value(person.getOffice().getId()));
    mvc.perform(get(BASE + "/offices/" + person.getOffice().getId())
        .with(configurationsClient()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(person.getOffice().getId()));
    mvc.perform(get(BASE + "/people/0").with(configurationsClient()))
        .andExpect(status().isNotFound());
  }

  @Test
  void recomputationForUnknownOwnerIsRefused() throws Exception {
    mvc.perform(post(BASE + "/recomputations").with(configurationsClient())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"ownerType":"PERSON","ownerId":0,"types":["DAYS"],"from":"2026-01-01"}
            """))
        .andExpect(status().isNotFound());
    mvc.perform(post(BASE + "/recomputations").with(configurationsClient())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"ownerType":"PERSON","ownerId":0,"types":[]}
            """))
        .andExpect(status().isBadRequest());
    mvc.perform(get(BASE + "/recomputations/unknown").with(configurationsClient()))
        .andExpect(status().isNotFound());
  }
}
