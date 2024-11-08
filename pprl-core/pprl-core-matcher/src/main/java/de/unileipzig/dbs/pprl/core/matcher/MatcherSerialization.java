/*
 * Copyright © 2018 - 2021 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unileipzig.dbs.pprl.core.matcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.unileipzig.dbs.pprl.core.matcher.matcher.BatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.IncrementalMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.Matcher;

import java.io.IOException;

/**
 * Utils for (de-)serialization of {@link Matcher} objects to and from strings
 */
public class MatcherSerialization {

  private static final ObjectMapper om = JsonMapper.builder()
    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    .build();

  public static BatchMatcher deserializeJsonBatch(String jsonString) throws IOException {
    return om.readValue(jsonString, BatchMatcher.class);
  }

  public static IncrementalMatcher deserializeJsonIncremental(String jsonString) throws IOException {
    return om.readValue(jsonString, IncrementalMatcher.class);
  }

  public static String serializeJson(Matcher matcher) {
    try {
      return om.writerWithDefaultPrettyPrinter().writeValueAsString(matcher);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize " + matcher + ": " + e);
    }
  }
}
