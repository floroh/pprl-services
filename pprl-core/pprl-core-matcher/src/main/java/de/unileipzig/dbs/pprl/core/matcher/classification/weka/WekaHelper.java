package de.unileipzig.dbs.pprl.core.matcher.classification.weka;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.matcher.classification.WekaClassifier;
import de.unileipzig.dbs.pprl.core.matcher.classification.model.ClassifierConfig;
import de.unileipzig.dbs.pprl.core.matcher.classification.model.InstanceCommon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WekaHelper {

  private static final Logger logger = LogManager.getLogger(WekaHelper.class);

  public static int getIndexOfMaxValue(double[] array) {
    int index = 0;
    double maxProb = 0;
    for (int i = 0; i < array.length; i++) {
      if (array[i] > maxProb) {
        index = i;
        maxProb = array[i];
      }
    }
    return index;
  }

  public static ArrayList<Attribute> createAttributesConfig(ClassifierConfig classifierComponent) {
    ArrayList<Attribute> attributes = new ArrayList<>();
    for (String attrName : classifierComponent.getAttributeNames()) {
      Attribute current = new Attribute(attrName);
      attributes.add(current);
    }

    attributes.add(createNominalAttribute(
        classifierComponent.getClassAttributeName(),
        classifierComponent.getClassAttributeValues()
      )
    );
    return attributes;
  }

  public static Attribute createNominalAttribute(String name, List<String> values) {
    return new Attribute(name, values);
  }

  /**
   * Create a Weka Instance Attribute with boolean values
   *
   * @param name Name of the Attribute
   * @return boolean class attribute
   */
  private static Attribute createBinaryClassAttribute(String name) {
    ArrayList<String> binaryClass = new ArrayList<>();
    binaryClass.add("true");
    binaryClass.add("false");
    return createNominalAttribute(name, binaryClass);
  }

  public static Instance createWekaInstance(InstanceCommon instance, Instances wekaDataset) {
    Instance inst = new DenseInstance(wekaDataset.numAttributes());
    inst.setDataset(wekaDataset);
    for (int i = 0; i < instance.getFeatures().size(); i++) {
      inst.setValue(i, instance.getFeatures().get(i));
    }

    if (instance.isLabeled()) {
      inst.setClassValue(instance.getLabel());
    }
    if (instance.getProbability() != null) {
      inst.setWeight(instance.getProbability());
    }
    return inst;
  }

  public static Double parseNumericAttribute(Record record, String attributeName) {
    Optional<de.unileipzig.dbs.pprl.core.common.model.api.Attribute> optionalAttribute =
      record.getAttribute(attributeName);
    if (optionalAttribute.isPresent()) {
      try {
        return Double.parseDouble(optionalAttribute.get().getAsString());
      } catch (NumberFormatException e) {
        return Double.NaN;
      }
    }
    return Double.NaN;
  }

  public static void writeDatasetToArffFile(Instances wekaDataset, ClassifierConfig config) {
    try {
      String outputPath = config.getTrainingDataOutputDirectory();
      if (outputPath != null && !outputPath.isEmpty()) {
        Path tmpDataset = Files.createFile(
          Path.of(outputPath, config.getConfigString() + UUID.randomUUID() + ".arff"));
        logger.info("Writing dataset to arff file: " + tmpDataset.toAbsolutePath());
        weka.core.converters.ArffSaver saver = new weka.core.converters.ArffSaver();
        saver.setInstances(wekaDataset);
        saver.setFile(tmpDataset.toFile());
        saver.writeBatch();
      }
    } catch (Exception e) {
      logger.error("Could not write dataset to arff file " + e.getMessage());
//      throw new RuntimeException(e);
    }
  }
}
