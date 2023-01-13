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
package it.cnr.iit.epas.controller;

import it.cnr.iit.epas.dto.v4.PersonShowDto;
import it.cnr.iit.epas.dto.v4.mapper.PersonShowMapper;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.repo.PersonRepository;
import it.cnr.iit.epas.security.SecureUtils;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rest/v4/personinfo")
public class PersonInfo {

  private PersonRepository repo;
  private PersonShowMapper mapper;
  private SecureUtils securityUtils;
  
  PersonInfo(PersonRepository repo, PersonShowMapper mapper,
      SecureUtils securityUtils) {
    this.repo = repo;
    this.mapper = mapper;
    this.securityUtils = securityUtils;
  }

  @GetMapping("/")
  public ResponseEntity<PersonShowDto> show() {
    Optional<User> user = securityUtils.getCurrentUser();
    log.debug("UserInfo::show user = {}", user.orElse(null));
    if (!user.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    long personId = user.get().getId();
    Optional<Person> entity = repo.findById(personId);
    if (entity.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().body(mapper.convert(entity.get()));
  }
}
