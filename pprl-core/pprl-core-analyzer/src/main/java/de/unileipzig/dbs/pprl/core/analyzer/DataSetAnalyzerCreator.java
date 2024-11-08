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

package de.unileipzig.dbs.pprl.core.analyzer;

import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeAvailability;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeBitPositionFrequency;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeLength;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeMostFrequent;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributeMostFrequentNGrams;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributePatternFrequency;
import de.unileipzig.dbs.pprl.core.analyzer.attribute.AttributePrivacy;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.ClusterPairwiseDiff;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.ClusterPairwiseDiffPattern;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.ClusterPairwiseEqual;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.ClusterPairwiseEqualCount;
import de.unileipzig.dbs.pprl.core.analyzer.cluster.ClusterSize;
import de.unileipzig.dbs.pprl.core.analyzer.record.RecordCounter;
import de.unileipzig.dbs.pprl.core.analyzer.record.RecordOverlap;
import de.unileipzig.dbs.pprl.core.analyzer.record.WeightAnalyzer;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class DataSetAnalyzerCreator {

  private static Logger logger = LogManager.getLogger(DataSetAnalyzerCreator.class);

  public static DataSetAnalyzer createForDataSet(Collection<Record> records) {
    boolean isSingleSource = isSingleSource(records);
    if (isEncoded(records)) {
      return createForEncodedDataSet(isSingleSource);
    }
    return create(isSingleSource);
  }

  public static boolean isEncoded(Collection<Record> records) {
    Optional<Record> sampleRecord = records.stream().findFirst();
    if (sampleRecord.isEmpty()) {
      logger.warn("Cannot determine type of an empty dataset");
      return false;
    }
    boolean containsBitVector = sampleRecord.get().getAttributes().values().stream()
      .map(Attribute::getType)
      .distinct()
      .anyMatch(s -> s.equals(Attribute.Type.BITVECTOR));
    logger.info("Dataset type: " + (containsBitVector ? "encoded" : "plain"));
    return containsBitVector;
  }

  public static boolean isSingleSource(Collection<Record> records) {
    boolean isSingleSource = records.stream().map(Record::getId).map(RecordId::getSourceId).distinct().count() == 1;
    logger.info("Dataset type: " + (isSingleSource ? "single source" : "multi source"));
    return isSingleSource;
  }

  public static DataSetAnalyzer createForEncodedDataSet(boolean forSingleSource) {
    DataSetAnalyzer dsa = new DataSetAnalyzer();

    // RecordAnalyzer
    dsa.addAnalyzer(new RecordCounter());

    if (!forSingleSource) {
      dsa.addAnalyzer(new RecordOverlap());

      // ClusterAnalyzer
      dsa.addAnalyzer(new ClusterSize());
      dsa.addAnalyzer(new ClusterPairwiseEqual());
      dsa.addAnalyzer(new ClusterPairwiseEqualCount());
    }

    // AttributeAnalyzer
    dsa.addAnalyzer(new AttributeAvailability());
    dsa.addAnalyzer(new AttributeLength());
    dsa.addAnalyzer(new AttributeBitPositionFrequency());
    dsa.addAnalyzer(new AttributePrivacy());

    return dsa;
  }

  public static DataSetAnalyzer create(boolean forSingleSource) {
    DataSetAnalyzer dsa = new DataSetAnalyzer();

//    dsa.setRecordPreprocessor(new DateSplitter(true));

    // RecordAnalyzer
    dsa.addAnalyzer(new RecordCounter());
    if (!forSingleSource) {
      dsa.addAnalyzer(new RecordOverlap());
      dsa.addAnalyzer(new WeightAnalyzer());
//   		dsa.addAnalyzer(new RecordOverlapEstimate());

      //TODO Find clusters according to equal attributes (e.g. same last name and address -> family)

      // ClusterAnalyzer
      dsa.addAnalyzer(new ClusterSize());
      dsa.addAnalyzer(new ClusterPairwiseDiff());
      dsa.addAnalyzer(new ClusterPairwiseDiffPattern());
      dsa.addAnalyzer(new ClusterPairwiseEqual());
      dsa.addAnalyzer(new ClusterPairwiseEqualCount());
    }

    //TODO DiffSchema
    //TODO Share of completely different attributes of matching records (e.g. family name)
    //TODO Share of completely equal records within a cluster

    // AttributeAnalyzer
    dsa.addAnalyzer(new AttributeAvailability());
    dsa.addAnalyzer(new AttributeLength());
//		dsa.addAnalyzer(new AttributeBitPositionFrequency());
    AttributeMostFrequent attributeMostFrequent = new AttributeMostFrequent();
//    attributeMostFrequent.setMinCount(1);
    dsa.addAnalyzer(attributeMostFrequent);
//		dsa.addAnalyzer(new AttributeMostFrequentNGrams(1));
    dsa.addAnalyzer(new AttributeMostFrequentNGrams(2));
    dsa.addAnalyzer(new AttributeMostFrequentNGrams(3));

    AttributePatternFrequency apf = new AttributePatternFrequency();
    apf.addPatterns("FIRSTNAME", List.of(".*-.*", ".\\.", ".*\\s.*"));
    apf.addPatterns("LASTNAME", List.of(".*-.*", ".\\.", ".*\\s.*"));
    dsa.addAnalyzer(apf);

    //TODO Share of types per attribute (may be mixed...) including share of list attributes
    //TODO correlations of certain attribute types (e.g. first name and date of birth)

    return dsa;
  }
}
