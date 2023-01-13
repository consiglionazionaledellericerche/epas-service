package it.cnr.iit.epas.security;

import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.models.User;
import java.util.Optional;
import javax.inject.Inject;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecureUtils {

  private UserDao userDao;
  
  final String AUTHORITIES = "authorities";

  @Inject
  SecureUtils(UserDao userDao) {
    this.userDao = userDao;
  }

  private Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  public Optional<User> getCurrentUser() {
    var authentication = getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
      val principal = (Jwt) authentication.getPrincipal();
      val username = principal.getClaimAsString("preferred_username");
      User user = userDao.byUsername(username);
      if (user == null) {
        log.warn("username {} not found in this service", username);
      }
      return Optional.ofNullable(user);
    }
    return Optional.empty();
  }

}