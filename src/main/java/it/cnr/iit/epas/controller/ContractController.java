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
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  
  @Transactional
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
