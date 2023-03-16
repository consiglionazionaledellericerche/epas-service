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

package it.cnr.iit.epas.config;

import it.cnr.iit.epas.security.MyBasicAuthenticationEntryPoint;
import javax.inject.Inject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
    http.csrf().disable();
    http.cors();

    http.authorizeRequests(authz -> authz.antMatchers("/rest/**").authenticated());
    if (securityConfig.getOauth2().isResourceserverEnabled()) {
      http.oauth2ResourceServer(oauth2 -> oauth2.jwt());
    }
    http.httpBasic().realmName("epas-service").authenticationEntryPoint(authenticationEntryPoint);

    //Lo swagger è utilizzabile da tutti, anche gli utenti anonimi.
    http.authorizeRequests()
      .antMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll();

    return http.build();
  }

}