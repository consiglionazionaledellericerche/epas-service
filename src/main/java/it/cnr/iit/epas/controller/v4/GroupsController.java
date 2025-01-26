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

package it.cnr.iit.epas.controller.v4;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.controller.v4.utils.PersonFinder;
import it.cnr.iit.epas.dao.RoleDao;
import it.cnr.iit.epas.dao.UsersRolesOfficesDao;
import it.cnr.iit.epas.dto.v4.SeatOrganizationChartDto;
import it.cnr.iit.epas.dto.v4.UserShowDto;
import it.cnr.iit.epas.dto.v4.mapper.AbsenceFormMapper;
import it.cnr.iit.epas.dto.v4.mapper.SeatOrganizationChartMapper;
import it.cnr.iit.epas.manager.GroupManager;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.Role;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.security.SecurityRules;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirements(
    value = {
        @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION),
        @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTHENTICATION)})
@Tag(
    name = "Groups controller",
    description = "Controller per la gestione dei gruppi.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/groups")
class GroupsController {
  private final GroupManager groupManager;
  private final SecurityRules rules;
  private final PersonFinder personFinder;

  private final UsersRolesOfficesDao uroDao;
  private final RoleDao roleDao;

  private final SeatOrganizationChartMapper seatOrganizationChartMapper;

  @Operation(
      summary = "Visualizzazione le informazioni sui ruoli presenti nella sede di appartenenza del dipendente.",
      description = "Questo endpoint è utilizzabile dalle persone autenticate per visualizzare "
          + "le informazioni sui ruoli presenti nella sede di appartenenza del dipendente.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Restituiti le informazioni sui ruoli presenti nella sede di appartenenza del dipendente"),
      @ApiResponse(responseCode = "401",
          description = "Autenticazione non presente", content = @Content),
      @ApiResponse(responseCode = "403",
          description = "Utente che ha effettuato la richiesta non autorizzato a visualizzare"
              + " le informazioni sui ruoli.",
          content = @Content),
      @ApiResponse(responseCode = "404",
          description = "Informazioni sui ruoli non trovati con l'id e/o il codice fiscale fornito.",
          content = @Content)
  })
  @GetMapping("/seatOrganizationChart")
  ResponseEntity<SeatOrganizationChartDto> seatOrganizationChart(
      @RequestParam("personId") Optional<Long> personId,
      @RequestParam("fiscalCode") Optional<String> fiscalCode) {
    log.debug("REST method {} invoked with parameters year={}, month={}, personId ={}",
        "/rest/v4/groups/seatOrganizationChart", personId);

    Person currentPerson = personFinder.getPerson(personId, fiscalCode)
        .orElseThrow(() -> new EntityNotFoundException("Person not found"));

    rules.checkifPermitted(currentPerson);

    log.debug("Person {}", currentPerson);
    Map<Role, List<User>> seatSupervisors = groupManager
        .createOrganizationChart(currentPerson, roleDao.getRoleByName(Role.SEAT_SUPERVISOR));
    Map<Role, List<User>> personnelAdmins = groupManager
        .createOrganizationChart(currentPerson, roleDao.getRoleByName(Role.PERSONNEL_ADMIN));
    Map<Role, List<User>> technicalAdmins = groupManager
        .createOrganizationChart(currentPerson, roleDao.getRoleByName(Role.TECHNICAL_ADMIN));
    Map<Role, List<User>> registryManagers = groupManager
        .createOrganizationChart(currentPerson, roleDao.getRoleByName(Role.REGISTRY_MANAGER));
    Map<Role, List<User>> mealTicketsManagers = groupManager
        .createOrganizationChart(currentPerson, roleDao.getRoleByName(Role.MEAL_TICKET_MANAGER));
    Map<Role, List<User>> personnelAdminsMini = groupManager
        .createOrganizationChart(currentPerson, roleDao.getRoleByName(Role.PERSONNEL_ADMIN_MINI));
    Map<Role, List<User>> shiftManagers = groupManager
        .createOrganizationChart(currentPerson, roleDao.getRoleByName(Role.SHIFT_MANAGER));
    Map<Role, List<User>> reperibilityManagers = groupManager
        .createOrganizationChart(currentPerson, roleDao.getRoleByName(Role.REPERIBILITY_MANAGER));

    SeatOrganizationChartDto dto = new SeatOrganizationChartDto();

    List<Role> roles = uroDao.getUsersRolesOfficesByUser(currentPerson.getUser())
        .stream().map(uro -> uro.getRole()).collect(Collectors.toList());

    dto.setSeatSupervisors(seatOrganizationChartMapper.convert(seatSupervisors));
    dto.setPersonnelAdmins(seatOrganizationChartMapper.convert(personnelAdmins));
    dto.setTechnicalAdmins(seatOrganizationChartMapper.convert(technicalAdmins));
    dto.setRegistryManagers(seatOrganizationChartMapper.convert(registryManagers));
    dto.setMealTicketsManagers(seatOrganizationChartMapper.convert(mealTicketsManagers));
    dto.setPersonnelAdminsMini(seatOrganizationChartMapper.convert(personnelAdminsMini));
    dto.setShiftManagers(seatOrganizationChartMapper.convert(shiftManagers));
    dto.setReperibilityManagers(seatOrganizationChartMapper.convert(reperibilityManagers));

    dto.setCurrentPerson(seatOrganizationChartMapper.convert(currentPerson));
    dto.setRoles(seatOrganizationChartMapper.convertRoles(roles));
    return ResponseEntity.ok().body(dto);
  }

}