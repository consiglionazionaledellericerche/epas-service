package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class StampingFormDto {
  private PersonShowDto person;
  private LocalDate date;
  private List<StampModificationTypeDto> StampTypes= Lists.newArrayList();
  private List<String> offsite= Lists.newArrayList();
  private boolean insertOffsite;
  private boolean insertNormal;
  private boolean autocertification;
  private List<ZoneDto> zones;

}
