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

import it.cnr.iit.epas.security.DroolsHandler;
import javax.inject.Inject;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig.
 *
 * @author Daniele Murgia
 * @author Cristian Lucchesi
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Inject
  private DroolsHandler droolsHandler;

  /**
   * Automatizza la chiamata delle drools all'ingresso dei metodi dei controller. Non ha effetto sui
   * metodi annotati con la @Preauthorize.
   *
   * @param registry InterceptorRegistry.
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(droolsHandler);
  }

}