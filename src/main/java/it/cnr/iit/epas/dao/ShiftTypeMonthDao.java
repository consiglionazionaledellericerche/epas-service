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
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.QPersonShiftDay;
import it.cnr.iit.epas.models.QShiftCategories;
import it.cnr.iit.epas.models.QShiftTypeMonth;
import it.cnr.iit.epas.models.ShiftType;
import it.cnr.iit.epas.models.ShiftTypeMonth;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Il dao sui riepiloghi di turno mensili.
 *
 * @author Daniele Murgia
 * @since 10/06/17
 */
@Component
public class ShiftTypeMonthDao extends DaoBase<ShiftTypeMonth> {

  @Inject
  ShiftTypeMonthDao(Provider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Il riepilogo di turno mensile con id passato come parametro.
   *
   * @param id l'id del riepilogo.
   * @return Il riepilogo di turno mensile con id passato come parametro.
   */
  public Optional<ShiftTypeMonth> byId(long id) {
    final QShiftTypeMonth stm = QShiftTypeMonth.shiftTypeMonth;

    return Optional.ofNullable(getQueryFactory().selectFrom(stm).where(stm.id.eq(id)).fetchOne());
  }

  /**
   * Il riepilogo di turno mensile relativo all'attività shiftType alla data date.
   *
   * @param shiftType l'attività di turno
   * @param date la data
   * @return il riepilogo di turno mensile relativo all'attività shiftType alla data date.
   */
  public Optional<ShiftTypeMonth> byShiftTypeAndDate(ShiftType shiftType, LocalDate date) {
    final QShiftTypeMonth stm = QShiftTypeMonth.shiftTypeMonth;
    final YearMonth yearMonth = YearMonth.from(date);

    return Optional.ofNullable(getQueryFactory().selectFrom(stm)
        .where(stm.shiftType.eq(shiftType).and(stm.yearMonth.eq(yearMonth))).fetchOne());
  }

  /**
   * Questo metodo è utile in fase di assegnazione delle competenze in seguito all'approvazione del
   * responsabile di turno (bisogna ricalcolare tutte le competenze delle persone coinvolte).
   *
   * @param month mese richiesto
   * @param people lista delle persone coinvolte nel mese richiesto
   * @return La lista
   */
  public List<ShiftTypeMonth> approvedInMonthRelatedWith(YearMonth month, List<Person> people) {

    final QShiftTypeMonth stm = QShiftTypeMonth.shiftTypeMonth;
    final QPersonShiftDay psd = QPersonShiftDay.personShiftDay;
    final LocalDate monthBegin = month.atDay(1);
    final LocalDate monthEnd = DateUtility.endOfMonth(monthBegin);

    return getQueryFactory().select(stm).from(psd)
        .leftJoin(psd.shiftType.monthsStatus, stm)
        .where(psd.personShift.person.in(people)
            .and(psd.date.goe(monthBegin))
            .and(psd.date.loe(monthEnd))
            .and(stm.yearMonth.eq(month).and(stm.approved.isTrue()))).distinct().fetch();
  }

  /**
   * La lista dei riepiloghi mensili di turno della sede office nell'anno/mese.
   *
   * @param office la sede su cui cercare i riepiloghi
   * @param month l'anno/mese su cui cercare i riepiloghi
   * @return la lista dei riepiloghi mensili di turno della sede office nell'anno/mese.
   */
  public List<ShiftTypeMonth> byOfficeInMonth(Office office, YearMonth month) {
    final QShiftTypeMonth stm = QShiftTypeMonth.shiftTypeMonth;
    final QShiftCategories sc = QShiftCategories.shiftCategories;

    return getQueryFactory().selectFrom(stm)
        .leftJoin(stm.shiftType.shiftCategories, sc)
        .where(stm.yearMonth.eq(month).and(sc.office.eq(office))).distinct().fetch();

  }
}