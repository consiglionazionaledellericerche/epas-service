/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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