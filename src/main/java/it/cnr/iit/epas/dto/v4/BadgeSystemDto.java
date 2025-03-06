package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class BadgeSystemDto {
  private String name;
  private String description;
  private OfficeShowTerseDto office;

  private boolean enabled = true;
}
