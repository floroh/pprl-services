package de.unileipzig.dbs.pprl.service.generator.generation.germany.households;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * This class estimates, how many households of what size need to be generated.
 */
public class HouseholdCounter {

  private final RandomGenerator householdSizeGenerator;

  private final StringBuilder sb;

  private int generatedRecordsSUM = 0;
  private int generatedHouseholdsSUM = 0;

  private int generatedRecordsInPhaseX = 0;
  private int generatedHouseholdsInPhaseX = 0;

  //<editor-fold desc="Class attributes for phase 2.">
  private int phase2_plannedHouseholds = 0;
  private int phase2_timesPartner1DidNotExistsInPool = 0;
  private int phase2_failedAttemptsToFindPartner2 = 0;
  //</editor-fold>

  private final HashMap<Integer, Integer> generatedSizes;
  private final HashMap<Integer, Integer> plannedSizes;

  public HouseholdCounter(int requestedNumberOfRecords) {

    sb = new StringBuilder();

    householdSizeGenerator = RandomGeneratorBuilder.buildRandomGenerator(this, null);

    generatedSizes = new HashMap<>();
    plannedSizes = new HashMap<>();

    /* Do random experiments with the random generator. Estimate how many households of each size should
     * be generated.
     */
    int size;
    for (int i = requestedNumberOfRecords; i > 0; ) {
      size = householdSizeGenerator.next_int();
      if (size <= i) {
        /* If key does not exists, put 1 as value, otherwise sum 1 to the value linked to key. */
        plannedSizes.merge(size, 1, Integer::sum);
        i -= size;
      } else {
        while (i > 0) {
          plannedSizes.merge(1, 1, Integer::sum);
          i--;
        }
      }
    }
  }

  //<editor-fold desc="Standard get and set methods.">
  public int getGeneratedRecordsInPhaseX() {
    return generatedRecordsInPhaseX;
  }

  public void setGeneratedRecordsInPhaseX(int generatedRecordsInPhaseX) {
    this.generatedRecordsInPhaseX = generatedRecordsInPhaseX;
  }

  public int getGeneratedHouseholdsInPhaseX() {
    return generatedHouseholdsInPhaseX;
  }

  public void setGeneratedHouseholdsInPhaseX(int generatedHouseholdsInPhaseX) {
    this.generatedHouseholdsInPhaseX = generatedHouseholdsInPhaseX;
  }

  public int getPhase2_plannedHouseholds() {
    return phase2_plannedHouseholds;
  }

  public int getPhase2_timesPartner1DidNotExistsInPool() {
    return phase2_timesPartner1DidNotExistsInPool;
  }

  public int getPhase2_failedAttemptsToFindPartner2() {
    return phase2_failedAttemptsToFindPartner2;
  }

  public int getGeneratedRecordsSUM() {
    return generatedRecordsSUM;
  }

  public int getGeneratedHouseholdsSUM() {
    return generatedHouseholdsSUM;
  }

  public void setPhase2_plannedHouseholds(int phase2_plannedHouseholds) {
    this.phase2_plannedHouseholds = phase2_plannedHouseholds;
  }

  public HashMap<Integer, Integer> getPlannedSizes() {
    return plannedSizes;
  }
  //</editor-fold>

  //<editor-fold desc="Mathods for phase 2.">
  public void timesPartner1DidNotExistsInPool(int i) {
    phase2_timesPartner1DidNotExistsInPool += i;
  }

  public void increaseFailedAttemptsToFindPartner2() {
    phase2_failedAttemptsToFindPartner2++;
  }
  //</editor-fold>

  public String printOutInitialisationInfo() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Integer, Integer> entry : plannedSizes.entrySet()) {
      if (entry.getKey() == 5) {
        sb.append(String.format("Planned households of size >=%s: %s%n", entry.getKey(), entry.getValue()));
      } else {
        sb.append(String.format("Planned households of size   %s: %s%n", entry.getKey(), entry.getValue()));
      }
    }
    return sb.toString();
  }

  /**
   * Counts, when ever a household of a specific size was generated. At the same time subtracts from the amount of
   * planned households for that sizes.
   *
   * @param size Size of the household, that has to be count.
   */
  public void generatedHouseholdOfSize(int size) {
    generatedSizes.merge(size, 1, Integer::sum);

    /* In case households of size greater 5 are generated (during phase 1), count them to the households of size
     *  >= 5
     */
    if (plannedSizes.containsKey(size)) {
      plannedSizes.merge(size, -1, Integer::sum);
    } else {
      plannedSizes.merge(5, -1, Integer::sum);
    }
  }

  public void countRecord() {
    generatedRecordsInPhaseX++;
    generatedRecordsSUM++;
  }

  public void countHousehold() {
    generatedHouseholdsInPhaseX++;
    generatedHouseholdsSUM++;
  }

  @Override
  public String toString() {
    /* Clear the StringBuilder first. */
    sb.setLength(0);
    sb.append("Household Counter:" + "\n");
    for (Map.Entry<Integer, Integer> entry : generatedSizes.entrySet()) {
      sb.append(String.format("Generated households of size %s: %s \n", entry.getKey(), entry.getValue()));
    }
    sb.append("Sum of all generated records since start: ").append(generatedRecordsSUM).append("\n");
    sb.append("Sum of all generated households since start: ").append(generatedHouseholdsSUM);
    return sb.toString();
  }

  public void calculateStillNeededSizes() {
    /* Remove all key-value pairs with negative values (all household sizes, that where already generated enough). */
    plannedSizes.entrySet().removeIf(entry -> entry.getValue() <= 0);
  }
}