package de.unileipzig.dbs.pprl.core.matcher.classification.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Basic data structure for classifier creation
 * Defines the names of the (numeric) attributes,
 * the name and values of the class attribute
 * and the training method
 */
public class ClassifierConfig implements Serializable {
  private List<String> attributeNames;
  private String classAttributeName;
  private List<String> classAttributeValues;
  private ClassifierMethod classifierMethod;

  private ClassBalancerMethod classBalancerMethod;

  private InstanceWeightMethod instanceWeightMethod;

  private String classifierOptions = "";

  private double certaintyThreshold;

  private String trainingDataOutputDirectory;

  public ClassifierConfig(List<String> attributeNames,
    String classAttributeName,
    List<String> classAttributeValues,
    ClassifierMethod classifierMethod, ClassBalancerMethod classBalancerMethod,
    InstanceWeightMethod instanceWeightMethod, String classifierOptions,
    double certaintyThreshold) {
    this.attributeNames = attributeNames;
    this.classAttributeName = classAttributeName;
    this.classAttributeValues = classAttributeValues;
    this.classifierMethod = classifierMethod;
    this.classBalancerMethod = classBalancerMethod;
    this.instanceWeightMethod = instanceWeightMethod;
    this.classifierOptions = classifierOptions;
    this.certaintyThreshold = certaintyThreshold;
  }

  private ClassifierConfig() {
  }

  public static ClassifierConfig createBinaryClassifierConfig(List<String> attributeNames,
    ClassifierMethod classifierMethod) {
    return new ClassifierConfig(
      attributeNames, "match",
      Arrays.asList(InstanceBinary.FALSE, InstanceBinary.TRUE),
      classifierMethod,
      ClassBalancerMethod.DOWNSAMPLING, InstanceWeightMethod.NONE, "", 0.6
    );
  }

  @JsonIgnore
  public String getConfigString() {
    StringBuilder sb = new StringBuilder();
    sb.append("_").append(getClassifierMethod().toString());
    sb.append("_").append(getClassBalancerMethod().toString());
    sb.append("_").append(getClassifierOptions());
    return sb.toString();
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public void setAttributeNames(List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public String getClassAttributeName() {
    return classAttributeName;
  }

  public List<String> getClassAttributeValues() {
    return classAttributeValues;
  }

  public ClassifierMethod getClassifierMethod() {
    return classifierMethod;
  }

  public ClassBalancerMethod getClassBalancerMethod() {
    return classBalancerMethod == null ? ClassBalancerMethod.NONE : classBalancerMethod;
  }

  public InstanceWeightMethod getInstanceWeightMethod() {
    return instanceWeightMethod == null ? InstanceWeightMethod.NONE : instanceWeightMethod;
  }

  public String getClassifierOptions() {
    return classifierOptions;
  }

  public void setClassifierOptions(String classifierOptions) {
    this.classifierOptions = classifierOptions;
  }

  public double getCertaintyThreshold() {
    return certaintyThreshold;
  }

  public String getTrainingDataOutputDirectory() {
    return trainingDataOutputDirectory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ClassifierConfig that = (ClassifierConfig) o;

    if (!attributeNames.equals(that.attributeNames)) {
      return false;
    }
    if (!classAttributeName.equals(that.classAttributeName)) {
      return false;
    }
    if (!classAttributeValues.equals(that.classAttributeValues)) {
      return false;
    }
    if (certaintyThreshold != that.certaintyThreshold) {
      return false;
    }
    if (classBalancerMethod != that.classBalancerMethod) {
      return false;
    }
    if (instanceWeightMethod != that.instanceWeightMethod) {
      return false;
    }
    if (!Objects.equals(trainingDataOutputDirectory, that.trainingDataOutputDirectory)) {
      return false;
    }
    return classifierMethod == that.classifierMethod;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = attributeNames.hashCode();
    result = 31 * result + classAttributeName.hashCode();
    result = 31 * result + classAttributeValues.hashCode();
    result = 31 * result + classifierMethod.hashCode();
    result = 31 * result + classBalancerMethod.hashCode();
    result = 31 * result + instanceWeightMethod.hashCode();
    result = 31 * result + trainingDataOutputDirectory.hashCode();
    temp = Double.doubleToLongBits(certaintyThreshold);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "ClassifierConfig{" +
      "attributeNames=" + attributeNames +
      ", classAttributeName='" + classAttributeName + '\'' +
      ", classAttributeValues=" + classAttributeValues +
      ", classifierMethod=" + classifierMethod +
      ", classBalancerMethod=" + classBalancerMethod +
      ", instanceWeightMethod=" + instanceWeightMethod +
      ", outputPath=" + trainingDataOutputDirectory +
      ", certaintyThreshold=" + certaintyThreshold +
      '}';
  }
}
