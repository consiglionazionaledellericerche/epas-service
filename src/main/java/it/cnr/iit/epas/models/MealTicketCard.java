package it.cnr.iit.epas.models;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.base.PeriodModel;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;


/**
 * Nuova classe che implementa le card dei buoni elettronici.
 *
 * @author dario
 *
 */
@Getter
@Setter
@Entity
@Audited
@Table(name = "meal_ticket_card")
public class MealTicketCard extends PeriodModel {

  private static final long serialVersionUID = -2102768569995667210L;

  private String number;

  @ManyToOne
  private Person person;

  @OneToMany(mappedBy = "mealTicketCard")
  private List<MealTicket> mealTickets = Lists.newArrayList();

  private boolean isActive;

  /**
   * Data di consegna.
   */
  private LocalDate deliveryDate;

  /**
   * La sede che ha consegnato la tessera.
   */
  @ManyToOne
  private Office deliveryOffice;

  @Override
  public String getLabel() {
    return this.number + "";
  }
}

