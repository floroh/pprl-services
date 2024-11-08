package de.unileipzig.dbs.pprl.core.common.frequencies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.unileipzig.dbs.pprl.core.common.exceptions.UnexpectedRuntimeConditionException;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ScaledWeightCalculator extends DefaultWeightCalculator {

  private AttributesFrequencyLookupProvider frequencyLookupProvider;

  private double lowerBound = 0.75;

  private double upperBound = 1.5;

  private boolean useFellegiSunter = false;

  private boolean normalizeScale = true;
  private boolean useNonlinearRescaling = false;

  private boolean useInverseDocumentFrequency = false;

  private boolean downWeightingOnly = false;

  private double relativeTop = 0.01;

  @JsonIgnore
  private AttributesFrequencyLookup frequencyLookup;

  @JsonIgnore
  private Map<String, Double> highestScale;

  @JsonIgnore
  private Map<String, Double> lowestScale;

  @JsonIgnore
  private Map<String, Double> referenceInverseDocumentFrequency;

  private static Logger logger = LogManager.getLogger(ScaledWeightCalculator.class);

  public ScaledWeightCalculator(
    AttributesFrequencyLookupProvider frequencyLookupProvider, Map<String, Double> defaultWeights) {
    super(defaultWeights);
    this.frequencyLookupProvider = frequencyLookupProvider;
  }

  public ScaledWeightCalculator(
    AttributesFrequencyLookup frequencyLookup, Map<String, Double> defaultWeights) {
    super(defaultWeights);
    this.frequencyLookup = frequencyLookup;
    initScaleBoundaries();
  }

  private ScaledWeightCalculator(Map<String, Double> defaultWeights) {
    super(defaultWeights);
  }

  private ScaledWeightCalculator() {
    super();
  }

  private void init() {
    if (frequencyLookup == null) {
      frequencyLookup = frequencyLookupProvider.provide();
    }
    initScaleBoundaries();
  }

  public void initScaleBoundaries() {
    logger.info("Initialising scale boundaries");
    lowestScale = new HashMap<>();
    highestScale = new HashMap<>();
    referenceInverseDocumentFrequency = new HashMap<>();
    for (String attribute : frequencyLookup.getAttributes()) {
      AttributeFrequencyLookup afl =
        frequencyLookup.getAttributeFrequencyLookup(attribute).get();
      double ref = 1.0;
      if (useInverseDocumentFrequency) {
        if (!afl.getFrequencies().isEmpty()) {
          logger.info("Using idf with relativeTop=" + relativeTop);
          Optional<Long> frequency = Optional.empty();
          if (relativeTop == 0) { // Median
            String referenceValue = afl.getMedianValue().get();
            frequency = afl.getFrequency(referenceValue);
            logger.info("Reference entry for scale normalization: " + referenceValue + ", " + frequency.get());
          } else if (relativeTop < 0) { // Lowest frequency in (absolute top filtered) AFL
            frequency = Optional.of(afl.getLowestFrequency());
            logger.info("Reference frequency for scale normalization (least frequent): " + frequency.get());
          } else if (relativeTop > 1.001) {
            String referenceValue = afl.getAbsoluteTopValue((int) Math.round(relativeTop)).get();
            frequency = afl.getFrequency(referenceValue);
            logger.info("Reference entry for scale normalization: " + referenceValue + ", " + frequency.get());
          } else {
            int pos = (int) (relativeTop * afl.getUniqueCount());
            String referenceValue = afl.getAttributesReverseSortedByFrequency().get(pos);
            frequency = afl.getFrequency(referenceValue);
            logger.info("Reference entry for scale normalization: " + referenceValue + ", " + frequency.get());
          }
          if (frequency.isPresent()) {
            ref = getInverseDocumentFrequency(frequency.get(), attribute);
          }
//          logger.info("Reference scale: " + ref);
        }
      }
      referenceInverseDocumentFrequency.put(attribute, ref);
      double low = getScale(afl.getHighestFrequency(), attribute);
//      double low = getScale(afl.getHighestFrequency(), afl.getTotalCount(), afl.getUniqueCount());
      double high = getScale(afl.getLowestFrequency(), attribute);
//      double high = getScale(afl.getLowestFrequency(), afl.getTotalCount(), afl.getUniqueCount());
      lowestScale.put(attribute, low);
      highestScale.put(attribute, high);
      logger.info("Scale range for " + attribute + ": " +
        "[low=" + low + ", ref=" + ref + ", high=" + high + "]");
    }

    System.out.println(this);
  }

  @Override
  public double getWeight(String attributeName, String attributeValue) {
    if (frequencyLookup == null) {
      init();
    }
    if (useFellegiSunter && frequencyLookup.getAttributes().contains(attributeName)) {
      return getFSScaledWeight(attributeName, attributeValue);
    }
    double weight = super.getWeight(attributeName, attributeValue);
    weight = weight * getScale(attributeName, attributeValue);
    return weight;
  }

  private double getFSScaledWeight(String attributeName, String attributeValue) {
    double probM = WeightUtils.getProbM(0.01);
    double probU = 0.01;
//    double weightU = -3;
    if (attributeName.equals(PersonalAttributeType.FIRSTNAME.asString())) {
      probM = 0.9300;
      probU = 0.0027;
//      weightU = -3.7925;
    } else if (attributeName.equals(PersonalAttributeType.LASTNAME.asString())) {
      probM = 0.7187;
      probU = 0.0010  ;
//      weightU = -1.8106;
    }
    Optional<Long> frequency = frequencyLookup.getFrequency(attributeName, attributeValue);
    if (frequency.isPresent()) {
      probU = frequency.get() / (double) frequencyLookup.getTotalCount(attributeName);
    }
    return WeightUtils.getWeightDiff(probM, probU);
  }

  @Override
  public double getScale(String attributeName, String attributeValue) {
    if (frequencyLookup == null) {
      init();
    }
    if (useFellegiSunter) {
      double attributeWeight = getWeight(attributeName, attributeValue);
      double defaultWeight = getDefaultWeights().get(attributeName);
      return attributeWeight / defaultWeight;
//      throw new RuntimeException("Fellegi-Sunter Weight calculation works via getWeight only, not via " +
//        "getScale");
    }
    double scale = 1.0;
    Optional<Long> frequency = frequencyLookup.getFrequency(attributeName, attributeValue);

    if (frequency.isPresent()) {
      scale = getScale(frequency.get(), attributeName);
      if (normalizeScale) {
        scale = adaptScale(scale, attributeName);
//      return clipScale(scale);
      }
    }
    if (downWeightingOnly && scale > 1.0) return 1.0;
    return scale;
  }

  private double adaptScale(double scale, String attributeName) {
    if (scale == 1) {
      return 1;
    }
    double normed = normalizeScale(scale, attributeName);
    if (useNonlinearRescaling) {
      double rescaled = nonlinearRescaling(normed);
      normed = rescaled;
    }

    checkIsInRange(normed, 0, 2);
    double resized = rescaleToBoundaries(normed);
    checkIsInRange(resized, lowerBound, upperBound);
    return resized;
  }

  private double normalizeScale(double scale, String attributeName) {
//    Double refScale = referenceScale.get(attributeName);
    Double refScale = 1.0001;
//    if (referenceScale.get(attributeName) != 1) {
//      refScale = referenceScale.get(attributeName);
//    }
    if (scale < refScale) {
      double lowBound = lowestScale.get(attributeName);
      return (scale - lowBound) / (refScale - lowBound);
    }
    if (scale > refScale) {
      double highBound = highestScale.get(attributeName);
      return 1 + (scale - refScale) / (highBound - refScale);
    }
    return 1.0;
  }

  private double rescaleToBoundaries(double x) {
    checkIsInRange(x, 0, 2);
    if (x < 1) {
      return lowerBound + x * (1.0 - lowerBound);
    }
    if (x > 1) {
      return 1.0 + (x - 1.0) * (upperBound - 1.0);
    }
    return 1.0;
  }

  /**
   * Rescale input value (between 0 and 2) to the same domain
   * but with non-edge values closer to 1
   */
  private double nonlinearRescaling(double x) {
    checkIsInRange(x, 0, 2);
    return 1 + Math.pow(x - 1, 3);
  }

  private static void checkIsInRange(double x, double lowerBound, double upperBound) {
    if (x < lowerBound || x > upperBound) {
      throw new UnexpectedRuntimeConditionException("Out of expected range " +
        "[" + lowerBound + ", " + upperBound + "]: " + x);
    }
  }

  private double getScale(Long valueFrequency, String attributeName) {
    if (useInverseDocumentFrequency) {
      return getScaleInverseDocumentFrequency(valueFrequency, attributeName);
    }
    return getScaleZhu(valueFrequency, attributeName);
  }

  private double getScaleZhu(Long valueFrequency, String attributeName) {
    Long total = frequencyLookup.getTotalCount(attributeName);
    Long unique = frequencyLookup.getUniqueCount(attributeName);
    return Math.sqrt((double) total / (valueFrequency * unique));
  }

  private double getScaleInverseDocumentFrequency(Long valueFrequency, String attributeName) {
    return 1 + getInverseDocumentFrequency(valueFrequency, attributeName) - referenceInverseDocumentFrequency.get(attributeName);
  }
  private double getInverseDocumentFrequency(Long valueFrequency, String attributeName) {
    Long total = frequencyLookup.getTotalCount(attributeName);
    double idf = Math.log(total / (double) valueFrequency) / Math.log(2);
//    return idf - referenceScale.get(attributeName);
    return idf;
  }

  private double clipScale(double scale) {
    if (scale < lowerBound) {
      return lowerBound;
    }
    if (scale > upperBound) {
      return upperBound;
    }
    return scale;
  }

  public AttributesFrequencyLookupProvider getFrequencyLookupProvider() {
    return frequencyLookupProvider;
  }

  public double getLowerBound() {
    return lowerBound;
  }

  public void setLowerBound(double lowerBound) {
    this.lowerBound = lowerBound;
  }

  public double getUpperBound() {
    return upperBound;
  }

  public void setUpperBound(double upperBound) {
    this.upperBound = upperBound;
  }

  public boolean isUseFellegiSunter() {
    return useFellegiSunter;
  }

  public void setUseFellegiSunter(boolean useFellegiSunter) {
    this.useFellegiSunter = useFellegiSunter;
  }

  public boolean isNormalizeScale() {
    return normalizeScale;
  }

  public void setNormalizeScale(boolean normalizeScale) {
    this.normalizeScale = normalizeScale;
  }

  public boolean isUseNonlinearRescaling() {
    return useNonlinearRescaling;
  }

  public void setUseNonlinearRescaling(boolean useNonlinearRescaling) {
    this.useNonlinearRescaling = useNonlinearRescaling;
  }

  public boolean isUseInverseDocumentFrequency() {
    return useInverseDocumentFrequency;
  }

  public void setUseInverseDocumentFrequency(boolean useInverseDocumentFrequency) {
    this.useInverseDocumentFrequency = useInverseDocumentFrequency;
  }

  public boolean isDownWeightingOnly() {
    return downWeightingOnly;
  }

  public void setDownWeightingOnly(boolean downWeightingOnly) {
    this.downWeightingOnly = downWeightingOnly;
  }

  public double getRelativeTop() {
    return relativeTop;
  }

  public void setRelativeTop(double relativeTop) {
    this.relativeTop = relativeTop;
//    init();
  }

  @Override
  public String toString() {
    return "ScaledWeightCalculator{" +
            "frequencyLookupProvider=" + frequencyLookupProvider +
            ", lowerBound=" + lowerBound +
            ", upperBound=" + upperBound +
            ", useFellegiSunter=" + useFellegiSunter +
            ", normalizeScale=" + normalizeScale +
            ", useNonlinearRescaling=" + useNonlinearRescaling +
            ", useInverseDocumentFrequency=" + useInverseDocumentFrequency +
            ", relativeTop=" + relativeTop +
            ", frequencyLookup=" + frequencyLookup +
            ", highestScale=" + highestScale +
            ", lowestScale=" + lowestScale +
            ", referenceInverseDocumentFrequency=" + referenceInverseDocumentFrequency +
            '}';
  }
}
