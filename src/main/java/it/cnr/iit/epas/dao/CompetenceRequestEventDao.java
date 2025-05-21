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
import it.cnr.iit.epas.models.flows.CompetenceRequestEvent;
import it.cnr.iit.epas.models.flows.QCompetenceRequestEvent;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * DAO per i CompetenceRequestEvent.
 */
@Component
public class CompetenceRequestEventDao extends DaoBase<CompetenceRequestEvent> {

  @Inject
  CompetenceRequestEventDao(ObjectProvider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Ritorna il CompetenceRequestEvent identificato dall'id passato come parametro.
   */
  public Optional<CompetenceRequestEvent> findById(Long id) {
    final QCompetenceRequestEvent competenceRequestEvent = 
        QCompetenceRequestEvent.competenceRequestEvent;
    return 
        Optional.of(
            getQueryFactory()
              .selectFrom(competenceRequestEvent)
              .where(competenceRequestEvent.id.eq(id)).fetchOne());
  }

}