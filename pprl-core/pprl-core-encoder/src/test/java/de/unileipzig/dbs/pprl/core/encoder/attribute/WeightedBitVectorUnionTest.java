package de.unileipzig.dbs.pprl.core.encoder.attribute;

import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.frequencies.CsvAttributesFrequencyLookupProvider;
import de.unileipzig.dbs.pprl.core.common.frequencies.ScaledWeightCalculator;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.BitVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.BitSetVector;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.encoder.TestBase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeightedBitVectorUnionTest extends TestBase {

  @Test
  void merge() {
    Map<String, BitVector> attributes = Map.of(
      PersonalAttributeType.FIRSTNAME.asString(), BitSetVector.fromBitString("1010101010"),
      PersonalAttributeType.LASTNAME.asString(), BitSetVector.fromBitString("0000111111"),
      PersonalAttributeType.DATEOFBIRTH.asString(), BitSetVector.fromBitString("1100110010")
    );

    Map<String, Attribute> pAttributes = Map.of(
      PersonalAttributeType.FIRSTNAME.asString(), AttributeFactory.getAttribute("peter"),
      PersonalAttributeType.LASTNAME.asString(), AttributeFactory.getAttribute("unkown"),
      PersonalAttributeType.DATEOFBIRTH.asString(), AttributeFactory.getAttribute("10.10.2000")
    );

    CsvAttributesFrequencyLookupProvider frequencyLookupProvider = new CsvAttributesFrequencyLookupProvider(
      getFullPath("AttributeMostFrequent")
    );
    frequencyLookupProvider.setAttributeNamesToParse(List.of(PersonalAttributeType.FIRSTNAME.asString()));
    frequencyLookupProvider.getFilter().setRelativeTopLimit(0.2);
//    frequencyLookupProvider.getFilter().setRelativeBottomLimit(1.0);
//    frequencyLookupProvider.getFilter().setRelativeBottomLimit(0.5);

    ScaledWeightCalculator weightCalculator = new ScaledWeightCalculator(
      frequencyLookupProvider,
      Map.of(
        PersonalAttributeType.FIRSTNAME.asString(), 12.0,
        PersonalAttributeType.LASTNAME.asString(), 15.0,
        PersonalAttributeType.DATEOFBIRTH.asString(), 15.0
      )
    );
    WeightedBitVectorUnion merger = new WeightedBitVectorUnion(weightCalculator, 20);
    merger.setCurrentPlainAttributes(pAttributes);
    BitVector mergedBv = merger.merge(attributes);
    assertEquals(20, mergedBv.getLength());
    BitVector mergedBvSame = merger.merge(attributes);
    assertEquals(mergedBv, mergedBvSame);
  }
}