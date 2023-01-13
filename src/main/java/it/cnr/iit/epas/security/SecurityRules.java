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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.UsersRolesOffices;
import it.cnr.iit.epas.models.enumerate.AccountRole;
import it.cnr.iit.epas.utils.RequestScopeData;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Component;

/**
 * SecurityRules.
 *
 * @author Daniele Murgia
 */
@Component
@Slf4j
public class SecurityRules {

  private static final String CURRENT_USER = "currentUser";
  private static final String CURRENT_ROLES = "userRoles";
  private static final String CURRENT_ROLES_OFFICES = "userRolesOffices";


  private final KieBase kieBase;
  private final SecureUtils secureUtils;
  private final RequestScopeData requestScope;

  private static final Pattern PATH_PARAMS_PATTERN = Pattern.compile(":.*?}");

  @Inject
  public SecurityRules(KieBase kieBase, SecureUtils secureUtils, RequestScopeData requestScope) {
    this.kieBase = kieBase;
    this.secureUtils = secureUtils;
    this.requestScope = requestScope;
  }

  public void checkifPermitted() {
    checkifPermitted(null);
  }

  public void checkifPermitted(Object target) {
    if (!check(target)) {
      throw new AccessDeniedException(SpringSecurityMessageSource.getAccessor().getMessage(
          "AbstractAccessDecisionManager.accessDenied", "Access is denied"));
    }
  }

  private boolean check(String method, String permission, Object target) {
    // Ripuliamo la stringa dalle eventuali espressioni regolari derivanti dai path
    // Es. /v1/ruoloutente/{id:^\d+$} -> /v1/ruoloutente/{id}
    final String normalized = PATH_PARAMS_PATTERN.matcher(permission).replaceAll("}");
    final PermissionCheck check = new PermissionCheck(target, normalized, method);
    fireRules(check);
    return check.isPermitted();
  }

  public boolean check(Object target) {
    final String permission = (String) requestScope.getData().get(RequestScopeData.REQUEST_PATH);
    final String method = (String) requestScope.getData().get(RequestScopeData.REQUEST_METHOD);
    return check(method, permission, target);
  }

  public boolean check(String permission, Object target) {
    return check(null, permission, target);
  }

  public boolean check() {
    return check(null);
  }

  private void fireRules(PermissionCheck check) {

    final User user = secureUtils.getCurrentUser().get();
    final List<UsersRolesOffices> userRolesOffices = user.getUsersRolesOffices();
    final Set<AccountRole> userRoles = user.getRoles();

    log.debug("hasPermission({}, {}, {}) called", user.getUsername(), check.getTarget(),
        check.getPermission());

    final List<Command<?>> commands = Lists.newArrayList();

    StatelessKieSession session = kieBase.newStatelessKieSession();
    session.setGlobal(CURRENT_USER, user);
    session.setGlobal(CURRENT_ROLES, userRoles);
    session.setGlobal(CURRENT_ROLES_OFFICES, userRolesOffices);
    session.addEventListener(new AgendaLogger());

    commands.add(CommandFactory.newInsert(check.getTarget()));
    commands.add(CommandFactory.newInsert(check));

    log.debug("session is {}", session);
    session.execute(CommandFactory.newBatchExecution(commands));

  }

  private static class AgendaLogger extends DefaultAgendaEventListener {

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
      log.info("RULE {} {}", event.getMatch().getRule().getName(), event.getMatch().getObjects());
    }
  }
}