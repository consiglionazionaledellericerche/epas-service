/*
 * Copyright (C) 2024  Consiglio Nazionale delle Ricerche
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
package it.cnr.iit.epas.security;

import java.util.Optional;

import org.springframework.stereotype.Service;

import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.models.base.BaseEntity;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class SecurityService {

  public enum EntityType {
    Office, Person, Absence, PersonDay
  }

  private final SecurityRules rules;

  private final OfficeDao officeDao;
  private final PersonDao personDao;
  private final AbsenceDao absenceDao;
  private final PersonDayDao personDayDao;
  
  public Boolean secureCheck(String method, String path,
      Optional<EntityType> entityType, Optional<EntityType> targetType,
      Optional<Long> id) throws Exception {

    BaseEntity entity = null;
    Object entityToTarget = null;

    log.debug("SecurityService::secureCheck method= {}, path = {}, id = {}, target={}",
        method, path, id, entityType);

    if (entityType.isPresent() && id.isPresent()) {
      switch (entityType.get()) {
      case Office: {
        val office = officeDao.getOfficeById(id.get());
        entity = office; 
        entityToTarget = office;
        break;
      }
      case Person: {
        val person = personDao.byId(id.get()).orElse(null);
        entity = person;
        if (targetType.isPresent() && targetType.get().equals(EntityType.Office)) {
          entityToTarget = person.getOffice();
        } else {
          entityToTarget = person;
        }
        break;
      }
      case Absence: {
        val absence = absenceDao.byId(id.get()).orElse(null);
        entity = absence;
        if (targetType.isPresent()) {
          if (targetType.get().equals(EntityType.Absence)) {
            entityToTarget = absence;
          } else if (targetType.get().equals(EntityType.Office)) {
            entityToTarget = absence.getPersonDay().getPerson().getOffice();
          }
        } else {
          //Il default per i controlli sulle assenze è la verifica sulla Person.
          entityToTarget = absence.getPersonDay().getPerson();
        }
        break;
      }
      case PersonDay: {
        val personDay = personDayDao.getPersonDayById(id.get());
        entity = personDay;
        if (targetType.isPresent()) {
          if (targetType.get().equals(EntityType.PersonDay)) {
            entityToTarget = personDay;
          } else if (targetType.get().equals(EntityType.Office)) {
            entityToTarget = personDay.getPerson().getOffice();
          }
        } else {
          //Il default per i controlli sui personDay è la verifica sulla Person.
          entityToTarget = personDay.getPerson();
        }
        break;
      }
      default:
        throw new IllegalArgumentException("Unexpected value: " + entityType.get());
      }
    }

    log.debug("SecurityService::secureCheck method= {}, path = {}, id = {}, "
        + "targetFromObject={}, targetToOject={}",
        method, path, id, entity, entityToTarget);

    //Le drools sulle assenze non controllano come target l'assenza ma la person
    return rules.check(method, path, entityToTarget);
  }

}