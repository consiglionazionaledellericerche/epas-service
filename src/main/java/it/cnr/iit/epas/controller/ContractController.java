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

package it.cnr.iit.epas.controller;

import it.cnr.iit.epas.controller.utils.ApiRoutes;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dto.v4.ContractDto;
import it.cnr.iit.epas.dto.v4.mapper.ContractShowMapper;
import it.cnr.iit.epas.manager.ContractManager;
import it.cnr.iit.epas.security.NoCheck;
import java.time.LocalDate;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Metodi REST per la gestione delle informazioni sui contratti.
 */
@Transactional
@Slf4j
@RestController
@RequestMapping("/rest/v4/contracts")
public class ContractController {

  private final ContractDao contractDao;
  private final ContractManager contractManager;
  private final ContractShowMapper mapper;

  @Inject
  ContractController(ContractDao contractDao, ContractShowMapper mapper,
      ContractManager contractManager) {
    this.contractDao = contractDao;
    this.mapper = mapper;
    this.contractManager = contractManager;
  }
  
  @NoCheck
  @PutMapping("/endContract/" + ApiRoutes.ID_REGEX)
  public ResponseEntity<ContractDto> endContract(
      @PathVariable("id") Long id) {
    // , @RequestParam("endContract") LocalDate endContract) {
    log.debug("Chiamato metodo endContract con id = {}", id);
    val entity = contractDao.byId(id);
    if (entity == null) {
      return ResponseEntity.notFound().build();
    }
    entity.setEndContract(LocalDate.now());
    contractManager.properContractUpdate(entity, LocalDate.now().minusDays(1), false);
    return ResponseEntity.ok().body(mapper.convert(entity));
    
  }
  
}
