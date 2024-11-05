/*
 * Copyright (C) 2024 Consiglio Nazionale delle Ricerche
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

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configurazione dei parametri relativi al CORS.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

  private final CorsSettings cors;

  @Bean
  CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration = new CorsConfiguration();

    log.info("Cors config ={}", cors);
    if (cors.getAllowedOrigins() != null || cors.getAllowedMethods() != null) {
      configuration.setAllowedHeaders(Arrays.asList("*"));
    }

    if (cors.getAllowedOrigins() != null) {
      log.info("cors.getAllowedOrigins() != null, imposto allowedOrigins = {}", 
          cors.getAllowedOrigins().toString());
      configuration.setAllowedOriginPatterns(Arrays.asList(cors.getAllowedOrigins()));
    }

    if (cors.getAllowedMethods() != null) {
      configuration.setAllowedMethods(Arrays.asList(cors.getAllowedMethods()));
    }

    if (cors.getMaxAge() != null) {
      configuration.setMaxAge(cors.getMaxAge());
    }

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}