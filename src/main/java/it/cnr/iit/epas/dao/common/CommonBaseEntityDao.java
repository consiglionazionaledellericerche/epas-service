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

package it.cnr.iit.epas.dao.common;

import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.base.QBaseEntity;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class CommonBaseEntityDao<T extends BaseEntity> implements IBaseDao<T> {

  @Inject
  protected JPQLQueryFactory query;
  @Inject
  public JpaRepository<T, Integer> repository;

  @Getter
  private final Class<T> genericType;

  @SuppressWarnings("unchecked")
  protected CommonBaseEntityDao() {
    genericType = (Class<T>) GenericTypeResolver
        .resolveTypeArgument(getClass(), CommonBaseEntityDao.class);
  }

  @Override
  public T byId(Long id) {
    QBaseEntity c = getQBaseEntity();
    T entity = entityJoin()
        .distinct()
        .where(c.id.eq(id))
        .fetchFirst();

    if (entityFetchJoin() != null && entity != null) {
      entityFetchJoin().distinct().where(getQBaseEntity().id.eq(entity.getId())).fetch();
    }
    completeFetch(Lists.newArrayList(entity));

    return entity;
  }

  @Override
  public List<T> getAll() {
    List<T> list = permissionFilterCondition(entityJoin().distinct()).fetch();

    if (entityFetchJoin() != null) {
      entityFetchJoin().distinct().fetch();
    }
    completeFetch(list);

    return list;
  }

  @Override
  public List<T> pageAll(Integer page, Integer offset) {
    JPQLQuery<T> query = permissionFilterCondition(entityJoin().distinct());
    query = pageQuery(query, page, offset);
    List<T> list = query.orderBy(defaultOrder()).fetch();

    if (entityFetchJoin() != null) {
      entityFetchJoin().distinct().fetch();
    }
    completeFetch(list);

    return list;
  }

  @Override
  public Integer countAll() {
    return ((Long) permissionFilterCondition(entityJoin().distinct()).fetchCount()).intValue();
  }

  public QBaseEntity getQBaseEntity() {
    return DaoUtils.getQBase(getQModel());
  }

  /**
   * Join della entity di tipo T. Utilizzata per la query SQL. Per sfruttare limit e offset NON
   * effettuare fetchJoin().
   */
  @SuppressWarnings("unchecked")
  protected JPQLQuery<T> entityJoin() {
    return (JPQLQuery<T>) query.selectFrom(getQBaseEntity());
  }

  /**
   * Join della entity di tipo T. Per la fetch join delle entity.
   */
  protected JPQLQuery<T> entityFetchJoin() {
    return null;
  }

  protected void completeFetch(List<T> items) {
  }

  protected BooleanBuilder pageableCondition(Map<DaoArg, Object> args) {
    return new BooleanBuilder();
  }

  protected List<T> pageableImpl(Integer page, Integer offset, OrderSpecifier<?> order,
      Map<DaoArg, Object> args) {
    JPQLQuery<T> query = permissionFilterCondition(entityJoin().distinct())
        .where(pageableCondition(args));
    query = pageQuery(query, page, offset);
    List<T> items = query.distinct().orderBy(order).fetch();

    // fetch esplicita
    Set<Long> ids = items.stream().map(BaseEntity::getId).collect(Collectors.toSet());
    fetchIds(ids);

    completeFetch(items);

    return items;
  }

  protected List<T> fetchIds(Set<Long> ids) {
    if (entityFetchJoin() != null) {
      List<T> items = entityFetchJoin().where(getQBaseEntity().id.in(ids)).distinct().fetch();
      return items;
    }
    return null;
  }

  protected Integer countPageableImpl(Map<DaoArg, Object> args) {
    JPQLQuery<T> query = permissionFilterCondition(entityJoin().distinct())
        .where(pageableCondition(args));
    query = pageQuery(query, null, null);
    return ((Long) query.distinct().fetchCount()).intValue();
  }

  /**
   * Applica le condizioni di paginazione alla query.
   */
  protected JPQLQuery<T> pageQuery(JPQLQuery<T> query, Integer page, Integer offset) {
    if (offset != null) {
      int _page = page == null ? 0 : page;
      query = query.offset(offset * _page).limit(offset);
    }
    return query;
  }

  /**
   * Applica l'ordine di default (id).
   */
  protected OrderSpecifier<?> defaultOrder() {
    return getQBaseEntity().id.asc();
  }

  /**
   * Un matcher contains case-insensitive per gli string path.<br> Se il term è null non viene
   * applicata alcuna condizione.
   *
   * @param path StringPath
   * @param number number
   * @return BooleanBuilder
   */
  protected BooleanBuilder numberEquals(NumberPath<Integer> path, Integer number) {
    BooleanBuilder builder = new BooleanBuilder();
    if (number != null) {
      return builder.and(path.eq(number));
    }
    // return builder.and(path.isNull());
    return builder;
  }

  /**
   * Un matcher contains case-insensitive per gli string path.<br> Se il term è null non viene
   * applicata alcuna condizione.
   *
   * @param path StringPath
   * @param term term
   * @return BooleanBuilder
   */
  protected BooleanBuilder stringContainsMatcher(StringPath path, String term) {
    BooleanBuilder builder = new BooleanBuilder();
    if (term != null) {
      return builder.and(path.containsIgnoreCase(term.trim().toLowerCase()));
    }
    return builder;
  }

  /**
   * Un matcher contains case-insensitive per gli string path.<br> Se il term è null non viene
   * applicata alcuna condizione.
   *
   * @param path StringPath
   * @param term term
   * @return BooleanBuilder
   */
  protected BooleanBuilder integerContainsMatcher(NumberPath<Integer> path, String term) {
    BooleanBuilder builder = new BooleanBuilder();
    if (term != null) {
      return builder.and(path.like(term.trim().toLowerCase()));
    }
    return builder;
  }

  /**
   * Un matcher esatto per gli string path.
   */
  protected BooleanBuilder stringExactMatcher(StringPath path, String term) {
    BooleanBuilder builder = new BooleanBuilder();
    if (term != null) {
      builder.and(path.eq(term.trim()));
    } else {
      builder.and(path.isNull());
    }
    return builder;
  }

  protected BooleanBuilder tokenizedFilter(BooleanBuilder builder, Object term,
      Set<Path<?>> paths) {
    if (builder == null) {
      builder = new BooleanBuilder();
    }
    if (!(term instanceof String)) {
      return builder;
    }
    String tokens = (String) term;
    if (tokens.isEmpty()) {
      return builder;
    }

    // Cercare ogni word in ogni string path
    for (String word : tokens.split(" ")) {
      BooleanBuilder orCondition = new BooleanBuilder();
      for (Path<?> path : paths) {
        orCondition.or(tokenizedPathCondition(path, word));
      }
      builder.and(orCondition);
    }
    return builder;
  }

  private BooleanBuilder tokenizedPathCondition(Path<?> path, String word) {
    if (path instanceof StringPath) {
      return stringContainsMatcher((StringPath) path, word);
    }
    if (path instanceof NumberPath) {
      return new BooleanBuilder(((NumberPath<?>) path).like("%" + word + "%"));
    }
    return new BooleanBuilder();
  }

  protected BooleanBuilder periodCondition(BooleanBuilder builder, LocalDate date,
      DatePath<LocalDate> begin, DatePath<LocalDate> end) {
    if (builder == null) {
      builder = new BooleanBuilder();
    }
    if (date == null) {
      return builder;
    }
    builder.and(
        begin.isNull().or(begin.loe(date))
            .and(
                end.isNull().or(end.goe(date))
            ));
    return builder;
  }

  /**
   * Filtro sulle query relativo ai permessi posseduti.
   *
   * @return le condizioni di filtro.
   */
  protected JPQLQuery<T> permissionFilterCondition(JPQLQuery<T> basequery) {
    return basequery;
  }

  public T save(T entity) {
    return repository.save(entity);
  }

  public void delete(T entity) {
    repository.delete(entity);
  }

  /**
   * Restituisce true se l'entity esiste ma con id differente rispetto a quello specificato. E'
   * utilizzato per tutti i metodi di verifica dei campi unique.
   *
   * @param id id
   * @param entity entity da berificare
   * @return True se esiste con id diverso rispetto a quello specificato
   */
  protected boolean alreadyPresent(Long id, T entity) {
    return entity != null && !entity.getId().equals(id);
  }


  public List<T> find(Predicate predicate) {

    List<Long> ids = entityJoin()
        .select(getQBaseEntity().id) // per la proiezione degli id
        .where(predicate).distinct().fetch();

    return fetchIds(new HashSet<>(ids));
  }
}
