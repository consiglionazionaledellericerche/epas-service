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

package it.cnr.iit.epas.manager.configurations;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dto.v4.configurations.RecomputationDto;
import it.cnr.iit.epas.dto.v4.configurations.RecomputationDto.Status;
import it.cnr.iit.epas.dto.v4.configurations.RecomputationRequestDto;
import it.cnr.iit.epas.dto.v4.configurations.RecomputationRequestDto.OwnerType;
import it.cnr.iit.epas.manager.ConsistencyManager;
import it.cnr.iit.epas.models.base.IPropertiesInPeriodOwner;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Ricalcoli chiesti da epas-configurations-service dopo una modifica di configurazione.
 *
 * <p>Lo stato è tenuto in memoria per un giorno: dopo un riavvio di questo servizio viene perso e
 * i ricalcoli in corso non riprendono, per questo i fallimenti vengono anche registrati nel log.
 * </p>
 */
@Slf4j
@Service
public class ConfigurationRecomputationManager {

  private final Cache<String, RecomputationDto> statuses = CacheBuilder.newBuilder()
      .expireAfterWrite(Duration.ofDays(1)).build();

  private final OfficeDao officeDao;
  private final PersonDao personDao;
  private final ConsistencyManager consistencyManager;
  private final TransactionTemplate transactionTemplate;
  private final ConfigurationRecomputationManager self;

  /**
   * Costruttore: self è il proxy di questo bean, necessario per l'esecuzione asincrona.
   */
  public ConfigurationRecomputationManager(OfficeDao officeDao, PersonDao personDao,
      ConsistencyManager consistencyManager, PlatformTransactionManager transactionManager,
      @Lazy ConfigurationRecomputationManager self) {
    this.officeDao = officeDao;
    this.personDao = personDao;
    this.consistencyManager = consistencyManager;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.self = self;
  }

  /**
   * Registra la richiesta e avvia il ricalcolo in background.
   */
  public RecomputationDto start(RecomputationRequestDto request) {
    val id = UUID.randomUUID().toString();
    val running = new RecomputationDto(id, Status.RUNNING, null);
    statuses.put(id, running);
    self.run(id, request);
    return running;
  }

  public Optional<RecomputationDto> status(String id) {
    return Optional.ofNullable(statuses.getIfPresent(id));
  }

  /**
   * Esegue il ricalcolo nella sua transazione e ne registra l'esito.
   */
  @Async
  public void run(String id, RecomputationRequestDto request) {
    try {
      transactionTemplate.executeWithoutResult(transaction ->
          consistencyManager.performRecomputation(
              owner(request), request.getTypes(), request.getFrom()));
      statuses.put(id, new RecomputationDto(id, Status.COMPLETED, null));
    } catch (RuntimeException e) {
      log.error("Ricalcolo {} fallito per {} {} dal {}", id, request.getOwnerType(),
          request.getOwnerId(), request.getFrom(), e);
      statuses.put(id, new RecomputationDto(id, Status.FAILED, e.getMessage()));
    }
  }

  private IPropertiesInPeriodOwner owner(RecomputationRequestDto request) {
    final Optional<? extends IPropertiesInPeriodOwner> owner =
        request.getOwnerType() == OwnerType.OFFICE
        ? officeDao.byId(request.getOwnerId()) : personDao.byId(request.getOwnerId());
    return owner.orElseThrow(() -> new EntityNotFoundException(
        request.getOwnerType() + " " + request.getOwnerId() + " not found"));
  }

  /**
   * Controlla che il proprietario esista, prima di accettare la richiesta.
   */
  public void checkOwner(RecomputationRequestDto request) {
    transactionTemplate.executeWithoutResult(transaction -> owner(request));
  }
}
