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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.RoleDao;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.UsersRolesOffices;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Wrapper sede.
 *
 * @author Alessandro Martelli
 */
@Component
public class WrapperOffice implements IWrapperOffice {

  private Office value;
  private final RoleDao roleDao;

  @Inject
  WrapperOffice(RoleDao roleDao) {
    this.roleDao = roleDao;
  }

  public IWrapperOffice setValue(Office office) {
    this.value = office;
    return this;
  }
  
  @Override
  public final Office getValue() {
    return value;
  }

  @Override
  public final LocalDate initDate() {
    return this.value.getBeginDate();
  }


  /**
   * I Responsabili Sede dell'office (di norma 1).
   */
  public List<UsersRolesOffices> getSeatSupervisor() {
    Role role = roleDao.getRoleByName(Role.SEAT_SUPERVISOR);
    return filterUros(role);
  }
  
  /**
   * Gli amministratori tecnici dell'office.
   */
  public List<UsersRolesOffices> getTechnicalAdmins() {
    Role role = roleDao.getRoleByName(Role.TECHNICAL_ADMIN);
    return filterUros(role);
  }

  /**
   * Gli amministratori dell'office.
   */
  public List<UsersRolesOffices> getPersonnelAdmins() {

    Role role = roleDao.getRoleByName(Role.PERSONNEL_ADMIN);
    return filterUros(role);
  }

  /**
   * I mini amministratori dell'office.
   */
  public List<UsersRolesOffices> getMiniAdmins() {

    Role role = roleDao.getRoleByName(Role.PERSONNEL_ADMIN_MINI);
    return filterUros(role);
  }
  
  /**
   * Lista di gestori buoni basto.
   *
   * @return i gestori dei buoni pasto
   */
  public List<UsersRolesOffices> getMealTicketManagers() {
    Role role = roleDao.getRoleByName(Role.MEAL_TICKET_MANAGER);
    return filterUros(role);
  }
  
  /**
   * Lista di gestori anagrafica.
   *
   * @return i gestori dell'anagrafica
   */
  public List<UsersRolesOffices> getRegistryManagers() {
    Role role = roleDao.getRoleByName(Role.REGISTRY_MANAGER);
    return filterUros(role);
  }

  /**
   * Il primo mese per cui è possibile effettuare l'upload degli attestati.
   *
   * @return yearMonth se esiste.
   */
  @Override
  public Optional<YearMonth> getFirstMonthUploadable() {

    LocalDate officeInitUse = this.value.getBeginDate();

    return Optional.ofNullable(YearMonth.from(officeInitUse));

  }


  /**
   * La lista degli anni di cui è possibile effettuare l'invio degli attestati per la sede.
   *
   * @return lista degli anni
   */
  @Override
  public List<Integer> getYearUploadable() {

    List<Integer> years = Lists.newArrayList();

    Optional<YearMonth> firstMonthUploadable = getFirstMonthUploadable();
    if (!firstMonthUploadable.isPresent()) {
      return years;
    }

    int officeYearFrom = getFirstMonthUploadable().get().getYear();
    // anni in cui possono essere inviati gli attestati (installazione sede fino ad oggi)
    int officeYearTo = LocalDate.now().getYear();
    if (officeYearFrom > officeYearTo) {
      officeYearTo = officeYearFrom;
    }
    for (int yearFrom = officeYearFrom; yearFrom <= officeYearTo; yearFrom++) {
      years.add(yearFrom);
    }
    return years;
  }

  /**
   * La lista dei mesi di cui è possibile effettuare l'invio degli attestati.
   *
   * @return lista dei mesi
   */
  public List<Integer> getMonthUploadable() {
    return Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
  }

  /**
   * Il mese di cui presumubilmente occorre fare l'invio attestati. (Il precedente rispetto a
   * quello attuale se non è precedente al primo mese per attestati).
   *
   * @return prossimo mese da inviare
   */
  @Override
  public Optional<YearMonth> nextYearMonthToUpload() {

    // mese scorso preselezionato (se esiste) oppure installazione sede
    YearMonth previousYearMonth = YearMonth.from(LocalDate.now().minusMonths(1));

    Optional<YearMonth> first = getFirstMonthUploadable();
    if (!first.isPresent()) {
      return Optional.<YearMonth>empty();
    }

    if (previousYearMonth.isBefore(first.get())) {
      return first;
    }
    return Optional.ofNullable(previousYearMonth);
  }

  /**
   * Se il mese passato come parametro è inviabile.
   *
   * @param yearMonth mese da verificare
   * @return esito
   */
  @Override
  public boolean isYearMonthUploadable(YearMonth yearMonth) {
    Optional<YearMonth> first = getFirstMonthUploadable();
    if (!first.isPresent()) {
      return false;
    }
    if (yearMonth.isBefore(first.get())) {
      return false;
    }
    return true;
  }

  private List<UsersRolesOffices> filterUros(Role role) {
    List<UsersRolesOffices> uroList = Lists.newArrayList();
    for (UsersRolesOffices uro : this.value.getUsersRolesOffices()) {

      if (uro.office.getId().equals(this.value.getId()) && uro.role.getId().equals(role.getId())
          && uro.user.getPerson() != null) {
        uroList.add(uro);
      }
    }
    return uroList;
  }

}