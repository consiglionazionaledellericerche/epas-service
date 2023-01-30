/*
 * Copyright (C) 2023  Consiglio Nazionale delle Ricerche
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

import com.google.common.base.Verify;
import com.querydsl.core.BooleanBuilder;
import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.QContractWorkingTimeType;
import it.cnr.iit.epas.models.QWorkingTimeType;
import it.cnr.iit.epas.models.WorkingTimeType;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import it.cnr.iit.epas.models.dto.HorizontalWorkingTime;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import lombok.val;
import org.joda.time.DateTimeConstants;
import org.springframework.stereotype.Component;

/**
 * Dao per i WorkingTimeType.
 *
 * @author Dario Tagliaferri
 */
@Component
public class WorkingTimeTypeDao extends DaoBase<WorkingTimeType> {

  private final ContractDao contractDao;

  @Inject
  WorkingTimeTypeDao(Provider<EntityManager> emp, ContractDao contractDao) {
    super(emp);
    this.contractDao = contractDao;
  }

  public WorkingTimeTypeDay merge(WorkingTimeTypeDay wttd) {
    return emp.get().merge(wttd);
  }

  /**
   * Se office è present il tipo orario di con quella descrizione se esiste. Se office non è present
   * il tipo orario di default con quella descrizione.
   */
  public WorkingTimeType workingTypeTypeByDescription(String description,
      Optional<Office> office) {

    final QWorkingTimeType wtt = QWorkingTimeType.workingTimeType;
    final BooleanBuilder condition = new BooleanBuilder();

    if (office.isPresent()) {
      condition.and(wtt.description.eq(description).and(wtt.office.eq(office.get())));
    } else {
      condition.and(wtt.description.eq(description).and(wtt.office.isNull()));
    }

    return getQueryFactory().selectFrom(wtt).where(condition).fetchOne();

  }

  /**
   * Tutti gli orari.
   */
  public List<WorkingTimeType> getAllWorkingTimeType() {
    final QWorkingTimeType wtt = QWorkingTimeType.workingTimeType;
    return getQueryFactory().selectFrom(wtt).fetch();
  }

  /**
   * Tutti gli orari di lavoro default e quelli speciali dell'office.
   */
  public List<WorkingTimeType> getEnabledWorkingTimeTypeForOffice(Office office) {

    final QWorkingTimeType wtt = QWorkingTimeType.workingTimeType;
    return getQueryFactory()
        .selectFrom(wtt)
        .where(wtt.office.isNull().and(wtt.disabled.eq(false))
            .or(wtt.office.eq(office).and(wtt.disabled.eq(false)))).fetch();
  }

  /**
   * WorkingTimeType by id.
   *
   * @param id identificativo dell'orario di lavoro
   * @return l'orario di lavoro con id id.
   */
  public WorkingTimeType getWorkingTimeTypeById(Long id) {
    final QWorkingTimeType wtt = QWorkingTimeType.workingTimeType;
    return getQueryFactory().selectFrom(wtt)
        .where(wtt.id.eq(id)).fetchOne();
  }


  /**
   * La lista degli orari di lavoro di default.
   *
   * @return la lista degli orari di lavoro presenti di default sul database.
   */
  public List<WorkingTimeType> getDefaultWorkingTimeType(Optional<Boolean> disabled) {
    final QWorkingTimeType wtt = QWorkingTimeType.workingTimeType;
    val condition = new BooleanBuilder(wtt.office.isNull());
    if (disabled.isPresent()) {
      condition.and(wtt.disabled.eq(disabled.get()));
    }
    return getQueryFactory().selectFrom(wtt)
        .where(condition).orderBy(wtt.description.asc()).fetch();
  }


  /**
   * Il tipo orario per la persona attivo nel giorno.
   *
   * @param date data
   * @param person persona
   * @return il tipo orario se presente
   */
  public Optional<WorkingTimeType> getWorkingTimeType(LocalDate date, Person person) {

    Contract contract = contractDao.getContract(date, person);

    if (contract != null) {
      for (ContractWorkingTimeType cwtt : contract.getContractWorkingTimeType()) {

        if (DateUtility.isDateIntoInterval(
            date, new DateInterval(cwtt.getBeginDate(), cwtt.getEndDate()))) {
          return Optional.of(cwtt.getWorkingTimeType());
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Il tipo orario del giorno per la persona.
   *
   * @param date data
   * @param person persona
   * @return il tipo orario del giorno se presente
   */
  public Optional<WorkingTimeTypeDay> getWorkingTimeTypeDay(LocalDate date, Person person) {
    Optional<WorkingTimeType> wtt = getWorkingTimeType(date, person);
    if (!wtt.isPresent()) {
      return Optional.empty();
    }
    int index = date.getDayOfWeek().getValue() - 1;
    Verify.verify(index < wtt.get().getWorkingTimeTypeDays().size(),
        String.format("Definiti %d giorni nel WorkingTimeType %s, "
                + "richiesto giorno non presente con indice %d",
            wtt.get().getWorkingTimeTypeDays().size(), wtt.get(), index));

    Optional<WorkingTimeTypeDay> wttd =
        Optional.ofNullable(wtt.get().getWorkingTimeTypeDays().get(index));

    Verify.verify(wttd.isPresent());
    Verify.verify(wttd.get().getDayOfWeek() == date.getDayOfWeek().getValue());

    return wttd;
  }

  /**
   * ContractWorkingTimeType by id.
   *
   * @param id identificativo dell'associazione tra contratto e tipologia di orario di lavoro
   * @return associazione tra contratto e tipologia di orario di lavoro con l'id passato.
   */
  public ContractWorkingTimeType getContractWorkingTimeType(Long id) {
    final QContractWorkingTimeType cwtt = QContractWorkingTimeType.contractWorkingTimeType;
    return getQueryFactory().selectFrom(cwtt)
        .where(cwtt.id.eq(id)).fetchOne();
  }

  /**
   * Dal pattern orizzontale costruisce il tipo orario con ogni giorno di lavoro e persiste i dati.
   */
  public WorkingTimeType buildWorkingTimeType(
      final HorizontalWorkingTime pattern, final Office office) {
    WorkingTimeType wtt = new WorkingTimeType();

    wtt.setHorizontal(true);
    wtt.setDescription(pattern.getName());
    wtt.setOffice(office);
    wtt.setDisabled(false);
    wtt.setExternalId(pattern.getExternalId());
    wtt.setEnableAdjustmentForQuantity(pattern.isReproportionAbsenceCodesEnabled());
    emp.get().persist(wtt);
    //wtt.save();
    
    for (int i = 0; i < DateTimeConstants.DAYS_PER_WEEK; i++) {

      WorkingTimeTypeDay wttd = new WorkingTimeTypeDay();
      wttd.dayOfWeek = i + 1;
      wttd.workingTime =
          pattern.workingTimeHour * DateTimeConstants.SECONDS_PER_MINUTE
                      + pattern.workingTimeMinute;
      wttd.holiday = isHoliday(pattern, wttd);

      if (pattern.mealTicketEnabled) {
        wttd.mealTicketTime =
            pattern.mealTicketTimeHour
                        *
                        DateTimeConstants.SECONDS_PER_MINUTE
                        +
                        pattern.mealTicketTimeMinute;
        wttd.breakTicketTime = pattern.breakTicketTime;

        if (pattern.afternoonThresholdEnabled) {
          wttd.ticketAfternoonThreshold =
              pattern.ticketAfternoonThresholdHour
                          *
                          DateTimeConstants.SECONDS_PER_MINUTE
                          +
                          pattern.ticketAfternoonThresholdMinute;
          wttd.ticketAfternoonWorkingTime =
              pattern.ticketAfternoonWorkingTime;
        }
      }

      wttd.workingTimeType = wtt;
      emp.get().persist(wttd);
      //wttd.save();
    }
    return wtt;
  }

  private final boolean isHoliday(
      final HorizontalWorkingTime hwtt, final WorkingTimeTypeDay wttd) {
    return hwtt.holidays.contains(org.joda.time.LocalDate.now()
            .withDayOfWeek(wttd.dayOfWeek).dayOfWeek().getAsText());
  }
}
