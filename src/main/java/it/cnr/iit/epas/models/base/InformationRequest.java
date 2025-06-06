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

package it.cnr.iit.epas.models.base;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.enumerate.InformationType;
import it.cnr.iit.epas.models.informationrequests.InformationRequestEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Classe di base delle richieste di informazione.
 *
 * @author dario
 *
 */
@Getter
@Setter
@ToString(of = {"informationType", "person", "startAt", "endTo", 
    "officeHeadApproved", "officeHeadApprovalRequired"})
@Audited
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "information_requests")
public abstract class InformationRequest extends BaseEntity {
  
  private static final long serialVersionUID = -3294556588987879116L;

  @NotNull
  @ManyToOne(optional = false)
  private Person person;

  /**
   * Data e ora di inizio.
   */
  @NotNull
  private LocalDateTime startAt;

  private LocalDateTime endTo;

  @NotNull
  @Enumerated(EnumType.STRING)
  private InformationType informationType;

  /**
   * Data di approvazione del responsabili sede.
   */
  private LocalDateTime officeHeadApproved;

  /**
   * Data di approvazione dell'amministratore del personale.
   */
  private LocalDateTime administrativeApproved;

  /**
   * Data di approvazione del responsabile di gruppo.
   */
  private LocalDateTime managerApproved;

  /**
   * Indica se è richieta l'approvazione da parte del responsabile di sede.
   */
  private boolean officeHeadApprovalRequired = true;

  /**
   * Indica se è richieta l'approvazione da parte dell'amministrativo.
   */
  private boolean administrativeApprovalRequired = false;

  /**
   * Indica se è richiesta l'approvazione del responsabile di gruppo.
   */
  private boolean managerApprovalRequired = false;

  /**
   * Se il flusso è avviato.
   */
  private boolean flowStarted = false; 

  /**
   * Se il flusso è terminato.
   */
  private boolean flowEnded = false;

  @NotAudited
  @OneToMany(mappedBy = "informationRequest")
  @OrderBy("createdAt DESC")
  private List<InformationRequestEvent> events = Lists.newArrayList();

  @Transient
  public InformationRequestEvent actualEvent() {
    return this.events.get(0);
  }

  @Transient
  public boolean isOfficeHeadApproved() {
    return officeHeadApproved != null;
  }
  
  @Transient
  public boolean isAdministrativeApproved() {
    return administrativeApproved != null;
  }
  
  @Transient
  public boolean isManagerApproved() {
    return managerApproved != null;
  }
  
  /**
   * Un flusso è completato se tutte le approvazioni richieste sono state
   * impostate.
   *
   * @return true se è completato, false altrimenti.
   */
  public boolean isFullyApproved() {
    return (!this.officeHeadApprovalRequired || this.isOfficeHeadApproved())
        && (!this.administrativeApprovalRequired 
            || this.isAdministrativeApproved())
            && (!this.managerApprovalRequired || this.isManagerApproved());
  }
  
  /**
   * Se non sono state già rilasciate approvazioni necessarie allora il possessore 
   * può cancellare o modificare la richiesta.
   *
   * @return true se la richiesta di permesso è ancora modificabile o cancellabile.
   */
  @Transient
  public boolean ownerCanEditOrDelete() {
    return !flowStarted && (officeHeadApproved == null || !officeHeadApprovalRequired);
  }

  @Transient
  public boolean autoApproved() {
    return !this.officeHeadApprovalRequired && !this.managerApprovalRequired 
        && !this.administrativeApprovalRequired;
  }
}