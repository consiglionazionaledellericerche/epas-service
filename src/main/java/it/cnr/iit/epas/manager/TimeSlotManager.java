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

import com.google.common.base.Verify;
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.PersonShiftDayDao;
import it.cnr.iit.epas.models.PersonDay;
import javax.inject.Inject;
import javax.inject.Provider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Gestione dei time slot obbligatori.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@Service
public class TimeSlotManager {

  private final ContractDao contractDao;
  private final PersonShiftDayDao personShiftDayDao;
  private final AbsenceDao absenceDao;
  private final Provider<PersonDayManager> personDayManager;

  @Inject
  TimeSlotManager(ContractDao contractDao, PersonShiftDayDao personShiftDayDao,
      AbsenceDao absenceDao, Provider<PersonDayManager> personDayManager) {
    this.contractDao = contractDao;
    this.personShiftDayDao = personShiftDayDao;
    this.absenceDao = absenceDao;
    this.personDayManager = personDayManager;
  }

  /**
   * Questo metodo non fa niente ma viene utilizzato per attivare un listener
   * alla fine della transazione (@AfterRequest), in modo che venga gestito con un processo 
   * separato in una nuova transazione il metodo checkAndManageMandatoryTimeSlot.
   *
   * @see it.cnr.iit.epas.manager.listeners.TimeSlotManagerListener
   */
  public PersonDay activateAfterRequesCheckAndManageMandatoryTimeSlot(PersonDay personDay) {
    return personDay;
  }

  /**
   * Verifica e gestisce eventuali Permessi brevi legati a fascie orarie obbligatorie
   * per il dipendente.
   *
   * @param personDay il personday da verificare per l'eventuale permesso breve
   */
  public void checkAndManageMandatoryTimeSlot(PersonDay personDay) {
    Verify.verifyNotNull(personDay);
    Verify.verifyNotNull(personDay.getPerson());

    if (personDay.isIgnoreShortLeave()) {
      log.info("Calcolo del permesso per breve per il giorno {} di {} ignorato come "
          + "da configurazione del person day id={}", 
          personDay.getDate(), personDay.getPerson().getFullname(), personDay.getId());
      return;
    }

    val mandatoryTimeSlot = contractDao
        .getContractMandatoryTimeSlot(personDay.getDate(), personDay.getPerson().getId());
    if (!mandatoryTimeSlot.isPresent()) {
      log.trace("Le timbrature di {} del giorno {} NON necessitano di controlli "
          + "sulla fascia obbligatoria",
          personDay.getPerson(), personDay.getDate());
      return;
    }
    log.trace("Le timbrature di {} del giorno {} necessitano di controlli "
        + "sulla fascia obbligatoria", 
        personDay.getPerson(), personDay.getDate());

    //I turni non hanno vincoli di fascia obbligatoria nei giorni in cui sono in turno
    boolean inShift = personShiftDayDao
        .getPersonShiftDay(personDay.getPerson(), personDay.getDate()).isPresent();

    //Se sono presenti assenze giornalieri la fascia obbigatoria non deve essere 
    //rispettata anche in presenta di timbrature
    boolean isAllDayAbsencePresent = personDayManager.get().isAllDayAbsences(personDay); 

    val previousShortPermission = 
        personDay.getAbsences().stream().filter(a -> a.absenceType.code.equals("PB")).findFirst();

    if (inShift || isAllDayAbsencePresent || personDay.isHoliday()) {
      if (previousShortPermission.isPresent()) {
        //Viene fatta prima la merge perché l'assenza è detached
        val previousShortPemissionToDelete = 
            absenceDao.merge(previousShortPermission.get());
        absenceDao.delete(previousShortPemissionToDelete);
        log.info("Rimosso permesso breve di {} minuti nel giorno {} per {} poiché sono presenti"
            + " assenze giornaliere oppure il dipendente è in turno, "
            + "oppure è un giorno festivo.",
            previousShortPermission.get().getJustifiedMinutes(), personDay.getDate(), 
            personDay.getPerson().getFullname());
        return;
      } else {
        log.debug("Le timbrature di {} del giorno {} NON necessitano di controlli sulla fascia "
            + "obbligatoria poichè sono presenti assenze giornaliere oppure il dipendente "
            + "è in turno, oppure è un giorno festivo.",
            personDay.getPerson(), personDay.getDate());
        return;
      }
    }

    val shortPermission = 
        personDayManager.get().buildShortPermissionAbsence(
            personDay, mandatoryTimeSlot.get().timeSlot);

    if (!shortPermission.isPresent() && !previousShortPermission.isPresent()) {
      return;
    }

    if (shortPermission.isPresent() && !previousShortPermission.isPresent()) {
      log.info("Inserito permesso breve di {} minuti nel giorno {} per {}",
          shortPermission.get().justifiedMinutes, personDay.getDate(), 
          personDay.getPerson().getFullname());
      absenceDao.save(shortPermission.get());
      return;
    }

    if (!shortPermission.isPresent() && previousShortPermission.isPresent()) {
      //Viene fatta prima la merge perché l'assenza è detached
      val previousShortPemissionToDelete = 
          absenceDao.merge(previousShortPermission.get());
      absenceDao.delete(previousShortPemissionToDelete);
      log.info("Rimosso permesso breve di {} minuti nel giorno {} per {}",
          previousShortPermission.get().justifiedMinutes, personDay.getDate(), 
          personDay.getPerson().getFullname());
      return;
    }

    //Se era già presente un permesso breve di durata diversa dall'attuale viene aggiornato 
    //il precedente permesso breve
    if (!previousShortPermission.get().justifiedMinutes
        .equals(shortPermission.get().justifiedMinutes)) {
      val newShortPermission = absenceDao.merge(previousShortPermission.get());
      newShortPermission.setJustifiedMinutes(shortPermission.get().getJustifiedMinutes());
      absenceDao.save(newShortPermission);
      log.debug("Permesso breve esistente nel giorno {} per {}, aggiornato da {} a {} minuti", 
          personDay.getDate(), personDay.getPerson().getFullname(),
          previousShortPermission.get().justifiedMinutes, 
          shortPermission.get().justifiedMinutes);

    }
  }
}