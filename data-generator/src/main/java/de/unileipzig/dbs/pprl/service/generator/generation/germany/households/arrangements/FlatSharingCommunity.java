package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person.CensusTuple;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.Constants;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.FlatMate;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlatSharingCommunity {

  private Random random;

  private Household household;

  private FlatMate[] flatmates;

  int flatSize;

  List<CensusTuple> potentialFlatMates;

  /**
   * Class attributes for values, that are extracted from the most upper tuple in the potentialFlatMates ArrayList
   * (because this ArrayList is sorted ascending by age, it will always be the youngest person in the ArrayList).
   */
  private CensusTuple referenceTuple;
  private String referenceFederalState;
  private String referenceGender;
  private int referenceAge;

  public FlatSharingCommunity(Household household, int maximumFlatSize, List<String> requestedPersonAttributes) {

    random = RandomSingleton.getRandom();

    this.household = household;

    flatmates = new FlatMate[maximumFlatSize];

    potentialFlatMates = new ArrayList<>();

    for (int i = 0; i < flatmates.length; i++) {

      flatmates[i] = new FlatMate(requestedPersonAttributes);

      flatmates[i].configure(household);

      if (flatmates[i].getHouseholdRole() != null) {
        flatmates[i].getHouseholdRole().setValue_String("FM" + i);
      }
    }
  }

  //<editor-fold desc="Standard get and set methods.">
  public FlatMate[] getFlatmates() {
    return flatmates;
  }

  public int getFlatSize() {
    return flatSize;
  }
  //</editor-fold>

  public void calculate_FederalState_Gender_Age_OfYoungestFlatMate(FlatMate flatMate) {
    flatMate.getCensusTuple().setFederalState(household.getFederalStateFromPool());
    flatMate.getCensusTuple().setGender(household.getGenderFromPool());
    flatMate.getCensusTuple().setAge(household.getAgeFromPool());
  }

  public void calculateFlatSize() {
    flatSize = 0;
    for (FlatMate f : flatmates) {
      if (f.activated()) {
        flatSize++;
      }
    }
  }

  /**
   * Calculates the age of flatmate based on the age of an other flat mate. The other flat mate is always the youngest
   * person from the microPool.
   *
   * @param referenceAge Age of the other flat mate.
   * @return Age of flat mate.
   */
  public int calculateAgeOfFlatMate(int referenceAge) {
    int ageOfFlatMate;
    do {
      /* Use Gaussian distribution with self defines standard deviation. Calculated age will "cluster" around
       * the referenceAge.
       */
      double calculatedAge = random.nextGaussian() * Constants.STANDARD_DEVIATION_FLATMATE + referenceAge;
      ageOfFlatMate = (int) Math.round(calculatedAge);

      /* Because always the youngest person from the microPool will be flatmates[0], return always a greater
       * value.
       */
      if (ageOfFlatMate < referenceAge) {
        ageOfFlatMate = referenceAge + (referenceAge - ageOfFlatMate);
      }
    } while (ageOfFlatMate > Constants.MAXIMUM_AGE || ageOfFlatMate < Constants.MINIMUM_AGE);
    return ageOfFlatMate;
  }

  public void setReferenceTuple() {
    if (!potentialFlatMates.isEmpty()) {
      if (potentialFlatMates.getFirst().getFrequency() > 0) {
        referenceTuple = potentialFlatMates.getFirst();
        referenceFederalState = referenceTuple.getFederalState();
        referenceGender = referenceTuple.getGender();
        referenceAge = referenceTuple.getAge();
      } else {
        System.out.println("Error @ Flat, tuple with frequency <= 0 in microPool.");
      }
    } else {
      System.out.println("Error @ Flat, microPool is empty.");
    }
  }

  public void generateFlat(int greatestNeedHouseholdSize) {

    /* Collection which contains all remained tuples from one federal state. */
    potentialFlatMates = household.getSelectedMicroPool();

    setReferenceTuple();

    for (int i = 0; i < flatmates.length; i++) {

      if (i < greatestNeedHouseholdSize) {

        flatmates[i].activate();

        if (i == 0) {
          /* Set the first flatmate, who should be always available in the pool. */
          flatmates[i].getCensusTuple().setFederalState(referenceFederalState);
          flatmates[i].getCensusTuple().setGender(referenceGender);
          flatmates[i].getCensusTuple().setAge(referenceAge);

          /* Subtract tuple directly form the micro pool. */
          household.getPool().subtractFromPool(potentialFlatMates, flatmates[i].getCensusTuple());

        } else {
          /* For other flat mates, try to calculate gender and age depending on flatmate[0]. */

          flatmates[i].getCensusTuple().setFederalState(referenceFederalState);

          /* Change or keep gender based on a given probability. */
          flatmates[i].getCensusTuple().setGender(referenceGender);
          if (random.nextInt(100) > Constants.PROBABILITY_FLATMATE_SAME_GENDER) {
            flatmates[i].flipGender();
          }

          flatmates[i].getCensusTuple().setAge(calculateAgeOfFlatMate(referenceAge));

          /* Check if the calculated person/tuple exists. */
          if (!household.getPool().availableInPool(potentialFlatMates, flatmates[i].getCensusTuple())) {

            /* Person/tuple does not exist, change gender and try again. */
            flatmates[i].flipGender();

            if (!household.getPool().availableInPool(potentialFlatMates, flatmates[i].getCensusTuple())) {

              /* Person still doesn't exist, take the next youngest person out of the microPool. */
              setReferenceTuple();

              flatmates[i].getCensusTuple().setFederalState(referenceFederalState);
              flatmates[i].getCensusTuple().setGender(referenceGender);
              flatmates[i].getCensusTuple().setAge(referenceAge);
            }
          }

          /* Subtract person/tuple from the microPool. */
          household.getPool().subtractFromPool(potentialFlatMates, flatmates[i].getCensusTuple());
        }
      } else {
        flatmates[i].deactivate();
      }
    }
  }
}