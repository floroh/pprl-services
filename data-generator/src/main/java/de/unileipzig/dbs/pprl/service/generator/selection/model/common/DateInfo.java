/*******************************************************************************
 *  Copyright © 2017 - 2022 Leipzig University (Database Research Group)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under 
 * the License.
 *******************************************************************************/
package de.unileipzig.dbs.pprl.service.generator.selection.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DateInfo {

  @BsonIgnore
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

	@BsonProperty(value = "date")
	private String date;

  @BsonProperty(value = "year")
  private Integer year;

  @BsonProperty(value = "month")
	private Integer month;

  @BsonProperty(value = "day")
  private Integer day;

	public DateInfo(Integer year, Integer month, Integer day) {
		this.month = month;
		this.day = day;
		this.year = year;
	}

	public DateInfo(String date) {
		this.date = date;
	}

  public Optional<LocalDate> getAsDate() {
    try {
      return Optional.of(LocalDate.of(year, month, day));
    } catch (DateTimeException e) {
      return Optional.empty();
    }
  }

  public String asString() {
    if (date != null) return date;

    Optional<LocalDate> d = getAsDate();

    return d.map(DATE_FMT::format).orElse(null);
//    if (year != null && month != null && day != null) {
//      return String.format("%04d-%02d-%02d", year, month, day);
//    }
//    return null

  }

  public static DateInfo fromString(String s) {
    if (s == null || s.isEmpty()) {
      return null;
    }

    try {
      LocalDate ld = LocalDate.parse(s, DATE_FMT);  //  format example: '2011-12-03'.
//      LocalDate ld = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);  //  format example: '2011-12-03'.
      return new DateInfo(s, ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth());
    } catch (DateTimeParseException e) {
      return new DateInfo(s);
    }
  }
}
