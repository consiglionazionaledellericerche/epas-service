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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/**
 * Client verso epas-meal-ticket-service autenticato con le credenziali di servizio (client
 * credentials) della registrazione OAuth2 "epas-meal-ticket-service".
 */
@Configuration
public class MealTicketServiceClientConfig {

  public static final String REGISTRATION_ID = "epas-meal-ticket-service";

  /**
   * Il token di servizio è unico e non dipende dall'utente che sta facendo la richiesta.
   */
  private static final Authentication SERVICE_PRINCIPAL = new AnonymousAuthenticationToken(
      REGISTRATION_ID, "epas-service", AuthorityUtils.createAuthorityList("ROLE_SERVICE"));

  @Bean
  OAuth2AuthorizedClientManager serviceAuthorizedClientManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientService authorizedClientService) {
    var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
        clientRegistrationRepository, authorizedClientService);
    manager.setAuthorizedClientProvider(
        OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
    return manager;
  }

  @Bean
  MealTicketServiceClient mealTicketServiceClient(
      @Value("${epas.meal-ticket-service.url}") String baseUrl,
      OAuth2AuthorizedClientManager serviceAuthorizedClientManager) {
    OAuth2ClientHttpRequestInterceptor interceptor =
        new OAuth2ClientHttpRequestInterceptor(serviceAuthorizedClientManager);
    interceptor.setClientRegistrationIdResolver(request -> REGISTRATION_ID);
    interceptor.setPrincipalResolver(request -> SERVICE_PRINCIPAL);
    return new MealTicketServiceClient(RestClient.builder()
        .baseUrl(baseUrl)
        .requestInterceptor(interceptor)
        .build());
  }
}
