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

package it.cnr.iit.epas.manager.cache;

import com.google.common.base.Preconditions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import it.cnr.iit.epas.models.QStampModificationType;
import it.cnr.iit.epas.models.StampModificationType;
import it.cnr.iit.epas.models.StampModificationTypeCode;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import javax.inject.Inject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Manager per gli StampType.
 */

@Transactional
@Component
public class StampTypeManager {

  protected final ObjectProvider<EntityManager> emp;
  private final JPQLQueryFactory queryFactory;

  private static final String SMT_PREFIX = "smt";
  private final CacheManager cacheManager;

  /**
   * Default constructor per l'injection.
   */
  @Inject
  StampTypeManager(ObjectProvider<EntityManager> emp, CacheManager cacheManager) {
    this.emp = emp;
    this.queryFactory = new JPAQueryFactory(emp.getObject());
    this.cacheManager = cacheManager;
  }

  /**
   * Lo stampModificationType relativo al codice passato.
   *
   * @return lo stampModificationType relativo al codice code passato come parametro.
   */
  private StampModificationType getStampModificationTypeByCode(
      StampModificationTypeCode smtCode) {

    Preconditions.checkNotNull(smtCode);

    final QStampModificationType smt = QStampModificationType.stampModificationType;

    JPQLQuery<?> query = queryFactory.from(smt)
        .where(smt.code.eq(smtCode.getCode()));

    return (StampModificationType) query.fetchOne();
  }

  /**
   * Preleva dalla cache lo stamp modifcation type.
   */
  public StampModificationType getStampMofificationType(
      StampModificationTypeCode code) {

    Preconditions.checkNotNull(code);

    Cache cache = cacheManager.getCache(SMT_PREFIX);
    ValueWrapper valueWrapper = cache.get(code);
    StampModificationType value = null;

    if (valueWrapper == null) {
      value = getStampModificationTypeByCode(code);
      Preconditions.checkNotNull(value);
      cache.put(code, value);
    } else {
      value = (StampModificationType) valueWrapper.get();
    }
    emp.getObject().merge(value);
    return value;

  }

}
