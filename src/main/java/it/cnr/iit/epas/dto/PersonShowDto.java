package it.cnr.iit.epas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonShowDto extends PeriodModelDto {

  private Long perseoId;
  private String name;
  private String surname;
  private String othersSurnames;
  private String fiscalCode;
  private LocalDate birthday;
  private String email;
  private Long userId;
  private String number;
  private Long oldId;
  private String eppn;
  private String telephone;
  private String fax;

  private String mobile;

  private boolean wantEmail;
  private LocalDateTime updatedAt;

  private Long officeId;

}
