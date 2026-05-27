/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
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

package de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute;

import java.util.Random;

public class CharReplacer implements AttributeModifier<String> {

  public static final String TAG_POSTFIX = "_CHARREPLACE";

  private final long seed;

  private final Random r;

  public CharReplacer() {
    this(123);
  }

  public CharReplacer(long seed) {
    this.seed = seed;
    this.r = new Random(seed);
  }

  @Override
  public String modify(String in) {
    if (in.isEmpty()) return in;
    int pos = r.nextInt(in.length());
    char c = getReplacementChar(in.charAt(pos));
    String out = in.substring(0, pos) + c + in.substring(pos + 1);
    return out;
  }

  @Override
  public String getTagPostFix() {
    return TAG_POSTFIX;
  }

  public long getSeed() {
    return seed;
  }

  private char getReplacementChar(char old) {
    char c = getRandomChar();
    while (c == old) {
      c = getRandomChar();
    }
    return c;
  }

  private char getRandomChar() {
    return (char) (97 + r.nextInt(26));
  }
}
