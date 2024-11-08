package de.unileipzig.dbs.pprl.core.matcher.classification.weka;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;
import weka.core.Utils;

import java.util.Arrays;

public class ExpandableRandomForest extends RandomForest implements ShiftingClassifier {

  public static final int DEFAULT_NUMBER_OF_NEW_TREES_ON_UPDATE = 0;
  public static final int DEFAULT_MAXIMUM_NUMBER_OF_TREES = 0;
  private int numberOfNewTreesOnUpdate = DEFAULT_NUMBER_OF_NEW_TREES_ON_UPDATE;
  private int maximumNumberOfTrees = DEFAULT_MAXIMUM_NUMBER_OF_TREES;

  public static ExpandableRandomForest build(Classifier[] classifiers) {
    ExpandableRandomForest expandableRandomForest = new ExpandableRandomForest();
    expandableRandomForest.m_Classifiers = classifiers;
    return expandableRandomForest;
  }

  @Override
  public void buildClassifier(Instances data) throws Exception {
    if (super.m_Classifiers == null || numberOfNewTreesOnUpdate == 0) {
      super.buildClassifier(data);
    } else {
      update(data);
    }
  }

  @Override
  public void setOptions(String[] options) throws Exception {
    String numberOfNewTreesOnUpdate = Utils.getOption('J', options);
    if (!numberOfNewTreesOnUpdate.isEmpty()) {
      setNumberOfNewTreesOnUpdate(Integer.parseInt(numberOfNewTreesOnUpdate));
    } else {
      setNumberOfNewTreesOnUpdate(DEFAULT_NUMBER_OF_NEW_TREES_ON_UPDATE);
    }
    String maximumNumberOfTrees = Utils.getOption('N', options);
    if (!maximumNumberOfTrees.isEmpty()) {
      setMaximumNumberOfTrees(Integer.parseInt(maximumNumberOfTrees));
    } else {
      setMaximumNumberOfTrees(DEFAULT_MAXIMUM_NUMBER_OF_TREES);
    }
    String bagSize = Utils.getOption('P', options);
    if (!bagSize.isEmpty()) {
      setBagSizePercent(Integer.parseInt(bagSize));
    } else {
      setBagSizePercent(100);
    }

    setCalcOutOfBag(Utils.getFlag('O', options));

    setStoreOutOfBagPredictions(Utils.getFlag("store-out-of-bag-predictions",
      options));

    setOutputOutOfBagComplexityStatistics(Utils.getFlag(
      "output-out-of-bag-complexity-statistics", options));

    setPrintClassifiers(Utils.getFlag("print", options));

    setComputeAttributeImportance(Utils
      .getFlag("attribute-importance", options));

    String iterations = Utils.getOption('I', options);
    if (!iterations.isEmpty()) {
      setNumIterations(Integer.parseInt(iterations));
    } else {
      setNumIterations(defaultNumberOfIterations());
    }

    String numSlots = Utils.getOption("num-slots", options);
    if (!numSlots.isEmpty()) {
      setNumExecutionSlots(Integer.parseInt(numSlots));
    } else {
      setNumExecutionSlots(1);
    }

    RandomTree classifier = new RandomTree();
    classifier.setOptions(options);
//      ((RandomTree) AbstractClassifier.forName(defaultClassifierString(),
//        options));
    classifier.setComputeImpurityDecreases(m_computeAttributeImportance);
    setDoNotCheckCapabilities(classifier.getDoNotCheckCapabilities());
    setSeed(classifier.getSeed());
    setDebug(classifier.getDebug());
    setNumDecimalPlaces(classifier.getNumDecimalPlaces());
    setBatchSize(classifier.getBatchSize());
    classifier.setDoNotCheckCapabilities(true);

    // Set base classifier and options
    setClassifier(classifier);

    Utils.checkForRemainingOptions(options);
//    super.setOptions(options);
  }

  public ExpandableRandomForest update(Instances data) throws Exception {
    ExpandableRandomForest newRandomForest = new ExpandableRandomForest();
    newRandomForest.setOptions(this.getOptions());
    newRandomForest.setNumIterations(numberOfNewTreesOnUpdate);
    newRandomForest.buildClassifier(data);
    addClassifiers(newRandomForest.m_Classifiers);
    return this;
  }

  public ExpandableRandomForest addClassifiers(Classifier[] newTrees) {
    if (maximumNumberOfTrees > 0) {
      int numberOfOldTreesToKeep = Math.min(maximumNumberOfTrees - newTrees.length, m_Classifiers.length);
      numberOfOldTreesToKeep = Math.min(m_Classifiers.length, numberOfOldTreesToKeep);
      m_Classifiers = Arrays.copyOfRange(m_Classifiers, m_Classifiers.length - numberOfOldTreesToKeep,
        m_Classifiers.length);
    }
    m_Classifiers = concatWithArrayCopy(m_Classifiers, newTrees);
    m_NumIterations = m_Classifiers.length;
    return this;
  }

  public int getNumberOfNewTreesOnUpdate() {
    return numberOfNewTreesOnUpdate;
  }

  public void setNumberOfNewTreesOnUpdate(int numberOfNewTreesOnUpdate) {
    this.numberOfNewTreesOnUpdate = numberOfNewTreesOnUpdate;
  }

  public int getMaximumNumberOfTrees() {
    return maximumNumberOfTrees;
  }

  public void setMaximumNumberOfTrees(int maximumNumberOfTrees) {
    this.maximumNumberOfTrees = maximumNumberOfTrees;
  }

  public Classifier[] getTrees() {
    return m_Classifiers;
  }

  private static <T> T[] concatWithArrayCopy(T[] array1, T[] array2) {
    T[] result = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ExpandableRandomForest{")
      .append("numberOfNewTreesOnUpdate=").append(numberOfNewTreesOnUpdate)
      .append(", maximumNumberOfTrees=").append(maximumNumberOfTrees)
      .append(")}\n");
    sb.append(super.toString());
    Classifier[] trees = getTrees();
    if (trees != null) {
      int shownTrees = 3;
      for (int i = 0; i < Math.min(trees.length, shownTrees - 1); i++) {
        sb.append("\nTree ").append(i).append(":\n");
        sb.append(trees[i].toString()).append("\n");
      }
      if (trees.length >= shownTrees) {
        sb.append("\n...");
        sb.append("\nTree ").append(trees.length - 1).append(":\n");
        sb.append(trees[trees.length - 1].toString()).append("\n");
      }
    }
    return sb.toString();
  }
}
