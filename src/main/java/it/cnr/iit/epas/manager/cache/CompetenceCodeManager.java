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
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.QCompetenceCode;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Manager per i CompetenceCode.
 */
@Component
public class CompetenceCodeManager {

  protected final JPQLQueryFactory queryFactory;
  private static final String COMPETENCE_PREFIX = "comp";
  private final CacheManager cacheManager;
  private final Provider<EntityManager> emp;
  
  @Inject
  CompetenceCodeManager(Provider<EntityManager> emp, CacheManager cacheManager) {
    this.emp = emp;
    this.queryFactory = new JPAQueryFactory(emp.get());
    this.cacheManager = cacheManager;
  }

  /**
   * Preleva dalla cache il competence code.
   */
  public CompetenceCode getCompetenceCode(
      String code) {

    Preconditions.checkNotNull(code);

   // String key = COMPETENCE_PREFIX + code;
    //FIXME: da verificare se e come funziona con spring boot
    Cache cache = cacheManager.getCache(COMPETENCE_PREFIX);
    CompetenceCode value = (CompetenceCode) cache.get(code);

    if (value == null) {

      value = getCompetenceCodeByCode(code);

      Preconditions.checkNotNull(value);

      cache.put(code, value);
    }
    emp.get().merge(value);
    //value.merge();
    return value;

  }

  /**
   * Il codice di competenza relativo al codice passato.
   *
   * @return il CompetenceCode relativo al codice code passato come parametro.
   */
  private CompetenceCode getCompetenceCodeByCode(
      String code) {

    Preconditions.checkNotNull(code);

    final QCompetenceCode compCode = QCompetenceCode.competenceCode;

    JPQLQuery<?> query = queryFactory.from(compCode)
        .where(compCode.code.eq(code));

    return (CompetenceCode) query.fetchOne();
  }

}
