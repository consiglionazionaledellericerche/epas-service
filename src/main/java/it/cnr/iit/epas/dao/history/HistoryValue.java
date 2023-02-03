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

package it.cnr.iit.epas.dao.history;

import com.google.common.base.Function;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.base.Revision;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.hibernate.envers.RevisionType;

/**
 * Rappresenta il valore di un Entity ad una specifica revisione.
 *
 * @author Marco Andreini
 */
public class HistoryValue<T extends BaseEntity> {

  public final T value;
  public final Revision revision;
  public final RevisionType type;


  HistoryValue(T value, Revision revision, RevisionType type) {
    this.value = value;
    this.revision = revision;
    this.type = type;
  }

  /**
   * Funzione per trasformare in un HistoryValue.
   */
  public static <T extends BaseEntity> Function<Object[], HistoryValue<T>> fromTuple(
      final Class<T> cls) {

    return new Function<Object[], HistoryValue<T>>() {
      @Override
      public HistoryValue<T> apply(Object[] tuple) {
        return new HistoryValue<T>(cls.cast(tuple[0]), (Revision) tuple[1],
            (RevisionType) tuple[2]);
      }
    };
  }

  /**
   * Versione formatta della data della revisione.
   */
  public String formattedRevisionDate() {
    LocalDateTime time = this.revision.getRevisionDate();
    if (time == null) {
      return "";
    }
    return DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm").format(time);
  }

  /**
   * Versione formattata dell'owner della revisione.
   */
  public String formattedOwner() {
    if (this.revision.owner != null) {
      return this.revision.owner.getUsername();
    } else {
      return "ePAS";
    }
  }

  /**
   * Verifica se si tratta di una cancellazione.
   */
  public boolean typeIsDel() {
    return type.name().equals("DEL");
  }

  /**
   * Verifica se si tratta di una creazione.
   */
  public boolean typeIsAdd() {
    return type.name().equals("ADD");
  }

}