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
package it.cnr.iit.epas.manager.recaps.personstamping;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.manager.cache.StampTypeManager;
import it.cnr.iit.epas.models.StampModificationType;
import it.cnr.iit.epas.models.StampModificationTypeCode;
import it.cnr.iit.epas.models.Stamping;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Oggetto che modella la singola timbratura nelle viste personStamping e stampings.
 *
 * @author Alessandro Martelli
 */
public class StampingTemplate {

  public Stamping stamping;
  public Long stampingId;
  public String colour;
  public int pairId;
  public String pairPosition;            //left center right none
  public LocalDateTime date;

  public String way;
  public String hour = "";

  List<StampModificationType> stampModificationTypes = Lists.newArrayList();

  public boolean valid;

  private static final String STAMPING_FORMAT = "HH:mm";

  /**
   * Costruttore per la StampingTemplate.
   *
   * @param stampTypeManager injected
   * @param stamping         la timbratura formato BaseModel.
   * @param position         la posizione della timbratura all'interno della sua coppia [left,
   *                         center, right, none] (none per nessuna coppia)
   */
  public StampingTemplate(StampTypeManager stampTypeManager, Stamping stamping, String position) {

    this.stamping = stamping;
    this.stampingId = stamping.getId();
    this.pairId = stamping.getPairId();

    this.pairPosition = position;

    //stamping nulle o exiting now non sono visualizzate
    if (stamping.getDate() == null || stamping.isExitingNow()) {
      this.valid = true;
      setColor(stamping);
      return;
    }

    this.date = stamping.getDate();

    this.way = stamping.getWay().getDescription();

    
    this.hour = DateTimeFormatter.ofPattern(STAMPING_FORMAT).format(stamping.getDate());

    //timbratura modificata dall'amministatore
    if (stamping.isMarkedByAdmin()) {
      stampModificationTypes.add(stampTypeManager.getStampMofificationType(
          StampModificationTypeCode.MARKED_BY_ADMIN));
    }

    //timbratura modificata dal dipendente
    if (stamping.isMarkedByEmployee()) {
      stampModificationTypes.add(stampTypeManager.getStampMofificationType(
          StampModificationTypeCode.MARKED_BY_EMPLOYEE));
    }
    
    //timbratura modificata dal dipendente
    if (stamping.isMarkedByTelework()) {
      stampModificationTypes.add(stampTypeManager.getStampMofificationType(
          StampModificationTypeCode.MARKED_BY_TELEWORK));
    }

    //timbratura di mezzanotte
    if (stamping.getStampModificationType() != null) {
      stampModificationTypes.add(stamping.getStampModificationType());
    }

    //timbratura valida (colore cella)
    LocalDate today = LocalDate.now();
    LocalDate stampingDate = LocalDate.of(this.date.getYear(),
        this.date.getMonthValue(), this.date.getDayOfMonth());
    if (today.isEqual(stampingDate)) {
      this.valid = true;
    } else {
      this.valid = stamping.isValid();
    }

    setColor(stamping);
  }

  protected void setColor(Stamping stamping) {
    this.colour = stamping.getWay().description;
    if (!this.valid) {
      this.colour = "warn";
    }
  }

  /**
   * Se stampare il popover sulla stampingTemplate.
   */
  public boolean showPopover() {
    if (!stampModificationTypes.isEmpty() || stamping.getStampType() != null) {
      return true;
    }
    return false;
  }

}
