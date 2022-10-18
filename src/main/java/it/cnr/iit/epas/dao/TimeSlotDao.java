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
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMandatoryTimeSlot;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.QTimeSlot;
import it.cnr.iit.epas.models.TimeSlot;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * DAO per TimeSlot.
 */
@Component
public class TimeSlotDao extends DaoBase<TimeSlot> {

  private ContractDao contractDao;
  
  @Inject
  TimeSlotDao(Provider<EntityManager> emp, ContractDao contractDao) {
    super(emp);
    this.contractDao = contractDao;
  }

  /**
   * Il timeslot con id passato come parametro.
   *
   * @param id l'id del timeslot
   * @return il timeSlot, se esiste, con id passato come parametro.
   */
  public Optional<TimeSlot> byId(long id) {
    final QTimeSlot ts = QTimeSlot.timeSlot;
    return Optional.ofNullable(getQueryFactory()
        .selectFrom(ts)
        .where(ts.id.eq(id)).fetchOne());
  }
  
  /**
   * Tutte le fasce di orario attive predefinite (non associate a nessun ufficio).
   */
  public List<TimeSlot> getPredefinedEnabledTimeSlots() {

    final QTimeSlot ts = QTimeSlot.timeSlot;
    return getQueryFactory()
        .selectFrom(ts)
        .where(ts.disabled.eq(false).and(ts.office.isNull())).fetch();
  }

  
  /**
   * Tutti le fasce di orario associate all'office.
   */
  public List<TimeSlot> getEnabledTimeSlotsForOffice(Office office) {

    final QTimeSlot ts = QTimeSlot.timeSlot;
    return getQueryFactory()
        .selectFrom(ts)
        .where(ts.office.isNull().or(ts.office.eq(office))
            .and(ts.disabled.eq(false))).fetch();
  }
  
  /**
   * L'eventuale fascia oraria di presenza obbligatoria per la persona attiva
   * nel giorno.
   *
   * @param date data
   * @param person persona
   * @return la fascia oraria obbligatoria se presente
   */
  public Optional<TimeSlot> getMandatoryTimeSlot(LocalDate date, Person person) {

    Contract contract = contractDao.getContract(date, person);

    if (contract != null) {
      for (ContractMandatoryTimeSlot mts : contract.getContractMandatoryTimeSlots()) {

        if (DateUtility.isDateIntoInterval(date, new DateInterval(mts.getBeginDate(), mts.getEndDate()))) {
          return Optional.of(mts.timeSlot);
        }
      }
    }
    return Optional.empty();
  }
}