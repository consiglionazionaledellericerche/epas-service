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

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * Esito della simulazione o dell'inserimento delle assenze di una richiesta.
 */
@Data
public class AbsenceInsertReportDto {

  private List<String> criticalErrors = Lists.newArrayList();
  private int howManySuccess;
  private int howManyError;
  /** Ultimo giorno del periodo considerato nell'inserimento, null se nessuno. */
  private LocalDate lastInsertableDate;
  /** Giorni in cui la persona è in turno o in reperibilità. */
  private List<LocalDate> reperibilityShiftDates = Lists.newArrayList();
  /** Il periodo è interamente coperto da un contratto. */
  private boolean insideContracts;
  /** Il mese di inizio è già stato inviato ad Attestati. */
  private boolean monthAlreadyCertified;
}
