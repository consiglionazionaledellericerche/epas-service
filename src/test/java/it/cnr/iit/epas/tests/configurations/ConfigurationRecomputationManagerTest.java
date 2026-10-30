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

package it.cnr.iit.epas.tests.configurations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dto.v4.configurations.RecomputationDto.Status;
import it.cnr.iit.epas.dto.v4.configurations.RecomputationRequestDto;
import it.cnr.iit.epas.dto.v4.configurations.RecomputationRequestDto.OwnerType;
import it.cnr.iit.epas.manager.ConsistencyManager;
import it.cnr.iit.epas.manager.configurations.ConfigurationRecomputationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam.RecomputationType;
import it.cnr.iit.epas.models.Person;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Il ricalcolo gira in background e ne viene registrato l'esito.
 */
@SpringBootTest
class ConfigurationRecomputationManagerTest {

  private static final LocalDate FROM = LocalDate.of(2026, 1, 1);

  @Inject
  private ConfigurationRecomputationManager manager;
  @MockitoBean
  private PersonDao personDao;
  @MockitoBean
  private ConsistencyManager consistencyManager;

  private RecomputationRequestDto request(Long personId) {
    val request = new RecomputationRequestDto();
    request.setOwnerType(OwnerType.PERSON);
    request.setOwnerId(personId);
    request.setTypes(List.of(RecomputationType.DAYS));
    request.setFrom(FROM);
    return request;
  }

  private Status awaitEnd(String id) throws InterruptedException {
    for (int i = 0; i < 100; i++) {
      Status status = manager.status(id).orElseThrow().getStatus();
      if (status != Status.RUNNING) {
        return status;
      }
      Thread.sleep(50);
    }
    return Status.RUNNING;
  }

  @Test
  void completedRecomputation() throws Exception {
    Person person = new Person();
    when(personDao.byId(1L)).thenReturn(Optional.of(person));

    String id = manager.start(request(1L)).getId();

    assertEquals(Status.COMPLETED, awaitEnd(id));
    verify(consistencyManager, timeout(1000)).performRecomputation(
        person, List.of(RecomputationType.DAYS), FROM);
  }

  @Test
  void failureIsRecorded() throws Exception {
    when(personDao.byId(2L)).thenReturn(Optional.of(new Person()));
    doThrow(new IllegalStateException("errore di calcolo")).when(consistencyManager)
        .performRecomputation(any(), any(), eq(FROM));

    String id = manager.start(request(2L)).getId();

    assertEquals(Status.FAILED, awaitEnd(id));
    assertEquals("errore di calcolo", manager.status(id).orElseThrow().getError());
  }
}
