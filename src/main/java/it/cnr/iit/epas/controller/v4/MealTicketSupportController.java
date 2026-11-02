/*
 * Copyright (C) 2026  Consiglio Nazionale delle Ricerche
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.iit.epas.config.OpenApiConfiguration;
import it.cnr.iit.epas.controller.v4.utils.ApiRoutes;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.ContractMonthRecapDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dto.v4.ContractMonthRecapDto;
import it.cnr.iit.epas.dto.v4.ContractShowDto;
import it.cnr.iit.epas.dto.v4.DateIntervalDto;
import it.cnr.iit.epas.dto.v4.OfficeShowTerseDto;
import it.cnr.iit.epas.dto.v4.PersonDayDto;
import it.cnr.iit.epas.dto.v4.mapper.ContractShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.OfficeShowMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonDayMapper;
import it.cnr.iit.epas.dto.v4.mapper.PersonMonthsMapper;
import it.cnr.iit.epas.dto.v4.mealtickets.MealTicketPersonDto;
import it.cnr.iit.epas.manager.ConsistencyManager;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.enumerate.BlockType;
import it.cnr.iit.epas.security.NoCheck;
import it.cnr.iit.epas.utils.DateInterval;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servizi per epas-meal-ticket-service, che gestisce i buoni pasto e le card elettroniche.
 *
 * <p>Gli endpoint sono riservati ai client di servizio configurati in
 * epas.security.meal-ticket-client-ids (vedi SecurityConfig), per questo non passano dalle
 * regole drools.</p>
 */
@NoCheck
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTHENTICATION)
@Tag(name = "MealTicket support controller",
    description = "Servizi riservati a epas-meal-ticket-service.")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + MealTicketSupportController.PATH)
public class MealTicketSupportController {

  public static final String PATH = "/mealticketservice";

  private final PersonDao personDao;
  private final ContractDao contractDao;
  private final ContractMonthRecapDao contractMonthRecapDao;
  private final OfficeDao officeDao;
  private final IWrapperFactory wrapperFactory;
  private final ConfigurationManager configurationManager;
  private final ConsistencyManager consistencyManager;
  private final ContractShowMapper contractMapper;
  private final OfficeShowMapper officeMapper;
  private final PersonDayMapper personDayMapper;
  private final PersonMonthsMapper monthRecapMapper;

  @Operation(summary = "Persona individuata da una delle sue chiavi.")
  @GetMapping("/people/find")
  @Transactional
  public ResponseEntity<MealTicketPersonDto> findPerson(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String eppn,
      @RequestParam(required = false) Long perseoId,
      @RequestParam(required = false) String fiscalCode,
      @RequestParam(required = false) String number) {
    Person person = notFoundIfEmpty(personDao.byIdOrEppnOrEmailOrPerseoIdOrFiscalCodeOrNumber(
        id, eppn, email, perseoId, fiscalCode, number), "Person");
    val dto = new MealTicketPersonDto();
    dto.setId(person.getId());
    dto.setName(person.getName());
    dto.setSurname(person.getSurname());
    dto.setFiscalCode(person.getFiscalCode());
    dto.setEmail(person.getEmail());
    dto.setNumber(person.getNumber());
    dto.setEppn(person.getEppn());
    dto.setOfficeId(person.getOffice().getId());
    return ResponseEntity.ok(dto);
  }

  @Operation(summary = "Contratto attuale della persona.")
  @GetMapping("/people/{personId}/currentContract")
  @Transactional
  public ResponseEntity<ContractShowDto> currentContract(@PathVariable Long personId) {
    return contract(wrapperFactory.create(findPerson(personId)).getCurrentContract());
  }

  @Operation(summary = "Contratto della persona attivo alla data.")
  @GetMapping("/people/{personId}/contract")
  @Transactional
  public ResponseEntity<ContractShowDto> contractAt(@PathVariable Long personId,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
    return contract(Optional.ofNullable(contractDao.getContract(date, findPerson(personId))));
  }

  @Operation(summary = "Giorni di presenza della persona nell'intervallo.")
  @GetMapping("/people/{personId}/personDays")
  @Transactional
  public ResponseEntity<List<PersonDayDto>> personDays(@PathVariable Long personId,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate to,
      @RequestParam boolean onlyWithMealTicket) {
    return ResponseEntity.ok(personDao.getPersonDayIntoInterval(findPerson(personId),
        new DateInterval(from, to), onlyWithMealTicket).stream()
        .map(personDayMapper::convert).toList());
  }

  @Operation(summary = "Aggiorna la situazione della persona a partire dalla data.")
  @PostMapping("/people/{personId}/updateSituation")
  public ResponseEntity<Void> updateSituation(@PathVariable Long personId,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from) {
    notFoundIfEmpty(personDao.byId(personId), "Person");
    consistencyManager.updatePersonSituation(personId, from);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Ricalcola i riepiloghi mensili della persona a partire dalla data.")
  @PostMapping("/people/{personId}/updateRecaps")
  public ResponseEntity<Void> updateRecaps(@PathVariable Long personId,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from) {
    notFoundIfEmpty(personDao.byId(personId), "Person");
    consistencyManager.updatePersonRecaps(personId, from);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Contratto individuato dall'id.")
  @GetMapping("/contracts/{contractId}")
  @Transactional
  public ResponseEntity<ContractShowDto> contract(@PathVariable Long contractId) {
    return contract(contractDao.byId(contractId));
  }

  @Operation(summary = "Contratto precedente in ordine temporale.")
  @GetMapping("/contracts/{contractId}/previous")
  @Transactional
  public ResponseEntity<ContractShowDto> previousContract(@PathVariable Long contractId) {
    return contract(Optional.ofNullable(
        personDao.getPreviousPersonContract(findContract(contractId))));
  }

  @Operation(summary = "Intervallo del contratto.")
  @GetMapping("/contracts/{contractId}/interval")
  @Transactional
  public ResponseEntity<DateIntervalDto> contractInterval(@PathVariable Long contractId) {
    return ResponseEntity.ok(interval(
        wrapperFactory.create(findContract(contractId)).getContractDateInterval()));
  }

  @Operation(summary = "Intervallo del contratto per la gestione dei buoni pasto.")
  @GetMapping("/contracts/{contractId}/mealTicketInterval")
  @Transactional
  public ResponseEntity<DateIntervalDto> contractMealTicketInterval(
      @PathVariable Long contractId) {
    return ResponseEntity.ok(interval(wrapperFactory.create(findContract(contractId))
        .getContractDatabaseIntervalForMealTicket()));
  }

  @Operation(summary = "Sede della persona titolare del contratto.")
  @GetMapping("/contracts/{contractId}/office")
  @Transactional
  public ResponseEntity<OfficeShowTerseDto> contractOffice(@PathVariable Long contractId) {
    return ResponseEntity.ok(
        officeMapper.convertTerse(findContract(contractId).getPerson().getOffice()));
  }

  @Operation(summary = "Riepilogo mensile del contratto.")
  @GetMapping("/contracts/{contractId}/monthRecaps/{year}/{month}")
  @Transactional
  public ResponseEntity<ContractMonthRecapDto> contractMonthRecap(@PathVariable Long contractId,
      @PathVariable int year, @PathVariable int month) {
    return ResponseEntity.ok(monthRecap(notFoundIfEmpty(
        wrapperFactory.create(findContract(contractId))
            .getContractMonthRecap(YearMonth.of(year, month)), "Recap")));
  }

  /**
   * Sono esposti solo i parametri che riguardano i buoni pasto.
   */
  @Operation(summary = "Data di inizio della gestione dei buoni pasto della sede.")
  @GetMapping("/offices/{officeId}/configurations/date_start_meal_ticket")
  @Transactional
  public ResponseEntity<String> dateStartMealTicket(@PathVariable Long officeId) {
    return ResponseEntity.ok(configurationManager
        .configValue(findOffice(officeId), EpasParam.DATE_START_MEAL_TICKET).toString());
  }

  /**
   * Il valore predefinito del parametro non è una tipologia di blocchetto, in quel caso la sede
   * non ne ha una impostata.
   */
  @Operation(summary = "Tipologia di blocchetto attuale della sede.")
  @GetMapping("/offices/{officeId}/currentBlockType")
  @Transactional
  public ResponseEntity<String> currentBlockType(@PathVariable Long officeId) {
    return ResponseEntity.ok(notFoundIfEmpty(findOffice(officeId).getConfigurations().stream()
        .filter(configuration -> configuration.epasParam == EpasParam.MEAL_TICKET_BLOCK_TYPE)
        .map(configuration -> configuration.fieldValue)
        .filter(value -> Arrays.stream(BlockType.values())
            .anyMatch(blockType -> blockType.name().equals(value)))
        .findFirst(), "Block type"));
  }

  @Operation(summary = "Id dei contratti delle persone della sede.")
  @GetMapping("/offices/{officeId}/contractIds")
  @Transactional
  public ResponseEntity<List<Long>> officeContractIds(@PathVariable Long officeId) {
    return ResponseEntity.ok(contractDao.contractIdsByOffice(findOffice(officeId)));
  }

  @Operation(summary = "Riepiloghi mensili dei contratti delle persone della sede.")
  @GetMapping("/offices/{officeId}/mealTicketRecaps/{year}/{month}")
  @Transactional
  public ResponseEntity<List<ContractMonthRecapDto>> officeMealTicketRecaps(
      @PathVariable Long officeId, @PathVariable int year, @PathVariable int month) {
    return ResponseEntity.ok(contractMonthRecapDao.getPersonMealticket(
        new org.joda.time.YearMonth(year, month), Optional.empty(), Optional.empty(),
        Set.of(findOffice(officeId))).stream().map(this::monthRecap).toList());
  }

  /**
   * Il servizio dei buoni pasto usa il contractId e il personId del contratto del riepilogo.
   */
  private ContractMonthRecapDto monthRecap(ContractMonthRecap recap) {
    ContractMonthRecapDto dto = monthRecapMapper.convert(recap);
    dto.setContract(contractMapper.convert(recap.getContract()));
    return dto;
  }

  private ResponseEntity<ContractShowDto> contract(Optional<Contract> contract) {
    return ResponseEntity.ok(contractMapper.convert(notFoundIfEmpty(contract, "Contract")));
  }

  private static DateIntervalDto interval(DateInterval interval) {
    val dto = new DateIntervalDto();
    dto.setBegin(interval.getBegin());
    dto.setEnd(interval.getEnd());
    return dto;
  }

  private Person findPerson(Long personId) {
    return notFoundIfEmpty(personDao.byId(personId), "Person");
  }

  private Contract findContract(Long contractId) {
    return notFoundIfEmpty(contractDao.byId(contractId), "Contract");
  }

  private Office findOffice(Long officeId) {
    return notFoundIfEmpty(officeDao.byId(officeId), "Office");
  }

  private static <T> T notFoundIfEmpty(Optional<T> value, String entity) {
    return value.orElseThrow(() -> new EntityNotFoundException(entity + " not found"));
  }
}
