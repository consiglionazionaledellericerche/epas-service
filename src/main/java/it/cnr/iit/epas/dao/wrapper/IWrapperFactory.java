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
package it.cnr.iit.epas.dao.wrapper;

import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.TimeSlot;
import it.cnr.iit.epas.models.WorkingTimeType;

/**
 * Interfaccia per generico factory di alcuni componenti di ePAS.
 *
 * @author Marco Andreini
 */
public interface IWrapperFactory {

  IWrapperPerson create(Person person);

  IWrapperContract create(Contract contract);

  IWrapperWorkingTimeType create(WorkingTimeType wtt);
  
  IWrapperTimeSlot create(TimeSlot ts);

  IWrapperCompetenceCode create(CompetenceCode cc);

  IWrapperOffice create(Office office);

  IWrapperPersonDay create(PersonDay pd);

  IWrapperContractMonthRecap create(ContractMonthRecap cmr);

  IWrapperContractWorkingTimeType create(ContractWorkingTimeType cwtt);
}
