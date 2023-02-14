/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.UsersRolesOfficesDao;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.manager.configurations.EpasParam.EpasParamValueType;
import it.cnr.iit.epas.manager.configurations.EpasParam.EpasParamValueType.IpList;
import it.cnr.iit.epas.models.Configuration;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.UsersRolesOffices;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manager per la gestione degli uffici.
 */
@Slf4j
@Component
public class OfficeManager {

  public static final String SKIP_IP_CHECK = "skip.ip.check";

  private final UsersRolesOfficesDao usersRolesOfficesDao;
  private final ConfigurationManager configurationManager;
  private final PeriodManager periodManager;
  private final OfficeDao officeDao;
  private final Provider<EntityManager> emp;

  /**
   * Default constructor.
   */
  @Inject
  public OfficeManager(
      UsersRolesOfficesDao usersRolesOfficesDao,
      ConfigurationManager configurationManager,
      OfficeDao officeDao, PeriodManager periodManager,
      Provider<EntityManager> emp) {
    this.usersRolesOfficesDao = usersRolesOfficesDao;
    this.configurationManager = configurationManager;
    this.periodManager = periodManager;
    this.officeDao = officeDao;
    this.emp = emp;
  }

  /**
   * True se il permesso sull'ufficio viene creato, false se è esistente.
   *
   * @return true Se il permesso su quell'ufficio viene creato, false se è già esistente.
   */
  public boolean setUro(User user, Office office, Role role) {

    Optional<UsersRolesOffices> uro = 
        usersRolesOfficesDao.getUsersRolesOffices(user, role, office);

    if (!uro.isPresent()) {

      UsersRolesOffices newUro = new UsersRolesOffices();
      newUro.setUser(user);
      newUro.setOffice(office);
      newUro.role = role;
      emp.get().persist(newUro);
      //newUro.save();
      return true;
    }

    return false;
  }


  /**
   * Le sedi che hanno la timbratura web abilitata ed almeno uno degli ip 
   * abilitato per timbrature.
   *
   * @param ipAddresses indirizzi ip da verificare
   * @return Set di uffici abilitati dagli indirizzi ip passati come parametro
   */
  public Set<Office> getOfficesWithAllowedIp(final List<String> ipAddresses) {

    Preconditions.checkNotNull(ipAddresses);
    Preconditions.checkState(!ipAddresses.isEmpty());

    //FIXME: da correggere per il passaggio allo spring boot
    //    if ("true".equals(Play.configuration.getProperty(SKIP_IP_CHECK))) {
    //      log.debug("Skipped IP check");
    //      return new HashSet<>(officeDao.getAllOffices());
    //    }

    List<Office> officesWebStampingEnabled = officeDao.getOfficesWebStampingEnabled();
    log.debug("officesWebStampingEnabled= {}", officesWebStampingEnabled);
    
    Set<Office> offices = Sets.newHashSet();
    List<Configuration> configurationWithType = configurationManager
        .configurationWithType(EpasParam.ADDRESSES_ALLOWED);

    for (Configuration configuration : configurationWithType) {
      IpList ipList = (IpList) EpasParamValueType.parseValue(EpasParamValueType.IP_LIST,
          (String) configuration.getValue());
      for (String ip : ipAddresses) {
        if (ipList != null && ipList.ipList.contains(ip) 
            && officesWebStampingEnabled.contains(configuration.office)) {
          offices.add(configuration.office);
        }
      }
    }
    return offices;
  }

  /**
   * Inserisce o aggiorna l'ufficio, creando o aggiornando le configurazioni
   * associate necessarie.
   */
  @Transactional
  public void save(Office office) {
    officeDao.save(office);

    // Configurazione iniziale di default ...
    configurationManager.updateConfigurations(office);

    periodManager.updatePropertiesInPeriodOwner(office);
  }
}