package it.cnr.iit.epas.models.informationrequests;

import it.cnr.iit.epas.models.base.InformationRequest;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Classe di richiesta di uscite di servizio.
 *
 * @author Dario Tagliaferri
 *
 */
@Getter
@Setter
@Audited
@Entity
@Table(name = "parental_leave_requests")
@PrimaryKeyJoinColumn(name = "informationRequestId")
public class ParentalLeaveRequest extends InformationRequest {

  private static final long serialVersionUID = -8903988853720152320L;
  
  @NotNull
  private LocalDate beginDate;

  @NotNull
  private LocalDate endDate;
  
  ///FIXME: da completare prima del passaggio a spring boot
  //private Blob bornCertificate;
  //private Blob expectedDateOfBirth;
}