package it.cnr.iit.epas.dto.v4;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorkingTimeTypeDto extends BaseModelDto {

  private String description;

  private Boolean horizontal;

  /**
   * True se il tipo di orario corrisponde ad un "turno di lavoro" false altrimenti.
   */
  private boolean shift = false;

  private boolean disabled = false;

  private boolean enableAdjustmentForQuantity = true;

  private String externalId;

  private LocalDateTime updatedAt;
}
