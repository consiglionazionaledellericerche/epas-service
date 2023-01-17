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

package it.cnr.iit.epas.manager;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.UserDao;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.UsersRolesOffices;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

/**
 * Manager per user.
 *
 * @author Daniele Murgia
 * @since 13/10/15
 */
@Slf4j
@Component
public class UserManager {

  private final UserDao userDao;
  private final Provider<EntityManager> emp;

  /**
   * Construttore per l'injection.
   */
  @Inject
  public UserManager(UserDao userDao, Provider<EntityManager> emp) {
    this.userDao = userDao;
    this.emp = emp;
  }

  /**
   * Return generated token for the recovery password procedure.
   *
   * @param person person for which to generate the token.
   */
  public void generateRecoveryToken(Person person) {

    Preconditions.checkState(person != null);
    //Preconditions.checkState(person != null && person.isPersistent());
    
    //generate random token
    SecureRandom random = new SecureRandom();

    person.getUser().setRecoveryToken(new BigInteger(130, random).toString(32));
    person.getUser().setExpireRecoveryToken(LocalDate.now());
    emp.get().persist(person.getUser());
    //person.getUser().save();
  }

  /**
   * Return generated username using pattern 'name.surname'.
   *
   * @param name    Name
   * @param surname Surname
   * @return generated Username
   */
  public String generateUserName(final String name, final String surname) {

    final String username;
    final String standardUsername = CharMatcher.whitespace().removeFrom(
        Joiner.on(".").skipNulls().join(name.replaceAll("\\W", ""), surname.replaceAll("\\W", ""))
            .toLowerCase());

    List<String> overlapUsers = userDao.containsUsername(standardUsername);
    //  Caso standard
    if (overlapUsers.isEmpty()) {
      username = standardUsername;
    } else {

      //  Caso di omonimia

      //  Cerco tutti i numeri della sequenza autogenerata per i casi di omonimia
      List<Integer> sequence = Lists.newArrayList();
      for (String user : overlapUsers) {
        String number = user.replaceAll("\\D+", "");
        if (!Strings.isNullOrEmpty(number)) {
          sequence.add(Integer.parseInt(number));
        }
      }
      //  Solo un omonimo
      if (sequence.isEmpty()) {
        username = standardUsername + 1;
      } else {
        //  Più di un omonimo
        username = standardUsername + (Collections.max(sequence) + 1);
      }
    }
    return username;
  }

  /**
   * Crea l'utente.
   *
   * @param person la persona per cui creare l'utente
   * @return l'utente creato.
   */
  public User createUser(final Person person) {

    User user = new User();

    user.setUsername(generateUserName(person.getName(), person.getSurname()));

    SecureRandom random = new SecureRandom();
    user.setPassword(hexMD5(new BigInteger(130, random).toString(32)));

    emp.get().persist(user);
    //user.save();

    person.setUser(user);

    log.info("Creato nuovo user per {}: username = {}", person.fullName(), user.getUsername());

    return user;
  }

  /**
   * funzione che salva l'utente e genere i ruoli sugli uffici.
   *
   * @param user    l'user da salvare
   * @param offices la lista degli uffici
   * @param roles   la lista dei ruoli
   * @param enable  se deve essere disabilitato
   */
  public void saveUser(User user, Set<Office> offices, Set<Role> roles, boolean enable) {
    user.setPassword(hexMD5(user.getPassword()));
    if (enable) {
      user.setDisabled(false);
      user.setExpireDate(null);
    }
    emp.get().merge(user);
    //user.save();
    for (Role role : roles) {
      for (Office office : offices) {
        UsersRolesOffices uro = new UsersRolesOffices();
        uro.user = user;
        uro.office = office;
        uro.role = role;
        emp.get().persist(uro);
        //uro.save();
      }
    }
  }

  /**
   * Build an hexadecimal MD5 hash for a String
   * Metodo copiato dal play 1.5!
   *
   * @param value The String to hash
   * @return An hexadecimal Hash
   */
  static String hexMD5(String value) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.reset();
      messageDigest.update(value.getBytes("utf-8"));
      byte[] digest = messageDigest.digest();
      return byteToHexString(digest);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Write a byte array as hexadecimal String.
   *
   * @param bytes byte array
   * @return The hexadecimal String
   */
  static String byteToHexString(byte[] bytes) {
    return String.valueOf(Hex.encodeHex(bytes));
  }
}