/*
 * Copyright (C) 2026  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.dto.v4.workflow;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

/**
 * Dati per simulare o eseguire l'inserimento delle assenze di una richiesta.
 */
@Data
public class AbsenceInsertDto {

  /** Nome del gruppo di assenze (es. FERIE_CNR, G_661). */
  private String groupAbsenceType;
  /** Codice di assenza esplicito, opzionale. */
  private String absenceCode;
  @NotNull
  private LocalDate from;
  private LocalDate to;
  private Integer hours;
  private Integer minutes;
  /** Utente a cui attribuire l'inserimento, chi ha completato il flusso. */
  private Long operatorUserId;
}
