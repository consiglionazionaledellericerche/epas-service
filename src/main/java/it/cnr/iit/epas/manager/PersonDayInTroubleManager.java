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

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonDayInTroubleDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.PersonDayInTrouble;
import it.cnr.iit.epas.models.enumerate.Troubles;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Manager per la gestione dei giorni con problemi di timbrature/assenze.
 */
@Slf4j
@Component
public class PersonDayInTroubleManager {

  private final Provider<IWrapperFactory> factory;
  private final PersonDayInTroubleDao personDayInTroubleDao;
  private final PersonDao personDao;
  private final ConfigurationManager configurationManager;
  private final Provider<EntityManager> emp;

  /**
   * Costruttore.
   *
   * @param personDayInTroubleDao       personDayInTroubleDao
   * @param configurationManager        configurationManager
   * @param factory                     factory
   */
  @Inject
  public PersonDayInTroubleManager(
      PersonDayInTroubleDao personDayInTroubleDao,
      PersonDao personDao,
      ConfigurationManager configurationManager,
      Provider<IWrapperFactory> factory,
      Provider<EntityManager> emp) {
    this.personDayInTroubleDao = personDayInTroubleDao;
    this.personDao = personDao;
    this.configurationManager = configurationManager;
    this.factory = factory;
    this.emp = emp;
  }

  /**
   * Crea il personDayInTrouble per quel giorno (se non esiste già).
   *
   * @param pd    giorno
   * @param cause causa
   */
  public void setTrouble(PersonDay pd, Troubles cause) {

    for (PersonDayInTrouble pdt : pd.getTroubles()) {
      if (pdt.getCause() == cause) {
        // Se esiste gia' non faccio nulla
        return;
      }
    }

    // Se non esiste lo creo
    PersonDayInTrouble trouble = new PersonDayInTrouble(pd, cause);

    personDayInTroubleDao.persist(trouble);
    //trouble.save();
    pd.getTroubles().add(trouble);

    log.info("Nuovo PersonDayInTrouble {} - {} - {}",
        pd.getPerson().getFullname(), pd.getDate(), cause);
  }


  /**
   * Metodo per rimuovere i problemi con una determinata causale all'interno del
   * personDay.
   */
  public void fixTrouble(final PersonDay pd, final Troubles cause) {

    Iterables.removeIf(pd.getTroubles(), pdt -> {
      if (pdt.getCause() == cause) {
        emp.get().remove(pdt);
        //pdt.delete();

        log.info("Rimosso PersonDayInTrouble {} - {} - {}",
            pd.getPerson().getFullname(), pd.getDate(), cause);
        return true;
      }
      return false;
    });
  }

  /**
   * Invia le email alle persone appartenenti alla lista, inerenti i giorni con problemi
   * nell'intervallo fromDate, toDate del tipo appartenente alla lista di troblesToSend.
   *
   * @param personList          le persone a cui inviare le email
   * @param fromDate            da
   * @param toDate              fino
   * @param troubleCausesToSend tipi di problemi da inviare.
   */
  public void sendTroubleEmails(List<Person> personList, LocalDate fromDate, LocalDate toDate,
      List<Troubles> troubleCausesToSend) {

    log.info("Invio mail troubles per i giorni dal {} al {}. I troubles considerati sono {}.",
        fromDate, toDate, troubleCausesToSend);

    for (Person person : personList) {

      final Optional<Contract> currentContract = factory.get().create(person).getCurrentContract();
      if (!currentContract.isPresent()) {
        log.error("Nessun contratto trovato attivo alla data odierna per {} - {} ", person,
            person.getOffice());
        continue;
      }
      DateInterval intervalToCheck = DateUtility.intervalIntersection(
          factory.get().create(currentContract.get()).getContractDatabaseInterval(),
          new DateInterval(fromDate, toDate));
      if (intervalToCheck == null) {
        continue;
      }

      List<PersonDayInTrouble> pdList = personDayInTroubleDao.getPersonDayInTroubleInPeriod(person,
          Optional.ofNullable(intervalToCheck.getBegin()),
          Optional.ofNullable(intervalToCheck.getEnd()),
          Optional.of(troubleCausesToSend));

      if (pdList.isEmpty()) {
        log.debug("{} (matricola = {})  non ha problemi da segnalare.", person, person.getNumber());
        continue;
      }

      try {
        // Invio della e-mail ...
        log.trace("Preparo invio mail per {}", person.getFullname());
        SimpleEmail simpleEmail = new SimpleEmail();
        String reply = (String) configurationManager
            .configValue(person.getOffice(), EpasParam.EMAIL_TO_CONTACT);

        if (!reply.isEmpty()) {
          simpleEmail.addReplyTo(reply);
        }
        simpleEmail.addTo(person.getEmail());
        simpleEmail.setSubject("ePas Controllo timbrature");
        simpleEmail.setMsg(troubleEmailBody(person, pdList, troubleCausesToSend));
        //FIXME: da correggere per il passaggio a spring boot
        //Mail.send(simpleEmail);

        log.info("Inviata mail a {} (matricola = {})  per segnalare i problemi {}",
            person, person.getNumber(), troubleCausesToSend);

        // Imposto il campo e-mails inviate ...
        for (PersonDayInTrouble pd : pdList) {
          pd.setEmailSent(true);
          emp.get().merge(pd);
          //pd.save();
        }

      } catch (Exception ex) {
        log.error("sendEmailToPerson({}, {}, {}): fallito invio email per {}",
            pdList, person, troubleCausesToSend, person.getFullname(), ex);
      }
    }
  }

  /**
   * Formatta il corpo della email da inviare al dipendente con i suoi troubles.
   *
   * @param person              persona
   * @param daysInTrouble       la lista dei personDaysInTrouble da segnalare.
   * @param troubleCausesToSend i troubles da inviare.
   * @return il corpo
   */
  private String troubleEmailBody(Person person, List<PersonDayInTrouble> daysInTrouble,
      List<Troubles> troubleCausesToSend) {

    final String dateFormatter = "dd/MM/YYYY";

    final StringBuilder message = new StringBuilder()
        .append(String.format("Gentile %s,\r\n", person.fullName()));

    // caso del Expandable
    if (troubleCausesToSend.contains(Troubles.NO_ABS_NO_STAMP)) {

      final List<String> formattedDates = daysInTrouble.stream()
          .filter(pdt -> pdt.getCause() == Troubles.NO_ABS_NO_STAMP)
          .map(pdt -> pdt.getPersonDay().getDate()).sorted()
          .map(localDate -> DateTimeFormatter.ofPattern(dateFormatter).format(localDate))
          .collect(Collectors.toList());

      if (!formattedDates.isEmpty()) {
        message.append("\r\nNelle seguenti date: ")
            .append(Joiner.on(", ").skipNulls().join(formattedDates)).append("\r\n")
            .append("Il sistema ePAS ha rilevato un caso di "
                + "mancanza di timbrature e di codici di assenza.\r\n");
      }
    }

    //caso del DarkNight
    if (troubleCausesToSend.contains(Troubles.UNCOUPLED_WORKING)) {

      final List<String> formattedDates = daysInTrouble.stream()
          .filter(pdt -> pdt.getCause() == Troubles.UNCOUPLED_WORKING)
          .map(pdt -> pdt.getPersonDay().getDate()).sorted()
          .map(localDate -> DateTimeFormatter.ofPattern(dateFormatter).format(localDate))
          .collect(Collectors.toList());

      if (!formattedDates.isEmpty()) {
        message.append("\r\nNelle seguenti date: ")
            .append(Joiner.on(", ").skipNulls().join(formattedDates)).append("\r\n")
            .append("Il sistema ePAS ha rilevato un caso di timbratura disaccoppiata.\r\n");
      }
    }

    message.append("\r\nLa preghiamo di contattare l'ufficio del"
        + " personale per regolarizzare la sua posizione.\r\n")
        .append("\r\nSaluti,\r\n")
        .append("Il team di ePAS");

    return message.toString();
  }

  /**
   * Elimina i personDayInTrouble che non appartengono ad alcun contratto valido ePAS. (Cioè
   * anche quelli che appartengono ad un contratto ma sono precedenti la sua inizializzazione).
   *
   * @param person persona
   */
  @Transactional
  @Async
  public CompletableFuture<List<PersonDayInTrouble>> cleanPersonDayInTrouble(Person person) {
    log.debug("Chiamata cleanPersonDayInTrouble per {}", person.getFullname());
    person = personDao.byId(person.getId()).get();
    final List<PersonDayInTrouble> pdtList =
        personDayInTroubleDao.getPersonDayInTroubleInPeriod(
            person, Optional.empty(), Optional.empty(), Optional.empty());
    log.debug("Trovati {} PersonDayInTrouble per {}", pdtList.size(), person.getFullname());
    List<IWrapperContract> wrapperContracts = 
        person.getContracts().stream()
          .map(contract -> factory.get().create(contract))
          .collect(Collectors.toList());

    List<PersonDayInTrouble> deleted = Lists.newArrayList();
    for (PersonDayInTrouble pdt : pdtList) {
      boolean toDelete = wrapperContracts.stream()
          .noneMatch(wrContract -> DateUtility.isDateIntoInterval(pdt.getPersonDay().getDate(),
              wrContract.getContractDatabaseInterval()));
      if (toDelete) {
        log.info("Eliminato PersonDayInTrouble di {} data {}", 
            person.fullName(), pdt.getPersonDay().getDate());
        personDayInTroubleDao.delete(pdt);
        deleted.add(pdt);
      }
    }
    return CompletableFuture.completedFuture(deleted);
  }
}