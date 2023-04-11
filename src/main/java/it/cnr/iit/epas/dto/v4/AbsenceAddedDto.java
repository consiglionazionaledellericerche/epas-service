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

import com.google.common.base.Joiner;
import it.cnr.iit.epas.manager.services.absences.model.DayInPeriod.TemplateRow;
import java.util.stream.Collectors;

/**
 * DTO per rappresentare il risultato di un'inserimento di un'assenza via REST.
 */
public class AbsenceAddedDto {

  public String absenceCode;
  public Long absenceTypeId;
  public String date;
  public boolean isOk;
  public String reason;
  
  /**
   * Costruisce un'instanza del DTO AbsenceAddedRest a partire dal DTO templateRow.
   */
  public static AbsenceAddedDto build(TemplateRow templateRow) {
    AbsenceAddedDto aar = new AbsenceAddedDto();
    aar.date = templateRow.absence.getDate().toString();
    aar.absenceCode = templateRow.absence.getAbsenceType().getCode();
    aar.absenceTypeId = templateRow.absence.getAbsenceType().getId();
    aar.isOk = templateRow.absenceErrors.isEmpty();
    aar.reason = Joiner.on(", ").join(
        templateRow.absenceErrors.stream()
        .map(ae -> ae.absenceProblem).collect(Collectors.toList()));
    return aar;
  }
}
