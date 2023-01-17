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

package it.cnr.iit.epas.dao;

import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.models.QRole;
import it.cnr.iit.epas.models.Role;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Dao per l'accesso alle informazioni dei Role.
 *
 * @author Dario Tagliaferri
 */
@Component
public class RoleDao extends DaoBase<Role> {

  @Inject
  RoleDao(Provider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Il ruolo identificato dall'id passato come parametro.
   *
   * @param id l'identificativo del ruolo
   * @return il ruolo identificato dall'id passato come parametro.
   */
  public Role getRoleById(Long id) {
    final QRole role = QRole.role;
    return getQueryFactory().selectFrom(role)
        .where(role.id.eq(id))
        .fetchOne();
  }

  /**
   * Il ruolo identificato dal nome passato come parametro.
   *
   * @name il nome del ruolo
   * @return il ruolo identificato dal nome passato come parametro.
   */
  public Role getRoleByName(String name) {
    final QRole role = QRole.role;
    return getQueryFactory().selectFrom(role)
        .where(role.name.eq(name))
        .fetchOne();
  }

  /**
   * La lista dei ruoli disponibili.
   *
   * @return Tutti i ruoli disponibili.
   */
  public List<Role> getAll() {
    final QRole role = QRole.role;
    return getQueryFactory().selectFrom(role).fetch();
  }

}