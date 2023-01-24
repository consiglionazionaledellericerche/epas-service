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

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Seam like check.
 */
@RequiredArgsConstructor
@Getter
public class PermissionCheck {

  private final Object target;
  private final String permission;
  private final String httpMethod;
  private boolean granted;
  private boolean revoked;

  public void grant() {
    granted = true;
  }

  public void revoke() {
    revoked = true;
  }

  public boolean isPermitted() {
    return granted && !revoked;
  }

  public boolean toCheck() {
    return !granted && !revoked;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("method", httpMethod)
        .add("action", permission)
        .add("target", target)
        .addValue(isPermitted() ? "GRANTED" : "DENIED").toString();
  }
}