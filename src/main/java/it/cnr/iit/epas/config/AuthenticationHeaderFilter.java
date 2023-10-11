package it.cnr.iit.epas.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro per prelevare informazioni aggiuntive sull'autenticazione
 * dell'utente via basic auth.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
public class AuthenticationHeaderFilter extends OncePerRequestFilter {

  private  static final String AUTHENTICATION_HEADER_NAME = "X-Authentication-Sudo";

  public static final String REQUEST_CURRENT_USER_ATTRIBUTE = "current-user";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader(AUTHENTICATION_HEADER_NAME);

    RequestContextHolder.getRequestAttributes()
        .setAttribute(REQUEST_CURRENT_USER_ATTRIBUTE, authHeader, RequestAttributes.SCOPE_REQUEST);

    log.info("AuthenticationHeaderFilter -> {}={}", AUTHENTICATION_HEADER_NAME, authHeader);
    filterChain.doFilter(request, response);
  }

}
