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
import it.cnr.iit.epas.models.QTeleworkValidation;
import it.cnr.iit.epas.models.TeleworkValidation;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import javax.inject.Inject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * DAO per i TeleworkValidation.
 */
@Component
public class TeleworkValidationDao extends DaoBase<TeleworkValidation> {

  @Inject
  TeleworkValidationDao(ObjectProvider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Ritorna il CompetenceRequestEvent identificato dall'id passato come parametro.
   */
  public Optional<TeleworkValidation> findById(Long id) {
    final QTeleworkValidation teleworkValidation = 
        QTeleworkValidation.teleworkValidation;
    return 
        Optional.of(
            getQueryFactory()
              .selectFrom(teleworkValidation)
              .where(teleworkValidation.id.eq(id)).fetchOne());
  }

}