package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.history.HistoryValue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StampingEditFormDto extends StampingCreateDto  {

  private PersonShowDto person;
  private boolean ownStamping;
  private List<ZoneDto> zones;

  //private List<HistoryValue<StampingDto>> historyStamping;
}
