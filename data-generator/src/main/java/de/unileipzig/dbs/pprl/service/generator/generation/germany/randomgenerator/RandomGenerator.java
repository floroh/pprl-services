package de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator;

import lombok.Getter;

@Getter
public class RandomGenerator {

  //<editor-fold desc="Standard get and set methods.">
  private ProbabilityDistribution probabilityDistribution;
  private AliasMethod aliasMethod;


  public RandomGenerator(ProbabilityDistribution probabilityDistribution) {
    this.probabilityDistribution = probabilityDistribution;
    this.aliasMethod = new AliasMethod(probabilityDistribution.getProbabilities());
  }

  //</editor-fold>

  public String next_String() {
    return probabilityDistribution.getEventSpace_String().get(aliasMethod.next());
  }

  public int next_int() {
    return probabilityDistribution.getEventSpace_int()[aliasMethod.next()];
  }

  public int[] next_intArray() {
    return probabilityDistribution.getEventSpace_intArray()[aliasMethod.next()];
  }

  /**
   * Used to change the event Space of a RandomGenerator. An event is removed from the possible events,
   * the respective probability is removed and a new probability distribution and AliasMethod is set.
   */
  public void update(String event) {

    int index = probabilityDistribution.getEventSpace_String().indexOf(event);

    probabilityDistribution.getEventSpace_String().remove(index);

    probabilityDistribution.getProbabilities().remove(index);

    ProbabilityDistribution updated =
            ProbabilityDistribution.fromDoubleCounts(
                    probabilityDistribution.getEventSpace_String(), // old event removed
                    probabilityDistribution.getProbabilities());    // old probability removed

    this.probabilityDistribution = updated;
    this.aliasMethod = new AliasMethod(updated.getProbabilities());
  }
}