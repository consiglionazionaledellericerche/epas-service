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

import com.querydsl.core.BooleanBuilder;
import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.models.contractuals.ContractualClause;
import it.cnr.iit.epas.models.contractuals.QContractualClause;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * DAO per gli Istituti contrattuali.
 *
 * @author Cristian Lucchesi
 */
@Component
public class ContractualClauseDao extends DaoBase<ContractualClause> {

  @Inject
  ContractualClauseDao(Provider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Lista degli istituti contrattuali.
   *
   * @param onlyEnabled se non presente o uguale a false mostra solo gli istituti contrattuali
   *     attivi alla data corrente.
   * @return la lista degli istituti contrattuali.
   */
  public List<ContractualClause> all(Optional<Boolean> onlyEnabled) {
    QContractualClause contractualClause = QContractualClause.contractualClause;
    BooleanBuilder condition = new BooleanBuilder();
    if (onlyEnabled.orElse(true)) {
      condition.and(
          contractualClause.beginDate.loe(LocalDate.now()))
          .and(contractualClause.endDate.isNull()
              .or(contractualClause.beginDate.goe(LocalDate.now())));
    }
    return getQueryFactory().selectFrom(contractualClause).where(condition)
        .orderBy(contractualClause.name.desc()).fetch();
  }

}