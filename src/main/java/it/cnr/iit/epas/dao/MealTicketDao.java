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

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQuery;
import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.manager.services.mealtickets.MealTicketsServiceImpl.MealTicketOrder;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.MealTicket;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.QContract;
import it.cnr.iit.epas.models.QMealTicket;
import it.cnr.iit.epas.models.QPerson;
import it.cnr.iit.epas.models.QPersonDay;
import it.cnr.iit.epas.models.enumerate.BlockType;
import it.cnr.iit.epas.utils.DateInterval;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * DAO per i MealTicket.
 *
 * @author Alessandro Martelli
 */
@Component
public class MealTicketDao extends DaoBase<MealTicket> {

  @Inject
  MealTicketDao(ObjectProvider<EntityManager> emp) {
    super(emp);
  }

  /**
   * MealTickets assegnati alla persona nella finestra temporale specificata.<br> Ordinati in base
   * al tipo di ordinamento enumerato e come secondo ordine per codice.
   *
   * @param contract contratto
   * @param interval intervallo
   * @param order ordinamento
   * @param returned se voglio quelli riconsegnati o quelli disponibili
   */
  public List<MealTicket> contractMealTickets(Contract contract, Optional<DateInterval> interval,
      MealTicketOrder order, boolean returned) {

    final QMealTicket mealTicket = QMealTicket.mealTicket;

    final BooleanBuilder condition = new BooleanBuilder();

    condition.and(mealTicket.contract.eq(contract));

    if (returned) {
      condition.and(mealTicket.returned.eq(true));
    } else {
      condition.and(mealTicket.returned.eq(false));
    }

    if (interval.isPresent()) {
      condition.and(mealTicket.date.between(interval.get().getBegin(), interval.get().getEnd()));
    }

    final JPQLQuery<MealTicket> query = getQueryFactory().selectFrom(mealTicket).where(condition);

    if (order == MealTicketOrder.ORDER_BY_EXPIRE_DATE_ASC) {
      query.orderBy(mealTicket.expireDate.asc());
    } else if (order == MealTicketOrder.ORDER_BY_DELIVERY_DATE_DESC) {
      query.orderBy(mealTicket.date.desc());
    }

    query.orderBy(mealTicket.block.asc()).orderBy(mealTicket.number.asc());

    return query.fetch();
  }

  /**
   * La scadenza massima precedentemente assegnata ai buoni pasto inseriti per le persone
   * appartenenti all'office passato come argomento.
   *
   * @param office sede
   * @return data
   */
  public LocalDate getFurtherExpireDateInOffice(Office office) {

    final QMealTicket qmt = QMealTicket.mealTicket;
    final QPerson qp = QPerson.person;
    final QContract qc = QContract.contract;

    return getQueryFactory().select(qmt.expireDate)
        .from(qmt)
        .leftJoin(qmt.contract, qc)
        .leftJoin(qc.person, qp)
        .where(qp.office.id.eq(office.getId()))
        .groupBy(qmt.expireDate)
        .orderBy(qmt.expireDate.desc()).fetchFirst();
  }

  /**
   * utilizzare contractMealTickets con gli opportuni parametri I buoni pasto di un blocco ordinati
   * per codice asc. Se contract presente i soli associati a quel contratto.
   *
   * @param codeBlock il codice del blocco.
   * @param contract contratto
   * @return la lista dei meal tickets nel blocco.
   */
  public List<MealTicket> getMealTicketsInCodeBlock(String codeBlock, Optional<Contract> contract) {

    final QMealTicket mealTicket = QMealTicket.mealTicket;
    BooleanBuilder condition = new BooleanBuilder().and(
        mealTicket.block.eq(codeBlock));

    if (contract.isPresent()) {
      condition.and(mealTicket.contract.eq(contract.get()));
    }

    return getQueryFactory()
        .selectFrom(mealTicket)
        .where(condition)
        .orderBy(mealTicket.code.asc())
        .fetch();
  }

  /**
   * I buoni pasto che matchano il codice passato. Se office presente i soli appartenti a contratti
   * di quell'office.
   *
   * @param code codice match
   * @param office sede
   * @return elenco
   */
  public List<MealTicket> getMealTicketsMatchCodeBlock(String code, Optional<Office> office) {

    QMealTicket mealTicket = QMealTicket.mealTicket;
    QContract contract = QContract.contract;
    QPerson person = QPerson.person;

    final JPQLQuery<MealTicket> query = getQueryFactory()
        .selectFrom(mealTicket);

    if (office.isPresent()) {
      query.leftJoin(mealTicket.contract, contract);
      query.leftJoin(contract.person, person);
    }

    query.where(mealTicket.block.like("%" + code + "%"));
    if (office.isPresent()) {
      query.where(person.office.eq(office.get()).and(mealTicket.returned.eq(false)));
    }

    return query.orderBy(mealTicket.block.asc()).orderBy(mealTicket.number.asc()).fetch();

  }
  
  /**
   * Lista dei MealTicket ancora non assegnati.
   */
  public List<MealTicket> getUnassignedElectronicMealTickets(Contract contract) {
    QMealTicket mealTicket = QMealTicket.mealTicket;
    
    final JPQLQuery<MealTicket> query = getQueryFactory()
        .selectFrom(mealTicket);
    
    query.where(mealTicket.blockType.eq(BlockType.electronic)
        .and(mealTicket.mealTicketCard.isNull()).and(mealTicket.contract.eq(contract)));
    return query.fetch();
  }
  
  /**
   * Mappa con il numero di buoni pasto maturati per ogni persona indicata.
   */
  public Map<Person, Integer> getNumberOfMealTicketAccrued(
      List<Person> persons, LocalDate from, LocalDate to) {
    QPersonDay personDay = QPersonDay.personDay;
    Map<Person, List<PersonDay>> result = getQueryFactory()
        .from(personDay)
        .where(personDay.person.in(persons), personDay.date.goe(from),
            personDay.date.loe(to), personDay.isTicketAvailable.eq(true))
        .transform(GroupBy.groupBy(personDay.person).as(GroupBy.list(personDay)));

    Map<Person, Integer> ticketsCountMap = new HashMap<>();
    result.keySet().forEach(person -> {
      ticketsCountMap.put(person, result.get(person).size());
    });
    return ticketsCountMap;
  }

}