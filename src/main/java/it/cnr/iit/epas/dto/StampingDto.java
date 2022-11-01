package it.cnr.iit.epas.dto;

import it.cnr.iit.epas.models.Stamping.WayType;
import it.cnr.iit.epas.models.enumerate.StampTypes;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class StampingDto {

  private Integer personDayId;

  private StampTypes stampType;

  //private StampModificationType stampModificationType;

  private LocalDateTime date;

  private WayType way;

  private String note;

  private String place;
  private String reason;

  private boolean markedByAdmin;
  private boolean markedByEmployee;
  private boolean markedByTelework;
  private String stampingZone;
}
