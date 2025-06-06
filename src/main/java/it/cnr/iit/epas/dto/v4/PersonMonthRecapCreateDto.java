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

package it.cnr.iit.epas.dto.v4;

import lombok.Data;

/**
 * DTO per creare/aggiornare le ore di formazione PersonMonthRecap per TrainingHours.
 *
 * @author Andrea Generosi
 *
 */
@Data
public class PersonMonthRecapCreateDto {
  private Long id = null;
  private Integer year;
  private Integer month;
  private Integer begin;
  private Integer end;
  private Integer trainingHours;
}
