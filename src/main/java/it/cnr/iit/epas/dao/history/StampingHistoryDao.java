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

package it.cnr.iit.epas.dao.history;

import com.google.common.collect.FluentIterable;
import it.cnr.iit.epas.models.Stamping;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;


/**
 * Dao sullo storico delle timbrature.
 *
 * @author Marco Andreini
 */
@RequiredArgsConstructor
@Component
public class StampingHistoryDao {

  private final ObjectProvider<EntityManager> emp;

  /**
   * La lista delle revisioni sulla timbratura con id passato.
   *
   * @param stampingId l'identificativo della timbratura
   * @return la lista delle revisioni sulla timbratura con id passato.
   */
  @SuppressWarnings("unchecked")
  public List<HistoryValue<Stamping>> stampings(long stampingId) {
    val auditReader = AuditReaderFactory.get(emp.getObject());
    final AuditQuery query = auditReader.createQuery()
            .forRevisionsOfEntity(Stamping.class, false, true)
            .add(AuditEntity.id().eq(stampingId))
            .addOrder(AuditEntity.revisionNumber().asc());

    return FluentIterable.from(query.getResultList())
            .transform(HistoryValue.fromTuple(Stamping.class))
            .toList();
  }

}