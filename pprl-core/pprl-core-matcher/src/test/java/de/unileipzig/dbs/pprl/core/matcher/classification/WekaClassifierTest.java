package de.unileipzig.dbs.pprl.core.matcher.classification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.unileipzig.dbs.pprl.core.common.factories.AttributeFactory;
import de.unileipzig.dbs.pprl.core.common.factories.RecordFactory;
import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordPairSimple;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeFrequencyEncoderGroup;
import de.unileipzig.dbs.pprl.core.matcher.classification.model.ClassifierConfig;
import de.unileipzig.dbs.pprl.core.matcher.classification.model.ClassifierMethod;
import de.unileipzig.dbs.pprl.core.matcher.classification.weka.ExpandableRandomForest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class WekaClassifierTest {

  private static final String wekaClassifierJson = """
    {
      "@class": ".WekaClassifier",
      "config": {
        "attributeNames": [
          "FIRSTNAME",
          "LASTNAME",
          "DATEOFBIRTH",
          "YEAROFBIRTH",
          "CITY",
          "PLZ",
          "EQUAL_FIRSTNAME_FRQLABEL",
          "EQUAL_LASTNAME_FRQLABEL",
          "EQUAL_CITY_FRQLABEL"
        ],
        "certaintyThreshold": 0.8,
        "classAttributeName": "match",
        "classAttributeValues": [
          "false",
          "true"
        ],
        "classBalancerMethod": "UPSAMPLING",
        "classifierMethod": "WEKA_J48",
        "classifierOptions": "-C 0.25 -M 60",
        "trainingDataOutputDirectory": "/tmp/"
      },
      "serializedClassifier": \
    "rO0ABXNyABp3ZWthLmNsYXNzaWZpZXJzLnRyZWVzLko0OPz6dNZCE5ZkAgAORgAEbV9DRkkABm1fU2VlZFoADm1fYmluYXJ5U3BsaXRzWgAObV9jb2xsYXBzZVRyZWVaACBtX2RvTm90TWFrZVNwbGl0UG9pbnRBY3R1YWxWYWx1ZUkAC21fbWluTnVtT2JqWgALbV9ub0NsZWFudXBJAAptX251bUZvbGRzWgAVbV9yZWR1Y2VkRXJyb3JQcnVuaW5nWgAQbV9zdWJ0cmVlUmFpc2luZ1oACm1fdW5wcnVuZWRaAAxtX3VzZUxhcGxhY2VaABJtX3VzZU1ETGNvcnJlY3Rpb25MAAZtX3Jvb3R0ACtMd2VrYS9jbGFzc2lmaWVycy90cmVlcy9qNDgvQ2xhc3NpZmllclRyZWU7eHIAI3dla2EuY2xhc3NpZmllcnMuQWJzdHJhY3RDbGFzc2lmaWVyWj6EIb0mI00CAARaAAdtX0RlYnVnWgAYbV9Eb05vdENoZWNrQ2FwYWJpbGl0aWVzSQASbV9udW1EZWNpbWFsUGxhY2VzTAALbV9CYXRjaFNpemV0ABJMamF2YS9sYW5nL1N0cmluZzt4cAAAAAAAAnQAAzEwMD6AAAAAAAABAAEAAAAAPAAAAAADAAEAAAFzcgA1d2VrYS5jbGFzc2lmaWVycy50cmVlcy5qNDguQzQ1UHJ1bmVhYmxlQ2xhc3NpZmllclRyZWW9MeAPUwTmngIABUYABG1fQ0ZaAAltX2NsZWFudXBaABFtX2NvbGxhcHNlVGhlVHJlZVoADm1fcHJ1bmVUaGVUcmVlWgAQbV9zdWJ0cmVlUmFpc2luZ3hyACl3ZWthLmNsYXNzaWZpZXJzLnRyZWVzLmo0OC5DbGFzc2lmaWVyVHJlZYb0WGdRMX6PAgAISQAEbV9pZFoACW1faXNFbXB0eVoACG1faXNMZWFmTAAMbV9sb2NhbE1vZGVsdAAxTHdla2EvY2xhc3NpZmllcnMvdHJlZXMvajQ4L0NsYXNzaWZpZXJTcGxpdE1vZGVsO1sABm1fc29uc3QALFtMd2VrYS9jbGFzc2lmaWVycy90cmVlcy9qNDgvQ2xhc3NpZmllclRyZWU7TAAGbV90ZXN0dAApTHdla2EvY2xhc3NpZmllcnMvdHJlZXMvajQ4L0Rpc3RyaWJ1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"
    }""";

  private static List<RecordPair> trainingData;

  private static RecordPair testDataTrueMatch;
  private static RecordPair testDataTrueNonMatch;

  @BeforeEach
  void beforeEach() {
    trainingData = new ArrayList<>();
    trainingData.add(getRecordPair(List.of(1.0, 1.0, 1.0), true));
    trainingData.add(getRecordPair(List.of(1.0, 1.0, 0.9), true));
    trainingData.add(getRecordPair(List.of(1.0, 1.0, 0.8), true));
    trainingData.add(getRecordPair(List.of(0.4, 0.5, 0.7), false));
    trainingData.add(getRecordPair(List.of(1.0, 0.6, 0.4), false));
    trainingData.add(getRecordPair(List.of(0.2, 0.1, 0.3), false));

    testDataTrueMatch = getRecordPair(List.of(1.0, 1.0, 0.95));
    testDataTrueNonMatch = getRecordPair(List.of(0.2, 0.2, 0.3));
  }

  @Test
  void numericAttribute() {
    Random random = new Random();
    RecordPairSimple recordPair = new RecordPairSimple(
      RecordFactory.getEmptyRecord(), RecordFactory.getEmptyRecord(), 1.0);
    recordPair.addTag((Classifier.Label.TRUE_MATCH).toString());
    recordPair.getLeftRecord().setAttribute(
      "FN" + AttributeFrequencyEncoderGroup.SUFFIX_RELATIVE_FREQUENCY,
      getRandomSimilarityAttribute(random)
    );
    recordPair.getRightRecord().setAttribute(
      "FN" + AttributeFrequencyEncoderGroup.SUFFIX_RELATIVE_FREQUENCY,
      getRandomSimilarityAttribute(random)
    );
    recordPair.setAttributeSimilarities(Map.of(
      "FN", 1.0
    ));
    RecordPair recordPair2 = recordPair.duplicate();
    recordPair2.setAttributeSimilarities(Map.of(
      "FN", 0.7
    ));

    List<RecordPair> modifiedTrainingData = new ArrayList<>();
    modifiedTrainingData.add(recordPair);
    modifiedTrainingData.add(recordPair2);
    ClassifierConfig config = ClassifierConfig.createBinaryClassifierConfig(
      List.of(
        "LEFT_FN" + AttributeFrequencyEncoderGroup.SUFFIX_RELATIVE_FREQUENCY,
        "RIGHT_FN" + AttributeFrequencyEncoderGroup.SUFFIX_RELATIVE_FREQUENCY,
        "EQUAL_FN" + AttributeFrequencyEncoderGroup.SUFFIX_RELATIVE_FREQUENCY
      ),
      ClassifierMethod.WEKA_J48
    );
    config.setClassifierOptions("-C 0.5 -M 10");
    WekaClassifier wekaClassifier = new WekaClassifier(config);
    wekaClassifier.fit(modifiedTrainingData);
  }

  @Test
  void fitAndPredict() {
    ClassifierConfig config = ClassifierConfig.createBinaryClassifierConfig(
      List.of("f0", "f1", "f2"), ClassifierMethod.WEKA_HOEFFDING_TREE);

    WekaClassifier wekaClassifier = new WekaClassifier(config);
    wekaClassifier.fit(trainingData);
    MatchGrade matchGradTrueMatch = wekaClassifier.classify(testDataTrueMatch);
    assertEquals(MatchGrade.CERTAIN_MATCH, matchGradTrueMatch);
    assertEquals(MatchGrade.NON_MATCH, wekaClassifier.classify(testDataTrueNonMatch));

    for (RecordPair trainingDatum : trainingData) {
      Optional<Tag> targetLabelTag = trainingDatum.getTags().stream()
        .filter(tag -> tag.getTag().equals(Classifier.Label.TRUE_MATCH.name()) ||
          tag.getTag().equals(Classifier.Label.TRUE_NON_MATCH.name()))
        .findAny();
      assertTrue(targetLabelTag.isPresent());
      List<MatchGrade> expectedMatchGrades = new ArrayList<>();
      if (targetLabelTag.get().getTag().equals(Classifier.Label.TRUE_MATCH.name())) {
        expectedMatchGrades.add(MatchGrade.CERTAIN_MATCH);
        expectedMatchGrades.add(MatchGrade.POSSIBLE_MATCH);
      } else {
        expectedMatchGrades.add(MatchGrade.PROBABLE_MATCH);
        expectedMatchGrades.add(MatchGrade.NON_MATCH);
      }
      RecordPair unlabeled = trainingDatum.duplicate();
      unlabeled.getTags().clear();
      MatchGrade curMatchGrad = wekaClassifier.classify(unlabeled);
      assertTrue(expectedMatchGrades.contains(curMatchGrad));
    }
  }

  @Test
  void update() {
    ClassifierConfig config = ClassifierConfig.createBinaryClassifierConfig(
      List.of("f0", "f1", "f2"), ClassifierMethod.WEKA_HOEFFDING_TREE);

    List<RecordPair> preTraining = trainingData.subList(0, 5);
    List<RecordPair> updates = trainingData.subList(5, trainingData.size());
    WekaClassifier wekaClassifier = new WekaClassifier(config);
    wekaClassifier.fit(preTraining);
//    System.out.println("Initial model: " + wekaClassifier.getModelDescription());
    for (RecordPair update : updates) {
      update.removeTag(Classifier.Label.TRUE_NON_MATCH.toString());
//      System.out.println(update);
      MatchGrade matchGrade = wekaClassifier.classify(update);
//      System.out.println("Old matchgrade: " + matchGrade);
      update.addTag(Classifier.Label.TRUE_MATCH.toString());
      wekaClassifier.update(update);

      update.removeTag(Classifier.Label.TRUE_MATCH.toString());
      matchGrade = wekaClassifier.classify(update);
//      System.out.println("New matchgrade: " + matchGrade);
      System.out.println(wekaClassifier.getModelDescription());
    }
  }

  @Test
  void updateMultiple() throws JsonProcessingException {
    ClassifierConfig config = ClassifierConfig.createBinaryClassifierConfig(
      List.of("f0", "f1", "f2"), ClassifierMethod.WEKA_HOEFFDING_TREE);

    List<RecordPair> preTraining = trainingData.subList(0, 4);
    List<RecordPair> updates = trainingData.subList(4, trainingData.size());
    WekaClassifier wekaClassifier = new WekaClassifier(config);
    wekaClassifier.fit(preTraining);
//    System.out.println("Initial model: " + wekaClassifier.getModelDescription());

    ObjectMapper om = JsonMapper.builder()
      .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
      .build();
    String classifierString = om.writerWithDefaultPrettyPrinter().writeValueAsString(wekaClassifier);
    wekaClassifier = om.readValue(classifierString, WekaClassifier.class);
//    System.out.println("Deserialized model: " + wekaClassifier.getModelDescription());

    wekaClassifier.update(updates);
//    System.out.println("Updated model: " + wekaClassifier.getModelDescription());
  }

  @Test
  void updateShiftingClassifier() throws JsonProcessingException {
    ClassifierConfig config = ClassifierConfig.createBinaryClassifierConfig(
      List.of("f0", "f1", "f2"), ClassifierMethod.WEKA_EXP_RANDOM_FOREST);
    int initialTreeNumber = 10;
    int newTreesOnUpdate = 3;
    config.setClassifierOptions("-I " + initialTreeNumber + " -J " + newTreesOnUpdate);

    assertTrue(trainingData.size() >= 6);
    List<RecordPair> preTraining = trainingData.subList(0, 3);
    List<RecordPair> updates1 = trainingData.subList(3, 5);
    List<RecordPair> updates2 = trainingData.subList(5, trainingData.size());
    WekaClassifier wekaClassifier = new WekaClassifier(config);
    wekaClassifier.fit(preTraining);
//    System.out.println("Initial model: " + wekaClassifier.getModelDescription());
    weka.classifiers.Classifier model = wekaClassifier.getClassifierModel();
    assertTrue(model instanceof ExpandableRandomForest);
    ExpandableRandomForest shiftingClassifier = (ExpandableRandomForest) model;

    assertEquals(initialTreeNumber, shiftingClassifier.getTrees().length);

    wekaClassifier.update(updates1);
    assertEquals(initialTreeNumber + newTreesOnUpdate, shiftingClassifier.getTrees().length);
//    System.out.println("Updated(1) model: " + wekaClassifier.getModelDescription());

    wekaClassifier.update(updates2);
    assertEquals(initialTreeNumber + 2 * newTreesOnUpdate, shiftingClassifier.getTrees().length);
//    System.out.println("Updated(2) model: " + wekaClassifier.getModelDescription());
  }

  @Test
  void fitStability() {
    ClassifierConfig config = ClassifierConfig.createBinaryClassifierConfig(
      List.of("f0", "f1", "f2"), ClassifierMethod.WEKA_HOEFFDING_TREE);

    List<RecordPair> preTraining = trainingData.subList(0, 4);
    WekaClassifier wekaClassifier = new WekaClassifier(config);
    wekaClassifier.fit(preTraining);
    String model1 = wekaClassifier.getModelDescription();
//    System.out.println("Initial model: " + model1);

    wekaClassifier = new WekaClassifier(config);
    wekaClassifier.fit(preTraining);
    String model2 = wekaClassifier.getModelDescription();
//    System.out.println("Second model: " + model2);
    assertEquals(model1, model2);
  }

  @Test
  void serialization() throws JsonProcessingException {
    ClassifierConfig config = ClassifierConfig.createBinaryClassifierConfig(
      List.of("f0", "f1", "f2"), ClassifierMethod.WEKA_HOEFFDING_TREE);
    WekaClassifier wekaClassifier = new WekaClassifier(config);
    wekaClassifier.fit(trainingData);
    MatchGrade matchGradTrueMatch = wekaClassifier.classify(testDataTrueMatch);

    ObjectMapper om = JsonMapper.builder()
      .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
      .build();

    String classifierString = om.writerWithDefaultPrettyPrinter().writeValueAsString(wekaClassifier);
//    System.out.println(classifierString);

    WekaClassifier clonedWekaClassifier = om.readValue(classifierString, WekaClassifier.class);
    assertEquals(wekaClassifier.getConfig(), clonedWekaClassifier.getConfig());
    MatchGrade matchGradTrueMatch2 = clonedWekaClassifier.classify(testDataTrueMatch);
    assertEquals(matchGradTrueMatch, matchGradTrueMatch2);
  }

  @Test
  void testToString() {
    ClassifierConfig config = ClassifierConfig.createBinaryClassifierConfig(
      List.of("f0", "f1", "f2"), ClassifierMethod.WEKA_HOEFFDING_TREE);

    WekaClassifier wekaClassifier = new WekaClassifier(config);
    assertTrue(wekaClassifier.toString().contains("datasetSize=0"));
    wekaClassifier.fit(trainingData);
    assertTrue(wekaClassifier.toString().contains("datasetSize=" + trainingData.size()));
  }

  @Test
  void missingFeature() throws JsonProcessingException {
    ObjectMapper om = JsonMapper.builder()
      .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
      .build();
    WekaClassifier wekaClassifier = om.readValue(wekaClassifierJson, WekaClassifier.class);

    RecordPairSimple recordPair = new RecordPairSimple(
      RecordFactory.getEmptyRecord(), RecordFactory.getEmptyRecord(), 1.0);
    recordPair.getLeftRecord().setAttribute(
      "FIRSTNAME" + AttributeFrequencyEncoderGroup.SUFFIX_FREQUENCY_LABEL,
      AttributeFactory.getAttribute("1")
    );
    recordPair.getRightRecord().setAttribute(
      "FIRSTNAME" + AttributeFrequencyEncoderGroup.SUFFIX_FREQUENCY_LABEL,
      AttributeFactory.getAttribute("1")
    );
    recordPair.getLeftRecord().setAttribute(
      "LASTNAME" + AttributeFrequencyEncoderGroup.SUFFIX_FREQUENCY_LABEL,
      AttributeFactory.getAttribute("2")
    );
    recordPair.getRightRecord().setAttribute(
      "LASTNAME" + AttributeFrequencyEncoderGroup.SUFFIX_FREQUENCY_LABEL,
      AttributeFactory.getAttribute("1")
    );
    recordPair.setAttributeSimilarities(Map.of(
      "FIRSTNAME", 0.9,
      "LASTNAME", 0.5,
      "YEAROFBIRTH", 1.0,
      "CITY", 1.0,
      "PLZ", 0.5
    ));
    MatchGrade matchGrade = wekaClassifier.classify(recordPair);
    System.out.println(matchGrade);
  }

  private static Attribute getRandomSimilarityAttribute(Random random) {
    return getSimilarityAttribute(random.nextDouble(1.0));
  }

  private static Attribute getSimilarityAttribute(double value) {
    return AttributeFactory.getAttribute(String.format(Locale.US, "%.3f", value));
  }

  private static RecordPairSimple getRecordPair(List<Double> features, boolean isMatch) {
    RecordPairSimple recordPair = getRecordPair(features);
    recordPair.addTag((isMatch ? Classifier.Label.TRUE_MATCH : Classifier.Label.TRUE_NON_MATCH).toString());
    return recordPair;
  }

  private static RecordPairSimple getRecordPair(List<Double> features) {
    Map<String, Double> attributeSimilarities = new HashMap<>();
    for (int i = 0; i < features.size(); i++) {
      attributeSimilarities.put("f" + i, features.get(i));
    }
    return getRecordPair(attributeSimilarities);
  }

  private static RecordPairSimple getRecordPair(Map<String, Double> attributeSimilarities) {
    RecordPairSimple recordPair = new RecordPairSimple(
      RecordFactory.getEmptyRecord(), RecordFactory.getEmptyRecord(), 1.0);
    recordPair.setAttributeSimilarities(attributeSimilarities);

    return recordPair;
  }
}