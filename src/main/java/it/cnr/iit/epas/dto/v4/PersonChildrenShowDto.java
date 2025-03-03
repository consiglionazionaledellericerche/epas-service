package it.cnr.iit.epas.dto.v4;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonChildrenShowDto extends PersonChildrenBaseDto {

  private Long id;
}
