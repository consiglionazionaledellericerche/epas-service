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

package it.cnr.iit.epas.models.flows;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.base.MutableModel;
import it.cnr.iit.epas.models.flows.enumerate.AbsenceRequestType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Richiesta di assenza.
 *
 * @author Cristian Lucchesi
 *
 */
@Getter
@Setter
@ToString(of = {"type", "person", "startAt", "endTo", 
    "managerApproved", "administrativeApproved", "officeHeadApproved",
    "administrativeApprovalRequired", "officeHeadApprovalRequired",
    "officeHeadApprovalForManagerRequired"})
@Audited
@Entity
@Table(name = "absence_requests")
public class AbsenceRequest extends MutableModel {

  private static final long serialVersionUID = 328199210648734558L;

  @NotNull
  @Enumerated(EnumType.STRING)
  private AbsenceRequestType type;

  @NotNull
  @ManyToOne(optional = false)
  private Person person;

  /**
   * Data e ora di inizio.
   */
  @NotNull
  private LocalDateTime startAt;

  private LocalDateTime endTo;

  /*
   * Campi ore e minuti per le assenze orarie
   */
  private Integer hours;

  private Integer minutes;

  /**
   * Descrizione facoltativa della richiesta.
   */
  private String note;

//  /**
//   * Eventuale allegato alla richiesta.
//   */
//  @Column(name = "attachment", nullable = true)
//  public Blob attachment;


  /**
   * Data di approvazione del responsabile.
   */
  private LocalDateTime managerApproved;

  /**
   * Data di approvazione dell'amministrativo.
   */
  private LocalDateTime administrativeApproved;

  /**
   * Data di approvazione del responsabili sede.
   */
  private LocalDateTime officeHeadApproved;

  /**
   * Indica se è richieta l'approvazione da parte del responsabile.
   */
  private boolean managerApprovalRequired = true;

  /**
   * Indica se è richieta l'approvazione da parte dell'amministrativo.
   */
  private boolean administrativeApprovalRequired = true;

  /**
   * Indica se è richieta l'approvazione da parte del responsabile di sede.
   */
  private boolean officeHeadApprovalRequired = true;

  private boolean officeHeadApprovalForManagerRequired = true;

  @NotAudited
  @OneToMany(mappedBy = "absenceRequest", cascade = CascadeType.REMOVE)
  @OrderBy("createdAt DESC")
  private List<AbsenceRequestEvent> events = Lists.newArrayList();

  /**
   * Se il flusso è avviato.
   */
  @Column(name = "flow_started")
  private boolean flowStarted = false; 

  /**
   * Se il flusso è terminato.
   */
  @Column(name = "flow_ended")
  private boolean flowEnded = false;

  @Transient
  public LocalDate startAtAsDate() {
    return startAt != null ? startAt.toLocalDate() : null;
  }

  @Transient
  public LocalDate endToAsDate() {
    return endTo != null ? endTo.toLocalDate() : null;
  }

  @Transient
  public boolean isManagerApproved() {
    return managerApproved != null;
  }

  @Transient
  public boolean isAdministrativeApproved() {
    return administrativeApproved != null;
  }

  @Transient
  public boolean isOfficeHeadApproved() {
    return officeHeadApproved != null;
  }

  /**
   * Se non sono state già rilasciate approvazioni necessarie allora il possessore 
   * può cancellare o modificare la richiesta.
   *
   * @return true se la richiesta di permesso è ancora modificabile o cancellabile.
   */
  @Transient
  public boolean ownerCanEditOrDelete() {
    return !flowStarted && (officeHeadApproved == null || !officeHeadApprovalRequired) 
        && (managerApproved == null || !managerApprovalRequired
        && (administrativeApproved == null || !administrativeApprovalRequired));
  }

  
  @Transient
  public AbsenceRequestEvent actualEvent() {
    return this.events.get(0);
  }
  
  /**
   * Un flusso è completato se tutte le approvazioni richieste sono state
   * impostate.
   *
   * @return true se è completato, false altrimenti.
   */
  public boolean isFullyApproved() {
    return (!this.managerApprovalRequired || this.isManagerApproved()) 
        && (!this.administrativeApprovalRequired 
            || this.isAdministrativeApproved())
        && (!this.officeHeadApprovalRequired || this.isOfficeHeadApproved())
        && (!this.officeHeadApprovalForManagerRequired 
            || this.isOfficeHeadApproved());
  }
}
