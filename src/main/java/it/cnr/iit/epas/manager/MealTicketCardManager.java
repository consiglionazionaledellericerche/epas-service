/*
 * Copyright (C) 2023  Consiglio Nazionale delle Ricerche
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

import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.MealTicketCardDao;
import it.cnr.iit.epas.dao.MealTicketDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dao.wrapper.IWrapperPerson;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.MealTicket;
import it.cnr.iit.epas.models.MealTicketCard;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.User;
import it.cnr.iit.epas.models.enumerate.BlockType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * Gestore delle informazioni sui buoni pasto elettronici.
 */
@Slf4j
@Service
public class MealTicketCardManager {

  private MealTicketCardDao mealTicketCardDao;
  private MealTicketDao mealTicketDao;
  private IWrapperFactory wrapperFactory;
  private ContractDao contractDao;

  /**
   * Construttore predefinito per l'injection.
   */
  @Inject
  public MealTicketCardManager(
      MealTicketCardDao mealTicketCardDao, MealTicketDao mealTicketDao,
      IWrapperFactory wrapperFactory, ContractDao contractDao) {
    this.mealTicketCardDao = mealTicketCardDao;
    this.mealTicketDao = mealTicketDao;
    this.wrapperFactory = wrapperFactory;
    this.contractDao = contractDao;
  }
  
  /**
   * Salvataggio della mealTicketCard.
   */
  public void saveMealTicketCard(
      MealTicketCard mealTicketCard, Person person, Office office) {
    MealTicketCard previous = person.actualMealTicketCard();
    if (previous != null) {
      log.info("Termino la validità della precedente tessera per {}", person.getFullname());
      previous.setActive(false);
      previous.setEndDate(LocalDate.now().minusDays(1));
      mealTicketCardDao.persist(previous);
      
    }
    mealTicketCard.setActive(true);
    mealTicketCard.setPerson(person);
    mealTicketCard.setDeliveryOffice(office);
    mealTicketCard.setBeginDate(LocalDate.now());
    mealTicketCard.setEndDate(null);
    mealTicketCardDao.persist(mealTicketCard);
    log.info("Aggiunta nuova tessera con identificativo {} a {}", 
        mealTicketCard.getNumber(), person.getFullname());
  }

  /**
   * Assegna i buoni pasto elettronici inseriti su epas finora alla scheda 
   * attuale assegnata al dipendente.
   *
   * @param card l'attuale scheda elettronica per i buoni pasto elettronici
   * @return true se i buoni sono stati assegnati correttamente, false altrimenti.
   */
  public boolean assignOldElectronicMealTicketsToCard(MealTicketCard card) {
    Person person = card.getPerson();
    IWrapperPerson wrPerson = wrapperFactory.create(person);
    Optional<Contract> actualContract = wrPerson.getCurrentContract();
    if (!actualContract.isPresent()) {
      return false;
    }
    List<MealTicket> electronicMealTickets = mealTicketDao
        .getUnassignedElectronicMealTickets(actualContract.get());
    for (MealTicket mealTicket : electronicMealTickets) {
      mealTicket.setMealTicketCard(card);
      mealTicketDao.persist(mealTicket);      
    }
    return true;    
  }
  
  /**
   * Salvataggio del blocco di buoni pasto di tipo elettronico
   * associandoli ad una carta elettronica dei buoni.
   */
  public void saveElectronicMealTicketBlock(
      MealTicketCard card, LocalDate deliveryDate, 
      Integer tickets, User admin, LocalDate expireDate, Office office) {
    String block = "" + card.getNumber() + deliveryDate.getYear() + deliveryDate.getMonthValue();
    for (Integer i = 1; i <= tickets; i++) {
      MealTicket mealTicket = new MealTicket();
      mealTicket.setBlock(block);
      mealTicket.setBlockType(BlockType.electronic);
      mealTicket.setContract(contractDao.getContract(deliveryDate, card.getPerson()));
      mealTicket.setMealTicketCard(card);
      mealTicket.setAdmin(admin.getPerson());
      mealTicket.setDate(deliveryDate);
      mealTicket.setExpireDate(expireDate);
      mealTicket.setOffice(office);
      mealTicket.setNumber(i);
      if (i < 10) {
        mealTicket.setCode(block + "0" + i);
      } else {
        mealTicket.setCode("" + block + i);
      }
      mealTicketDao.persist(mealTicket);      
    }
  }

}