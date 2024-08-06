package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;

@Data
public class ZoneDto {
  private String name;
  private String description;
  private BadgeReaderDto badgeReader;
  private List<ZoneToZonesDto> zoneLinkedAsMaster = Lists.newArrayList();
  private List<ZoneToZonesDto> zoneLinkedAsSlave = Lists.newArrayList();

}
