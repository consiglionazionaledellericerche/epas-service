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

package it.cnr.iit.epas.models.flows;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.base.MutableModel;
import it.cnr.iit.epas.models.enumerate.ShiftSlot;
import it.cnr.iit.epas.models.flows.enumerate.CompetenceRequestType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.NotAudited;

/**
 * Richiesta di attribuzione di una competenza mensile.
 *
 * @author Dario Tagliaferri
 *
 */
@Getter
@Setter
@Entity
@Table(name = "competence_requests")
public class CompetenceRequest extends MutableModel {
  
  private static final long serialVersionUID = -2458580703574435339L;

  @NotNull
  @Enumerated(EnumType.STRING)
  private CompetenceRequestType type;

  @NotNull
  @ManyToOne(optional = false)
  private Person person;
  
  /*
   * Descrizione della richiesta
   */
  private String note;

  /*
   * Destinatario della richiesta di cambio turno/reperibilità
   */
  @ManyToOne(optional = true)
  private Person teamMate;

  /*
   * L'eventuale valore da salvare
   */
  private Integer value;

  /*
   * L'eventuale anno in cui salvare la competenza
   */
  private Integer year;
  
  /*
   * L'eventuale mese in cui salvare la competenza
   */
  private Integer month;

  /*
   * L'eventuale data inizio da chiedere
   */
  private LocalDate beginDateToAsk;
  /*
   * L'eventuale data fine da chiedere
   */
  private LocalDate endDateToAsk;
  /*
   * L'eventuale data inizio da dare
   */
  private LocalDate beginDateToGive;
  /*
   * L'eventuale data fine da dare
   */
  private LocalDate endDateToGive;

  /*
   * Lo slot per cui richiedere il cambio
   */
  @Enumerated(EnumType.STRING)
  private ShiftSlot shiftSlot;

  /**
   * Data e ora di inizio.
   */
  @NotNull
  @Column(name = "start_at")
  private LocalDateTime startAt;

  @Column(name = "end_to")
  private LocalDateTime endTo;

  private LocalDateTime employeeApproved;

  private LocalDateTime reperibilityManagerApproved;

  private boolean employeeApprovalRequired = true;

  private boolean reperibilityManagerApprovalRequired = true;

  @NotAudited
  @OneToMany(mappedBy = "competenceRequest")
  @OrderBy("createdAt DESC")
  private List<CompetenceRequestEvent> events = Lists.newArrayList();

  /**
   * Se il flusso è avviato.
   */
  private boolean flowStarted = false; 

  /**
   * Se il flusso è terminato.
   */
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
  public boolean isEmployeeApproved() {
    return employeeApproved != null;
  }  
  
  @Transient
  public boolean isManagerApproved() {
    return reperibilityManagerApproved != null;
  }

  
  /**
   * Se non sono state già rilasciate approvazioni necessarie allora il possessore 
   * può cancellare o modificare la richiesta.
   *
   * @return true se la richiesta di permesso è ancora modificabile o cancellabile.
   */
  @Transient
  public boolean ownerCanEditOrDelete() {
    return !flowStarted  
        && (reperibilityManagerApproved == null || !reperibilityManagerApprovalRequired)
        && (employeeApproved == null || !employeeApprovalRequired);
  }
  
  /**
   * Un flusso è completato se tutte le approvazioni richieste sono state
   * impostate.
   *
   * @return true se è completato, false altrimenti.
   */
  public boolean isFullyApproved() {
    return (!this.reperibilityManagerApprovalRequired || this.isManagerApproved()) 
        && (!this.employeeApprovalRequired
            || this.isEmployeeApproved());
  }
  
  @Transient
  public CompetenceRequestEvent actualEvent() {
    return this.events.get(0);
  }
  
}
