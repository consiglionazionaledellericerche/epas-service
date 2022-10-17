/*
 * Copyright (C) 2021  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.dao.wrapper;

import it.cnr.iit.epas.models.CompetenceCode;

/**
 * Interfaccia per l'estensione con Wrapper del CompetenceCode.
 */
public interface IWrapperCompetenceCode extends IWrapperModel<CompetenceCode> {
  
  public IWrapperCompetenceCode setValue(CompetenceCode cc);

  /**
   * Il totale delle competenze per quel mese.
   *
   * @return il totale per quel mese e quell'anno di ore/giorni relativi a quel codice competenza.
   */
  public int totalFromCompetenceCode(int month, int year, Long officeId);

}