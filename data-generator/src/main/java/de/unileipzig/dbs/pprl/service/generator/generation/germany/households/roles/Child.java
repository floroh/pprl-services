package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.Constants;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;

import java.util.List;
import java.util.Random;

public class Child extends FamilyMember {

  //<editor-fold desc="Class attributes for calculating results randomly">
  Random random = RandomSingleton.getRandom();
  RandomGenerator childSpacingGenerator_2ndChild;
  RandomGenerator childSpacingGenerator_3rdChild;
  RandomGenerator childSpacingGenerator_4thChild;


  int probabilityForMaleNewBorn;

  //</editor-fold>

  /* This array contains all siblings of the household, including itself.
   * Inside this array, siblings are ordered by age, youngest at index 0, oldest with the biggest index.
   */
  Child[] allSiblings;

  /* The child in the allSiblings array, which is the next younger one (child with next lower index). */
  Child nextYoungerSibling;

  public Child(List<String> requestedPersonAttributes) {

    super(requestedPersonAttributes);

    probabilityForMaleNewBorn = Constants.PROBABILITY_MALE_NEW_BORN;

    childSpacingGenerator_2ndChild = RandomGeneratorBuilder.buildRandomGenerator(this, "Child2");
    childSpacingGenerator_3rdChild = RandomGeneratorBuilder.buildRandomGenerator(this, "Child3");
    childSpacingGenerator_4thChild = RandomGeneratorBuilder.buildRandomGenerator(this, "Child4");
  }

  public void setAllSiblings(Child[] siblings) {
    this.allSiblings = siblings;
  }

  public void setNextYoungerSibling(Child child) {
    this.nextYoungerSibling = child;
  }

  public void setGenderRandomly() {
    if (random.nextInt(100) < probabilityForMaleNewBorn) {
      this.getCensusTuple().setGender("M");
    } else {
      this.getCensusTuple().setGender("F");
    }
  }

  /**
   *
   * @param numberOfChildren     Defines, how many children to consider for this household.
   * @param indexInChildrenArray Defines the order of all siblings with respect to age. Youngest child has the index 0,
   *                             oldest child has the biggest index.
   */
  public void calculateAgeDependentOnNextYoungerChild(int numberOfChildren, int indexInChildrenArray) {

    /* Youngest child has the highest RANK (last born), oldest child the lowest rank (first born).*/
    int rankOfThisChild = numberOfChildren - indexInChildrenArray;

    int ageDifference;
    if (rankOfThisChild == 1) {
      /* For the first born child, the age DIFFERENCE to the second born child is calculated etc. */
      ageDifference = childSpacingGenerator_2ndChild.next_int();
    } else if (rankOfThisChild == 2) {
      ageDifference = childSpacingGenerator_3rdChild.next_int();
    } else if (rankOfThisChild == 3) {
      ageDifference = childSpacingGenerator_4thChild.next_int();
    } else {
      ageDifference = childSpacingGenerator_4thChild.next_int();
    }

    /* Add up the age difference with the age of the nextYoungerSibling to get the age of this child. */
    this.getCensusTuple().setAge(nextYoungerSibling.getCensusTuple().getAge() + ageDifference);
  }
}