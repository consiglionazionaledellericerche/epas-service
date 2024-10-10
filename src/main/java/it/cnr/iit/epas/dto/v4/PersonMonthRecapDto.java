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

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;

/**
 * DTO per i PersonMonthRecap per TrainingHours.
 *
 * @author Andrea Generosi
 *
 */
@Data
public class PersonMonthRecapDto {
  @Schema(description = "Persona")
  private PersonShowDto person;
  private Long id;
  private Integer year;
  private Integer month;
  private LocalDate fromDate;
  private LocalDate toDate;
  private Integer trainingHours;
  private boolean hoursApproved = false;
  private boolean editable = false;
}
