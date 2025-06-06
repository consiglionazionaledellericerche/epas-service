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

package it.cnr.iit.epas.manager;

import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.google.common.collect.Maps;
import it.cnr.iit.epas.dao.GeneralSettingDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dao.StampingDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dao.wrapper.IWrapperPersonDay;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingDayRecap;
import it.cnr.iit.epas.manager.recaps.personstamping.PersonStampingDayRecapFactory;
import it.cnr.iit.epas.messages.Messages;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.Stamping;
import it.cnr.iit.epas.models.Stamping.WayType;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import it.cnr.iit.epas.models.dto.TeleworkDto;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import it.cnr.iit.epas.models.enumerate.TeleworkStampTypes;
import it.cnr.iit.epas.models.exports.StampingFromClient;
import it.cnr.iit.epas.security.SecureUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Manager per la gestione delle timbrature.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@Service
public class StampingManager {

  private final PersonDayDao personDayDao;
  private final PersonDao personDao;
  private final PersonDayManager personDayManager;
  private final PersonStampingDayRecapFactory stampingDayRecapFactory;
  private final ConsistencyManager consistencyManager;
  private final StampingDao stampingDao;
  private final NotificationManager notificationManager;
  private final IWrapperFactory wrapperFactory;
  private final GeneralSettingDao generalSettingDao;
  private final SecureUtils secureUtils;
  private final Messages messages;

  /**
   * Injection.
   *
   * @param personDayDao il dao per cercare i personday
   * @param personDao il dao per cercare le persone
   * @param personDayManager il manager per lavorare sui personday
   * @param stampingDayRecapFactory il factory per lavorare sugli stampingDayRecap
   * @param consistencyManager il costruttore dell'injector.
   */
  @Inject
  public StampingManager(PersonDayDao personDayDao,
      PersonDao personDao,
      PersonDayManager personDayManager,
      PersonStampingDayRecapFactory stampingDayRecapFactory,
      ConsistencyManager consistencyManager, StampingDao stampingDao,
      NotificationManager notificationManager, IWrapperFactory wrapperFactory,
      GeneralSettingDao generalSettingDao, 
      SecureUtils secureUtils, Messages messages) {

    this.personDayDao = personDayDao;
    this.personDao = personDao;
    this.personDayManager = personDayManager;
    this.stampingDayRecapFactory = stampingDayRecapFactory;
    this.consistencyManager = consistencyManager;
    this.stampingDao = stampingDao;
    this.notificationManager = notificationManager;
    this.wrapperFactory = wrapperFactory;
    this.generalSettingDao = generalSettingDao;
    this.secureUtils = secureUtils;
    this.messages = messages;
  }



  /**
   * Metodo che verifica se la timbratura precedente a quella che si vuole inserire è con 
   * causale lavoro fuori sede per permettere al chiamante di inserire una timbratura
   * "fittizia" di fine lavoro fuori sede un minuto prima di quella che si sta inserendo
   * nel caso in cui la precedente e l'attuale abbiano lo stesso verso (ingresso).
   *
   * @param personDay il personDay a cui si vuole associare la timbratura
   * @param stampingFromClient il dto creato a partire dalla timbratura ricevuta dal client
   * @return true se la timbratura precedente a quella che si sta per inserire è con causale
   *     Lavoro fuori sede ed è dello stesso verso, false altrimenti.
   */
  private boolean checkOffSite(PersonDay personDay, StampingFromClient stampingFromClient) {
    IWrapperPersonDay wrPersonDay = wrapperFactory.create(personDay);
    Stamping stamp = wrPersonDay.getLastStamping(); 
    if (stamp == null) {
      return false;
    }
    if (stamp.getStampType() != null && stamp.getStampType().equals(StampTypes.LAVORO_FUORI_SEDE)
        && stamp.getWay().equals(WayType.in)
        && stampingFromClient.getStampType() == null
        && stampingFromClient.getInOut() == 0) {
      return true;
    }
    return false;
  }


  /**
   * Crea il tempo. Il campo time è già stato validato HH:MM o HHMM
   */
  public LocalDateTime deparseStampingDateTime(LocalDate date, String time) {

    time = time.replaceAll(":", "");
    Integer hour = Integer.parseInt(time.substring(0, 2));
    Integer minute = Integer.parseInt(time.substring(2, 4));
    return LocalDateTime.of(date.getYear(), date.getMonthValue(),
        date.getDayOfMonth(), hour, minute, 0);
  }

  /**
   * Crea il tempo. Il campo time è già stato validato HH:MM o HHMM
   */
  public LocalDateTime deparseStampingDateTimeAsJavaTime(LocalDate date, String time) {

    time = time.replaceAll(":", "");
    Integer hour = Integer.parseInt(time.substring(0, 2));
    Integer minute = Integer.parseInt(time.substring(2, 4));
    return java.time.LocalDateTime.of(date.getYear(), date.getMonthValue(),
        date.getDayOfMonth(), hour, minute, 0);
  }

  /**
   * Calcola il numero massimo di coppie ingresso/uscita in un giorno specifico per tutte le persone
   * presenti nella lista di persone attive a quella data.
   *
   * @param date data
   * @param activePersonsInDay lista delle persone da verificare
   * @return numero di coppie
   */
  public final int maxNumberOfStampingsInMonth(final LocalDate date,
      final List<Person> activePersonsInDay) {

    int max = 0;

    for (Person person : activePersonsInDay) {
      Optional<PersonDay> pd = personDayDao.getPersonDay(person, date);

      if (pd.isPresent()) {
        PersonDay personDay = pd.get();
        if (max < personDayManager.numberOfInOutInPersonDay(personDay)) {
          max = personDayManager.numberOfInOutInPersonDay(personDay);
        }
      }
    }

    if (max < 2) {
      max = 2;
    }
    return max;
  }

  /**
   * Controlla che la timbratura da inserire non sia troppo nel passato.
   */
  public boolean isTooFarInPast(LocalDateTime dateTime) {
    return dateTime.compareTo(
      LocalDateTime.now().minusDays(
        generalSettingDao.generalSetting().getMaxDaysInPastForRestStampings())) < 0;
  }

  /**
   * Metodo che salva la timbratura.
   *
   * @param stamping la timbratura da persistere
   * @param person la persona a cui associare la timbratura
   * @return la stringa contenente un messaggio di errore se il salvataggio non è andato a
   *     buon fine, stringa vuota altrimenti.
   */
  @Transactional
  public String persistStamping(Stamping stamping, 
      Person person, User currentUser, boolean newInsert, boolean isTeleworkStamping) {
    String result = "";

    val alreadyPresentStamping = 
        stampingDao.getStamping(stamping.getDate(), person, stamping.getWay());
    //Se la timbratura allo stesso orario e con lo stesso verso non è già presente o è una modifica
    //alla timbratura esistente allora creo/modifico la timbratura.
    log.info("alreadyPresentStamping.isPresent() = {}, "
        + "alreadyPresentStamping.get().getId().equals(stamping.getId()) = {}",
        alreadyPresentStamping.isPresent(), 
        alreadyPresentStamping.get().getId().equals(stamping.getId()));
    if (!alreadyPresentStamping.isPresent() 
        || alreadyPresentStamping.get().getId().equals(stamping.getId())) {

      if (!currentUser.isSystemUser()) {
        if (currentUser.hasRoles(Role.PERSONNEL_ADMIN)) {
          stamping.setMarkedByEmployee(false);
          stamping.setMarkedByAdmin(true);
        } else {
          stamping.setMarkedByAdmin(false);
          if (isTeleworkStamping) {
            stamping.setMarkedByTelework(true);
            stamping.setMarkedByEmployee(false);
          } else {
            stamping.setMarkedByTelework(false);
            stamping.setMarkedByEmployee(true);
          }
        }
      }
      stampingDao.save(stamping);

      consistencyManager
          .updatePersonSituation(stamping.getPersonDay().getPerson().getId(), 
          stamping.getPersonDay().getDate());

      notificationManager
      .notificationStampingPolicy(currentUser, stamping, newInsert, !newInsert, false);
    } else {
      if ((stamping.getStampType() != null 
          && !stamping.getStampType().equals(alreadyPresentStamping.get().getStampType())) 
          || (stamping.getStampType() == null 
          && alreadyPresentStamping.get().getStampType() != null)) {
        result = "Timbratura già presente ma con causale diversa, "
            + "modificare la timbratura presente.";  
      } else {
        result = "Timbratura ignorata perché già presente.";
      }
    }

    return result;
  }
  
  /**
   * Crea l'oggetto stamping da persistere sul db di ePAS.
   *
   * @param stamping il dto da cui creare la timbratura
   * @param pd il personday cui associare la timbratura
   * @param time l'orario della timbratura
   * @return l'oggetto stamping correttamente formato.
   */
  public Stamping generateStampingFromTelework(TeleworkDto stamping, PersonDay pd, String time) {
    Stamping persistStamping = new Stamping(pd, 
        deparseStampingDateTime(pd.getDate(), time));
    switch (stamping.getStampType()) {
      case INIZIO_TELELAVORO:
        persistStamping.setWay(WayType.in);
        break;
      case FINE_TELELAVORO:
        persistStamping.setWay(WayType.out);
        break;
      case INIZIO_PRANZO_TELELAVORO:
        persistStamping.setWay(WayType.out);
        persistStamping.setStampType(StampTypes.PAUSA_PRANZO);
        break;
      case FINE_PRANZO_TELELAVORO:
        persistStamping.setWay(WayType.in);
        persistStamping.setStampType(StampTypes.PAUSA_PRANZO);
        break;
      case INIZIO_INTERRUZIONE:
        persistStamping.setWay(WayType.out);
        break;
      case FINE_INTERRUZIONE:
        persistStamping.setWay(WayType.in);
        break;
      default:
        break;
    }
    
    return persistStamping;
  }

  /**
   * Ritorna il verso della timbratura corrispondente a quella del dto.
   *
   * @param dto il dto contenente le informazioni della timbratura in telelavoro
   * @return il verso della timbratura corrispondente.
   */
  public WayType retrieveWayFromTeleworkStamping(TeleworkDto dto) {
    if (dto.getStampType().equals(TeleworkStampTypes.INIZIO_TELELAVORO)
        || dto.getStampType().equals(TeleworkStampTypes.FINE_PRANZO_TELELAVORO)
        || dto.getStampType().equals(TeleworkStampTypes.FINE_INTERRUZIONE)) {
      return WayType.in;
    } else {
      return WayType.out;
    }
  }

  /**
   * Stamping dal formato del client al formato ePAS.
   */
  @Transactional
  public Optional<Stamping> createStampingFromClient(StampingFromClient stampingFromClient,
      boolean recompute) {

    // Check della richiesta
    Verify.verifyNotNull(stampingFromClient);
    Verify.verifyNotNull(stampingFromClient.getDateTime());

    final Person person = stampingFromClient.getPerson();
    // Recuperare il personDay
    PersonDay personDay = personDayManager.getOrCreateAndPersistPersonDay(
        stampingFromClient.getPerson(), stampingFromClient.getDateTime().toLocalDate());

    // Check stamping duplicata
    WayType way = stampingFromClient.getInOut() == 0 ? WayType.in : WayType.out;
    if (stampingDao.getStamping(stampingFromClient.getDateTime(), person, way).isPresent()) {
      log.info("Timbratura delle {} già presente per {} (matricola = {}) ",
          stampingFromClient.getDateTime(), person, person.getNumber());
      return Optional.empty();
    }

    //controllo se la precedente timbratura è per lavoro fuori sede e di ingresso
    if (checkOffSite(personDay, stampingFromClient)) {
      log.info("Il sistema inserisce una timbratura in automatico per {} "
          + "per fine lavoro fuori sede alle ore {}.", 
          person.fullName(), stampingFromClient.getDateTime().minusMinutes(1));
      Stamping stamping = new Stamping(personDay, stampingFromClient.getDateTime().minusMinutes(1));
      stamping.setDate(stampingFromClient.getDateTime().minusMinutes(1));
      stamping.setMarkedByAdmin(stampingFromClient.isMarkedByAdmin());
      stamping.setWay(WayType.out);
      stamping.setStampType(StampTypes.LAVORO_FUORI_SEDE);
      stamping.setNote(messages.get("stampingNote"));
      stampingDao.save(stamping);
    }

    //Creazione stamping e inserimento
    Stamping stamping = new Stamping(personDay, stampingFromClient.getDateTime());
    stamping.setDate(stampingFromClient.getDateTime());
    stamping.setMarkedByAdmin(stampingFromClient.isMarkedByAdmin());
    stamping.setWay(way);
    stamping.setStampType(stampingFromClient.getStampType());
    stamping.setStampingZone(
        (stampingFromClient.getZona() != null && !stampingFromClient.getZona().equals("")) 
        ? stampingFromClient.getZona() : null);
    stamping.setNote(stampingFromClient.getNote());
    stamping.setReason(stampingFromClient.getReason());
    stamping.setPlace(stampingFromClient.getPlace());
    stampingDao.save(stamping);

    log.info("Inserita timbratura {} per {} (matricola = {}) ",
        stamping.getLabel(), person, person.getNumber());

    // Ricalcolo
    if (recompute) {
      consistencyManager.updatePersonSituation(person.getId(), personDay.getDate());
    }

    return Optional.of(stamping);
  }

  /**
   * La lista dei PersonStampingDayRecap renderizzata da presenza giornaliera.
   */
  public List<PersonStampingDayRecap> populatePersonStampingDayRecapList(
      List<Person> activePersonsInDay,
      LocalDate dayPresence, int numberOfInOut) {

    List<PersonStampingDayRecap> daysRecap = new ArrayList<>();
    for (Person person : activePersonsInDay) {

      person = personDao.getPersonById(person.getId());
      Optional<PersonDay> pd = personDayDao.getPersonDay(person, dayPresence);

      if (pd.isPresent()) {
        personDayManager.setValidPairStampings(pd.get().getStampings());
        daysRecap.add(stampingDayRecapFactory
            .create(pd.get(), numberOfInOut, true, Optional.empty()));
      }
    }
    return daysRecap;
  }

  /**
   * Metodo per formare una mappa di riepilogo nella presenza giornaliera.
   *
   * @param daysRecap la lista dei personStampingDayRecap per stabilire chi è presente
   *     e chi no in uno specifico giorno
   * @return la mappa contenente le motivazioni delle assenze e quanti hanno quella motivazione
   *     oltre a quanti sono presenti.
   */
  public Map<String, Integer> createDailyMap(List<PersonStampingDayRecap> daysRecap) {
    SortedMap<String, Integer> map = Maps.newTreeMap();
    String key = "";
    int value = 0;
    for (PersonStampingDayRecap day : daysRecap) {
      if (day.getPersonDay().getStampings().isEmpty() 
          && day.getPersonDay().getAbsences().isEmpty()) {
        key = "Giorno in attesa di completamento";
      } else if (day.getPersonDay().getStampings().isEmpty() 
          && (day.getPersonDay().getAbsences().get(0).getJustifiedType().getName()
              .equals(JustifiedTypeName.all_day) 
          || day.getPersonDay().getAbsences().get(0).getJustifiedType().getName()
          .equals(JustifiedTypeName.assign_all_day)
          || day.getPersonDay().getAbsences().get(0).getJustifiedType().getName()
          .equals(JustifiedTypeName.complete_day_and_add_overtime))) {
        key = day.getPersonDay().getAbsences().get(0).getAbsenceType().getCode() + " - " 
            + day.getPersonDay().getAbsences().get(0).getAbsenceType().getShortDescription();
      } else {
        key = "Presenti";
      }
      if (!map.containsKey(key)) {
        map.put(key, 1); 
      } else {
        value = map.get(key);
        value++;
        map.put(key, value);
      }
    }

    return map;
  }

  /**
   * Controlla se lo stamptype è da inserire tra quelli per la timbratura fuori sede, 
   * false altrimenti.
   *
   * @param stamping la timbratura da controllare
   * @param user l'utente che vuole inserire la timbratura
   * @param employee la persona per cui si vuole inserire la timbratura
   * @return true se lo stampType relativo alla timbratura da inserire è tra quelli previsti per la
   *     timbratura fuori sede, false altrimenti.
   */
  public boolean checkStampType(Stamping stamping, User user, Person employee) {
    return user.getPerson().getId().equals(employee.getId())
        && stamping.getStampType() == StampTypes.LAVORO_FUORI_SEDE;
  }

  /**
   * Associa la persona alla timbratura ricevuta via REST.
   *
   * @param stamping DTO costruito dal Json
   * @return un Optional contenente la person o absent
   */
  public Optional<Person> linkToPerson(StampingFromClient stamping) {

    Optional<User> user = secureUtils.getCurrentUser();
    if (!user.isPresent()) {
      log.error("Impossibile recuperare l'utente che ha inviato la timbratura: {}", stamping);
      return Optional.empty();
    }
    if (user.get().getBadgeReader() == null) {
      log.error("L'utente {} utilizzato per l'invio della timbratura"
          + " non ha una istanza badgeReader valida associata.", user.get().getUsername());
      return Optional.empty();
    }
    if (stamping.getNumeroBadge() == null) {
      return Optional.empty();
    }
    final Optional<Person> person = Optional.ofNullable(personDao
        .getPersonByBadgeNumber(stamping.getNumeroBadge(), user.get().getBadgeReader()));

    if (person.isPresent()) {
      stamping.setPerson(person.get());
    } else {
      log.warn("Non e' stato possibile recuperare la persona a cui si riferisce la timbratura,"
          + " matricolaFirma={}. Controllare il database.", stamping.getNumeroBadge());
    }

    return person;

  }

  /**
   * Preleva l'utente associato al numero di badge passato ed al bagdeReader
   * individuato tramite l'autenticazione corrente. 
   *
   * @param badgeNumber numero di badge
   * @return un Optional contenente la Person se presente, altrimenti absent
   */
  public Optional<Person> getPersonFromBadgeAndCurrentBadgeReader(String badgeNumber) {

    Optional<User> user = secureUtils.getCurrentUser();
    if (!user.isPresent()) {
      log.warn("Impossibile recuperare l'utente che ha inviato la timbratura con badgeNumber: {}", 
          badgeNumber);
      return Optional.empty();
    }
    if (user.get().getBadgeReader() == null) {
      log.warn("L'utente {} utilizzato per l'invio della timbratura"
          + " non ha una istanza badgeReader valida associata.", user.get().getUsername());
      return Optional.empty();
    }
    if (Strings.isNullOrEmpty(badgeNumber)) {
      return Optional.empty();
    }
    final Optional<Person> person = Optional.ofNullable(personDao
        .getPersonByBadgeNumber(badgeNumber, user.get().getBadgeReader()));

    if (!person.isPresent()) {
      log.warn("Non e' stato possibile recuperare la persona a cui si riferisce la timbratura,"
          + " badgeNumber={}. Controllare il database.", badgeNumber);
    }

    return person;
  }

}