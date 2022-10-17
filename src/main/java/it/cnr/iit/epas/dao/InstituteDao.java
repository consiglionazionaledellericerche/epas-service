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
import it.cnr.iit.epas.models.Institute;
import it.cnr.iit.epas.models.QInstitute;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Dao per gli istituti.
 *
 * @author Cristian Lucchesi
 *
 */
@Component
public class InstituteDao extends DaoBase<Institute> {

  @Inject
  InstituteDao(Provider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Tutti gli istituti presenti.
   *
   * @return la lista di tutti gli uffici presenti sul database.
   */
  public List<Institute> getAllInstitutes() {

    final QInstitute institute = QInstitute.institute;

    return getQueryFactory().selectFrom(institute).orderBy(institute.name.asc()).fetch();
  }

}