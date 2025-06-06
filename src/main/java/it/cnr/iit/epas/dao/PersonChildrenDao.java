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
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonChildren;
import it.cnr.iit.epas.models.QPersonChildren;
import jakarta.persistence.EntityManager;
import java.util.List;
import javax.inject.Inject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Dao per i PersonChildren.
 *
 * @author Dario Tagliaferri
 */
@Component
public class PersonChildrenDao extends DaoBase<PersonChildren> {

  @Inject
  PersonChildrenDao(ObjectProvider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Il personChildren con id passato come parametro.
   *
   * @param id l'identificativo del figlio del dipendente
   * @return il personChildren relativo all'id passato come parametro.
   */
  public PersonChildren getById(Long id) {
    final QPersonChildren personChildren = QPersonChildren.personChildren;
    return getQueryFactory().selectFrom(personChildren)
        .where(personChildren.id.eq(id))
        .fetchOne();
  }


  /**
   * La lista dei figli di una persona passata come parametro.
   *
   * @param person la persona di cui si vogliono i figli
   * @return la lista di tutti i figli della persona.
   */
  public List<PersonChildren> getAllPersonChildren(Person person) {
    final QPersonChildren personChildren = QPersonChildren.personChildren;
    return getQueryFactory().selectFrom(personChildren)
        .where(personChildren.person.eq(person))
        .orderBy(personChildren.bornDate.asc())
        .fetch();
  }
}