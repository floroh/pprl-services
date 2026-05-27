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

package de.unileipzig.dbs.pprl.core.encoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unileipzig.dbs.pprl.core.common.exceptions.PprlException;
import de.unileipzig.dbs.pprl.core.encoder.record.RecordEncoder;

import java.io.IOException;

/**
 * Utils for (de-)serialization of {@link RecordEncoder} objects to and from strings
 */
public class RecordEncoderSerialization {

  public static ObjectMapper om = new ObjectMapper();
  private static RecordEncoder recordEncoder;
//			.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "@class");

  public static RecordEncoder deserializeJson(String jsonString) throws IOException {
    return om.readValue(jsonString, RecordEncoder.class);
  }

  public static RecordEncoder deserializeJsonSafe(String jsonString) {
    try {
      return om.readValue(jsonString, RecordEncoder.class);
    } catch (JsonProcessingException e) {
      throw new PprlException("Failed to deserialize encoder " + jsonString);
    }
  }

  public static String serializeJson(RecordEncoder recordEncoder) {
    return serializeJson(recordEncoder, true);
  }

  public static String serializeJson(RecordEncoder recordEncoder, boolean pretty) {
    try {
      if (pretty) {
        return om.writerWithDefaultPrettyPrinter()
                .writeValueAsString(recordEncoder);
      } else {
        return om.writeValueAsString(recordEncoder);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.fillInStackTrace());
    }
  }
}
