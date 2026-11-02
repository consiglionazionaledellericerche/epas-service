/*
 * Copyright (C) 2025  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.config;

import it.cnr.iit.epas.controller.v4.CertificationSupportController;
import it.cnr.iit.epas.controller.v4.ConfigurationSupportController;
import it.cnr.iit.epas.controller.v4.MealTicketSupportController;
import it.cnr.iit.epas.controller.v4.WorkflowController;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.security.MyBasicAuthenticationEntryPoint;
import jakarta.inject.Inject;
import java.util.List;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configurazione della catena di filtri necessaria per la security dell'applicazione.
 *
 * @author Cristian Lucchesi
 *
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Inject
  private CustomAuthenticationProvider authProvider;
  
  @Inject
  private SecurityProperties securityConfig;
  
  @Inject
  public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(authProvider);
  }

  @Inject
  MyBasicAuthenticationEntryPoint authenticationEntryPoint;

  /**
   * Configurazione della catena di filtri di autenticazione da applicare ai metodi REST.
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    //Senza il csrf disabilitato le POST/PUT/DELETE non funzionerebbero con
    //l'autenticazione Basic Auth.
    //Con l'autenticazione con il Bearer token il csrf non sembra essere necessario.
    //http.csrf().disable();
    //http.cors();
    http.csrf(AbstractHttpConfigurer::disable);

    //http.authorizeRequests(authz -> authz.antMatchers("/rest/**").authenticated());
    //Lo swagger è utilizzabile da tutti, anche gli utenti anonimi.
    //    http.authorizeRequests()
    //      .antMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll();

    http.authorizeHttpRequests(authz -> authz
        .requestMatchers(HttpMethod.OPTIONS).permitAll()
        .requestMatchers(HttpMethod.GET, "/actuator/*").permitAll()
        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**").permitAll()
        //Servizi riservati ai client di servizio di epas-workflow-service
        .requestMatchers(ApiRoutes.BASE_PATH + WorkflowController.PATH + "/**")
          .access((authentication, context) -> new AuthorizationDecision(
              isServiceClient(authentication.get(), securityConfig.getWorkflowClientIds())))
        //Servizi riservati ai client di servizio di epas-certifications-service
        .requestMatchers(ApiRoutes.BASE_PATH + CertificationSupportController.PATH + "/**")
          .access((authentication, context) -> new AuthorizationDecision(
              isServiceClient(authentication.get(), securityConfig.getCertificationClientIds())))
        //Servizi riservati ai client di servizio di epas-configurations-service
        .requestMatchers(ApiRoutes.BASE_PATH + ConfigurationSupportController.PATH + "/**")
          .access((authentication, context) -> new AuthorizationDecision(
              isServiceClient(authentication.get(), securityConfig.getConfigurationClientIds())))
        //Servizi riservati ai client di servizio di epas-meal-ticket-service
        .requestMatchers(ApiRoutes.BASE_PATH + MealTicketSupportController.PATH + "/**")
          .access((authentication, context) -> new AuthorizationDecision(
              isServiceClient(authentication.get(), securityConfig.getMealTicketClientIds())))
        .anyRequest().authenticated());


    if (securityConfig.getOauth2().isResourceserverEnabled()) {
      http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    }
    http.httpBasic(basic -> basic.realmName("epas-service")
        .authenticationEntryPoint(authenticationEntryPoint));


    return http.build();

  }

  /**
   * Verifica che l'autenticazione sia un token di uno dei client di servizio indicati.
   */
  private boolean isServiceClient(Authentication authentication, List<String> clientIds) {
    if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
      return false;
    }
    val jwt = jwtAuthentication.getToken();
    val clientId = jwt.hasClaim("azp")
        ? jwt.getClaimAsString("azp") : jwt.getClaimAsString("client_id");
    return clientId != null && clientIds.contains(clientId);
  }

}
