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

package it.cnr.iit.epas.dao.history;

import com.google.common.base.Verify;
import com.google.common.collect.FluentIterable;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.base.QRevision;
import it.cnr.iit.epas.models.base.Revision;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Component;

/**
 * DAO per le interrogazioni sullo storico.
 */
@Slf4j
@Component
public class HistoricalDao {

  private final Provider<EntityManager> emp;
  private final JPQLQueryFactory queryFactory;

  @Inject
  HistoricalDao(Provider<EntityManager> emp) {
    this.emp = emp;
    this.queryFactory = new JPAQueryFactory(emp.get());

  }

  /**
   * Ritorna l'oggetto revisione.
   *
   * @param id the id to search.
   * @return the Revision object.
   */
  public Revision getRevision(int id) {
    return Verify.verifyNotNull(queryFactory.selectFrom(QRevision.revision)
        .where(QRevision.revision.id.eq(id))
        .fetchOne());
  }


  /**
   * Ritorna l'istanza di un'entità ad una specifica revisione.
   *
   * @param cls Entity Class to search.
   * @param id the entity primary key
   * @param revisionId the revision id
   * @return the entity instance at the specified revision.
   * @deprecated non utilizzare fino a quando non è stato verificato se
   *     il comportamento è uguale a quelle presente in ePAS per lo stsso metodo
   */
  @Deprecated
  public <T extends BaseEntity> T valueAtRevision(Class<T> cls, long id, int revisionId) {
    val auditReader = AuditReaderFactory.get(emp.get());

    final T current = Verify.verifyNotNull(emp.get().find(cls, id));
    final T history = cls.cast(auditReader.createQuery()
        .forEntitiesAtRevision(cls, revisionId)
        .add(AuditEntity.id().eq(current.getId()))
        .getSingleResult());

    //final LocalDateTime date = getRevision(revisionId).getRevisionDate();
    //return HistoryViews.historicalViewOf(cls, current, history, date);

    //XXX: verificare se è compatibile con la precedente versione presente in ePAS
    return history;

  }

  /**
   * Ritorna l'ultima revisione di una specifica entità.
   *
   * @param cls Entity Class to search
   * @param id the entity primary key
   * @return last revision of specified entity.
   */
  public HistoryValue<? extends BaseEntity> lastRevisionOf(
      Class<? extends BaseEntity> cls, long id) {
    List<HistoryValue<?>> lastRevisions = lastRevisionsOf(cls, id);
    if (lastRevisions.isEmpty()) {
      return null;
    }
    return lastRevisions.get(0);
  }

  /**
   * La lista di revisioni della specifica entità.
   *
   * @param cls Entity Class to search
   * @param id the entity primary key
   * @return List of revisions for the specified entity instance.
   */
  @SuppressWarnings("unchecked")
  public List<HistoryValue<?>> lastRevisionsOf(Class<? extends BaseEntity> cls, long id) {
    val auditReader = AuditReaderFactory.get(emp.get());
    return FluentIterable.from(auditReader.createQuery()
        .forRevisionsOfEntity(cls, false, true)
        .add(AuditEntity.id().eq(id))
        .addOrder(AuditEntity.revisionNumber().desc())
        .setMaxResults(100)
        .getResultList()).transform(HistoryValue.fromTuple(cls)).toList();
  }


  /**
   * La versione precedente.
   *
   * @param cls Entity Class to search
   * @param id the entity primary key
   * @return la versione precedente del istanza individuata da cls e id.
   */
  public <T extends BaseEntity> T previousRevisionOf(Class<T> cls, long id) {
    val auditReader = AuditReaderFactory.get(emp.get());
    final Integer currentRevision = (Integer) auditReader.createQuery()
        .forRevisionsOfEntity(cls, false, true)
        .add(AuditEntity.id().eq(id))
        .addProjection(AuditEntity.revisionNumber().max())
        .getSingleResult();
    final Integer previousRevision = (Integer) auditReader.createQuery()
        .forRevisionsOfEntity(cls, false, true)
        .addProjection(AuditEntity.revisionNumber().max())
        .add(AuditEntity.id().eq(id))
        .add(AuditEntity.revisionNumber().lt(currentRevision))
        .getSingleResult();
    log.debug("current-revision {} of ({}:{}), previous-revision: {}",
        currentRevision, cls, id, previousRevision);
    return valueAtRevision(cls, id, previousRevision);
  }

  /**
   * L'utente che ha effettuato l'ultima revisione dell'entity passata.
   */
  public User lastRevisionOperator(BaseEntity entity) {
    return lastRevisionOf(entity.getClass(), entity.getId()).revision.owner;
  }

}