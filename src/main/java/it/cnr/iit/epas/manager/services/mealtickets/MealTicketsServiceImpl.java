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
package it.cnr.iit.epas.manager.services.mealtickets;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.MealTicketDao;
import it.cnr.iit.epas.dao.PersonDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperContract;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.Configuration;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.MealTicket;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.dto.MealTicketComposition;
import it.cnr.iit.epas.models.enumerate.BlockType;
import it.cnr.iit.epas.utils.DateInterval;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;
import it.cnr.iit.epas.manager.ConsistencyManager;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;

/**
 * Implementazione di produzione del servizio meal tickets.
 *
 * @author Alessandro Martelli
 */
@Component
public class MealTicketsServiceImpl implements IMealTicketsService {

  private PersonDao personDao;
  private MealTicketDao mealTicketDao;
  private IWrapperFactory wrapperFactory;
  private ConsistencyManager consistencyManager;
  private MealTicketRecapBuilder mealTicketRecapBuilder;
  private ConfigurationManager configurationManager;
  private Provider<EntityManager> emp;
  /**
   * Costrutture.
   *
   * @param personDao            personDao
   * @param mealTicketDao        mealTicketDao
   * @param configurationManager configurationManager
   * @param consistencyManager   consistencyManager
   * @param wrapperFactory       wrapperFactory
   */
  @Inject
  public MealTicketsServiceImpl(PersonDao personDao,
      MealTicketDao mealTicketDao,
      ConsistencyManager consistencyManager,
      ConfigurationManager configurationManager,
      MealTicketRecapBuilder mealTicketRecapBuilder,
      IWrapperFactory wrapperFactory,
      Provider<EntityManager> emp) {

    this.personDao = personDao;
    this.mealTicketDao = mealTicketDao;
    this.consistencyManager = consistencyManager;
    this.configurationManager = configurationManager;
    this.mealTicketRecapBuilder = mealTicketRecapBuilder;
    this.wrapperFactory = wrapperFactory;
    this.emp = emp;
  }

  /**
   * Creat un'istanza di MealTicketRecap a partire da un Contract.
   */
  @Override
  public Optional<MealTicketRecap> create(Contract contract) {

    Preconditions.checkNotNull(contract);

    Optional<DateInterval> dateInterval = getContractMealTicketDateInterval(contract);

    if (!dateInterval.isPresent()) {
      return Optional.<MealTicketRecap>empty();
    }

    List<PersonDay> personDays = personDao.getPersonDayIntoInterval(contract.person,
        dateInterval.get(), true);

    List<MealTicket> expireOrderedAsc = mealTicketDao
        .contractMealTickets(contract, Optional.empty(),
            MealTicketOrder.ORDER_BY_EXPIRE_DATE_ASC, false);
    
    List<MealTicket> expireOrderedAscPostInit = mealTicketDao
        .contractMealTickets(contract, dateInterval,
            MealTicketOrder.ORDER_BY_EXPIRE_DATE_ASC, false);

    List<MealTicket> deliveryOrderedDesc = mealTicketDao
        .contractMealTickets(contract, Optional.empty(),
            MealTicketOrder.ORDER_BY_DELIVERY_DATE_DESC, false);

    List<MealTicket> returnedDeliveryOrderedDesc = mealTicketDao
        .contractMealTickets(contract, Optional.empty(),
            MealTicketOrder.ORDER_BY_DELIVERY_DATE_DESC, true);

    return Optional.ofNullable(mealTicketRecapBuilder.buildMealTicketRecap(
        contract, dateInterval.get(), personDays, expireOrderedAsc, expireOrderedAscPostInit, 
        deliveryOrderedDesc, returnedDeliveryOrderedDesc));
  }

  /**
   * Ritorna l'intervallo valido ePAS per il contratto riguardo la gestione dei buoni pasto. (scarto
   * la parte precedente a source se definita, e la parte precedente alla data inizio utilizzo per
   * la sede della persona).
   *
   * @return null in caso non vi siano giorni coperti dalla gestione dei buoni pasto.
   */
  @Override
  public Optional<DateInterval> getContractMealTicketDateInterval(Contract contract) {

    DateInterval intervalForMealTicket = wrapperFactory.create(contract)
        .getContractDatabaseIntervalForMealTicket();

    LocalDate officeStartDate = (LocalDate) configurationManager
        .configValue(contract.person.getOffice(), EpasParam.DATE_START_MEAL_TICKET);

    if (officeStartDate.isBefore(intervalForMealTicket.getBegin())) {
      return Optional.of(intervalForMealTicket);
    }
    if (DateUtility.isDateIntoInterval(officeStartDate, intervalForMealTicket)) {
      return Optional.of(new DateInterval(officeStartDate,
          intervalForMealTicket.getEnd()));
    }

    return Optional.<DateInterval>empty();
  }

  /**
   * Genera la lista di MealTicket appartenenti al blocco identificato dal codice codeBlock e dagli
   * estremi.
   *
   * @param codeBlock  codice blocco
   * @param first      il primo codice
   * @param last       l'ultimo codice
   * @param expireDate la data di scadenza
   * @return la lista dei buoni
   */
  @Override
  public List<MealTicket> buildBlockMealTicket(String codeBlock, BlockType blockType,
      Integer first, Integer last, LocalDate expireDate, Office office) {

    List<MealTicket> mealTicketList = Lists.newArrayList();

    for (int i = first; i <= last; i++) {

      MealTicket mealTicket = new MealTicket();
      mealTicket.expireDate = expireDate;
      mealTicket.block = codeBlock;
      mealTicket.blockType = blockType;
      mealTicket.office = office;
      mealTicket.number = i;


      if (i < 10) {
        mealTicket.code = codeBlock + "0" + i;
      } else {
        mealTicket.code = "" + codeBlock + i;
      }
      
      mealTicketList.add(mealTicket);
    }

    return mealTicketList;
  }

  /**
   * Verifica che nel contratto precedente a contract siano avanzati dei buoni pasto assegnati. In
   * tal caso per quei buoni pasto viene modificata la relazione col contratto successivo e cambiata
   * la data di attribuzione in modo che ricada all'inizio del nuovo contratto.
   *
   * @return il numero di buoni pasto trasferiti fra un contratto e l'altro.
   */
  @Override
  public int mealTicketsLegacy(Contract contract) {

    Contract previousContract = personDao.getPreviousPersonContract(contract);
    if (previousContract == null) {
      return 0;
    }

    IWrapperContract wrContract = wrapperFactory.create(previousContract);
    DateInterval previousContractInterval = wrContract.getContractDateInterval();

    Optional<ContractMonthRecap> recap = wrContract.getContractMonthRecap(
        YearMonth.from(previousContractInterval.getEnd()));

    if (!recap.isPresent() || recap.get().remainingMealTickets == 0) {
      return 0;
    }

    int mealTicketsTransfered = 0;

    List<MealTicket> contractMealTicketsDesc = mealTicketDao
        .contractMealTickets(previousContract, Optional.empty(),
            MealTicketOrder.ORDER_BY_DELIVERY_DATE_DESC, false);
      
    LocalDate pastDate = LocalDate.now();
    for (int i = 0; i < recap.get().remainingMealTickets; i++) {

      MealTicket ticketToChange = contractMealTicketsDesc.get(i);
      if (ticketToChange.date.isBefore(pastDate)) {
        pastDate = ticketToChange.date;
      }
      ticketToChange.contract = contract;
      ticketToChange.date = contract.getBeginDate();
      emp.get().merge(ticketToChange);
      //ticketToChange.save();
      mealTicketsTransfered++;
    }

    consistencyManager.updatePersonSituation(contract.person.getId(), pastDate);

    return mealTicketsTransfered;
  }


  /**
   * I tipi di ordinamento per la selezione della lista dei buoni pasto.
   *
   * @author Alessandro Martelli
   */
  public static enum MealTicketOrder {
    ORDER_BY_EXPIRE_DATE_ASC,
    ORDER_BY_DELIVERY_DATE_DESC
  }


  @Override
  public MealTicketComposition whichBlock(MealTicketRecap recap, 
      ContractMonthRecap monthRecap, Contract contract) {
    BlockType blockType = null;
    int buoniCartacei = 0;
    int buoniElettronici = 0;
    int buoniUsati = monthRecap.buoniPastoUsatiNelMese;
    int buoniDaConteggiare = 0;
    MealTicketComposition composition = new MealTicketComposition();
    List<BlockMealTicket> list = recap.getBlockMealTicketReceivedDeliveryDesc();
    if (monthRecap.remainingMealTickets < 0) {
      //devo guardare quale sia il default e contare quanti sono i buoni senza copertura
      buoniDaConteggiare = buoniUsati;
      composition.setBlockMealTicketTypeKnown(false);
      final java.util.Optional<Configuration> conf = 
          contract.person.getOffice().getConfigurations().stream()
          .filter(configuration -> 
          configuration.epasParam == EpasParam.MEAL_TICKET_BLOCK_TYPE).findFirst();
      if (conf.isPresent()) {        
        blockType = BlockType.valueOf(conf.get().fieldValue);
        switch (blockType) {
          case electronic:
            buoniElettronici = buoniDaConteggiare;
            break;
          case papery:
            buoniCartacei = buoniDaConteggiare;
            break;
          default:
            //log.warn("Errore nel parsing dell'enumerato per il tipo di blocchetto. Verificare.");
            break;
        }
        composition.setBlockType(blockType);
      }

    } else {
      int dimBlocchetto = 0;
      composition.setBlockMealTicketTypeKnown(true);
      buoniDaConteggiare = buoniUsati;
      for (BlockMealTicket block : list) {
        dimBlocchetto = block.getDimBlock();
        while (buoniDaConteggiare > 0 && dimBlocchetto != 0) {          
          switch (block.getBlockType()) {
            case papery:
              buoniCartacei++;
              break;
            case electronic:
              buoniElettronici++;
              break;
            default:
              break;
          }
          dimBlocchetto--;
          buoniDaConteggiare--;
        }
      }
    }
    composition.setElectronicMealTicket(buoniElettronici);
    composition.setPaperyMealTicket(buoniCartacei);

    return composition;
  }

}