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

package de.unileipzig.dbs.pprl.core.analyzer.record;

import de.unileipzig.dbs.pprl.core.analyzer.results.Result;
import de.unileipzig.dbs.pprl.core.analyzer.results.ResultSet;
import de.unileipzig.dbs.pprl.core.common.HashUtils;
import de.unileipzig.dbs.pprl.core.common.HelperUtils;
import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.impl.IntVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.encoder.blocking.Soundex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Estimate the number of records that are part of multiple groups
 */
public class RecordOverlapEstimate extends RecordAnalyzer {
  public static final String SOURCE_PAIR = "source pair";
  public static final String METHOD = "method";
  public static final String LENGTH = "length";
  public static final String OVERLAP_MEAN = "overlapMean";
  public static final String OVERLAP_STD = "overlapStd";

  public static final RecordFingerprintBuilder DEFAULT_RECORD_FINGERPRINT_BUILDER = new PkeUniqueId();
//  public static final RecordFingerprintBuilder DEFAULT_RECORD_FINGERPRINT_BUILDER = new PkeSouFNSouLNYOB();
//  public static final RecordFingerprintBuilder DEFAULT_RECORD_FINGERPRINT_BUILDER = new Pke1FN1LNYOB();
//  public static final RecordFingerprintBuilder DEFAULT_RECORD_FINGERPRINT_BUILDER = new Pke3FN3LNYOB();
  public static final int DEFAULT_VECTOR_LENGTH = 1000;
  public static final int DEFAULT_ITERATIONS = 20;

  private int vectorLength;
  private int iterations;
  private RecordFingerprintBuilder recordFingerprintBuilder;

  public RecordOverlapEstimate() {
    this.vectorLength = DEFAULT_VECTOR_LENGTH;
    this.iterations = DEFAULT_ITERATIONS;
    this.recordFingerprintBuilder = DEFAULT_RECORD_FINGERPRINT_BUILDER;
  }

  @Override
  public ResultSet analyze(List<Record> records) {
    ResultSet resultSet = getResultSet();
    Map<String, List<Record>> groupedRecords = RecordUtils.groupById(records, RecordId.SOURCE_ID);
    estimateOverlap(groupedRecords).forEach(resultSet::addResult);
    return resultSet;
  }

  private List<Result> estimateOverlap(Map<String, List<Record>> recordsBySources) {
    List<Result> results = new ArrayList<>();

    List<String> sourceNames = recordsBySources.keySet()
      .stream()
      .sorted()
      .collect(Collectors.toList());

    Map<String, List<IntVector>> cryptoSets = new HashMap<>();
    for (Map.Entry<String, List<Record>> recordsBySource : recordsBySources.entrySet()) {
      cryptoSets.put(recordsBySource.getKey(), getCryptoSets(recordsBySource.getValue()));
    }

    HelperUtils.combination(sourceNames, 2)
      .forEach(sourcePair -> {
        Result result = new Result();
        result.setParam(SOURCE_PAIR, String.join("-", sourcePair));
        result.setParam(LENGTH, String.valueOf(vectorLength));
        result.setParam(METHOD, recordFingerprintBuilder.getClass()
          .getSimpleName());
        DescriptiveStatistics stats = new DescriptiveStatistics();
        IntStream.range(0, iterations)
          .boxed()
          .map(i -> estimateOverlap(cryptoSets.get(sourcePair.getFirst())
            .get(i), cryptoSets.get(sourcePair.get(1))
            .get(i)))
          .forEach(stats::addValue);
        for (int i = 0; i < stats.getN(); i++) {
          List<String> row = new ArrayList<>();
          row.add(String.valueOf(vectorLength));
          row.add(recordFingerprintBuilder.getClass()
            .getSimpleName());
          row.add(String.valueOf(i));
          row.add(String.format(Locale.ENGLISH, "%.3f", stats.getElement(i)));
        }
        result.addMetric(OVERLAP_MEAN, BigDecimal.valueOf(stats.getMean()));
        result.addMetric(OVERLAP_STD, BigDecimal.valueOf(stats.getStandardDeviation()));
        results.add(result);
      });
    return results;
  }

  public static double estimateOverlap(IntVector csA, IntVector csB) {
    double pc = new PearsonsCorrelation().correlation(csA.getAsDoubleArray(), csB.getAsDoubleArray());
    long sumA = csA.getSum();
    long sumB = csB.getSum();
    long maxAB = Math.max(sumA, sumB);
    long minAB = Math.min(sumA, sumB);
    double overlapRelative = pc * Math.sqrt((double) maxAB / (double) minAB);
    double overlap = overlapRelative * minAB;
    return overlap;
  }

  private List<IntVector> getCryptoSets(Collection<Record> records) {
    List<IntVector> sets = new ArrayList<>();
    for (int i = 0; i < iterations; i++) {
      final int tmp = i;
      IntVector cryptoSet = new IntVector(vectorLength);
      records.stream()
        .map(recordFingerprintBuilder)
        .map(pk -> getPublicID(pk, tmp))
        .forEach(cryptoSet::inc);
      sets.add(cryptoSet);
    }
    return sets;
  }

  private int getPublicID(String privateID, int seed) {
    return Math.abs(HashUtils.getHMAC(privateID, String.valueOf(seed))) % vectorLength;
  }

  public void setVectorLength(int vectorLength) {
    this.vectorLength = vectorLength;
  }

  public void setIterations(int iterations) {
    this.iterations = iterations;
  }

  public void setRecordFingerprintBuilder(RecordFingerprintBuilder recordFingerprintBuilder) {
    this.recordFingerprintBuilder = recordFingerprintBuilder;
  }

  public static class PkeUniqueId extends RecordFingerprintBuilder {
    @Override
    public String apply(Record record) {
      RecordId rid = record.getId();
      return rid.getUniqueId();
    }
  }

  public static class Pke1FN1LNDOB extends RecordFingerprintBuilder {
    @Override
    public String apply(Record r) {
      String fn = get(r, "fn").get();
      String ln = get(r, "ln").get();
      String dob = get(r, "dob").get();
      String privKey = new StringBuilder().append(fn.isEmpty() ? "" : fn.charAt(0))
        .append(ln.isEmpty() ? "" : ln.charAt(0))
        .append(dob)
        .toString();
      return privKey;
    }
  }

  public static class Pke3FN3LNDOB extends RecordFingerprintBuilder {
    @Override
    public String apply(Record r) {
      String fn = get(r, "fn").get();
      String ln = get(r, "ln").get();
      String dob = get(r, "dob").get();
      String privKey = StringUtils.substring(fn, 0, 3) + StringUtils.substring(ln, 0, 3) + dob;
      return privKey;
    }
  }

  public static class PkeSouFNSouLNDOB extends RecordFingerprintBuilder {
    @Override
    public String apply(Record r) {
      String fn = get(r, "fn").get();
      String ln = get(r, "ln").get();
      String dob = get(r, "dob").get();
      String privKey = Soundex.encode(fn)
        .get() + Soundex.encode(ln)
        .get() + dob;
      return privKey;
    }
  }

  public static class Pke1FN1LNYOB extends RecordFingerprintBuilder {
    @Override
    public String apply(Record r) {
      String fn = get(r, "fn").get();
      String ln = get(r, "ln").get();
      String yob = get(r, "yob").get();
      String privKey = new StringBuilder().append(fn.isEmpty() ? "" : fn.charAt(0))
        .append(ln.isEmpty() ? "" : ln.charAt(0))
        .append(yob.isEmpty() ? "" : yob)
        .toString();
      return privKey;
    }
  }

  public static class Pke3FN3LNYOB extends RecordFingerprintBuilder {
    @Override
    public String apply(Record r) {
      String fn = get(r, "fn").get();
      String ln = get(r, "ln").get();
      String yob = get(r, "yob").get();
      String privKey = new StringBuilder().append(StringUtils.substring(fn, 0, 3))
        .append(StringUtils.substring(ln, 0, 3))
        .append(yob.isEmpty() ? "" : yob)
        .toString();
      return privKey;
    }
  }

  public static class PkeSouFNSouLNYOB extends RecordFingerprintBuilder {
    @Override
    public String apply(Record r) {
      String fn = get(r, "fn").get();
      String ln = get(r, "ln").get();
      String yob = get(r, "yob").get();
      String privKey = Soundex.encode(fn).get()
        + Soundex.encode(ln).get()
        + yob;
      return privKey;
    }
  }

  public static abstract class RecordFingerprintBuilder implements Function<Record, String> {

    @Override
    public String apply(Record r) {
      return "";
    }

    protected Optional<String> get(Record r, String name) {
      if (name.equals("fn")) {
        return r.getAttribute(PersonalAttributeType.FIRSTNAME.name())
          .map(Attribute::getAsString);
      } else if (name.equals("ln")) {
        return r.getAttribute(PersonalAttributeType.LASTNAME.name())
          .map(Attribute::getAsString);
      } else if (name.equals("yob")) {
        Optional<String> optYOB = r.getAttribute(PersonalAttributeType.YEAROFBIRTH.name())
          .map(Attribute::getAsString);
        if (optYOB.isEmpty() && r.getAttributeNames()
          .contains(PersonalAttributeType.DATEOFBIRTH.name())) {
          String sb = String.valueOf(r.getAttribute(PersonalAttributeType.DAYOFBIRTH.name())
            .map(Attribute::getAsString)) +
            r.getAttribute(PersonalAttributeType.MONTHOFBIRTH.name())
              .map(Attribute::getAsString) + r.getAttribute(PersonalAttributeType.YEAROFBIRTH.name())
            .map(Attribute::getAsString);
          return Optional.of(r.getAttribute(PersonalAttributeType.DATEOFBIRTH.name())
            .map(Attribute::getAsString)
            .map(a -> a.substring(a.length() - 4))
          .get());
        }
        return optYOB;
      } else if (name.equals("dob")) {
        Optional<String> optDOB = r.getAttribute(PersonalAttributeType.DATEOFBIRTH.name())
          .map(Attribute::getAsString);
        if (optDOB.isEmpty() && r.getAttributeNames()
          .containsAll(
            Arrays.asList(
              PersonalAttributeType.DAYOFBIRTH.name(),
              PersonalAttributeType.MONTHOFBIRTH.name(),
              PersonalAttributeType.YEAROFBIRTH.name()
            ))) {
          String sb = String.valueOf(r.getAttribute(PersonalAttributeType.DAYOFBIRTH.name())
            .map(Attribute::getAsString)) +
            r.getAttribute(PersonalAttributeType.MONTHOFBIRTH.name())
              .map(Attribute::getAsString) + r.getAttribute(PersonalAttributeType.YEAROFBIRTH.name())
            .map(Attribute::getAsString);
          return Optional.of(sb);
        }
        return optDOB;
      }
      return r.getAttribute(name)
        .map(Attribute::getAsString);
    }
  }
}
