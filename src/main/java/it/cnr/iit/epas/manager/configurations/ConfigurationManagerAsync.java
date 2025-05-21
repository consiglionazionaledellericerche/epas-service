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

package it.cnr.iit.epas.manager.configurations;

import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import jakarta.transaction.Transactional;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


/**
 * Servizio per lanciare in asincrono alcuni metodi
 * del ConfigurationManager.
 *
 */
@Transactional
@Slf4j
@Service
public class ConfigurationManagerAsync {

  private final ObjectProvider<EntityManager> emp;
  private final ConfigurationManagerUtils utils;

  @Inject
  ConfigurationManagerAsync(
      ObjectProvider<EntityManager> emp,
      ConfigurationManagerUtils utils) {
    this.emp = emp;
    this.utils = utils;
  }

  /**
   * Metodo asincrono per aggiornare i parametri di configurazione di una persona.
   */
  @Async
  public void updateConfigurations(Person owner) {
    log.debug("async updateConfigurations for {}", owner);
    emp.getObject().merge(owner);
    utils.updateConfigurations(owner);
  }

  /**
   * Metodo asincrono per aggiornare i parametri di configurazione di un ufficio.
   */
  @Async
  public void updateConfigurations(Office owner) {
    log.debug("async updateConfigurations for {}", owner);
    emp.getObject().merge(owner);
    utils.updateConfigurations(owner);
  }

}