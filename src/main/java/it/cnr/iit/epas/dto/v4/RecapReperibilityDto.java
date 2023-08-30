package it.cnr.iit.epas.dto.v4;

import java.time.LocalDate;
import java.util.Map;
import lombok.Data;

/**
 * DTO contenente la lista degli anni in cui ha lavorato un utente.
 */
@Data
public class RecapReperibilityDto {

  LocalDate start;
  PersonReperibilityTypeTerseDto reperibility;
  ReperibilityTypeMonthDto reperibilityTypeMonth;
  Map<PersonShowDto, Integer> workDaysReperibilityCalculatedCompetences;
  Map<PersonShowDto, Integer> holidaysReperibilityCalculatedCompetences;
}