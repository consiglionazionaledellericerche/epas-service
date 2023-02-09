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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione dei parametri generali della documentazione
 * tramite OpenAPI.
 *
 * @author Cristian Lucchesi
 *
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(title = "ePAS Service", 
    version = "4.0", 
    description = "ePAS Service contains all the business logic and related REST endpoints"
        + " to manage all the personnel information,")
    )
@SecuritySchemes(value = {
    @SecurityScheme(
        name = OpenApiConfiguration.BEARER_AUTHENTICATION,
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT"
    ),
    @SecurityScheme(
        name = OpenApiConfiguration.BASIC_AUTHENTICATION,
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "basic"
    )
})
public class OpenApiConfiguration {

  public static final String BEARER_AUTHENTICATION = "Bearer Authentication";
  public static final String BASIC_AUTHENTICATION = "Basic Authentication";

}