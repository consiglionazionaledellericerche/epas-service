package it.cnr.iit.epas.dto.v4;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonDayDto extends BaseModelDto {

  private Long personId;
  private LocalDate date;
  private Integer timeAtWork = 0;

  /**
   * Tempo all'interno di timbrature valide.
   */
  private Integer stampingsTime = 0;

  /**
   * Tempo lavorato al di fuori della fascia apertura/chiusura.
   */
  private Integer outOpening = 0;

  private Integer approvedOutOpening = 0;

  private Integer justifiedTimeNoMeal = 0;
  private Integer justifiedTimeMeal = 0;

  private Integer justifiedTimeBetweenZones = 0;

  private Integer workingTimeInMission = 0;
  private Integer difference = 0;
  private Integer progressive = 0;

  /**
   * Minuti tolti per pausa pranzo breve.
   */
  private Integer decurtedMeal = 0;

  private boolean isTicketAvailable;
  private boolean isTicketForcedByAdmin;
  private boolean isWorkingInAnotherPlace;
  private boolean isHoliday;

  /**
   * Tempo lavorato in un giorno di festa.
   */
  private Integer onHoliday = 0;

  /**
   * Tempo lavorato in un giorni di festa ed approvato.
   */
  private Integer approvedOnHoliday = 0;

  //private List<StampingDto> stampings = new ArrayList<StampingDto>();

  //private List<Absence> absences = new ArrayList<Absence>();

  
  //private StampModificationType stampModificationType;

}