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
package it.cnr.iit.epas.helpers.jpa;

import com.google.common.base.Function;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import it.cnr.iit.epas.models.base.BaseEntity;
import java.util.List;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Classe per le model query.
 *
 * @author Marco Andreini
 * @author Cristian Lucchesi
 */
@Component
public class ModelQuery {

  protected final JPQLQueryFactory queryFactory;
  protected final Provider<EntityManager> emp;

  public ModelQuery(Provider<EntityManager> emp) {
   this.emp = emp;
   this.queryFactory = new JPAQueryFactory(emp.get());
 }

 public JPQLQuery<?> createQuery() {
    return queryFactory.query();
  }

   /**
   * Il simpleresult che wrappa la lista o i listresults.
   *
   * @return a simplequery object, wrap list or listResults.
   */
  public static <T> SimpleResults<T> wrap(JPQLQuery<T> query, Expression<T> expression) {
    return new SimpleResults<T>(query, expression);
  }

  public boolean isPersistent(BaseEntity model) {
    return emp.get().contains(model);
  }

  public boolean isNotEmpty(BaseEntity model) {
    return model != null && isPersistent(model);
  }

  /**
   * La funzione di trasformazione da modello a id.
   *
   * @return la funzione di trasformazione da modello a proprio id.
   */
  public static <T extends BaseEntity> Function<T, Long> jpaId() {
    return input -> input.getId();
  }

  /**
   * Funzione di trasformazione da integer a modello.
   *
   * @return la funzione per ottenere un oggetto via em.find().
   */
  public <T extends BaseEntity> Function<Integer, T> jpaFind(final Class<T> model) {
    return id -> emp.get().find(model, id);
  }

  /**
   * Classe simpleResult.
   *
   * @author Marco Andreini
   */
  public static class SimpleResults<T> {

    private final Expression<T> expression;
    private final JPQLQuery<T> query;

    SimpleResults(JPQLQuery<T> query, Expression<T> expression) {
      this.query = query;
      this.expression = expression;
    }

    public long count() {
      return query.fetchCount();
    }

    public List<T> list() {
      return (List<T>) query.fetch();
    }

    public List<T> list(long limits) {
      return (List<T>) query.restrict(QueryModifiers.limit(limits)).fetch();
    }

  }
}
