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
import it.cnr.iit.epas.dao.filter.QFilters;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.QContract;
import it.cnr.iit.epas.models.QContractMonthRecap;
import it.cnr.iit.epas.models.QPerson;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.joda.time.YearMonth;
import org.springframework.stereotype.Component;

/**
 * DAO per i riepiloghi mensili.
 * <p>
 * - situazione residuale minuti anno passato e anno corrente - situazione residuale buoni pasto
 *
 * - situazione residuale ferie (solo per il mese di dicembre)
 * </p>
 *
 * @author Alessandro Martelli
 */
@Component
public class ContractMonthRecapDao extends DaoBase<ContractMonthRecap> {

  @Inject
  ContractMonthRecapDao(Provider<EntityManager> emp) {
    super(emp);
  }

  /**
   * I riepiloghi delle persone con un massimo di buoni pasto passato come parametro. TODO: il
   * filtro sugli office delle persone.
   */
  public List<ContractMonthRecap> getPersonMealticket(
      YearMonth yearMonth, Optional<Integer> max, Optional<String> name,
      Set<Office> offices) {

    final QContractMonthRecap recap = QContractMonthRecap.contractMonthRecap;
    final QContract contract = QContract.contract;
    final QPerson person = QPerson.person;

    final BooleanBuilder condition = new BooleanBuilder();
    if (max.isPresent()) {
      condition.and(recap.remainingMealTickets.loe(max.get()));
    }
    condition.and(new QFilters().filterNameFromPerson(person, name));

    return getQueryFactory().selectFrom(recap)
        .leftJoin(recap.contract, contract)
        .leftJoin(contract.person, person)
        .where(recap.year.eq(yearMonth.getYear())
            .and(recap.month.eq(yearMonth.getMonthOfYear())
                .and(person.office.in(offices))
                .and(condition))).orderBy(recap.contract.person.surname.asc())
        .fetch();
  }
  
  /**
   * Ritorna il riepilogo del contratto contract nell'anno/mese yearMonth.
   *
   * @param contract il contratto da riepilogare
   * @param yearMonth l'anno mese di riferimento
   * @return Il riepilogo del contratto nell'anno mese
   */
  public ContractMonthRecap getContractMonthRecap(Contract contract, YearMonth yearMonth) {
    final QContractMonthRecap recap = QContractMonthRecap.contractMonthRecap;
    return getQueryFactory().selectFrom(recap)
        .where(recap.contract.eq(contract).and(recap.year.eq(yearMonth.getYear())
            .and(recap.month.eq(yearMonth.getMonthOfYear())))).fetchFirst();
  }
}