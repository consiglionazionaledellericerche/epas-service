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

package it.cnr.iit.epas.models.base;

import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.security.SecureUtils;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Revision listener che aggiunge le informazioni su owner e ipaddress che hanno modificato
 * la revisione.
 *
 * @author Cristian Lucchesi
 */
@Slf4j
public class ExtendedRevisionListener implements RevisionListener {

  private final Provider<SecureUtils> secureUtils;

  @Inject
  public ExtendedRevisionListener(Provider<SecureUtils> securityUtils) {
    this.secureUtils = securityUtils;
  }

  @Override
  public void newRevision(Object revisionEntity) {
    try {
      final Revision revision = (Revision) revisionEntity;

      //Questo serve per prelevare l'utente corrente dal SecurityContext corrente,
      //che nel caso di metodi @Async è diverso da quello del thread della chiamata 
      //http originale.
      if (getUserFromCurrentSecurityContext() == null) {
        log.warn("unkown owner or user on revision {}", revision);
      } else {
        revision.setOwner(getUserFromCurrentSecurityContext().get());
      }


      if (getRemoteAddr().isPresent()) {
        revision.setIpaddress(getRemoteAddr().get());
      } else {
        log.info("unkown ip address on revision {}", revision);
      }

    } catch (NullPointerException ignored) {
      log.warn("NPE", ignored);
    }
  }

  private Optional<User> getUserFromCurrentSecurityContext() {
    val authentication = SecurityContextHolder.getContext().getAuthentication();
    return secureUtils.get().getUserFromAuthentication(authentication);
  }

  private Optional<String> getRemoteAddr() {
    RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
    if (attribs instanceof ServletRequestAttributes) {
      return Optional.of(((ServletRequestAttributes) attribs).getRequest().getRemoteAddr());
    }
    return Optional.empty();
  }

}