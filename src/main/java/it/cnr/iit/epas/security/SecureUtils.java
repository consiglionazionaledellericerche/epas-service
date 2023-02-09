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

package it.cnr.iit.epas.security;

import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.models.User;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Supporto per prelevare l'utente corrente.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@Component
public class SecureUtils {

  private final UserDao userDao;

  @Inject
  SecureUtils(UserDao userDao) {
    this.userDao = userDao;
  }

  /**
   * L'utente corrente prelevato tramite le informazioni presenti nel token Jwt.
   */
  public Optional<User> getCurrentUser() {
    return getUserFromAuthentication(SecurityContextHolder.getContext().getAuthentication());
  }

  /**
   * Preleva le informazioni dell'utente corrente dal token JWT se presente,
   * facendo il match con l'utente presente nel db del servizio.
   */
  public Optional<User> getUserFromAuthentication(Authentication authentication) {
    User user = null;
    if (authentication != null) {
      if (authentication.getPrincipal() instanceof Jwt) {
        val principal = (Jwt) authentication.getPrincipal();
        val username = principal.getClaimAsString("preferred_username");
        user = userDao.byUsername(username);
        if (user != null) {
          log.info("Autenticato utente {} tramite JWT", user.getUsername());
        }
      } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
        user = userDao.byUsername(authentication.getPrincipal().toString());
        if (user != null) {
          log.info("Autenticato utente {} tramite Basic Auth", user.getUsername());
        } 
      } else if (authentication instanceof AnonymousAuthenticationToken) {
        log.info("Nessun autenticazione, utente anononimo");
      } else {
        log.warn("Autenticazione avvenuta ma tipo di Authentication non supportato -> "
            + "authentication = {}", authentication);
      }
    } else {
      log.warn("Impossibile prelevare l'utente corrente, authentication non disponibile.");
    }

    return Optional.ofNullable(user);

  }

}