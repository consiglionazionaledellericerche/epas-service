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

package it.cnr.iit.epas.tests.missions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import it.cnr.iit.epas.dao.wrapper.WrapperFactory;
import it.cnr.iit.epas.manager.MissionManager;
import it.cnr.iit.epas.manager.services.absences.AbsenceService;
import it.cnr.iit.epas.models.exports.MissionFromClient;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@Transactional
@SpringBootTest
class MissionManagerTest {

  @Inject
  MissionManager missionManager;
  @Inject
  AbsenceService absenceService;
  @Inject
  WrapperFactory wrapperFactory;
  @Inject
  H2Examples h2Examples;

  @Test
  public void createMission() {
    absenceService.enumInitializator();
    
    val person = h2Examples.normalEmployee(LocalDate.now(), Optional.empty());
    assertEquals(1, person.getContracts().size());
    log.info("createMission -> Created person id = {}. {}", person.getId(), person);
    val currentContract = wrapperFactory.create(person).getCurrentContract();
    log.info("createMission -> currentContract {}", currentContract);
    assertTrue(currentContract.isPresent());
    val currentYear = YearMonth.now().getYear();
    val mission = MissionFromClient.builder()
        .id(currentYear * 100000 + 10101L)
        .anno(currentYear).codiceSede(person.getOffice().getCode())
        .destinazioneMissione("ITALIA").matricola(person.getNumber())
        .person(person)
        .dataInizio(LocalDateTime.now().plusDays(1)).dataFine(LocalDateTime.now().plusDays(3))
        .build();

    val missionCreated = missionManager.createMissionFromClient(mission, true);
    Assertions.assertThat(missionCreated).isTrue();

  }
}