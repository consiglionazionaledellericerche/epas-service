/*
 * Copyright (C) 2023 Consiglio Nazionale delle Ricerche
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

import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary.TypeSummary;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.GroupAbsenceType;
import it.cnr.iit.epas.models.absences.definitions.DefaultGroup;
import java.util.Optional;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


/**
 * Oggetto che modella il contenuto della vista contenente il riepilogo delle ferie e permessi.
 *
 * @author Andrea Generosi
 */
@ToString
@Slf4j
public class PersonVacationSummary {

  public Person person;
  public int year;
  public Long contractId;
  public TypeSummary typeSummary;
  public VacationSummary vacationSummary;

  /**
   * Costruisce l'oggetto contenente tutte le informazioni da renderizzare nella pagina riepilogo
   * ferie e permessi.
   *
   * @param absenceComponentDao absenceComponentDao
   * @param absenceService      absenceService
   * @param year                year
   * @param person              person
   * @param contractId          contractId
   * @param typeSummary         typeSummary
   * */
  public PersonVacationSummary(ContractDao contractDao, AbsenceComponentDao absenceComponentDao,
      AbsenceService absenceService, int year, Person person,
      Long contractId, TypeSummary typeSummary) {

    final long start = System.currentTimeMillis();
    log.trace("inizio creazione nuovo PersonVacationSummary. "
        + "Person = {}, year = {}, contractId = {}, typeSummary = {}",
        person.getFullname(), year, contractId, typeSummary);
    this.person = person;
    this.year = year;
    this.contractId = contractId;
    this.typeSummary = typeSummary;

    Contract contract = contractDao.getContractById(contractId);

    log.debug("contract>>>> {} this.typeSummary {}", contract, this.typeSummary);

    GroupAbsenceType vacationGroup = absenceComponentDao
        .groupAbsenceTypeByName(DefaultGroup.FERIE_CNR.name()).get();

    if (this.typeSummary.equals(TypeSummary.PERMISSION)) {
      vacationSummary = absenceService.buildVacationSituation(contract, year,
          vacationGroup, Optional.empty(), false).permissions;
    } else {
      vacationSummary = absenceService.buildVacationSituation(contract, year,
          vacationGroup, Optional.empty(), false).currentYear;
    }

    log.debug("fine creazione nuovo PersonVacationSummary in {} ms. Person = {}, year = {}",
        System.currentTimeMillis() - start, person.getFullname(), year);
  }
}
