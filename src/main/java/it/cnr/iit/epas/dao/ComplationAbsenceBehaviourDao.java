/*
 * Copyright (C) 2025  Consiglio Nazionale delle Ricerche
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
import it.cnr.iit.epas.models.absences.ComplationAbsenceBehaviour;
import it.cnr.iit.epas.models.absences.QComplationAbsenceBehaviour;
import jakarta.persistence.EntityManager;
import java.util.List;
import javax.inject.Inject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * DAO per i ComplationAbsenceBehaviour.
 */
@Component
public class ComplationAbsenceBehaviourDao extends DaoBase<ComplationAbsenceBehaviour> {

  @Inject
  ComplationAbsenceBehaviourDao(ObjectProvider<EntityManager> emp) {
    super(emp);
  }

  public List<ComplationAbsenceBehaviour> findAll() {
    return getQueryFactory()
        .selectFrom(QComplationAbsenceBehaviour.complationAbsenceBehaviour).fetch();
  }
}