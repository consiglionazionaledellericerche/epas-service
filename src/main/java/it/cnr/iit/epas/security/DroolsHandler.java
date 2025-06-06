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

package it.cnr.iit.epas.security;

import it.cnr.iit.epas.utils.RequestScopeData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import javax.inject.Inject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * DroolsHandler.
 *
 * @author Daniele Murgia
 * @author Cristian Lucchesi
 * @since 27/04/18
 */
@Component
public class DroolsHandler implements HandlerInterceptor {

  private static final String PACKAGE_REST_TO_HANDLE = "it.cnr.iit.epas";

  @Inject
  private SecurityRules rules;
  @Inject
  private RequestScopeData requestScope;

  @Transactional
  @Override
  public boolean preHandle(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object handler) {

    final String rawPath = (String) httpServletRequest
        .getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

    requestScope.getData().put(RequestScopeData.REQUEST_PATH, rawPath);
    requestScope.getData().put(RequestScopeData.REQUEST_METHOD, httpServletRequest.getMethod());

    // Questo if serve per risolvere un bug presente su spring boot 2.0.6 che genera un errore nel
    // cast dell'oggetto handler nei casi di chiamate su path non esistenti
    if (handler instanceof HandlerMethod) {
      HandlerMethod hm = (HandlerMethod) handler;
      Method method = hm.getMethod();
      Class<?> clazz = method.getDeclaringClass();
;
      //Chiamata delle drools su tutti i metodi dei RestController non annotati 
      //con la Preauthorize o NoCheck e che non contenuti nel package principale
      //di questo progetto. Si evitano così per esempio problemi con i controller
      //Rest di springdoc
      if (clazz.isAnnotationPresent(RestController.class)
          && clazz.getCanonicalName().startsWith(PACKAGE_REST_TO_HANDLE)
          && !clazz.isAnnotationPresent(NoCheck.class)
          && !method.isAnnotationPresent(PreAuthorize.class)
          && !method.isAnnotationPresent(NoCheck.class)) {
        rules.checkifPermitted();
      }
    }

    return true;
  }

  @Override
  public void postHandle(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {
  }

  @Override
  public void afterCompletion(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object o, Exception e) {

  }
}