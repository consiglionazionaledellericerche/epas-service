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
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Revision listener che aggiunge le informazioni su owner e ipaddress che hanno modificato
 * la revisione.
 *
 * @author Marco Andreini
 * @author Cristian Lucchesi
 */
@Slf4j
@Component
@RequestScope
public class ExtendedRevisionListener implements RevisionListener {

  private final Provider<SecureUtils> secureUtils;

  @Inject
  public ExtendedRevisionListener(Provider<SecureUtils> secureUtils) {
    this.secureUtils = secureUtils;
  }

  @Override
  public void newRevision(Object revisionEntity) {
    try {
      final Revision revision = (Revision) revisionEntity;
      final Optional<User> user = secureUtils.get().getCurrentUser();
      if (user.isPresent()) {
        revision.setOwner(user.orElse(null));
      } else {
        log.warn("unkown owner or user on revision {}", revision);
      }
      revision.setIpaddress(getRemoteAddress());
    } catch (NullPointerException ignored) {
      log.warn("NPE", ignored);
    }
  }

  private String getRemoteAddress() {
    RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
    log.info("getRequestAttributes = {}", attribs);
    if (attribs instanceof NativeWebRequest) {
      HttpServletRequest request = (HttpServletRequest) ((NativeWebRequest) attribs).getNativeRequest();
      return request.getRemoteAddr();
    }
    return null;
  }
}