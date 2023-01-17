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

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione delle drools utilizzate per il controllo delle autorizzazioni.
 *
 * @author Cristian Lucchesi
 *
 */
@Configuration
public class DroolsConfig {

  private static final String drlFile = "permissions.drl";

  /**
   * Produzione della configurazione di base del KIE delle Drools.
   */
  @Bean
  public KieBase kieBase() {
    final KieServices ks = KieServices.Factory.get();
    KieFileSystem kieFileSystem = ks.newKieFileSystem();
    kieFileSystem.write(ResourceFactory.newClassPathResource(drlFile));
    KieBuilder kieBuilder = ks.newKieBuilder(kieFileSystem);
    kieBuilder.buildAll();
    KieModule kieModule = kieBuilder.getKieModule();

    return ks.newKieContainer(kieModule.getReleaseId()).getKieBase();
  }

}