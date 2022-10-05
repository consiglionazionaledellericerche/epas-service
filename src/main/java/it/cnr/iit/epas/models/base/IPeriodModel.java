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
package it.cnr.iit.epas.models.base;

import it.cnr.iit.epas.utils.DateInterval;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;

/**
 * Il modello è un periodo del contratto con un valore.
 *
 * @author Cristian Lucchesi
 * @author Alessandro Martelli
 */
public interface IPeriodModel {

  /**
   * L'inizio del periodo.
   *
   * @return l'inizio del periodo
   */
  LocalDate getBeginDate();

  /**
   * Imposta l'inizio del periodo.
   *
   * @param begin l'inizio del periodo
   */
  void setBeginDate(@NotNull LocalDate begin);

  /**
   * La fine del periodo.
   *
   * @return la fine del periodo
   */
  LocalDate getEndDate();

  /**
   * Imposta la fine del periodo.
   *
   * @param end la fine del periodo
   */
  void setEndDate(LocalDate end);

  /**
   * L'effettiva data fine nel caso di periodi complessi.
   */
  LocalDate calculatedEnd();
  
  
  /**
   * L'intervallo calcolato del periodo. Considera beginDate e calculatedEnd.
   *
   * @return l'intervallo.
   */
  DateInterval periodInterval();

}
