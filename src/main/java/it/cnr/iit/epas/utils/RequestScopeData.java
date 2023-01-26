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

package it.cnr.iit.epas.utils;

import java.util.HashMap;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * RequestScopeData.
 *
 * @author Daniele Murgia
 */
@Getter
@Component
@RequestScope
public class RequestScopeData {

  // Quando nella richiesta http viene inserito questo custom header, il server si limiterà
  // a eseguire i controlli sui permessi, restituendo true o false al chiamante.
  public static final String REQUEST_PATH = "request_path";
  public static final String REQUEST_METHOD = "request_method";

  private final HashMap<String, Object> data = new HashMap<>();

}