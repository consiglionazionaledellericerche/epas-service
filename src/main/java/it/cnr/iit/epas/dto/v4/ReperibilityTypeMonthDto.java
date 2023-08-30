package it.cnr.iit.epas.dto.v4;

import java.time.YearMonth;
import lombok.Data;

/**
 * DTO contenente la lista degli anni in cui ha lavorato un utente.
 */
@Data
public class ReperibilityTypeMonthDto {

  YearMonth yearMonth;
  PersonReperibilityTypeTerseDto personReperibilityType;
  boolean approved;
}