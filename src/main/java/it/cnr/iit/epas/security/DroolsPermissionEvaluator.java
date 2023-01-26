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

package it.cnr.iit.epas.security;

import java.io.Serializable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DroolsPermissionEvaluator implements PermissionEvaluator {

  @Inject
  private SecurityRules rules;

  @Override
  public boolean hasPermission(Authentication authentication, Object target, Object permission) {
    if (permission != null) {
      return rules.check((String) permission, target);
    }

    return rules.check(target);
  }

  @Override
  public boolean hasPermission(Authentication authentication,
      Serializable targetId, String targetType, Object permission) {
    log.error("hasPermission(Authentication, Serializable, String, Object) called");
    throw new RuntimeException("ID based permission evaluation currently not supported.");
  }

}