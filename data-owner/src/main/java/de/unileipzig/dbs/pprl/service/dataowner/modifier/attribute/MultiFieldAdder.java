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

import java.util.List;
import java.util.Random;

public class MultiFieldAdder implements AttributeModifier<String> {

  public static final String TAG_POSTFIX = "_MULTIFIELD";

  private List<String> values;
  private String delimiter;
  private double appendShare = 0.5;

  private long seed;

  private Random r;

  public MultiFieldAdder(List<String> values, String delimiter) {
    this(values, delimiter, 123);
  }

  public MultiFieldAdder(List<String> values, String delimiter, long seed) {
    this.values = values;
    this.delimiter = delimiter;
    this.seed = seed;
  }

  private MultiFieldAdder() {
  }

  @Override
  public String modify(String in) {
    if (r == null) {
      r = new Random(seed);
    }
    boolean append = getAppend();
    String out = append ? in + delimiter + getValue(in) : getValue(in) + delimiter + in;
    return out;
  }

  @Override
  public String getTagPostFix() {
    return TAG_POSTFIX;
  }

  public double getAppendShare() {
    return appendShare;
  }

  public void setAppendShare(double appendShare) {
    this.appendShare = appendShare;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public long getSeed() {
    return seed;
  }

  public void setSeed(long seed) {
    this.seed = seed;
  }

  private String getValue(String old) {
    String r = getRandomValue();
    while (r.equals(old)) {
      r = getRandomValue();
    }
    return r;
  }

  private String getRandomValue() {
    return values.get(r.nextInt(values.size()));
  }

  private boolean getAppend() {
    return (r.nextInt(100) < appendShare * 100);
  }
}
