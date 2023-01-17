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

package it.cnr.iit.epas.manager.recaps.recomputation;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.models.base.IPropertyInPeriod;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.ToString;

/**
 * Riepilogo dei dati ricalcolati.
 */
@ToString
public class RecomputeRecap {

  public List<IPropertyInPeriod> periods = Lists.newArrayList();
  
  public LocalDate recomputeFrom;
  public Optional<LocalDate> recomputeTo;
  public boolean onlyRecaps;

  //Dato da utilizzare in caso di modifica contratto.
  public boolean initMissing;
  
  //Dato da utilizzare in caso di modifica configurazione.
  public EpasParam epasParam; 
  
  
  public boolean needRecomputation;

  public long days;
}
