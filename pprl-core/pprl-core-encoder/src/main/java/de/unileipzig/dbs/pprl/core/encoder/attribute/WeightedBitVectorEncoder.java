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

package de.unileipzig.dbs.pprl.core.encoder.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.core.common.frequencies.WeightCalculator;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagProvider;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureEncoder;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureExtractor;
import de.unileipzig.dbs.pprl.core.encoder.feature.RandomHashing;
import de.unileipzig.dbs.pprl.core.common.preprocessing.AttributePreprocessor;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Metrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WeightedBitVectorEncoder<P, F> implements AttributeEncoder<String, BitVector>, TagProvider {
  private String id;
  private int bvLength;
  private AttributePreprocessor<String, P> preprocessor;
  private FeatureExtractor<P, F> featureExtractor;
  private FeatureEncoder<F, BitVector> featureEncoder;

  private WeightCalculator weightCalculator;

  private final List<Tag> tags = new ArrayList<>();

  @JsonIgnore
  private String attributeName;

  @JsonIgnore
  private Attribute plaintextAttribute;

  private final DistributionSummary attributeLength = Metrics.summary("attributeLength");

  private Logger logger = LogManager.getLogger(WeightedBitVectorEncoder.class);

  public WeightedBitVectorEncoder(String id, FeatureExtractor<P, F> featureExtractor,
    FeatureEncoder<F, BitVector> featureEncoder, int bvLength,
    WeightCalculator weightCalculator) {
    this.id = id;
    this.bvLength = bvLength;
    this.featureExtractor = featureExtractor;
    this.featureEncoder = featureEncoder;
    this.weightCalculator = weightCalculator;
  }

  private WeightedBitVectorEncoder() {
  }

  @Override
  public BitVector encode(String attribute) {
    tags.clear();
    attributeLength.record(attribute.length());

    P processedAttributeValue = getPreprocessedAttributeValue(attribute);
    Collection<F> features = featureExtractor.extract(processedAttributeValue);
    BitVector bv = new BitSetVector(bvLength);

    double scale = 1.0;
    if (plaintextAttribute != null) {
      String plainValue = plaintextAttribute.getAsString();
      scale = weightCalculator.getScale(attributeName, plainValue);
    }
    if (!(featureEncoder instanceof RandomHashing)) {
      throw new IllegalStateException("Misconfiguration: Feature Encoder must be RandomHashing," +
        "but it is not.");
    }
    int kBase = ((RandomHashing) featureEncoder).getNumHashFunctions();
    int kNew = (int) (scale * kBase);
    String scaleDirection = getScaleDirection(scale);
    tags.add(Tag.create("NUM_HASH_FUNCTIONS_SCALE", scaleDirection, scale));
    tags.add(Tag.create("NUM_HASH_FUNCTIONS", String.valueOf(kNew), (double)kNew));
    ((RandomHashing) featureEncoder).setNumHashFunctions(kNew);
    for (F feature : features) {
      bv.or(featureEncoder.encode(bvLength, feature));
    }
    ((RandomHashing) featureEncoder).setNumHashFunctions(kBase);
    return bv;
  }

  private String getScaleDirection(double scale) {
    if (scale > 1) {
      return "Up";
    } else if (scale < 1) {
      return "Down";
    }
    return "Default";
  }

  @Override
  public List<Tag> provideTags() {
    return tags;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public void setPlaintextAttribute(Attribute plaintextAttribute) {
    this.plaintextAttribute = plaintextAttribute;
  }

  private P getPreprocessedAttributeValue(String in) {
    P out;
    if (preprocessor != null) {
      out = preprocessor.preprocess(in);
    } else {
      try {
        out = (P) in;
      } catch (ClassCastException e) {
        throw new RuntimeException("Cannot cast " + in.getClass()
          .getSimpleName());
      }
    }
    return out;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Class<String> getInputClass() {
    return String.class;
  }

  @Override
  public Class<BitVector> getOutputClass() {
    return BitVector.class;
  }

  public int getBvLength() {
    return bvLength;
  }

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  public AttributePreprocessor<String, P> getPreprocessor() {
    return preprocessor;
  }

  public WeightedBitVectorEncoder<P, F> setPreprocessor(AttributePreprocessor<String, P> preprocessor) {
    this.preprocessor = preprocessor;
    return this;
  }

  public FeatureExtractor<P, F> getFeatureExtractor() {
    return featureExtractor;
  }

  public FeatureEncoder<F, BitVector> getFeatureEncoder() {
    return featureEncoder;
  }

  public WeightCalculator getWeightCalculator() {
    return weightCalculator;
  }

  public void setWeightCalculator(WeightCalculator weightCalculator) {
    this.weightCalculator = weightCalculator;
  }

  @Override
  public String toString() {
    return "WeightedBitVectorEncoder{" + "id='" + id + '\'' + ", bvLength=" + bvLength +
      ", featureExtractor=" + featureExtractor + ", featureEncoder=" + featureEncoder + '}';
  }
}
