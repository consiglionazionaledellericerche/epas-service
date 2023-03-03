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

package it.cnr.iit.epas.dto.v4;

import it.cnr.iit.epas.config.SecurityProperties.UserAuthIdentifier;
import lombok.Data;

/**
 * DTO con le proprietà relative alla sicurezza esportabili via REST.
 */
@Data
public class SecurityPropertiesDto {

  private Oauth2Dto oauth2;

  /**
   * DTO per le configurazioni OAuth2.
   */
  @Data
  public static class Oauth2Dto {
    boolean resourceserverEnabled = false;
    
    String jwtField = "preferred_username";

    UserAuthIdentifier userAuthIdentifier = UserAuthIdentifier.subjectId;
  }
}
