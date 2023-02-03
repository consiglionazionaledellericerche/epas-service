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

import com.google.common.base.Strings;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import it.cnr.iit.epas.models.exports.StampingFromClient;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Rappresentazione dei dati inviati dai client predefiniti di ePAS.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@Data
public class StampingFromClientDto {

  private Integer operazione;
  private String causale;
  //Se impostato a true è una timbratura inserita da amministratore
  private String admin;

  //Lettore e terminare modellano la stessa cosa ma ci sono due proprietà per
  //compatibililtà con i vari client
  private String terminale;
  private String lettore;

  private Integer anno;
  private Integer mese;
  private Integer giorno;
  private Integer ora;
  private Integer minuti;

  private String note;
  private String luogo;
  private String motivazione;

  private String matricolaFirma;

  /**
   * Conversione da DTO ricevuta dal client a oggetto per salvare la timbratura.
   */
  public StampingFromClient convert() {
    StampingFromClient stamping = new StampingFromClient();

    stamping.setInOut(getOperazione());

    if (!Strings.isNullOrEmpty(getCausale())) {
      if (StampTypes.isActive(getCausale())) {
        stamping.setStampType(StampTypes.byCode(getCausale()));
      } else {
        log.warn("Causale con codice {} sconosciuta.", getCausale());
      }
    }

    if (!Strings.isNullOrEmpty(getAdmin()) 
        && getAdmin().equalsIgnoreCase("true")) {
      stamping.setMarkedByAdmin(true);
    }

    if (!Strings.isNullOrEmpty(getTerminale())) {
      stamping.setZona(getTerminale());
    } else if (!Strings.isNullOrEmpty(getLettore())) {
      stamping.setZona(getLettore());
    }

    if (getAnno() != null && getMese() != null && getGiorno() != null 
        && getOra() != null && getMinuti() != null) {
      stamping.setDateTime(
          LocalDateTime.of(
              getAnno(), getMese(), getGiorno(), 
              getOra(), getMinuti(), 0));
    } else {
      log.warn("Uno dei parametri relativi alla data è risultato nullo. "
          + "Impossibile crearla. StampingFromClientDto: {}", this);
      return null;
    }

    stamping.setNote(getNote());
    stamping.setPlace(getLuogo());
    stamping.setReason(getMotivazione());
    stamping.setNumeroBadge(getMatricolaFirma());

    log.debug("Effettuato il binding, stampingFromClient = {}", stamping);

    return stamping;
  }
}
