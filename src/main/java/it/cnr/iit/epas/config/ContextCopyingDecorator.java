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

package it.cnr.iit.epas.config;

import it.cnr.iit.epas.utils.RequestScopeData;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Decoratore dei Task @Async che grazie a questo supporto
 * hanno a disposizione il RequestContext ed il SecurityContext
 * anche nel thread (separato) in cui vengono eseguiti.
 */
@Component
class ContextCopyingDecorator implements TaskDecorator {

  @Autowired
  RequestScopeData requestScopeData;

  @Nonnull
  @Override
  public Runnable decorate(@Nonnull Runnable runnable) {
    RequestAttributes context =
        RequestContextHolder.currentRequestAttributes();
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return () -> {
      try {
        RequestContextHolder.setRequestAttributes(context);
        SecurityContextHolder.setContext(securityContext);
        runnable.run();
      } finally {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
      }
    };
  }
}