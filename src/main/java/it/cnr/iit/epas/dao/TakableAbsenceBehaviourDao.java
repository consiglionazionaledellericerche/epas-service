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
import it.cnr.iit.epas.models.absences.QTakableAbsenceBehaviour;
import it.cnr.iit.epas.models.absences.TakableAbsenceBehaviour;
import jakarta.persistence.EntityManager;
import java.util.List;
import javax.inject.Inject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Dao ler TakableAbsenceBehaviour.
 *
 * @author Cristian Lucchesi
 *
 */
@Component
public class TakableAbsenceBehaviourDao extends DaoBase<TakableAbsenceBehaviour> {

  @Inject
  TakableAbsenceBehaviourDao(ObjectProvider<EntityManager> emp) {
    super(emp);
  }

  public List<TakableAbsenceBehaviour> findAll() {
    return getQueryFactory().selectFrom(QTakableAbsenceBehaviour.takableAbsenceBehaviour).fetch();
  }
}