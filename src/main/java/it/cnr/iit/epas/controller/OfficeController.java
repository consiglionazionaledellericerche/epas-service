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
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dto.v4.OfficeShowDto;
import it.cnr.iit.epas.dto.v4.mapper.OfficeShowMapper;
import it.cnr.iit.epas.models.Office;
import java.util.Optional;
import javax.inject.Inject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/v3/offices")
public class OfficeController {

  @Inject
  OfficeDao officeDao;
  @Inject
  OfficeShowMapper officeMapper;

  @GetMapping(ApiRoutes.SHOW)
  public ResponseEntity<OfficeShowDto> show(@PathVariable("id") Long id) {
    Optional<Office> entity = Optional.ofNullable(officeDao.getOfficeById(id));
    if (entity.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().body(officeMapper.convert(entity.get()));
  }

}
