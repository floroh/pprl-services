package de.unileipzig.dbs.pprl.core.matcher.classification.weka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpandableRandomForestTest {

  private List<List<Double>> matchFeatures;
  private List<List<Double>> nonMatchFeatures;
  private Instances instances;

  @BeforeEach
  void setUp() {
    ArrayList<Attribute> attributes = new ArrayList<>();
    attributes.add(new Attribute("f0"));
    attributes.add(new Attribute("f1"));
    attributes.add(new Attribute("f2"));
    attributes.add(new Attribute("target", List.of("match", "non-match")));

    instances = new Instances("Dataset", attributes, 100);
    instances.setClassIndex(attributes.size() - 1);

    matchFeatures = new ArrayList<>();
    matchFeatures.add(List.of(1.0, 1.0, 1.0));
    matchFeatures.add(List.of(1.0, 1.0, 0.9));
    matchFeatures.add(List.of(1.0, 1.0, 0.8));
    for (List<Double> feature : matchFeatures) {
      Instance inst = buildInstance(feature, "match");
      instances.add(inst);
    }
    nonMatchFeatures = new ArrayList<>();
    nonMatchFeatures.add(List.of(0.9, 0.7, 1.0));
    nonMatchFeatures.add(List.of(1.0, 0.7, 0.6));
    nonMatchFeatures.add(List.of(0.8, 1.0, 0.2));
    for (List<Double> feature : nonMatchFeatures) {
      Instance inst = buildInstance(feature, "non-match");
      instances.add(inst);
    }
  }

  private Instance buildInstance(List<Double> values, String label) {
    Instance inst = new DenseInstance(values.size() + 1);
    for (int i = 0; i < values.size(); i++) {
      inst.setValue(i, values.get(i));
    }
    inst.setDataset(instances);
    inst.setValue(values.size(), label);
    return inst;
  }

  @Test
  void build() throws Exception {
    ExpandableRandomForest erf = new ExpandableRandomForest();
    erf.setOptions(Utils.splitOptions("-I 10 -attribute-importance"));
    erf.buildClassifier(instances);
    assertEquals(10, erf.getTrees().length);
//    System.out.println(erf.toString());
  }

  @Test
  void expand() throws Exception {
    ExpandableRandomForest erf = new ExpandableRandomForest();
    erf.setOptions(Utils.splitOptions("-I 10 -J 2 -N 13 -attribute-importance"));

    erf.buildClassifier(instances);
    assertEquals(10, erf.getTrees().length);
//    System.out.println(erf.toString());

    erf.buildClassifier(instances);
    assertEquals(12, erf.getTrees().length);
//    System.out.println(erf.toString());

    erf.buildClassifier(instances);
    assertEquals(13, erf.getTrees().length);
//    System.out.println(erf.toString());

    erf.buildClassifier(instances);
    assertEquals(13, erf.getTrees().length);
  }
}