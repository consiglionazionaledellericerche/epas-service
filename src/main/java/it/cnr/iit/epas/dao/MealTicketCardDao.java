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

package it.cnr.iit.epas.dao;

import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.models.MealTicket;
import it.cnr.iit.epas.models.MealTicketCard;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.QMealTicket;
import it.cnr.iit.epas.models.QMealTicketCard;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;


/**
 * Dao per le info sulle tessere elettroniche.
 *
 * @author dario
 *
 */
@Component
public class MealTicketCardDao extends DaoBase<MealTicketCard> {

  @Inject
  MealTicketCardDao(Provider<EntityManager> emp) {
    super(emp);
  }
  
  /**
   * Ritorna se esiste la card associata all'id passato come parametro.
   *
   * @param id l'identificativo della card
   * @return l'opzionale contenente o meno la card con id passato come parametro.
   */
  public Optional<MealTicketCard> getMealTicketCardById(Long id) {
    final QMealTicketCard mealTicketCard = QMealTicketCard.mealTicketCard;
    
    return Optional.ofNullable(getQueryFactory().selectFrom(mealTicketCard)
        .where(mealTicketCard.id.eq(id)).fetchFirst());
  }
  
  /**
   * Ritorna la lista dei buoni associati alla card passata come parametro.
   *
   * @param card la tessera elettronica di cui controllare i buoni associati
   * @return la lista dei buoni associati alla tessera passata come parametro.
   */
  public List<MealTicket> getMealTicketByCard(MealTicketCard card) {
    final QMealTicket mealTicket = QMealTicket.mealTicket;
    
    return getQueryFactory().selectFrom(mealTicket)
        .where(mealTicket.mealTicketCard.isNotNull()
            .and(mealTicket.mealTicketCard.eq(card))).fetch();
  }
  
  /**
   * Ritorna l'opzionale contenente o meno la card associata al numero passato come parametro.
   *
   * @param number il numero di card da ricercare
   * @return l'opzionale contenente o meno la card associata al numero passato come parametro.
   */
  public Optional<MealTicketCard> getMealTicketCardByNumberAndOffice(String number, Office office) {
    final QMealTicketCard mealTicketCard = QMealTicketCard.mealTicketCard;
    
    return Optional.ofNullable(getQueryFactory()
        .selectFrom(mealTicketCard)
            .where(mealTicketCard.number.eq(number)
                .and(mealTicketCard.person.office.eq(office))
                .and(mealTicketCard.isActive.eq(true))).fetchFirst());
  }
  
  /**
   * Ritorna se esiste la card con data di consegna uguale a quella passata come parametro.
   *
   * @param deliveryDate la data di consegna della card
   * @return se esiste la card con data di consegna uguale a quella passata come parametro.
   */
  public Optional<MealTicketCard> getMealTicketCardByDeliveryDate(LocalDate deliveryDate, 
      Person person) {
    final it.cnr.iit.epas.models.QMealTicketCard mealTicketCard = QMealTicketCard.mealTicketCard;
    return Optional.ofNullable(getQueryFactory()
        .selectFrom(mealTicketCard)
        .where(mealTicketCard.deliveryDate.eq(deliveryDate)
            .and(mealTicketCard.person.eq(person))).fetchFirst());
  }
  
  
}

