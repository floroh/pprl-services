package de.unileipzig.dbs.pprl.core.matcher.similarityCalculation.missing;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttributeDependentReplacementTest {

  @Test
  void fixedReplacement() {
    List<String> affectedAttributes = List.of(PersonalAttributeType.FIRSTNAME.name(), PersonalAttributeType.LASTNAME.name(),
      PersonalAttributeType.DATEOFBIRTH.name()
    );
    FixedReplacement fixedReplacement = FixedReplacement.ZERO;
    AttributeDependentReplacement attributeDependentReplacement = new AttributeDependentReplacement(
      affectedAttributes, fixedReplacement
    );

    // Do not affect required non-missing values
    affectedAttributes.forEach(attr -> {
      double outSimilarity = attributeDependentReplacement.modify(0.3, attr);
      assertEquals(0.3, outSimilarity);
    });

    // Replace required missing values
    affectedAttributes.forEach(attr -> {
      double outSimilarity = attributeDependentReplacement.modify(
        MissingSimilarityStrategy.MISSING_SIMILARITY, attr);
      assertNotEquals(MissingSimilarityStrategy.MISSING_SIMILARITY, outSimilarity);
      assertEquals(fixedReplacement.getReplacement(), outSimilarity);
    });

    // Do not affect optional non-missing values;
    double optionalNonMissingSimilarity = attributeDependentReplacement.modify(0.4, "OPTIONAL_ATTRIBUTE");
    assertEquals(0.4, optionalNonMissingSimilarity);

    // Do not replace optional missing values
    double optionalMissingSimilarity = attributeDependentReplacement.modify(
      MissingSimilarityStrategy.MISSING_SIMILARITY, "OPTIONAL_ATTRIBUTE");
    assertEquals(MissingSimilarityStrategy.MISSING_SIMILARITY, optionalMissingSimilarity);

  }
}