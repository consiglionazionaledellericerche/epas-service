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
import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.models.CertificatedData;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonMonthRecap;
import it.cnr.iit.epas.models.QCertificatedData;
import it.cnr.iit.epas.models.QPersonMonthRecap;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Dao relativo ai PersonMonthRecap.
 *
 * @author Dario Tagliaferri
 */
@Component
public class PersonMonthRecapDao extends DaoBase<PersonMonthRecap> {

  @Inject
  PersonMonthRecapDao(ObjectProvider<EntityManager> emp) {
    super(emp);
  }

  /**
   * La lista dei personMonthRecap relativi ai parametri passati.
   *
   * @param person la persona per cui cercare i riepiloghi
   * @param year l'anno da cercare
   * @param month (opzionale) il mese da cercare
   * @param hoursApproved (opzionale) se sono stati approvati
   * @return la lista di personMonthRecap relativa all'anno year per la persona person.
   */
  public List<PersonMonthRecap> getPersonMonthRecapInYearOrWithMoreDetails(Person person,
      Integer year, Optional<Integer> month, Optional<Boolean> hoursApproved) {

    QPersonMonthRecap personMonthRecap = QPersonMonthRecap.personMonthRecap;

    final BooleanBuilder condition = new BooleanBuilder();

    if (month.isPresent()) {
      condition.and(personMonthRecap.month.eq(month.get()));
    }
    if (hoursApproved.isPresent()) {
      condition.and(personMonthRecap.hoursApproved.eq(hoursApproved.get()));
    }

    return getQueryFactory().selectFrom(personMonthRecap)
        .where(condition.and(personMonthRecap.person.eq(person)
            .and(personMonthRecap.year.eq(year))))
        .fetch();
  }

  /**
   * Il personMonthRecap con id passato al metodo.
   *
   * @param id l'identificativo del personMonthRecap
   * @return il personMonthRecap relativo all'id passato come parametro.
   */
  public PersonMonthRecap getPersonMonthRecapById(Long id) {
    QPersonMonthRecap personMonthRecap = QPersonMonthRecap.personMonthRecap;
    return getQueryFactory().selectFrom(personMonthRecap)
        .where(personMonthRecap.id.eq(id))
        .fetchOne();
  }

  /**
   * La lista dei personMonthRecap appartenenti ai criteri del metodo.
   *
   * @param person la persona di cui cercare i riepiloghi
   * @param year l'anno da cercare
   * @param month il mese da cercare
   * @param begin la data da cui cercare
   * @param end la data fino a cui cercare
   * @return la lista dei personMonthRecap appartenenti ai criteri del metodo.
   */
  public List<PersonMonthRecap> getPersonMonthRecaps(
      Person person, Integer year, Integer month, LocalDate begin, LocalDate end) {
    QPersonMonthRecap personMonthRecap = QPersonMonthRecap.personMonthRecap;
    return getQueryFactory().selectFrom(personMonthRecap)
        .where(personMonthRecap.person.eq(person).and(personMonthRecap.year.eq(year)
            .and(personMonthRecap.month.eq(month)
                .andAnyOf(
                    personMonthRecap.fromDate.loe(begin).and(personMonthRecap.toDate.goe(end)),
                    personMonthRecap.fromDate.loe(end)
                        .and(personMonthRecap.toDate.goe(end))))))
        .fetch();
  }

  /**
   * La lista dei personmonthrecap delle persone appartenenti alla sede nell'anno/mese.
   *
   * @param year l'anno da cercare
   * @param month il mese da cercare
   * @office la sede su cui cercare
   * @return la lista dei personMonthRecap di tutte le persone che appartengono all'ufficio office
   *     nell'anno year e nel mese month passati come parametro.
   */
  public List<PersonMonthRecap> getPeopleMonthRecaps(Integer year, Integer month, Office office) {
    QPersonMonthRecap personMonthRecap = QPersonMonthRecap.personMonthRecap;
    return getQueryFactory().selectFrom(personMonthRecap)
        .where(personMonthRecap.month.eq(month)
            .and(personMonthRecap.year.eq(year)
                .and(personMonthRecap.person.office.eq(office))))
        .fetch();
  }


  /**
   * Il personMonthRecap, se esiste, per la persona nell'anno/mese.
   *
   * @param person la persona di cui cercare il pmr
   * @year l'anno da cercare
   * @month il mese da cercare
   * @return il personMonthRecap, se esiste, relativo ai parametri passati come riferimento.
   */
  public Optional<PersonMonthRecap> getPersonMonthRecapByPersonYearAndMonth(
      Person person, Integer year, Integer month) {
    QPersonMonthRecap personMonthRecap = QPersonMonthRecap.personMonthRecap;
    final PersonMonthRecap result = getQueryFactory().selectFrom(personMonthRecap)
        .where(personMonthRecap.person.eq(person).and(personMonthRecap.year.eq(year)
            .and(personMonthRecap.month.eq(month)))).fetchOne();
    return Optional.ofNullable(result);
  }


  /* *****************************************************************************************/
  /* Parte relativa a query su CertificatedData per la quale, essendo unica, non si è deciso */
  /* di creare un Dao ad hoc                                                                 */
  /* *****************************************************************************************/

  /**
   * Il certificato (vecchia modalità) con id passato come parametro.
   *
   * @return il certificatedData relativo all'id passato come parametro.
   */
  public CertificatedData getCertificatedDataById(Long id) {
    QCertificatedData cert = QCertificatedData.certificatedData;
    return getQueryFactory().selectFrom(cert)
        .where(cert.id.eq(id)).fetchOne();
  }


  /**
   * Il certificato (vecchia modalità) della persona nell'anno/mese.
   *
   * @return il certificatedData relativo alla persona 'person' per il mese 'month' e l'anno 'year'.
   */
  public CertificatedData getPersonCertificatedData(Person person, Integer month, Integer year) {

    QCertificatedData cert = QCertificatedData.certificatedData;

    return getQueryFactory().selectFrom(cert)
        .where(cert.person.eq(person).and(cert.month.eq(month).and(cert.year.eq(year))))
        .fetchOne();
  }

}