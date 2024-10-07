package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class BadgeReaderDto {
  private String code;
  private String description;
  private String location;
  private UserShowTerseDto user;
  private Set<BadgeDto> badges = Sets.newHashSet();
  private List<BadgeSystemDto> badgeSystems = Lists.newArrayList();
  private List<ZoneDto> zones = Lists.newArrayList();
  boolean enabled = true;
}
