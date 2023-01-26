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

package it.cnr.iit.epas.dao;

import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.models.QShiftType;
import it.cnr.iit.epas.models.ShiftType;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class ShiftTypeDao extends DaoBase<ShiftType> {

  @Inject
  ShiftTypeDao(Provider<EntityManager> emp) {
    super(emp);
  }

  public List<ShiftType> findAll() {
    return getQueryFactory().selectFrom(QShiftType.shiftType).fetch();
  }
  
  public Optional<ShiftType> findById(Long id) {
    val sfhiftType = QShiftType.shiftType;
    return Optional.ofNullable(
        getQueryFactory().selectFrom(sfhiftType)
          .where(sfhiftType.id.eq(id)).fetchOne());
  }
}