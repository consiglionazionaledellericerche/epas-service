package it.cnr.iit.epas.helpers.converter;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import javax.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YearMonthDateAttributeConverter
    implements AttributeConverter<YearMonth, String> {

  private static final String YEAR_MONTH_FORMAT = "yyyy-MM"; // You can adjust the format as needed

  @Override
  public String convertToDatabaseColumn(YearMonth attribute) {
    return attribute.format(DateTimeFormatter.ofPattern(YEAR_MONTH_FORMAT));
  }

  @Override
  public YearMonth convertToEntityAttribute(String dbData) {
    return YearMonth.parse(dbData, DateTimeFormatter.ofPattern(YEAR_MONTH_FORMAT));
  }

}