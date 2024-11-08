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

import de.unileipzig.dbs.pprl.core.matcher.matcher.BatchMatcher;
import de.unileipzig.dbs.pprl.core.matcher.matcher.IncrementalMatcher;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MatcherSerializationTest extends MatcherTestBase {

  @Test
  void bitVectorMatcher() throws IOException {
    BatchMatcher matcher = getFbfMatcherBatch();

    String jsonString = MatcherSerialization.serializeJson(matcher);
    assertFalse(jsonString.isEmpty());

    BatchMatcher clone = MatcherSerialization.deserializeJsonBatch(jsonString);
    assertNotNull(clone);
  }

  @Test
  void incrementalRbfMatcher() throws Exception {
    IncrementalMatcher matcher = getRbfMatcherIncremental();

    String jsonString = MatcherSerialization.serializeJson(matcher);
    assertFalse(jsonString.isEmpty());

    IncrementalMatcher clone = MatcherSerialization.deserializeJsonIncremental(jsonString);
    assertNotNull(clone);
  }

  @Test
  void rbfMatcher() throws IOException {
    BatchMatcher matcher = getRbfMatcher();

    String jsonString = MatcherSerialization.serializeJson(matcher);
    assertFalse(jsonString.isEmpty());

    BatchMatcher clone = MatcherSerialization.deserializeJsonBatch(jsonString);
    assertNotNull(clone);
  }

  @Test
  void stringMatcher() throws IOException {
    BatchMatcher matcher = getExamplePlainMatcher();

    String jsonString = MatcherSerialization.serializeJson(matcher);
    assertFalse(jsonString.isEmpty());

    BatchMatcher clone = MatcherSerialization.deserializeJsonBatch(jsonString);
    assertNotNull(clone);
  }
}