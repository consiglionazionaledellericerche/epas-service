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

package it.cnr.iit.epas.dto.v4.mapper;

import com.google.common.base.Verify;
import it.cnr.iit.epas.controller.exceptions.EntityNotFoundException;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dto.v4.ContractCreateDto;
import it.cnr.iit.epas.dto.v4.ContractUpdateDto;
import it.cnr.iit.epas.dto.v4.OfficeCreateDto;
import it.cnr.iit.epas.dto.v4.OfficeUpdateDto;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Office;
import javax.inject.Inject;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * Effettua la conversione e/o aggiornamento da DTO ad Entity.
 *
 * @author Cristian Lucchesi
 *
 */
@Component
public class EntityToDtoConverter {

  private final OfficeDao officeDao;
  private final ContractDao contractDao;
  private final DtoToEntityMapper mapper;

  @Inject
  EntityToDtoConverter(OfficeDao officeDao, ContractDao contractDao,
      DtoToEntityMapper mapper) {
    this.officeDao = officeDao;
    this.contractDao = contractDao;
    this.mapper = mapper;
  }

  /**
   * Aggiorna l'entity riferita dal DTO con i dati passati e la
   * restituite modificata.
   */
  public Office updateEntity(OfficeUpdateDto officeDto) {
    Verify.verifyNotNull(officeDto);
    val office = officeDao.byId(officeDto.getId())
        .orElseThrow(() -> new EntityNotFoundException(
            "Ufficio with id = " + officeDto.getId() + "not found"));
    mapper.update(office, officeDto);
    return office;
  }

  /**
   * Aggiorna l'entity riferita dal DTO con i dati passati e la
   * restituite modificata.
   */
  public Contract updateEntity(ContractUpdateDto contractDto) {
    Verify.verifyNotNull(contractDto);
    val contract = contractDao.byId(contractDto.getId())
        .orElseThrow(() -> new EntityNotFoundException(
            "Contract with id = " + contractDto.getId() + "not found"));
    mapper.update(contract, contractDto);
    return contract;
  }

  /**
   * Crea una nuova Entity Office a partire dai dati del DTO.
   */
  public Office createEntity(OfficeCreateDto officeDto) {
    Office office = new Office();
    mapper.update(office, officeDto);
    return office;
  }

  /**
   * Crea una nuova Entity Contract a partire dai dati del DTO.
   */
  public Contract createEntity(ContractCreateDto contractDto) {
    Contract contract = new Contract();
    mapper.create(contract, contractDto);
    return contract;
  }
}