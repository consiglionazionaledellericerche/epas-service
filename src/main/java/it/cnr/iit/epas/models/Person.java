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

package it.cnr.iit.epas.models;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.absences.InitializationGroup;
import it.cnr.iit.epas.models.base.IPropertiesInPeriodOwner;
import it.cnr.iit.epas.models.base.IPropertyInPeriod;
import it.cnr.iit.epas.models.base.PeriodModel;
import it.cnr.iit.epas.models.enumerate.CertificationType;
import it.cnr.iit.epas.models.flows.Affiliation;
import it.cnr.iit.epas.models.flows.Group;
import it.cnr.iit.epas.models.listeners.PersonListener;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Entity per le persone.
 *
 * @author Cristian Lucchesi
 */
@EntityListeners(PersonListener.class)
@Getter
@Setter
@Entity
@Audited
@Table(name = "persons")
public class Person extends PeriodModel implements IPropertiesInPeriodOwner {

  /*
   * IMPORTANTE: relazione con user impostata a LAZY per non scaricare tutte le informazioni della
   * persona in fase di personDao.list. Necessaria comunque la join con le relazioni OneToOne.
   */

  private static final long serialVersionUID = -2293369685203872207L;

  private Long perseoId;

  @NotNull
  private String name;

  @NotNull
  private String surname;

  private String othersSurnames;

  //@Unique
  //@As(binder = NullStringBinder.class)
  private String fiscalCode;

  private LocalDate birthday;

  private String residence;

  //  @Unique
  @Email
  @NotNull
  private String email;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  private User user;

  /**
   * Numero di matricola.
   */
  //@Unique
  private String number;

  /**
   * id che questa persona aveva nel vecchio database.
   */
  private Long oldId;

  /**
   * Campo da usarsi in caso di autenticazione via shibboleth.
   */
  //FIXME: introdurre dei validatori/binder analoghi per spring boot
  //  @Unique
  //  @As(binder = NullStringBinder.class)
  private String eppn;

  private String telephone;

  private String fax;

  private String mobile;

  private boolean wantEmail;

  /**
   * Le affiliazioni di una persona sono le appartenenze ai gruppi con percentuale
   * e date.
   */
  @OneToMany(mappedBy = "person")
  private List<Affiliation> affiliations = Lists.newArrayList();

  @OneToMany(mappedBy = "manager")
  private List<Group> groupsPeople = Lists.newArrayList();

  /**
   * relazione con i turni.
   */
  @OneToMany(mappedBy = "supervisor")
  private List<ShiftCategories> shiftCategories = Lists.newArrayList();

  @OneToMany(mappedBy = "supervisor")
  private List<PersonReperibilityType> reperibilityTypes = Lists.newArrayList();

  @Getter
  @NotAudited
  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private List<Contract> contracts = Lists.newArrayList();

  /**
   * relazione con la tabella dei figli del personale.
   */
  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private Set<PersonChildren> personChildren = Sets.newHashSet();

  /**
   * relazione con la nuova tabella dei person day.
   */
  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private List<PersonDay> personDays = Lists.newArrayList();

  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private List<CertificatedData> certificatedData = Lists.newArrayList();

  /**
   * Dati derivanti dall'invio col nuovo sistema degli attestati.
   */
  @OneToMany(mappedBy = "person")
  public List<Certification> certifications = Lists.newArrayList();

  @OneToMany(mappedBy = "admin")
  private List<MealTicket> mealTicketsAdmin = Lists.newArrayList();

  /**
   * relazione con la nuova tabella dei person_month.
   */
  @NotAudited
  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private List<PersonMonthRecap> personMonths = Lists.newArrayList();

  /**
   * relazione con la tabella Competence.
   */
  @NotAudited
  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private List<Competence> competences = Lists.newArrayList();

  @NotAudited
  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private Set<PersonCompetenceCodes> personCompetenceCodes = Sets.newHashSet();

  @OneToOne(mappedBy = "person")
  private PersonHourForOvertime personHourForOvertime;

  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private Set<PersonReperibility> reperibility = Sets.newHashSet();

  @NotAudited
  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private List<PersonShift> personShifts = Lists.newArrayList();

  @ManyToOne
  @NotNull
  private Qualification qualification;

  @ManyToOne
  @NotNull
  private Office office;  
  
  @OneToMany(mappedBy = "person")
  private Set<MealTicketCard> mealTicketCards = Sets.newHashSet();

  /**
   * TODO: da rimuovere quando si userà lo storico per intercettare il cambio di sede per adesso è
   * popolato dal valore su perseo alla costruzione dell'oggetto.
   */
  @Transient
  private Long perseoOfficeId = null;

  /**
   * Sono stati implementati i metodi Equals e HashCode sulla classe Badge in modo che Se sono
   * presenti più badge per la persona che differiscono solo per il campo badgeReader venga
   * restituito un solo elemento (effettivamente per noi è lo stesso badge).Quindi person.badges non
   * restituisce i duplicati.
   */
  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private Set<Badge> badges = Sets.newHashSet();

  /**
   * Le configurazioni della persona.
   */
  @OneToMany(mappedBy = "person", cascade = {CascadeType.REMOVE})
  private List<PersonConfiguration> personConfigurations = Lists.newArrayList();


  @ManyToMany(mappedBy = "managers")
  private List<ShiftCategories> categories = Lists.newArrayList();

  @ManyToMany(mappedBy = "managers")
  private List<PersonReperibilityType> reperibilities = Lists.newArrayList();

  @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
  private Set<InitializationGroup> initializationGroups;

  @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
  private Set<TeleworkValidation> teleworkValidations;

  /**
   * Nuova relazione con la tabella di check giornaliero del green pass.
   */
  @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
  private Set<CheckGreenPass> checkGreenPass;

  @NotAudited
  private LocalDateTime updatedAt;

  public String getName() {
    return this.name;
  }

  public String getSurname() {
    return this.surname;
  }

  /**
   * Nome completo della persona.
   *
   * @return il nome completo.
   */
  public String getFullname() {
    return String.format("%s %s", surname, name);
  }


  public String fullName() {
    return getFullname();
  }

  @Override
  public String toString() {
    return getFullname();
  }

  @Transient
  public Institute getInstitute() {
    return office == null ? null : office.getInstitute();
  }

  /**
   * Lista dei gruppi di una persona alla data odierna.
   *
   * @return la lista dei gruppi a cui appartiente oggi una persona. 
   */
  @Transient
  public List<Group> getGroups() {
    return getGroups(LocalDate.now());
  }

  /**
   * Lista dei gruppi di una persona alla data indicata.
   *
   * @return la lista dei gruppi a cui appartiente una persona ad una data
   *     passata per parametro.
   */
  @Transient
  public List<Group> getGroups(java.time.LocalDate date) {
    return affiliations.stream()
        .filter(a -> !a.getBeginDate().isAfter(date) 
            && (a.getEndDate() == null || a.getEndDate().isAfter(date)))
        .map(a -> a.getGroup()).collect(Collectors.toList());
  }

  @Override
  public Collection<IPropertyInPeriod> periods(Object type) {

    if (type.getClass().equals(EpasParam.class)) {
      return (Collection<IPropertyInPeriod>) filterConfigurations((EpasParam) type);
    }
    return null;
  }

  @Override
  public Collection<Object> types() {
    return Sets.newHashSet(Arrays.asList(EpasParam.values()));
  }

  /**
   * Filtra dalla lista di configurations le occorrenze del tipo epasParam.
   *
   * @param epasPersonParam filtro
   * @return insieme filtrato
   */
  private Set<IPropertyInPeriod> filterConfigurations(EpasParam epasPersonParam) {
    return personConfigurations.stream()
        .filter(conf -> conf.getEpasParam() == epasPersonParam).collect(Collectors.toSet());
  }

  /**
   * Comparatore di persone per fullname e poi id.
   *
   * @return un Comparator che compara per fullname poi id.
   */
  public static Comparator<Person> personComparator() {
    return Comparator
        .comparing(
            Person::getFullname,
            Comparator.nullsFirst(String::compareTo))
        .thenComparing(
            Person::getId,
            Comparator.nullsFirst(Long::compareTo));
  }

  /**
   * Controlla il valore del parametro indicato.
   *
   * @param param Parametro di configurazione da controllare.
   * @param value valore atteso
   * @return true se la persona contiene il parametro di configurazione specificato con il valore
   *     indicato.
   */
  public boolean checkConf(EpasParam param, String value) {
    return personConfigurations.stream().filter(conf -> conf.getEpasParam() == param
        && conf.getFieldValue().equals(value)).findFirst().isPresent();
  }

  /**
   * Verifica se il mese passato come parametro è successivo all'ultimo mese inviato con gli
   * attestati.
   *
   * @param yearMonth La 'data' da verificare
   * @return true se la data passata come parametro è successiva all'ultimo mese sulquale sono
   *     stati inviati gli attestai per la persona interessata.
   */
  public boolean checkLastCertificationDate(final YearMonth yearMonth) {

    //Gli attestati relativi ai MEAL Ticket vengono ignorati perchè vengono 
    //salvati i Certification relativi su ePAS anche se non stati effettivamente inviati
    //ad attestati.
    final Optional<Certification> ultimo = certifications.stream()
        .filter(c -> c.getCertificationType() != CertificationType.MEAL)
        .max(Certification.comparator());
    if (ultimo.isPresent()) {
      return ultimo.get().getYearMonth().isBefore(yearMonth) 
          || ultimo.get().getAttestatiId() == null;
    }
    // Se non c'è nessun mese presente considero che la condizione sia sempre vera
    return true;
  }

  /**
   * Associazione tra le zone.
   *
   * @return la lista delle ZoneToZones associate ai badge della persona.
   */
  public List<ZoneToZones> getZones() {
    return badges.stream().<ZoneToZones>flatMap(b -> b.getBadgeReader().getZones().stream()
        .map(z -> z.zoneLinkedAsMaster.stream().findAny().orElse(null)))
        .collect(Collectors.toList());
  }

  /**
   * Verifica se è un livello I-III.
   *
   * @return true se è un livello I-III, false altrimenti.
   */
  @Transient
  public boolean isTopQualification() {
    return qualification != null && qualification.isTopQualification();
  }

  @Transient
  public boolean isGroupManager() {
    return user.hasRoles(Role.GROUP_MANAGER);
  }
  
  @Transient
  public boolean isSeatSupervisor() {
    return user.hasRoles(Role.SEAT_SUPERVISOR);
  }

  public List<Person> getPersonsInCharge() {
    return groupsPeople.stream().flatMap(g -> g.getPeople().stream()).collect(Collectors.toList());
  }
  
  /**
   * La mealTicketCard attualmente assegnata al dipendente, ovvero
   * La prima che risulta attiva.
   */
  @Transient
  public MealTicketCard actualMealTicketCard() {
    if (this.mealTicketCards.isEmpty()) {
      return null;
    }
    return mealTicketCards.stream().filter(mtc -> mtc.isActive()).findFirst().orElse(null);
  }

  /**
   * Metodo che ritorna la precedente card per buoni elettronici.
   *
   * @return la precedente card per buoni elettronici.
   */
  @Transient
  public MealTicketCard previousMealTicketCard() {
    if (!this.mealTicketCards.isEmpty() && actualMealTicketCard() == null) {
      return mealTicketCards.stream().sorted((o1, o2) -> o2.getEndDate().compareTo(o1.getEndDate()))
          .filter(mtc -> !mtc.isActive()).findFirst().get();
    }
    return null;
  }
}