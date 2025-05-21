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
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.QPerson;
import it.cnr.iit.epas.models.QStampModificationType;
import it.cnr.iit.epas.models.QStamping;
import it.cnr.iit.epas.models.StampModificationType;
import it.cnr.iit.epas.models.Stamping;
import it.cnr.iit.epas.models.Stamping.WayType;
import it.cnr.iit.epas.utils.DateUtility;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * DAO per l'accesso alle informazioni delle timbrature.
 *
 * @author Dario Tagliaferri
 */
@Component
public class StampingDao extends DaoBase<Stamping> {

  @Inject
  StampingDao(ObjectProvider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Ritorna la prima (eventuale) timbratura che corrisponde ai dati passati.
   *
   * @param dateTime data della timbratura
   * @param person persona a cui si riferisce
   * @param way verso.
   * @return la prima timbratura (ordinando per id decrescente) trovata, oppure Optional::absent
   */
  public Optional<Stamping> getStamping(LocalDateTime dateTime, Person person, WayType way) {
    final QStamping stamping = QStamping.stamping;
    final Stamping result = getQueryFactory().selectFrom(stamping)
        .where(stamping.date.eq(dateTime).and(stamping.personDay.person.eq(person))
            .and(stamping.way.eq(way)))
        .orderBy(stamping.id.desc())
        .limit(1)
        .fetchOne();
    return Optional.ofNullable(result);
  }

  /**
   * Preleva una timbratura tramite il suo id.
   *
   * @param id l'id associato alla Timbratura sul db.
   * @return la timbratura corrispondente all'id passato come parametro.
   */
  public Stamping getStampingById(Long id) {
    final QStamping stamping = QStamping.stamping;
    return getQueryFactory().selectFrom(stamping)
        .where(stamping.id.eq(id)).fetchOne();
  }

  /**
   * lo stampModificationType relativo all'id passato come parametro.
   */
  @Deprecated
  public StampModificationType getStampModificationTypeById(Long id) {
    final QStampModificationType smt = QStampModificationType.stampModificationType;

    return getQueryFactory().selectFrom(smt)
        .where(smt.id.eq(id)).fetchOne();
  }

  /**
   * Lista delle timbrature inserire dall'amministratore in un determinato mese.
   *
   * @param yearMonth mese di riferimento
   * @param office ufficio
   * @return lista delle timbrature inserite dell'amministratore
   */
  public List<Stamping> adminStamping(YearMonth yearMonth, Office office) {
    final QStamping stamping = QStamping.stamping;
    final QPerson person = QPerson.person;
    return getQueryFactory().selectFrom(stamping)
        .join(stamping.personDay.person, person)
        .where(stamping.markedByAdmin.eq(true)
            .and(stamping.personDay.date.goe(yearMonth.atDay(1)))
            .and(stamping.personDay.date
                .loe(DateUtility.endOfMonth(yearMonth.atDay(1))))
            .and(person.office.isNotNull())
            .and(person.office.eq(office)))
        .orderBy(person.surname.asc(), stamping.personDay.date.asc())
        .fetch();
  }
}