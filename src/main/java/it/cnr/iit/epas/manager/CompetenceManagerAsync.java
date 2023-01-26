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

package it.cnr.iit.epas.manager;

import it.cnr.iit.epas.dao.CompetenceCodeDao;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingRecap;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingRecapFactory;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonCompetenceCodes;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Contiene in metodo da lanciare in asincrono dal CompetenceManager.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@Service
public class CompetenceManagerAsync {

  private final CompetenceCodeDao competenceCodeDao;
  private final PersonStampingRecapFactory stampingsRecapFactory;
  private final CompetenceDao competenceDao;
  private final PersonDao personDao;

  @Inject
  CompetenceManagerAsync(
      CompetenceCodeDao competenceCodeDao, 
      PersonStampingRecapFactory stampingsRecapFactory,
      CompetenceDao competenceDao, PersonDao personDao) {
    this.competenceCodeDao = competenceCodeDao;
    this.stampingsRecapFactory = stampingsRecapFactory;
    this.competenceDao = competenceDao;
    this.personDao = personDao;
  }

  /**
   * Effettua automaticamente l'aggiornamento del valore per la competenza a presenza mensile 
   * passata come parametro.
   *
   * @param person la persona su cui fare i conteggi
   * @param yearMonth l'anno/mese in cui fare i conteggi
   * @param code il codice di competenza da riconteggiare
   */
  public CompletableFuture<Boolean> applyBonusPerPerson(
      Person person, YearMonth yearMonth, CompetenceCode code) {
    person = personDao.getPersonById(person.getId());
    LocalDate date = LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1);
    Optional<PersonCompetenceCodes> pcc =
        competenceCodeDao.getByPersonAndCodeAndDate(person, code, date);
    if (pcc.isPresent()) {

      switch (code.limitType) {
        case onMonthlyPresence:
          PersonStampingRecap psDto = stampingsRecapFactory
              .create(person, yearMonth.getYear(), yearMonth.getMonthValue(), true);
          addSpecialCompetence(person, yearMonth, code, Optional.ofNullable(psDto));
          return CompletableFuture.completedFuture(true);
        case entireMonth:
          addSpecialCompetence(person, yearMonth, code, Optional.<PersonStampingRecap>empty());
          return CompletableFuture.completedFuture(true);
        default:
          return CompletableFuture.completedFuture(false);
      }
    } else {
      log.warn("La competenza {} non risulta abilitata per il dipendente {} nel mese "
          + "e nell'anno selezionati", code, person.fullName());
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * assegna le competenze speciali (su presenza mensile o assegnano interamente un mese).
   *
   * @param person la persona a cui assegnare la competenza
   * @param yearMonth l'anno mese per cui assegnare la competenza
   * @param code il codice competenza da assegnare
   * @param psDto (opzionale) se presente serve al calcolo dei giorni di presenza
   */
  private void addSpecialCompetence(Person person, YearMonth yearMonth, CompetenceCode code, 
      Optional<PersonStampingRecap> psDto) {
    int value = 0;
    if (psDto.isPresent()) {
      value = psDto.get().basedWorkingDays;
    } else {
      value = code.limitValue;
    }
    Optional<Competence> competence = competenceDao
        .getCompetence(person, yearMonth.getYear(), yearMonth.getMonthValue(), code);
    if (competence.isPresent()) {
      competence.get().valueApproved = value;
      competenceDao.save(competence.get());
    } else {
      Competence comp = new Competence();
      comp.competenceCode = code;
      comp.setPerson(person);
      comp.year = yearMonth.getYear();
      comp.month = yearMonth.getMonthValue();
      comp.valueApproved = value;
      competenceDao.persist(comp);
    }
    log.debug("Assegnata competenza a {}", person.fullName());
  }
}
