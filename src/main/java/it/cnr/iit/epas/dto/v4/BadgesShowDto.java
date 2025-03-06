package it.cnr.iit.epas.dto.v4;

import java.util.List;
import lombok.Data;

@Data
public class BadgesShowDto {
  private PersonShowDto person;
  private List<BadgeDto> badges;
}
