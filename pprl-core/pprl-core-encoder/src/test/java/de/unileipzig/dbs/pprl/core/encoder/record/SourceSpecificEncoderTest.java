package de.unileipzig.dbs.pprl.core.encoder.record;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.encoder.RecordEncoderSerialization;
import de.unileipzig.dbs.pprl.core.encoder.TestBase;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeEncoder;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeEncoderGroup;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeMerger;
import de.unileipzig.dbs.pprl.core.encoder.attribute.BitVectorEncoder;
import de.unileipzig.dbs.pprl.core.encoder.attribute.BitVectorUnion;
import de.unileipzig.dbs.pprl.core.encoder.attribute.MultiAttributeEncoderGroup;
import de.unileipzig.dbs.pprl.core.encoder.feature.DoubleHashing;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureEncoder;
import de.unileipzig.dbs.pprl.core.encoder.feature.FeatureExtractor;
import de.unileipzig.dbs.pprl.core.encoder.feature.NGramTokenizer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceSpecificEncoderTest extends TestBase {

  private RecordEncoder getSingleSourceEncoder(String source) {
    FeatureExtractor<String, String> biGramExtractor = new NGramTokenizer(2, false);

    FeatureEncoder<String, BitVector> doubleHashingLowWeight = new DoubleHashing(source.equals("A") ? 2 : 3);
    FeatureEncoder<String, BitVector> doubleHashingHighWeight = new DoubleHashing(source.equals("A") ? 5 : 7);

    AttributeEncoder<?, BitVector> ae1 =
            new BitVectorEncoder<>("ae1", biGramExtractor, doubleHashingLowWeight, 128);
    AttributeEncoder<?, BitVector> ae2 =
            new BitVectorEncoder<>("ae2", biGramExtractor, doubleHashingHighWeight, 128);

    AttributeMerger<BitVector> bitVectorMerger = new BitVectorUnion();

    AttributeEncoderGroup<BitVector> aeg1 =
            new MultiAttributeEncoderGroup<>("RBF", bitVectorMerger)
                    .addAttributeEncoder(PersonalAttributeType.FIRSTNAME.toString(), ae1)
                    .addAttributeEncoder(PersonalAttributeType.LASTNAME.toString(), ae2);

    return new DefaultRecordEncoder().addAttributeEncoderGroup(aeg1);
  }

  private SourceSpecificEncoder getEncoder() {
    return new SourceSpecificEncoder(Map.of(
            "A", getSingleSourceEncoder("A"),
            "B", getSingleSourceEncoder("B")
    ));
  }

  @Test
  void differenceComputation() {
    SourceSpecificEncoder encoder = getEncoder();
    String differences = encoder.getDifferences();
//    System.out.println(differences);
    assertFalse(differences.isBlank());
  }

  @Test
  void differentNumberOfHashFunctions() {
    SourceSpecificEncoder encoder = getEncoder();

    Record recordA = getPersonalRecord();
    recordA.getId().addId(RecordId.SOURCE_ID, "A");
    Record recordB = getPersonalRecord();
    recordB.getId().addId(RecordId.SOURCE_ID, "B");

    Record outputA = encoder.encode(recordA);
    Record outputA2 = encoder.encode(recordA);
    Record outputB = encoder.encode(recordB);
    Optional<Attribute> rbfA = outputA.getAttribute("RBF");
    assertTrue(rbfA.isPresent());
    Optional<Attribute> rbfA2 = outputA2.getAttribute("RBF");
    assertTrue(rbfA2.isPresent());
    assertEquals(rbfA.get().getAsString(), rbfA2.get().getAsString());

    Optional<Attribute> rbfB = outputB.getAttribute("RBF");
    assertTrue(rbfB.isPresent());
    assertNotEquals(rbfA.get().getAsString(), rbfB.get().getAsString());
  }

  @Test
  void serialize() {
    SourceSpecificEncoder encoder = getEncoder();
    String jsonString = RecordEncoderSerialization.serializeJson(encoder);
//    System.out.println(jsonString);
    assertFalse(jsonString.isEmpty());

    RecordEncoder encoderClone = null;
    try {
      encoderClone = RecordEncoderSerialization.deserializeJson(jsonString);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    assertTrue(encoderClone instanceof SourceSpecificEncoder);
  }
}