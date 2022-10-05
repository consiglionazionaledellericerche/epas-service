package it.cnr.iit.epas.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeriodModelDto extends BaseModelDto {

  private LocalDate beginDate;
  private LocalDate endDate;
}
