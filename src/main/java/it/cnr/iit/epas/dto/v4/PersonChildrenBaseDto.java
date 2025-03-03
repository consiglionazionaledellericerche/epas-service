package it.cnr.iit.epas.dto.v4;

import java.time.LocalDate;
import lombok.Data;

@Data
public class PersonChildrenBaseDto {

  private String name;
  private String surname;
  private LocalDate bornDate;
}
