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
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.Contract;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.ObjectProvider;


/**
 * DAO per i ContractHistory.
 */
@RequiredArgsConstructor
public class ContractHistoryDao {

  private final ObjectProvider<AuditReader> auditReader;

  /**
   * Metodo di storico per le modifiche sulla competenza.
   *
   * @param competenceId l'id della competenza di cui recuperare lo storico
   * @return la lista di modifiche per la competenza in oggetto.
   */
  @SuppressWarnings("unchecked")
  public List<HistoryValue<Competence>> competences(long competenceId) {

    final AuditQuery query = auditReader.getObject().createQuery()
        .forRevisionsOfEntity(Competence.class, false, true)
        .add(AuditEntity.id().eq(competenceId))
        .addOrder(AuditEntity.revisionNumber().asc());

    return FluentIterable.from(query.getResultList())
        .transform(HistoryValue.fromTuple(Competence.class))
        .toList();

  }

  /**
   * Metodo di storico per le modifiche sul contratto.
   *
   * @param contractId l'id del contratto di cui recuperare lo storico
   * @return la lista di modifiche per il contratto in oggetto.
   */
  @SuppressWarnings("unchecked")
  public List<HistoryValue<Contract>> contracts(long contractId) {
    
    final AuditQuery query = auditReader.getObject().createQuery()
        .forRevisionsOfEntity(Contract.class, false, true)
        .add(AuditEntity.id().eq(contractId))
        .addOrder(AuditEntity.revisionNumber().asc());
    
    return FluentIterable.from(query.getResultList())
        .transform(HistoryValue.fromTuple(Contract.class))
        .toList();
  }
  
  /**
   * Metodo di storico sull'ultima modifica al contratto.
   *
   * @param contractId l'id del contratto di cui recuperare lo storico
   * @return la lista contenente un solo elemento relativo alle modifiche al contratto
   *     in oggetto.
   */
  @SuppressWarnings("unchecked")
  public List<HistoryValue<Contract>> lastRevision(long contractId) {
    
    final AuditQuery query = auditReader.getObject().createQuery()
        .forRevisionsOfEntity(Contract.class, false, true)
        .add(AuditEntity.id().eq(contractId))
        .addOrder(AuditEntity.revisionNumber().desc())
        .setMaxResults(1);
    
    return FluentIterable.from(query.getResultList())
        .transform(HistoryValue.fromTuple(Contract.class)).toList();
  }

}
