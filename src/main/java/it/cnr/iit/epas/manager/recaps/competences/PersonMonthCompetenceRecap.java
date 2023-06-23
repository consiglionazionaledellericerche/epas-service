package it.cnr.iit.epas.manager.recaps.competences;

import it.cnr.iit.epas.models.ContractMonthRecap;
import java.time.YearMonth;
import java.util.Optional;
import it.cnr.iit.epas.dao.CompetenceCodeDao;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

/**
 * Riepilogo che popola la vista competenze del dipendente.
 *
 * @author Andrea Generosi
 */
@Slf4j
public class PersonMonthCompetenceRecap {

  private final CompetenceCodeDao competenceCodeDao;
  private final CompetenceDao competenceDao;

  public Contract contract;
  public int year;
  public int month;

  public int holidaysAvailability = 0;
  public int weekDayAvailability = 0;
  public int daylightWorkingDaysOvertime = 0;
  public int daylightholidaysOvertime = 0;
  public int ordinaryShift = 0;
  public int nightShift = 0;
  public int progressivoFinalePositivoMese = 0;

  /**
   * Costruttore.
   *
   * @param competenceCodeDao il dao dei codici di competenza
   * @param competenceDao     il dao delle competenze
   * @param wrapperFactory    il wrapperFactory
   * @param contract          il contratto
   * @param month             il mese
   * @param year              l'anno
   */
  public PersonMonthCompetenceRecap(CompetenceCodeDao competenceCodeDao,
      CompetenceDao competenceDao, IWrapperFactory wrapperFactory,
      Contract contract, int month, int year) {

    this.competenceCodeDao = competenceCodeDao;
    this.competenceDao = competenceDao;

    Preconditions.checkNotNull(contract);

    this.contract = contract;
    this.year = year;
    this.month = month;

    //TODO implementare dei metodi un pò più generali (con enum come parametro)
    this.holidaysAvailability = getHolidaysAvailability(contract.getPerson(), year, month);
    this.weekDayAvailability = getWeekDayAvailability(contract.getPerson(), year, month);
    this.daylightWorkingDaysOvertime =
        getDaylightWorkingDaysOvertime(contract.getPerson(), year, month);
    this.daylightholidaysOvertime = getDaylightholidaysOvertime(contract.getPerson(), year, month);
    this.ordinaryShift = getOrdinaryShift(contract.getPerson(), year, month);
    this.nightShift = getNightShift(contract.getPerson(), year, month);

    Optional<ContractMonthRecap> recap =
        wrapperFactory.create(contract).getContractMonthRecap(YearMonth.of(year, month));

    if (recap.isPresent()) {
      this.progressivoFinalePositivoMese = recap.get().getPositiveResidualInMonth();
    }
  }

  /**
   * Ritorna il numero di giorni di indennità di reperibilità festiva per la persona nel mese.
   */
  private int getHolidaysAvailability(Person person, int year, int month) {
    int holidaysAvailability = 0;
    CompetenceCode cmpCode = competenceCodeDao.getCompetenceCodeByCode("208");

    Optional<Competence> competence = competenceDao.getCompetence(person, year, month, cmpCode);

    if (competence.isPresent()) {
      holidaysAvailability = competence.get().getValueApproved();
    } else {
      holidaysAvailability = 0;
    }
    return holidaysAvailability;
  }

  /**
   * Ritorna il numero di giorni di indennità di reperibilità feriale per la persona nel mese.
   */
  private int getWeekDayAvailability(Person person, int year, int month) {
    int weekDayAvailability = 0;
    CompetenceCode cmpCode = competenceCodeDao.getCompetenceCodeByCode("207");

    Optional<Competence> competence = competenceDao.getCompetence(person, year, month, cmpCode);

    if (competence.isPresent()) {
      weekDayAvailability = competence.get().getValueApproved();
    } else {
      weekDayAvailability = 0;
    }
    return weekDayAvailability;
  }

  /**
   * Ritorna il numero di giorni di straordinario diurno nei giorni lavorativi per la persona nel
   * mese.
   */
  private int getDaylightWorkingDaysOvertime(Person person, int year, int month) {
    int daylightWorkingDaysOvertime = 0;
    CompetenceCode cmpCode = competenceCodeDao.getCompetenceCodeByCode("S1");

    Optional<Competence> competence = competenceDao.getCompetence(person, year, month, cmpCode);

    if (competence.isPresent()) {
      daylightWorkingDaysOvertime = competence.get().getValueApproved();
    } else {
      daylightWorkingDaysOvertime = 0;
    }
    return daylightWorkingDaysOvertime;
  }

  /**
   * Ritorna il numero di giorni di straordinario diurno nei giorni festivi o notturno nei giorni
   * lavorativi per la persona nel mese.
   */
  private int getDaylightholidaysOvertime(Person person, int year, int month) {
    int daylightholidaysOvertime = 0;
    CompetenceCode cmpCode = competenceCodeDao.getCompetenceCodeByCode("S2");

    Optional<Competence> competence = competenceDao.getCompetence(person, year, month, cmpCode);

    if (competence.isPresent()) {
      daylightholidaysOvertime = competence.get().getValueApproved();
    } else {
      daylightholidaysOvertime = 0;
    }
    return daylightholidaysOvertime;
  }

  /**
   * Ritorna il numero di giorni di turno ordinario per la persona nel mese.
   */
  private int getOrdinaryShift(Person person, int year, int month) {
    int ordinaryShift = 0;
    CompetenceCode cmpCode = competenceCodeDao.getCompetenceCodeByCode("T1");

    Optional<Competence> competence = competenceDao.getCompetence(person, year, month, cmpCode);

    if (competence.isPresent()) {
      ordinaryShift = competence.get().getValueApproved();
    } else {
      ordinaryShift = 0;
    }
    return ordinaryShift;
  }

  /**
   * Ritorna il numero di giorni di turno notturno per la persona nel mese.
   */
  private int getNightShift(Person person, int year, int month) {
    int nightShift = 0;
    CompetenceCode cmpCode = competenceCodeDao.getCompetenceCodeByCode("T2");

    if (cmpCode == null) {
      return 0;
    }
    Optional<Competence> competence = competenceDao.getCompetence(person, year, month, cmpCode);

    if (competence.isPresent()) {
      nightShift = competence.get().getValueApproved();
    } else {
      nightShift = 0;
    }
    return nightShift;
  }

}