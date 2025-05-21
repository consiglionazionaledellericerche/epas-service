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

package it.cnr.iit.epas.tests.db.h2support.base;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import it.cnr.iit.epas.models.absences.definitions.DefaultAbsenceType;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Classe di supporto per i test con H2.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@Component
public class H2AbsenceSupport {
  
  private final AbsenceComponentDao absenceComponentDao;
  private final PersonDayDao personDayDao;
  
  @Inject
  public H2AbsenceSupport(AbsenceComponentDao absenceComponentDao, PersonDayDao personDayDao) {
    this.absenceComponentDao = absenceComponentDao;
    this.personDayDao = personDayDao;
  }

  /**
   * Istanza di una assenza. Per adesso non persistita perchè ai fini dei test non mandatoria 
   * (ma lo sarà presto). Serve il personDay.
   *
   * @param defaultAbsenceType absenceType assenza
   * @param date                  data
   * @param justifiedTypeName     tipo giustificativo
   * @param justifiedMinutes      minuti giustificati
   * @return istanza non persistente
   */
  @Transactional
  public Absence absenceInstance(DefaultAbsenceType defaultAbsenceType,
      LocalDate date, Optional<JustifiedTypeName> justifiedTypeName,
      Integer justifiedMinutes) {

    AbsenceType absenceType = absenceComponentDao
        .absenceTypeByCode(defaultAbsenceType.getCode()).get();
    JustifiedType justifiedType = null;
    if (justifiedTypeName.isPresent()) {
      justifiedType = absenceComponentDao.getOrBuildJustifiedType(justifiedTypeName.get());
    } else {
      Verify.verify(absenceType.getJustifiedTypesPermitted().size() == 1);
      justifiedType = absenceType.getJustifiedTypesPermitted().iterator().next();
    }
    Absence absence = new Absence();
    absence.date = date;
    absence.absenceType = absenceType;
    absence.justifiedType = justifiedType;
    absence.justifiedMinutes = justifiedMinutes;
    
    return absence;
  }

  /**
   * Istanza di una assenza. 
   *
   * @param defaultAbsenceType absenceType assenza
   * @param date                  data
   * @param justifiedTypeName     tipo giustificativo
   * @param justifiedMinutes      minuti giustificati
   * @return istanza non persistente
   */
  @Transactional
  public Absence absence(DefaultAbsenceType defaultAbsenceType,
      LocalDate date, Optional<JustifiedTypeName> justifiedTypeName,
      Integer justifiedMinutes, Person person) {

    Absence absence = 
        absenceInstance(defaultAbsenceType, date, justifiedTypeName, justifiedMinutes);

    absence.setPersonDay(getPersonDay(person, date));
    absenceComponentDao.persist(absence);

    return absence;
  }

  /**
   * Il personDay della persona a quella data.
   *
   * @param person persona
   * @param date data
   * @return personDay
   */
  @Transactional
  public PersonDay getPersonDay(Person person, LocalDate date) {
    Optional<PersonDay> personDay = personDayDao.getPersonDay(person, date);
    if (personDay.isPresent()) {
      log.debug("Prelevato personDay dal db {}", personDay.get());
      return personDay.get();
    }
    
    PersonDay newPersonDay = new PersonDay(person, date);
    personDayDao.persist(newPersonDay);
    return newPersonDay;
  }

  /**
   * Costruzione multipla di assenze all day.
   *
   * @param person persona
   * @param defaultAbsenceType tipo essere all day permitted
   * @param dates date
   * @return list
   */
  public List<Absence> multipleAllDayInstances(Person person, DefaultAbsenceType defaultAbsenceType,
      Set<LocalDate> dates) {

    List<Absence> absences = Lists.newArrayList();
    for (LocalDate date : dates) {
      absences
      .add(absence(defaultAbsenceType, date, Optional.of(JustifiedTypeName.all_day), 0, person));
    }
    
    return absences;
  }
  

}