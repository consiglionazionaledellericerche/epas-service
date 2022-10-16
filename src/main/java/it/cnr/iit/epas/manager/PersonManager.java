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
package it.cnr.iit.epas.manager;

import com.google.common.base.Function;
import com.google.common.base.Verify;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.PersonDayDao;
import it.cnr.iit.epas.dao.RoleDao;
import it.cnr.iit.epas.dao.UsersRolesOfficesDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dao.wrapper.IWrapperPersonDay;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.JustifiedType.JustifiedTypeName;
import it.cnr.iit.epas.models.dto.AbsenceToRecoverDto;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
//import play.db.jpa.JPA;

/**
 * Manager per la gestione delle persone.
 */
@Slf4j
@Component
public class PersonManager {

  private final ContractDao contractDao;
  private final PersonDayDao personDayDao;
  public final PersonDayManager personDayManager;
  private final Provider<IWrapperFactory> wrapperFactory;
  private final AbsenceDao absenceDao;
  private final OfficeManager officeManager;
  private final UserManager userManager;
  private final RoleDao roleDao;
  private final Provider<EntityManager> emp;
  /**
   * Costrutture.
   *
   * @param contractDao      contractDao
   * @param personDayDao     personDayDao
   * @param absenceDao       absenceDao
   * @param personDayManager personDayManager
   * @param wrapperFactory   wrapperFactory
   */
  @Inject
  public PersonManager(ContractDao contractDao,
      PersonDayDao personDayDao,
      AbsenceDao absenceDao,
      PersonDayManager personDayManager,
      Provider<IWrapperFactory> wrapperFactory,
      UsersRolesOfficesDao uroDao,
      OfficeManager officeManager,
      UserManager userManager, 
      RoleDao roleDao,
      Provider<EntityManager> emp) {
    this.contractDao = contractDao;
    this.personDayDao = personDayDao;
    this.absenceDao = absenceDao;
    this.personDayManager = personDayManager;
    this.wrapperFactory = wrapperFactory;
    this.officeManager = officeManager;
    this.userManager = userManager;
    this.roleDao = roleDao;
    this.emp = emp;
  }

  /**
   * Calcola se la persona nel giorno non è nè in turno nè in reperibilità e quindi può prendere
   * l'assenza.
   *
   * @return esito
   */
  public boolean canPersonTakeAbsenceInShiftOrReperibility(Person person, LocalDate date) {
    Query queryReperibility =
        emp.get().createQuery(
            "Select count(*) from PersonReperibilityDay prd where prd.date = :date "
                + "and prd.personReperibility.person = :person");
    queryReperibility.setParameter("date", date).setParameter("person", person);
    int prdCount = queryReperibility.getFirstResult();
    if (prdCount != 0) {
      return false;
    }
    Query queryShift =
        emp.get().createQuery(
            "Select count(*) from PersonShiftDay psd where psd.date = :date "
                + "and psd.personShift.person = :person");
    queryShift.setParameter("date", date).setParameter("person", person);
    int psdCount = queryShift.getFirstResult();
    if (psdCount != 0) {
      return false;
    }

    return true;
  }

  /**
   * Conta i codici di assenza.
   *
   * @param personDays lista di PersonDay
   * @return La mappa dei codici di assenza utilizzati nei persondays specificati
   */
  public Map<AbsenceType, Integer> countAbsenceCodes(List<PersonDay> personDays) {

    final Map<AbsenceType, Integer> absenceCodeMap = Maps.newHashMap();

    personDays.stream().flatMap(personDay -> personDay.getAbsences().stream()
        .<AbsenceType>map(absence -> absence.absenceType)).forEach(absenceType -> {
          Integer count = absenceCodeMap.get(absenceType);
          absenceCodeMap.put(absenceType, (count == null) ? 1 : count + 1);
        });

    return absenceCodeMap;
  }
  
  /**
   * Metodo utile per il calcolo dei codici di assenza presenti in un certo arco temporale
   * derivante dalla lista dei personday.
   *
   * @param personDays la lista dei personDay
   * @return la lista dei codici di assenza presenti nella lista dei personDay.
   */
  public List<Absence> listAbsenceCodes(List<PersonDay> personDays) {
    final List<Absence> list = Lists.newArrayList();
    personDays.stream().flatMap(personDay -> personDay.getAbsences().stream()
        .<Absence>map(absence -> absence)).forEach(absence -> {          
          list.add(absence);
        });
    return list;
  }


  /**
   * Metodo che determina quanti giorni di lavoro in sede sono stati effettuati. Controlla anche
   * che tra questi giorni non ci siano giorni di lavoro FUORI SEDE che vengono sottratti
   * dal conteggio.
   *
   * @param personDays la lista dei personDay
   * @param contracts la lista dei contratti
   * @param end la data di fine
   * @return la lista dei giorni di lavoro IN SEDE derivanti dai parametri passati.
   */
  public int basedWorkingDays(List<PersonDay> personDays,
      List<Contract> contracts, LocalDate end) {

    int basedDays = 0;

    for (PersonDay pd : personDays) {

      if (pd.isHoliday()) {
        continue;
      }
      boolean find = false;
      for (Contract contract : contracts) {
        if (DateUtility.isDateIntoInterval(pd.getDate(), contract.periodInterval())) {
          find = true;
        }
      }

      if (!find) {
        continue;
      }
      IWrapperPersonDay day = wrapperFactory.get().create(pd);
      boolean fixed = day.isFixedTimeAtWork();
      
      if (fixed && !personDayManager.isAllDayAbsences(pd)) {
        basedDays++;
      } else if (!fixed && pd.getStampings().size() > 0 
          && !pd.getStampings().stream().anyMatch(st -> st.isMarkedByTelework())
          && !personDayManager.isAllDayAbsences(pd) 
          && pd.getPerson().getQualification().getQualification() < 4) {
        basedDays++;
      } else if (!fixed && pd.getStampings().size() > 0
          && !pd.getStampings().stream().anyMatch(st -> st.isMarkedByTelework())
          && !personDayManager.isAllDayAbsences(pd) 
          && personDayManager.enoughTimeInSeat(pd.getStampings(), day)) {
        basedDays++;
      } 

    }

    return basedDays;
  }

  /**
   * Il numero di riposi compensativi utilizzati tra 2 date (in linea di massima ha senso
   * dall'inizio dell'anno a una certa data).
   */
  public int numberOfCompensatoryRestUntilToday(Person person, LocalDate begin, LocalDate end) {

    List<Contract> contractsInPeriod = contractDao
        .getActiveContractsInPeriod(person, begin, Optional.of(end));

    Contract newerContract = contractsInPeriod.stream().filter(contract ->
        contract.sourceDateResidual != null).max(Comparator
        .comparing(Contract::getSourceDateResidual)).orElse(null);

    if (newerContract != null && newerContract.sourceDateRecoveryDay != null
        && !newerContract.sourceDateRecoveryDay.isBefore(begin)
        && !newerContract.sourceDateRecoveryDay.isAfter(end)) {
      return newerContract.sourceRecoveryDayUsed + absenceDao
          .absenceInPeriod(person, newerContract.sourceDateRecoveryDay.plusDays(1), end, "91")
      .size();
    }

    return absenceDao.absenceInPeriod(person, begin, end, "91").size();
  }

  /**
   * Il numero di riposi compensativi utilizzati nell'anno dalla persona.
   */
  public int numberOfCompensatoryRestUntilToday(Person person, int year, int month) {

    LocalDate begin = LocalDate.of(year, 1, 1);
    LocalDate end = DateUtility.endOfMonth(begin);
    return numberOfCompensatoryRestUntilToday(person, begin, end);
  }

  /**
   * Minuti di presenza festiva non accettata.
   *
   * @param person persona
   * @param year   anno
   * @param month  mese
   * @return minuti
   */
  public int holidayWorkingTimeNotAccepted(Person person, Optional<Integer> year,
      Optional<Integer> month) {

    List<PersonDay> pdList = personDayDao
        .getHolidayWorkingTime(person, year, month);
    int value = 0;
    for (PersonDay pd : pdList) {
      value += pd.getOnHoliday() - pd.getApprovedOnHoliday();
    }
    return value;
  }

  /**
   * Minuti di presenza festiva accettata.
   *
   * @param person persona
   * @param year   anno
   * @param month  mese
   * @return minuti
   */
  public int holidayWorkingTimeAccepted(Person person, Optional<Integer> year,
      Optional<Integer> month) {

    List<PersonDay> pdList = personDayDao
        .getHolidayWorkingTime(person, year, month);
    int value = 0;
    for (PersonDay pd : pdList) {
      value += pd.getApprovedOnHoliday();
    }
    return value;
  }

  /**
   * Minuti di presenza festiva totali.
   *
   * @param person persona
   * @param year   anno
   * @param month  mese
   * @return minuti
   */
  public int holidayWorkingTimeTotal(
      Person person, Optional<Integer> year, Optional<Integer> month) {
    List<PersonDay> pdList = personDayDao
        .getHolidayWorkingTime(person, year, month);
    int value = 0;
    for (PersonDay pd : pdList) {
      value += pd.getTimeAtWork();
    }
    return value;
  }
  
  /**
   * Metodo che ritorna la lista delle assenze di tipo recover_time non ancora evase nell'arco 
   * temporale compreso tra from e to.
   *
   * @param person la persona di cui si cercano le assenze
   * @param from la data da cui si cercano le assenze
   * @param to la data fino a cui si cercano le assenze
   * @param justifiedTypeName il tipo di giustificativo 
   * @return la lista di assenze di tipo recovery_time presenti nei parametri specificati
   */
  public List<Absence> absencesToRecover(Person person, LocalDate from, 
      LocalDate to, JustifiedTypeName justifiedTypeName) {
    List<Absence> absencesToRecover = Lists.newArrayList();
    List<Absence> absences = absenceDao.getAbsenceByCodeInPeriod(Optional.ofNullable(person), 
        Optional.empty(), from, to, Optional.ofNullable(justifiedTypeName), 
        false, false);
    for (Absence abs : absences) {
      int sum = abs.timeVariations.stream().mapToInt(o -> o.getTimeVariation()).sum();
      if (sum < abs.timeToRecover) {
        absencesToRecover.add(abs);
      }
    }
        
    return absencesToRecover;
  }
  
  /**
   * Metodo di utilità per trasformare una lista di assenze in lista di dto per il template.
   *
   * @param list lista di assenze a giustificazione recover_time
   * @return la lista di dto da ritornare alla vista.
   */
  public List<AbsenceToRecoverDto> dtoList(List<Absence> list) {

    List<AbsenceToRecoverDto> absencesToRecover =
        FluentIterable.from(list).transform(
            new Function<Absence, AbsenceToRecoverDto>() {
              @Override
              public AbsenceToRecoverDto apply(Absence absence) {
                return new AbsenceToRecoverDto(
                absence, absence.personDay.getDate(), absence.expireRecoverDate,
                absence.timeToRecover,
                absence.timeVariations.stream().mapToInt(i -> i.getTimeVariation()).sum(),
                Math.round(absence.timeVariations.stream().mapToInt(i -> i.getTimeVariation()).sum() 
                / (float) absence.timeToRecover * 100)
                );
              }
            }
       ).toList();

    return absencesToRecover;
  }
  
  /**
   * eppn viene calcolato come username @ ultimi due livelli 
   * del nome a dominio dell'email.
   * Per esempio se l'username è giuseppe.verdi e l'mail è g.verdi@iit.cnr.it
   * il campo ePPN viene impostato a giuseppe.verdi@cnr.it.
  */ 
  public String eppn(String username, String email) {
    Verify.verifyNotNull(username);
    Verify.verifyNotNull(email);
    
    val emailParts = email.split("@");
    if (emailParts.length < 2) {
      log.warn("Impossibile calcolare il campo eppn per username = {} e email = {}. "
          + "Email non valida.", username, email);
      return null;
    }
    String domain = emailParts[1];
    if (domain.split("\\.").length > 2) {
      val domainTokens = domain.split("\\.");
      domain = String.format("%s.%s",
          domainTokens[domainTokens.length - 2], domainTokens[domainTokens.length - 1]);
    }
    return String.format("%s@%s", username, domain);
  }
  
  /**
   * Si occupa di creare l'utente collegato alla persona,
   * di impostare i ruoli corretti e creare l'epp se
   * non passato.
   *
   * @param person l'oggetto Person da configurare con
   *     gli attributi ed oggetti correlati opportuni.
   */
  public void properPersonCreate(Person person) {
    userManager.createUser(person);
    // Se il campo eppn è vuoto viene calcolato euristicamente...
    if (person.getEmail() != null && person.getEppn() == null) {
      person.setEppn(eppn(person.getUser().username, person.getEmail()));
    }
    Role employee = roleDao.getRoleByName(Role.EMPLOYEE);
    officeManager.setUro(person.getUser(), person.getOffice(), employee);
  }

}