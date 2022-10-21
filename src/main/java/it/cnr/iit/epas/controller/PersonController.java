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

import it.cnr.iit.epas.controller.utils.ApiRoutes;
import it.cnr.iit.epas.dto.PersonShowDto;
import it.cnr.iit.epas.dto.PersonShowTerseDto;
import it.cnr.iit.epas.dto.mapper.PersonShowMapper;
import it.cnr.iit.epas.manager.ConsistencyManager;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.repo.PersonRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/v3/people")
public class PersonController {

  private PersonRepository repo;
  private PersonShowMapper personMapper;
  private ConsistencyManager consistencyManager;
  
  
  @Inject
  public PersonController(PersonRepository personRepository, PersonShowMapper personMapper,
      ConsistencyManager consistencyManager) {
    this.repo = personRepository;
    this.personMapper = personMapper;
    this.consistencyManager = consistencyManager;
  }

  @GetMapping
  public ResponseEntity<List<PersonShowTerseDto>> byOffice(Long id, String code, String codeId, LocalDate atDate, Boolean terse) {
    return null;
  }
  
  @GetMapping(ApiRoutes.SHOW)
  public ResponseEntity<PersonShowDto> show(@PathVariable("id") Long id) {
    Optional<Person> entity = repo.findById(id);
    if (entity.isEmpty()) {
      ResponseEntity.notFound();
    }

    return ResponseEntity.ok().body(personMapper.convert(entity.get()));
  }
  
  @PatchMapping(ApiRoutes.PATCH)
  @Transactional
  public ResponseEntity<PersonShowDto> update(@PathVariable("id") Long id) {
    Optional<Person> entity = repo.findById(id);
    if (entity.isEmpty()) {
      ResponseEntity.notFound();
    }
    consistencyManager.updatePersonSituation(id, LocalDate.now().minusDays(10));
    return ResponseEntity.ok().body(personMapper.convert(entity.get()));

  }
}

