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

package it.cnr.iit.epas.manager.recaps.personstamping;

import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.manager.PersonDayManager;
import it.cnr.iit.epas.manager.PersonManager;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.security.SecureUtils;
import it.cnr.iit.epas.security.SecurityRules;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Factory per PersonStampingRecap.
 */
@Component
public class PersonStampingRecapFactory {

  private final PersonDayManager personDayManager;
  private final PersonDayDao personDayDao;
  private final PersonManager personManager;
  private final PersonStampingDayRecapFactory stampingDayRecapFactory;
  private final IWrapperFactory wrapperFactory;

  private final SecurityRules rules;
  private final SecureUtils secureUtils;

  /**
   * Costruttore per l'injection.
   */
  @Inject
  PersonStampingRecapFactory(PersonDayManager personDayManager,
                             PersonDayDao personDayDao,
                             PersonManager personManager,
                             IWrapperFactory wrapperFactory,
                             PersonStampingDayRecapFactory stampingDayRecapFactory,
                              SecurityRules rules,
                              SecureUtils secureUtils) {

    this.personDayManager = personDayManager;
    this.personDayDao = personDayDao;
    this.personManager = personManager;
    this.stampingDayRecapFactory = stampingDayRecapFactory;
    this.wrapperFactory = wrapperFactory;
    this.rules = rules;
    this.secureUtils = secureUtils;
  }

  /**
   * Costruisce il riepilogo mensile delle timbrature.
   */
  public PersonStampingRecap create(Person person, int year, int month, 
      boolean considerExitingNow) {

    return new PersonStampingRecap(personDayManager, personDayDao,
        personManager, stampingDayRecapFactory,
        wrapperFactory, rules, secureUtils, year, month, person, considerExitingNow);
  }

}
