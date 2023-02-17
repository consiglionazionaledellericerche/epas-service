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

package it.cnr.iit.epas.manager.recaps.personvacation;

import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.absences.AbsenceComponentDao;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.manager.services.absences.model.VacationSituation.VacationSummary.TypeSummary;
import it.cnr.iit.epas.models.Person;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Factory per PersonVacationRecap.
 */
@Component
public class PersonVacationSummaryFactory {

  private final ContractDao contractDao;
  private final AbsenceComponentDao absenceComponentDao;
  private final AbsenceService absenceService;

  /**
   * Costruttore per l'injection.
   */
  @Inject
  PersonVacationSummaryFactory(ContractDao contractDao, AbsenceComponentDao absenceComponentDao,
      AbsenceService absenceService) {

    this.contractDao = contractDao;
    this.absenceComponentDao = absenceComponentDao;
    this.absenceService = absenceService;
  }

  /**
   * Costruisce il riepilogo mensile delle timbrature.
   */
  public PersonVacationSummary create(
      Person person, int year, Long contractId, TypeSummary typeSummary) {

    return new PersonVacationSummary(contractDao, absenceComponentDao, absenceService,
        year, person, contractId, typeSummary);
  }

}
