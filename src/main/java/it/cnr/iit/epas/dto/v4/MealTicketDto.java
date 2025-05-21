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

import java.time.LocalDate;
import lombok.Data;

/**
 * DTO per il buono pasto.
 *
 * @author Cristian Lucchesi
 *
 */
@Data
public class MealTicketDto {

  private ContractShowTerseDto contract;
  private Integer year;
  private LocalDate date;
  private String block;
  private String blockType;
  private Integer number;
  public String code;

  private PersonBaseDto admin;

  private LocalDate expireDate;
  private boolean returned = false;
  private OfficeShowTerseDto office;

  private MealTicketCardDto mealTicketCard;

  private Boolean used = null;
}