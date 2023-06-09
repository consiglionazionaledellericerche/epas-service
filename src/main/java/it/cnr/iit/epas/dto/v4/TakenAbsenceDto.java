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

package it.cnr.iit.epas.dto.v4;

import it.cnr.iit.epas.models.absences.AmountType;
import lombok.Data;

/**
 * DTO per una specifica giornata all'interno di un periodo di assenze.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class TakenAbsenceDto {

  public AbsenceShowTerseDto absence;
  public AmountType amountType;             // = 0;
  public long periodTakableTotal;   //situazione prima della assenza
  public long periodTakenBefore;
  public int takenAmount;
  public boolean beforeInitialization; // = false;
  public boolean toInsert;          //segnala che è l'assenza da inserire nella chain
}