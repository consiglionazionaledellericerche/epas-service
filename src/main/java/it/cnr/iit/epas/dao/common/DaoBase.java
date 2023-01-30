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

import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import it.cnr.iit.epas.models.base.BaseEntity;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

/**
 * Base dao which provides the JPQLQueryFactory and the EntityManager.
 *
 * @author Marco Andreini
 */
public abstract class DaoBase<T extends BaseEntity> {

  protected final JPQLQueryFactory queryFactory;
  protected final Provider<EntityManager> emp;

  @Inject
  public DaoBase(Provider<EntityManager> emp) {
    this.emp = emp;
    this.queryFactory = new JPAQueryFactory(emp.get());
  }

  public void persist(T object) {
    emp.get().persist(object);
  }

  public T merge(T object) {
    return emp.get().<T>merge(object);
  }

  /**
   * Se l'oggetto è già persistent effettua la merge, altrimenti la persist.
   */
  public T save(T object) {
    if (isPersistent(object)) {
      return emp.get().<T>merge(object);
    }
    persist(object);
    return object;
  }

  public void delete(T object) {
    emp.get().remove(object);
  }

  public void refresh(T object) {
    emp.get().refresh(object);
  }

  public boolean isPersistent(T object) {
    return emp.get().contains(object);
  }

  protected JPQLQueryFactory getQueryFactory() {
    return queryFactory;
  }

  public EntityManager getEntityManager() {
    return emp.get();
  }
}