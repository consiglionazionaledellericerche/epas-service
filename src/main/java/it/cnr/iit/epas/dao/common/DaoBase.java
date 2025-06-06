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

package it.cnr.iit.epas.dao.common;

import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import it.cnr.iit.epas.models.base.BaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import javax.inject.Inject;
import org.springframework.beans.factory.ObjectProvider;


/**
 * Base dao which provides the JPQLQueryFactory and the EntityManager.
 *
 * @author Marco Andreini
 */
public abstract class DaoBase<T extends BaseEntity> {

  protected final JPQLQueryFactory queryFactory;
  protected final ObjectProvider<EntityManager> emp;

  @Inject
  public DaoBase(ObjectProvider<EntityManager> emp) {
    this.emp = emp;
    this.queryFactory = new JPAQueryFactory(emp.getObject());
  }

  @Transactional
  public void persist(T object) {
    emp.getObject().persist(object);
  }

  @Transactional
  public T merge(T object) {
    return emp.getObject().<T>merge(object);
  }

  /**
   * Se l'oggetto è già persistent effettua la merge, altrimenti la persist.
   */
  @Transactional
  public T save(T object) {
    if (isPersistent(object)) {
      return emp.getObject().<T>merge(object);
    }
    persist(object);
    return object;
  }

  @Transactional
  public void delete(T object) {
    emp.getObject().remove(object);
  }

  @Transactional
  public void refresh(T object) {
    emp.getObject().refresh(object);
  }

  @Transactional
  public boolean isPersistent(T object) {
    return emp.getObject().contains(object);
  }

  protected JPQLQueryFactory getQueryFactory() {
    return queryFactory;
  }

  public EntityManager getEntityManager() {
    return emp.getObject();
  }
}