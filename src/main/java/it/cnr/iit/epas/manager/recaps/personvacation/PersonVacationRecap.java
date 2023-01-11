/*
 * Copyright (C) 2022 Consiglio Nazionale delle Ricerche
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package it.cnr.iit.epas.manager.recaps.personvacation;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperContractMonthRecap;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.manager.services.absences.AbsenceForm;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.model.PeriodChain;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;


/**
 * Oggetto che modella il contenuto della vista contenente il riepilogo delle ferie e permessi
 *
 * @author Andrea Generosi
 */
@Slf4j
public class PersonVacationRecap {

  public Person person;
  public int year;

  boolean showVacationPeriods;

  // I riepiloghi delle ferie e permessi
  public List<VacationSituation> vacationSituations = Lists.newArrayList();
  public List<Contract> contracts;

  public GroupAbsenceType permissionGroup;
  public PeriodChain periodChain;
  public AbsenceForm categorySwitcher;

  // I riepiloghi mensili (uno per ogni contratto attivo nel mese)
  public List<IWrapperContractMonthRecap> contractMonths = Lists.newArrayList();

  // Le informazioni su eventuali assenze a recupero (es.: 91CE)
  //public List<AbsenceToRecoverDto> absencesToRecoverList = Lists.newArrayList();

  /**
   * Costruisce l'oggetto contenente tutte le informazioni da renderizzare nella pagina riepilogo
   * ferie e permessi.
   *
   * @param absenceComponentDao absenceComponentDao
   * @param absenceService      absenceService
   * @param wrapperFactory      wrapperFactory
   * @param year                year
   * @param person              person
   */
  public PersonVacationRecap(AbsenceComponentDao absenceComponentDao,
      AbsenceService absenceService, IWrapperFactory wrapperFactory,
      int year, Person person) {

    final long start = System.currentTimeMillis();
    log.trace("inizio creazione nuovo PersonVacationRecap. Person = {}, year = {}",
        person.getFullname(), year);
    this.person = person;
    this.year = year;

    contracts = wrapperFactory.create(person).orderedYearContracts(year);

    GroupAbsenceType vacationGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();

    for (Contract contract : contracts) {
      VacationSituation vacationSituation = absenceService.buildVacationSituation(contract, year,
          vacationGroup, Optional.empty(), false);
      vacationSituations.add(vacationSituation);
    }

    permissionGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.G_661.name()).get();

    periodChain = absenceService
        .residual(person, permissionGroup, LocalDate.now());

    categorySwitcher = absenceService
        .buildForCategorySwitch(person, LocalDate.now(), permissionGroup);

    showVacationPeriods = true;

    log.debug("periodChain {}>> {}", periodChain.periods.size(), periodChain.periods.get(0) );

    log.debug("fine creazione nuovo PersonVacationRecap in {} ms. Person = {}, year = {}",
        System.currentTimeMillis() - start, person.getFullname(), year);
  }
}
