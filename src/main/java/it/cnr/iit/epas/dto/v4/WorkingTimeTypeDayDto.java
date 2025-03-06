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

package it.cnr.iit.epas.dto.v4;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * Per ogni giorno della settimana ci sono riportate le informazioni necessarie all'utilizzo di
 * questa tipologia di orario nel giorno specificato.
 *
 * @author Cristian Lucchesi
 * @author Dario Tagliaferri
 */
@ToString
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkingTimeTypeDayDto extends BaseModelDto {

  private int dayOfWeek;

  /**
   * tempo di lavoro giornaliero espresso in minuti.
   */
  private Integer workingTime;

  /**
   * booleano per controllo se il giorno in questione è festivo o meno.
   */
  private boolean holiday = false;

  /**
   * tempo di lavoro espresso in minuti che conteggia se possibile usufruire del buono pasto.
   */
  private Integer mealTicketTime = 0;

  private Integer breakTicketTime = 0;

  /**
   * La soglia pomeridiana dopo la quale è necessario effettuare lavoro per avere diritto al buono
   * pasto.
   */
  private Integer ticketAfternoonThreshold = 0;

  /**
   * La quantità di lavoro dopo la soglia pomeridiana necessaria per avere diritto al buono pasto.
   */
  private Integer ticketAfternoonWorkingTime = 0;

}