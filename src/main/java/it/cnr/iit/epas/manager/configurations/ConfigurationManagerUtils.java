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

package it.cnr.iit.epas.manager.configurations;

import com.google.common.base.Verify;
import it.cnr.iit.epas.manager.PeriodManager;
import it.cnr.iit.epas.manager.configurations.EpasParam.EpasParamTimeType;
import it.cnr.iit.epas.models.Configuration;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonConfiguration;
import it.cnr.iit.epas.models.base.IPropertiesInPeriodOwner;
import it.cnr.iit.epas.models.base.IPropertyInPeriod;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ConfigurationManagerUtils {

  private final PeriodManager periodManager;

  @Inject
  ConfigurationManagerUtils(PeriodManager periodManage) {
    this.periodManager = periodManage;
  }

  /**
   * Costruttore generico di una configurazione periodica. Effettua tutti i passaggi di
   * validazione.
   */
  IPropertyInPeriod build(EpasParam epasParam, IPropertiesInPeriodOwner target,
      String fieldValue, Optional<LocalDate> begin, Optional<LocalDate> end, boolean applyToTheEnd,
      boolean persist) {
    if (applyToTheEnd) {
      end = Optional.ofNullable(target.calculatedEnd());
    }

    IPropertyInPeriod configurationInPeriod = null;

    if (epasParam.target.equals(Office.class)) {
      Configuration configuration = new Configuration();
      configuration.office = (Office) target;
      configuration.fieldValue = fieldValue;
      configuration.epasParam = epasParam;
      configuration.setBeginDate(configuration.office.getBeginDate());
      if (begin.isPresent()) {
        configuration.setBeginDate(begin.get());
      }
      if (end.isPresent()) {
        configuration.setEndDate(end.get());
      }
      configurationInPeriod = configuration;
    }

    if (epasParam.target.equals(Person.class)) {
      PersonConfiguration configuration = new PersonConfiguration();
      configuration.setPerson((Person) target);
      configuration.setFieldValue(fieldValue);
      configuration.setEpasParam(epasParam);
      configuration.setBeginDate(configuration.getPerson().getBeginDate());
      if (begin.isPresent()) {
        configuration.setBeginDate(begin.get());
      }
      if (end.isPresent()) {
        configuration.setEndDate(end.get());
      }
      configurationInPeriod = configuration;
    }

    //Controllo sul fatto di essere un parametro generale, annuale, o periodico.
    //Decidere se rimandare un errore al chiamante.
    Verify.verify(validateTimeType(epasParam, configurationInPeriod));

    periodManager.updatePeriods(configurationInPeriod, persist);
    return configurationInPeriod;
  }

  /**
   * Costruttore della configurazione di default se non esiste.<br> Verificare che venga chiamata
   * esclusivamente nel caso di nuovo enumerato !!! Di norma la configurazione di default andrebbe
   * creata tramite migrazione o al momento della creazione della sede.
   *
   * @param target sede o persona o ??
   * @param epasParam epasParam
   * @return il valore di default per tutto il periodo del target
   */
  IPropertyInPeriod buildDefault(IPropertiesInPeriodOwner target, EpasParam epasParam) {

    return build(epasParam, target, (String) epasParam.defaultValue,
        Optional.ofNullable(target.getBeginDate()), Optional.empty(), true, true);

  }

  /**
   * Valida il parametro di configurazione sulla base del suo tipo tempo.
   *
   * @param configuration parametro
   * @return esito.
   */
  public boolean validateTimeType(EpasParam epasParam, IPropertyInPeriod configuration) {

    if (epasParam.epasParamTimeType == EpasParamTimeType.GENERAL) {
      //il parametro deve coprire tutta la durata di un owner.
      return DateUtility.areIntervalsEquals(
          new DateInterval(configuration.getBeginDate(), configuration.calculatedEnd()),
          new DateInterval(configuration.getOwner().getBeginDate(),
              configuration.getOwner().calculatedEnd()));
    }

    //il parametro PERIODIC non ha vincoli, il parametro YEARLY lo costruisco opportunamente 
    // passando dal builder.
    return true;
  }
  
  /**
   * Aggiunge i nuovi epasParam quando vengono definiti (con il valore di default). Da chiamare al
   * momento della creazione dell'owner (person/office) ed al bootstrap di epas.
   *
   * @param owner sede o persona
   */
  public void updateConfigurations(Person owner) {

    for (EpasParam epasParam : EpasParam.values()) {
      // Casi uscita
      if (!epasParam.target.equals(Person.class)) {
        continue;
      }
      boolean toCreate = true;
      for (PersonConfiguration configuration : ((Person) owner).getPersonConfigurations()) {
        if (configuration.getEpasParam() == epasParam) {
          toCreate = false;
        }
      }
      if (toCreate) {
        buildDefault(owner, epasParam);
      }
      log.debug("Updated configurations for {}", owner);
    }
  }

  /**
   * Aggiunge i nuovi epasParam quando vengono definiti (con il valore di default). Da chiamare al
   * momento della creazione dell'owner (person/office) ed al bootstrap di epas.
   *
   * @param owner sede o persona
   */
  public void updateConfigurations(Office owner) {

    for (EpasParam epasParam : EpasParam.values()) {

      // Casi uscita
      if (!epasParam.target.equals(Office.class)) {
        continue;
      }

      // Casi da gestire
      boolean toCreate = true;
      for (Configuration configuration : ((Office) owner).getConfigurations()) {
        if (configuration.epasParam == epasParam) {
          toCreate = false;
        }
      }
      if (toCreate) {
        log.trace("Creazione del parametro di default {} per {}", epasParam, owner);
        buildDefault(owner, epasParam);
        log.trace("Creato parametro {} per {}", epasParam, owner);
      }
    }
    log.debug("Updated configurations for {}", owner);
  }
}
