package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.PersonReperibilityType;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * DTO contenente la lista degli anni in cui ha lavorato un utente.
 */
@Data
public class ReperibilityTypeDropDownDto {

  LocalDate currentDate;
  PersonReperibilityTypeTerseDto reperibilitySelected;
  List<PersonReperibilityTypeTerseDto> reperibilities = Lists.newArrayList();
  boolean editable;
}