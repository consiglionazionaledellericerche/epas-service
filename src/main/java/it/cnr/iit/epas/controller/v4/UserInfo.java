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

package it.cnr.iit.epas.controller.v4;

import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dto.v4.UserShowDto;
import it.cnr.iit.epas.dto.v4.mapper.UserShowMapper;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.repo.UserRepository;
import it.cnr.iit.epas.security.SecureUtils;
import java.util.Optional;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller con i metodi REST relativi alla visualizzazione e gestione dei dati personali
 * di un utente del sistema.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/userinfo")
public class UserInfo {

  private UserRepository repo;
  private UserShowMapper mapper;
  private SecureUtils securityUtils;
  
  /**
   * Costruttore di default per l'injection.
   */
  @Inject
  UserInfo(UserRepository repo, UserShowMapper personMapper,
      SecureUtils securityUtils) {
    this.repo = repo;
    this.mapper = personMapper;
    this.securityUtils = securityUtils;
  }

  @GetMapping
  ResponseEntity<UserShowDto> show() {
    Optional<User> user = securityUtils.getCurrentUser();
    log.debug("UserInfo::show user = {}", user.orElse(null));
    if (!user.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    long personId = user.get().getId();
    val entity = repo.findById(personId)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    return ResponseEntity.ok().body(mapper.convert(entity));
  }
}