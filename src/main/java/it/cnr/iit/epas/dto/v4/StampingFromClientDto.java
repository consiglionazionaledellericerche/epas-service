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

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.media.Schema;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import it.cnr.iit.epas.models.exports.StampingFromClient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;
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
@Schema(description = "I dati forniti dai client di ePAS per registrare le timbrature.")
public class StampingFromClientDto {

  @Schema(description = "Numero di badge della persona a cui si riferisce la timbratura",
      example = "9802")
  @NotNull
  private String matricolaFirma;

  @Schema(description = "Verso della timbratura. Entrata = 0, Uscita = 1", 
      defaultValue = "0", allowableValues = { "0", "1" }, example = "0")
  @NotNull @Min(0) @Max(1)
  private Integer operazione;

  @Schema(description = "Anno della timbratura", example = "2023")
  @NotNull @Min(2020) @Max(2120)
  private Integer anno;
  @Schema(description = "MEse della timbratura", example = "1")
  @NotNull @Min(1) @Max(12)
  private Integer mese;
  @Schema(description = "Giorno della timbratura", example = "1")
  @NotNull @Min(1) @Max(31)
  private Integer giorno;
  @Schema(description = "Ora della timbratura", example = "12")
  @NotNull @Min(0) @Max(23)
  private Integer ora;
  @Schema(description = "Minuto della timbratura", example = "0")
  @NotNull @Min(0) @Max(59)
  private Integer minuti;

  @Schema(description = "La causale della timbratura.",
      allowableValues = { "", "motiviDiServizio", "lavoroFuoriSede", "pausaPranzo" },
      defaultValue = "", example = "")
  private String causale;

  //Se impostato a true è una timbratura inserita da amministratore
  @Schema(description = "Se impostato a true la timbratura viene contrassegnata "
      + "come inserita dall'amministratore del personale", defaultValue = "false", 
      example = "false")
  private boolean admin = false;

  //Lettore e terminare modellano la stessa cosa ma ci sono due proprietà per
  //compatibililtà con i vari client
  @Schema(description = "È utile solo in pochi casi, quando è presente tunnel "
      + "di timbratura tra due lettori diversi. " 
      + "Il campo terminale e lettore modellano la stessa cosa, il campo lettore ha "
      + "priorità sul campo lettore.", defaultValue = "", example = "")
  private String terminale;
  @Schema(description = "È utile solo in pochi casi, quando è presente tunnel "
      + "di timbratura tra due lettori diversi. " 
      + "Il campo terminale e lettore modellano la stessa cosa, il campo lettore ha "
      + "priorità sul campo lettore.", defaultValue = "", example = "")
  private String lettore;

  @Schema(description = "Note della timbratura", defaultValue = "", example = "")
  private String note;
  @Schema(description = "Luogo della timbratura", defaultValue = "", example = "")
  private String luogo;
  @Schema(description = "Motivazione della timbratura", defaultValue = "", example = "")
  private String motivazione;

  /**
   * Conversione da DTO ricevuta dal client a oggetto per salvare la timbratura.
   */
  public Optional<StampingFromClient> convert() {
    StampingFromClient stamping = new StampingFromClient();

    stamping.setInOut(getOperazione());

    if (!Strings.isNullOrEmpty(getCausale())) {
      if (StampTypes.isActive(getCausale())) {
        stamping.setStampType(StampTypes.byCode(getCausale()));
      } else {
        log.warn("Causale con codice {} sconosciuta.", getCausale());
      }
    }

    stamping.setMarkedByAdmin(admin);

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
      return Optional.empty();
    }

    stamping.setNote(getNote() != null ? getNote() : null);
    stamping.setPlace(getLuogo() != null ? getLuogo() : null);
    stamping.setReason(getMotivazione() != null ? getMotivazione() : null);
    stamping.setNumeroBadge(getMatricolaFirma());

    log.debug("Effettuato il binding, stampingFromClient = {}", stamping);

    return Optional.of(stamping);
  }
}
