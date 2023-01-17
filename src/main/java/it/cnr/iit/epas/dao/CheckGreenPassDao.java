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

import com.querydsl.jpa.JPQLQuery;
import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.models.CheckGreenPass;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.QCheckGreenPass;
import it.cnr.iit.epas.models.QPerson;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Dao per le query sul green pass.
 *
 * @author dario
 *
 */
@Component
public class CheckGreenPassDao extends DaoBase<CheckGreenPass> {

  @Inject
  CheckGreenPassDao(Provider<EntityManager> emp) {
    super(emp);
  }
  
  /**
   * Ritorna la lista dei sorteggiati per la data passata come parametro.
   *
   * @param date la data per cui cercare i check ai green pass
   * @return la lista dei sorteggiati per la data in oggetto.
   */
  public List<CheckGreenPass> listByDate(LocalDate date, Office office) {
    final QCheckGreenPass checkGreenPass = QCheckGreenPass.checkGreenPass;
    final QPerson person = QPerson.person;
    final JPQLQuery<CheckGreenPass> query = getQueryFactory()
        .selectFrom(checkGreenPass).leftJoin(checkGreenPass.person, person)
        .where(checkGreenPass.checkDate.eq(date)
            .and(person.office.eq(office)))
        .orderBy(person.surname.asc());
    
    return query.fetch();
  }
  
  /**
   * Ritorna, se esiste, il checkGreenPass identificato dall'id passato come parametro.
   *
   * @param checkGreenPassId l'identificativo del checkGreenPass
   * @return l'optional contenenente o meno l'oggetto identificato dall'id passato come parametro.
   */
  public CheckGreenPass getById(long checkGreenPassId) {
    final QCheckGreenPass checkGreenPass = QCheckGreenPass.checkGreenPass;
    final CheckGreenPass result = getQueryFactory().selectFrom(checkGreenPass)
        .where(checkGreenPass.id.eq(checkGreenPassId)).fetchFirst();
    return result;
  }
  
  /**
   * Verifica se esiste già una entry in tabella per la persona e la data passati.
   *
   * @param person la persona da controllare
   * @param date la data in cui controllare
   * @return se esiste il check green pass per i parametri passati.
   */
  public Optional<CheckGreenPass> byPersonAndDate(Person person, LocalDate date) {
    final QCheckGreenPass checkGreenPass = QCheckGreenPass.checkGreenPass;
    final CheckGreenPass result = getQueryFactory().selectFrom(checkGreenPass)
        .where(checkGreenPass.person.eq(person)
            .and(checkGreenPass.checkDate.eq(date))).fetchFirst();
    return Optional.ofNullable(result);
  }

  /**
   * Conta le volte in cui una persona è stata controllata.
   *
   * @param person la persona di cui controllare il numero di volte in cui è stata
   *     controllata
   * @return quante volte la persona passata come parametro è stata controllata.
   */
  public long howManyTimesChecked(Person person) {
    final QCheckGreenPass checkGreenPass = QCheckGreenPass.checkGreenPass;
    return getQueryFactory().selectFrom(checkGreenPass)
        .where(checkGreenPass.person.eq(person)).fetchCount();
  }
}
