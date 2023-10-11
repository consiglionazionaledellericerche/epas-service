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

package it.cnr.iit.epas.config;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.models.User;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * AuthenticationProvider per l'autenticazione di tipo Basic Auth.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

  @Autowired
  private UserDao userDao;

  @Transactional
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    log.debug("CustomAuthenticationProvider::authenticate -> authentication = {}", authentication);
    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    //FIXME: inserire un altro metodo per criptare le password, come bcrypt o sha256
    //in questa fase potremo crifrare la password ricevuta in chiaro con il nuovo algoritmo
    //e salvarla in locale
    User user = 
        userDao.getUserByUsernameAndPassword(
            username, Optional.of(hashing(password)));
    if (user == null) {
      throw new BadCredentialsException("invalid username and password for " + username);
    }

    String overridedUserName = 
        RequestContextHolder.getRequestAttributes().getAttribute(
          AuthenticationHeaderFilter.REQUEST_CURRENT_USER_ATTRIBUTE, 
          RequestAttributes.SCOPE_REQUEST).toString();

    log.info("overridedUserName = {}", overridedUserName);

    val overridedUser = userDao.byUsername(overridedUserName);

    List<GrantedAuthority> authorities = Lists.newArrayList();
    authorities.addAll(
        user.getRoles().stream()
        .map(r -> new SimpleGrantedAuthority(r.name()))
        .collect(Collectors.toList()));

    return new UsernamePasswordAuthenticationToken(username, password, authorities);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

  private String hashing(String password) {
    return Hashing.md5().hashString(password, Charsets.UTF_8).toString();
  }
  
}