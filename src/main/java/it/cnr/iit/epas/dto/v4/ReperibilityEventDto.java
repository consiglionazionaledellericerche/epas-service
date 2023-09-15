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

import it.cnr.iit.epas.models.enumerate.EventColor;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ReperibilityEventDto {

  // Campi di default dell'eventObject fullcalendar
  private String title;
  private boolean allDay;
  private LocalDate start;
  private LocalDate end;
  private String url;
  private String className;
  private String style;
  // Usata la classe Boolean per poter lasciare i valori null in modo che
  // non vengano serializzati nel Json
  private Boolean editable;
  private Boolean startEditable;
  private Boolean durationEditable;
  private Boolean resourceEditable;
  private Boolean overlap;
  private String rendering;
  private String constraint;
  private String color;
  private String backgroundColor;
  private String borderColor;
  private String textColor;

  // CAMPI CUSTOM
  private Long id;
  private Long groupId;
  private Long personId;
  private Long personReperibilityDayId;
  private EventColor eventColor;
  private String email;
  private String mobile;

}
