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
package it.cnr.iit.epas.models.base;

import it.cnr.iit.epas.models.User;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionListener;
import org.springframework.stereotype.Component;

/**
 * Revision listener che aggiunge le informazioni su owner e ipaddress che hanno modificato
 * la revisione.
 *
 * @author Marco Andreini
 */
@Slf4j
@Component
public class ExtendedRevisionListener implements RevisionListener {

  //private final static String REMOTE_ADDRESS = "request.remoteAddress";
  @Inject
  static Provider<Optional<User>> user;
  
  //@Inject
  //@Named(REMOTE_ADDRESS)
  //XXX: da riattivare prima del passaggio a Spring boot
  static Provider<String> ipaddress;

  @Override
  public void newRevision(Object revisionEntity) {
    try {
      final Revision revision = (Revision) revisionEntity;
      if (user.get().isPresent()) {
        revision.setOwner(user.get().orElse(null));
      } else {
        log.warn("unkown owner or user on revision {}", revision);
      }
      if (ipaddress.get() != null) {
        revision.setIpaddress(ipaddress.get());
      } else {
        log.warn("unkown owner or user on revision {}", revision);
      }
    } catch (NullPointerException ignored) {
      log.warn("NPE", ignored);
    }
  }
}
