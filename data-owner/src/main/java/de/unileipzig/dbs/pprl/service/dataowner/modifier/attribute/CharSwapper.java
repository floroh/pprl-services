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

public class CharSwapper implements AttributeModifier<String> {

  public static final String TAG_POSTFIX = "_CHARSWAP";

  private final long seed;

  private final Random r;

  public CharSwapper() {
    this(123);
  }

  public CharSwapper(long seed) {
    this.seed = seed;
    this.r = new Random(seed);
  }

  @Override
  public String modify(String in) {
    if (in.isEmpty()) return in;
    int pos = r.nextInt(in.length() - 1);
    char c0 = in.charAt(pos);
    char c1 = in.charAt(pos + 1);
    String out = in.substring(0, pos) + c1 + c0 + in.substring(pos + 2);
    return out;
  }

  @Override
  public String getTagPostFix() {
    return TAG_POSTFIX;
  }

  public long getSeed() {
    return seed;
  }
}
