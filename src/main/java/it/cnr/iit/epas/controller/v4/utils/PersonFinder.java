package it.cnr.iit.epas.controller.v4.utils;

import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.security.SecureUtils;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Utilità per cercare una persona tra quella autenticata
 * o ricercata attraverso parametri.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PersonFinder {

  private final SecureUtils securityUtils;
  private final PersonDao personDao;

  /**
   * Restituisce la Person associata ai parametri passati o 
   * all'utente autenticato (se la persona è presente ed autenticata).
   */
  public Optional<Person> getPerson(Optional<Long> id, Optional<String> fiscalCode) {
    if (id.isPresent() || fiscalCode.isPresent()) {
      return personDao.byIdOrFiscalCode(id, fiscalCode);
    }

    Optional<User> user = securityUtils.getCurrentUser();
    if (!user.isPresent()) {
      log.info("Non è presente nessun utente");
      return Optional.empty();
    }
    return Optional.ofNullable(user.get().getPerson());
  }
}
