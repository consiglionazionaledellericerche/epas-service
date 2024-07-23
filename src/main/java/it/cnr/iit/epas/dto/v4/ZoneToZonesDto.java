package it.cnr.iit.epas.dto.v4;

import lombok.Data;

@Data
public class ZoneToZonesDto {
  private ZoneDto zoneBase;
  private ZoneDto zoneLinked;
  private int delay;
}
