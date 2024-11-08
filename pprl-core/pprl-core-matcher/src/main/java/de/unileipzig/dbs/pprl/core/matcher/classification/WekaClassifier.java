package de.unileipzig.dbs.pprl.core.matcher.classification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.encoder.attribute.AttributeFrequencyEncoderGroup;
import de.unileipzig.dbs.pprl.core.matcher.classification.model.ClassifierConfig;
import de.unileipzig.dbs.pprl.core.matcher.classification.model.InstanceBinary;
import de.unileipzig.dbs.pprl.core.matcher.classification.model.InstanceCommon;
import de.unileipzig.dbs.pprl.core.matcher.classification.model.InstanceWeightMethod;
import de.unileipzig.dbs.pprl.core.matcher.classification.weka.ClassifierFactory;
import de.unileipzig.dbs.pprl.core.matcher.classification.weka.ShiftingClassifier;
import de.unileipzig.dbs.pprl.core.matcher.classification.weka.WekaHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import static de.unileipzig.dbs.pprl.core.matcher.MatcherUtils.getLabel;

/**
 * Classifier based on the Weka library.
 */
public class WekaClassifier implements TrainableClassifier {

  public static final String PREFIX_LEFT_RECORD = RecordPair.Side.LEFT.name() + "_";
  public static final String PREFIX_RIGHT_RECORD = RecordPair.Side.RIGHT.name() + "_";
  public static final String PREFIX_BOTH_EQUAL = "EQUAL_";
  private ClassifierConfig config;

  @JsonIgnore
  private Classifier classifier;

  @JsonIgnore
  private Instances wekaDataset;

  private String serializedClassifier;

  private String serializedDataset;

  private Logger logger = LogManager.getLogger(WekaClassifier.class);

  public WekaClassifier(ClassifierConfig config) {
    this.config = config;
  }

  private WekaClassifier() {
  }

  private void init() {
    if (wekaDataset == null) {
      initWekaDataset();
    }
    if (classifier == null) {
      initClassifier();
    }
  }

  private void initClassifier() {
    if (serializedClassifier != null) {
      logger.debug("Initializing classifier from serialized state");
      try {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
          Base64.getDecoder().decode(serializedClassifier)
        );
        classifier = (Classifier) SerializationHelper.read(inputStream);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      logger.debug("Initialized new classifier with config: " + config.toString());
      try {
        classifier = ClassifierFactory.getClassifier(config);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void initWekaDataset() {
    if (serializedDataset != null && !serializedDataset.isEmpty()) {
      logger.debug("Initializing weka dataset from serialized state");
      try {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
          Base64.getDecoder().decode(serializedDataset)
        );
        weka.core.converters.ArffLoader loader = new weka.core.converters.ArffLoader();
        loader.setSource(inputStream);
        wekaDataset = loader.getDataSet();
        wekaDataset.setClassIndex(config.getAttributeNames().size());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      logger.debug("Initialized new weka dataset with config: " + config.toString());
      ArrayList<Attribute> attributes = WekaHelper.createAttributesConfig(config);
      wekaDataset = new Instances("Dataset", attributes, 100);
      wekaDataset.setClassIndex(config.getAttributeNames().size());
    }
  }

  private void initOnTraining() {
    init();
    if (!(classifier instanceof ShiftingClassifier)) {
      classifier = null;
      serializedClassifier = null;
      initClassifier();
    }
  }

  @Override
  public MatchGrade classify(RecordPair recordPairWithSimilarity) {
    init();
    InstanceCommon instanceCommon = createInstanceCommon(recordPairWithSimilarity);
    InstanceBinary labeledInstance = (InstanceBinary) predict(instanceCommon);
    addProbabilityTag(recordPairWithSimilarity, labeledInstance.getProbability());
    return getMatchGrade(labeledInstance.isTrue(), labeledInstance.getProbability());
  }

  @Override
  public void fit(Collection<RecordPair> recordPairs) {
    boolean isFirstFit = (wekaDataset == null) && (serializedDataset == null);
    resetDataset();
    initOnTraining();
    wekaDataset.addAll(recordPairs.stream()
      .map(this::createInstanceCommon)
      .map(instance -> WekaHelper.createWekaInstance(instance, wekaDataset))
      .collect(Collectors.toList()));
    if (isFirstFit) {
      WekaHelper.writeDatasetToArffFile(wekaDataset, config);
    }
    buildClassifier();
  }

  @Override
  public void update(RecordPair newRecordPair) {
    update(List.of(newRecordPair));
  }

  @Override
  public void update(Collection<RecordPair> newRecordPairs) {
    logger.debug("Pre-Update classifier: " + getModelDescription());
    initOnTraining();
    logger.info("Updating WekaClassifier with {} record pairs", newRecordPairs.size());

    for (RecordPair recordPair : newRecordPairs) {
      InstanceCommon instanceCommon = createInstanceCommon(recordPair);
      Instance wekaInstance = WekaHelper.createWekaInstance(instanceCommon, wekaDataset);
      wekaDataset.add(wekaInstance);
    }
    buildClassifier();
    logger.debug("Updated classifier: " + getModelDescription());
  }

  private void buildClassifier() {
    long highWeightCount = wekaDataset.stream().filter(instance -> instance.weight() > 1).count();
    long defaultWeightCount = wekaDataset.stream().filter(instance -> instance.weight() == 1).count();
    long decreasedWeightCount = wekaDataset.stream().filter(instance -> instance.weight() < 0.98).count();
    logger.info("Building classifier with {} instances ({} high weight, {} default weight, {} <1 weight)",
      wekaDataset.size(), highWeightCount, defaultWeightCount, decreasedWeightCount);
    try {
      classifier.buildClassifier(wekaDataset);
      if (classifier instanceof FilteredClassifier) {
        classifier = ((FilteredClassifier) classifier).getClassifier();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public ClassifierConfig getConfig() {
    return config;
  }

  @JsonIgnore
  public Classifier getClassifierModel() {
    if (classifier == null) initClassifier();
    return classifier;
  }

  @JsonIgnore
  public Instances getDataset() {
    if (wekaDataset == null) initWekaDataset();
    return wekaDataset;
  }

  public String getSerializedClassifier() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      SerializationHelper.write(outputStream, classifier);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    serializedClassifier = Base64.getEncoder().encodeToString(outputStream.toByteArray());
    return serializedClassifier;
  }

  public String getSerializedDataset() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      weka.core.converters.ArffSaver saver = new weka.core.converters.ArffSaver();
      saver.setInstances(wekaDataset);
      saver.setDestination(outputStream);
      saver.writeBatch();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    serializedDataset = Base64.getEncoder().encodeToString(outputStream.toByteArray());
    return serializedDataset;
  }

  public void resetDataset() {
    wekaDataset = null;
    serializedDataset = null;
  }

  private MatchGrade getMatchGrade(boolean isMatch, Double probability) {
    if (isMatch) {
      if (probability >= config.getCertaintyThreshold()) {
        return MatchGrade.CERTAIN_MATCH;
      } else {
        return MatchGrade.PROBABLE_MATCH;
      }
    } else {
      if (probability >= config.getCertaintyThreshold()) {
        return MatchGrade.NON_MATCH;
      } else {
        return MatchGrade.POSSIBLE_MATCH;
      }
    }
  }

  private InstanceCommon predict(InstanceCommon unlabeledInstance) {
    try {
      return predict(unlabeledInstance, classifier, wekaDataset);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static InstanceCommon predict(InstanceCommon unlabeledInstance, Classifier classifier,
    Instances wekaDataset) throws Exception {
    Instance wekaInstance = WekaHelper.createWekaInstance(unlabeledInstance, wekaDataset);
    final double[] prob = classifier.distributionForInstance(wekaInstance);
    int maxIndex = WekaHelper.getIndexOfMaxValue(prob);
    String label = wekaDataset.classAttribute().value(maxIndex);
    Double probability = prob[maxIndex];

    InstanceCommon labeledInstance = unlabeledInstance.duplicate();
    labeledInstance.setLabel(label);
    labeledInstance.setProbability(probability);
    return labeledInstance;
  }

  private InstanceCommon createInstanceCommon(RecordPair recordPairWithSimilarity) {
    Optional<Map<String, Double>> optAttributeSimilarities =
      recordPairWithSimilarity.getAttributeSimilarities();
    if (optAttributeSimilarities.isEmpty()) {
      optAttributeSimilarities = Optional.of(Map.of("Record", recordPairWithSimilarity.getSimilarity()));
    }
    Map<String, Double> attrSimilarities = optAttributeSimilarities.get();

    Vector<Double> features = new Vector<>();
    for (int i = 0; i < wekaDataset.numAttributes() - 1; i++) {
      Attribute attribute = wekaDataset.attribute(i);
      String attributeName = attribute.name();
      Double attributeValue = attrSimilarities.get(attributeName);
      if (attributeValue == null) {
        if (attributeName.contains(AttributeFrequencyEncoderGroup.SUFFIX_RELATIVE_FREQUENCY) ||
          attributeName.contains(AttributeFrequencyEncoderGroup.SUFFIX_RELATIVE_RANK) ||
          attributeName.contains(AttributeFrequencyEncoderGroup.SUFFIX_FREQUENCY_LABEL)) {
          attributeValue = parseRecordSpecificNumericAttribute(recordPairWithSimilarity, attributeName);
        } else {
//        logger.debug("Missing attribute value for " + attributeName);
          attributeValue = Double.NaN;
        }
      }
      features.add(attributeValue);
    }
    Optional<Label> optionalLabel = getLabel(recordPairWithSimilarity);
    double probability = getProbabilityTag(recordPairWithSimilarity).orElse(1.0);
    InstanceBinary instance = optionalLabel
      .map(label -> new InstanceBinary(features, label.equals(Label.TRUE_MATCH), probability))
      .orElseGet(() -> new InstanceBinary(features));
    if (optionalLabel.isPresent() &&
      InstanceWeightMethod.WEIGHTED_PROBABILITY.equals(config.getInstanceWeightMethod())) {
      boolean isFromClericalReview = recordPairWithSimilarity.getTags().stream()
        .filter(tag -> tag.getTag().equals("METHOD"))
        .anyMatch(tag -> tag.getStringValue().contains("CR"));
      if (isFromClericalReview) {
        instance.setProbability(2 * instance.getProbability());
      }
    }
    return instance;
  }

  private static Double parseRecordSpecificNumericAttribute(RecordPair recordPairWithSimilarity,
    String attributeName) {
    if (attributeName.startsWith(PREFIX_LEFT_RECORD)) {
      return WekaHelper.parseNumericAttribute(
        recordPairWithSimilarity.getRecord(RecordPair.Side.LEFT),
        attributeName.replace(PREFIX_LEFT_RECORD, "")
      );
    } else if (attributeName.startsWith(PREFIX_RIGHT_RECORD)) {
      return WekaHelper.parseNumericAttribute(
        recordPairWithSimilarity.getRecord(RecordPair.Side.RIGHT),
        attributeName.replace(PREFIX_RIGHT_RECORD, "")
      );
    } else if (attributeName.startsWith(PREFIX_BOTH_EQUAL)) {
      String strippedAttributeName = attributeName.
        replace(PREFIX_BOTH_EQUAL, "")
        .split("_")[0];
      if (recordPairWithSimilarity.getAttributeSimilarities().isPresent()) {
        Double attrSimilarity = recordPairWithSimilarity.getAttributeSimilarities().get().get(strippedAttributeName);
        if (attrSimilarity != null && attrSimilarity == 1.0) {
          return 1.0 + WekaHelper.parseNumericAttribute(recordPairWithSimilarity.getRecord(RecordPair.Side.LEFT),
            attributeName.replace(PREFIX_BOTH_EQUAL, ""));
        } else {
          return 0.0;
        }
      }
    }
    return Double.NaN;
  }

  @Override
  public String getModelDescription() {
    return "Dataset size: " + ((getDataset() != null)? getDataset().size() : "null") + "\n\n" +
      getClassifierDescription();
  }

  private String getClassifierDescription() {
    return getClassifierModel().toString();
  }

  @Override
  public String toString() {
    return "WekaClassifier{" +
      "config=" + config +
      ", datasetSize=" + ((getDataset() != null)? getDataset().size() : "null") +
      '}';
  }
}
