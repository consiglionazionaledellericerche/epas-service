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
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.PersonDayInTrouble;
import it.cnr.iit.epas.models.QPersonDayInTrouble;
import it.cnr.iit.epas.models.enumerate.Troubles;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * DAO per i PersonDayInTrouble.
 */
@Component
public class PersonDayInTroubleDao extends DaoBase {

  @Inject
  PersonDayInTroubleDao(Provider<EntityManager> emp) {
    super(emp);
  }

  /**
   * La lista dei trouble relativi alla persona nel periodo (opzionale) tra begin e end
   * appartenenti alla lista di tipi troubles.
   *
   * @param person la persona di cui si vogliono i trouble
   * @param begin (opzionale) da quando si cerca
   * @param end (opzionale) fino a quando si cerca
   * @param troubles la lista dei tipi di trouble da cercare
   * @return la lista dei personDayInTrouble relativi alla persona person nel periodo begin-end. 
   *     E possibile specificare se si vuole ottenere quelli fixati (fixed = true) o no 
   *     (fixed = false).
   */
  public List<PersonDayInTrouble> getPersonDayInTroubleInPeriod(
      Person person, Optional<LocalDate> begin, Optional<LocalDate> end,
      Optional<List<Troubles>> troubles) {

    QPersonDayInTrouble pdit = QPersonDayInTrouble.personDayInTrouble;

    BooleanBuilder conditions = new BooleanBuilder(pdit.personDay.person.eq(person));
    if (begin.isPresent()) {
      conditions.and(pdit.personDay.date.goe(begin.get()));
    }
    if (end.isPresent()) {
      conditions.and(pdit.personDay.date.loe(end.get()));
    }
    if (troubles.isPresent()) {
      conditions.and(pdit.cause.in(troubles.get()));
    }

    return getQueryFactory().selectFrom(pdit).where(conditions).fetch();
  }

  /**
   * Il persondayintrouble, se esiste, relativo ai parametri passati.
   *
   * @param pd il personDay per cui si ricerca il trouble
   * @param trouble la causa per cui si ricerca il trouble
   * @return il personDayInTrouble, se esiste, relativo ai parametri passati al metodo.
   */
  public Optional<PersonDayInTrouble> getPersonDayInTroubleByType(PersonDay pd, Troubles trouble) {
    QPersonDayInTrouble pdit = QPersonDayInTrouble.personDayInTrouble;
    final PersonDayInTrouble result = getQueryFactory()
        .selectFrom(pdit)
        .where(pdit.personDay.eq(pd).and(pdit.cause.eq(trouble))).fetchOne();
    return Optional.ofNullable(result);
  }
}