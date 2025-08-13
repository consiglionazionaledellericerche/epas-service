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

import io.swagger.v3.oas.annotations.media.Schema;
import it.cnr.iit.epas.models.enumerate.ContractType;
import java.time.LocalDate;
import lombok.Data;
import lombok.ToString;

/**
 * DTO con i dati per la creazione di un nuovo contratto.
 *
 * @author Cristian Lucchesi
 *
 */
@ToString
@Data
public class ContractBaseDto {

  @Schema(description = "Data di inizio del contratto")
  private LocalDate beginDate;
  @Schema(description = "Data di scadenza del contratto")
  private LocalDate endDate;
  @Schema(description = "Data in cui è terminato il contratto, può essere diversa dalla scadenza")
  private LocalDate endContract;
  @Schema(description = "Id esterno utilizzato per la sincronizzazione con l'anagrafica CNR")
  private String perseoId;
  @Schema(description = "Id esterno utilizzato per la sincronizzazione con altre anagrafiche")
  private String externalId;
  @Schema(description = "Contratto con gestione delle busta paga (ex. true per i dipendenti CNR")
  private boolean onCertificate;
  @Schema(description = "Tipo di contratto: structured_public_administration, interim, unstructured", defaultValue = "structured_public_administration")
  private ContractType contractType = ContractType.structured_public_administration;
}