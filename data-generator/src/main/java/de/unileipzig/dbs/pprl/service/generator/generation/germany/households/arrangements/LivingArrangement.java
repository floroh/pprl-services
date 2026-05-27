package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.Constants;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.Child;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.HouseholdMember;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class LivingArrangement {

  @Getter
  SurnameConfigurator surnameConfigurator;

  @Getter
  private final int minimumAgeForMarriage = Constants.MINIMUM_AGE_FOR_MARRIAGE;
  @Getter
  private final int minimumAgeForAdoption1 = Constants.MINIMUM_AGE_FOR_ADOPTION_1;
  @Getter
  private final int minimumAgeForAdoption2 = Constants.MINIMUM_AGE_FOR_ADOPTION_2;
  private final int minimumAgeForBirth = Constants.MINIMUM_AGE_FOR_BIRTH;
  private final int maximumAgeForBirth = Constants.MAXIMUM_AGE_FOR_BIRTH;

  @Getter
  Random random = RandomSingleton.getRandom();

  int numberOfChildren;
  @Getter
  int indexOfOldestChild;

  private final RandomGenerator ageDuringFirstBirthGenerator;

  private final RandomGenerator ageDifferenceBetweenSpousesGenerator;

  @Setter
  Household household;

  @Setter
  @Getter
  private HouseholdMember partner1;
  @Setter
  @Getter
  private HouseholdMember partner2;

  @Setter
  @Getter
  private Child[] children;

  @Getter
  private final List<HouseholdMember> allPersons;

  @Setter
  @Getter
  private String livingArrangementStructure;
  @Setter
  @Getter
  private int livingArrangementSize;

  public LivingArrangement(Household household, int maximumNumberOfChildren, List<String> requestedAttributes) {

    surnameConfigurator = new SurnameConfigurator(this);

    ageDuringFirstBirthGenerator =
            RandomGeneratorBuilder.buildRandomGenerator(this, "AgeOfMotherDuringFirstBirth");

    ageDifferenceBetweenSpousesGenerator =
            RandomGeneratorBuilder.buildRandomGenerator(this, "AgeDifferencesBetweenSpouses");

    this.household = household;

    allPersons = new ArrayList<>();

    children = new Child[maximumNumberOfChildren];

    for (int i = 0; i < children.length; i++) {

      children[i] = new Child(requestedAttributes);

      children[i].configure(household);

      if (children[i].getHouseholdRole() != null) {
        children[i].getHouseholdRole().setValue_String("C" + i);
      }

      /* Siblings "know" each other. */
      children[i].setAllSiblings(children);

      /* Youngest child, index 0, has no next younger sibling. */
      if (i > 0) {
        children[i].setNextYoungerSibling(children[i - 1]);
      }

      allPersons.add(children[i]);
    }
  }


  public void activateBothPartners() {
    partner1.activate();
    partner2.activate();
  }

  public void singleMale() {
  }

  public void singleFemale() {
  }

  public void determineNumberOfChildren() {
    /* numberOfChildren always > 1 */
    numberOfChildren = household.getNumberOfChildren();
    indexOfOldestChild = numberOfChildren - 1;
  }

  public void activateChildren() {
    for (int i = 0; i < children.length; i++) {
      if (i < numberOfChildren) {
        children[i].activate();
      } else {
        children[i].deactivate();
      }
    }
  }

  public void calculate_FederalState_Gender_Age_OfAllActivatedChildren() {
    /* Start with the youngest child (children[0]), it gets the federal state, gender and age from the
     * census tuple drawn from pool.
     */
    children[0].getCensusTuple().setFederalState(household.getFederalStateFromPool());
    children[0].getCensusTuple().setGender(household.getGenderFromPool());
    children[0].getCensusTuple().setAge(household.getAgeFromPool());
    /* Calculate for all other children. */
    for (int i = 1; i < numberOfChildren; i++) {

      children[i].getCensusTuple().setFederalState(household.getFederalStateFromPool());

      /* Setting the gender in it's CensusTuple object randomly. */
      children[i].setGenderRandomly();

      /* Setting the age in it's CensusTuple object. */
      children[i].calculateAgeDependentOnNextYoungerChild(numberOfChildren, i);
    }
  }

  public void calculate_FederalState_Age_OfPartner2() {

    partner2.getCensusTuple().setFederalState(household.getFederalStateFromPool());

    int ageDuringFirstBirth = ageDuringFirstBirthGenerator.next_int();
    int ageOfTheOldestChild = children[indexOfOldestChild].getCensusTuple().getAge();
    partner2.getCensusTuple().setAge(ageDuringFirstBirth + ageOfTheOldestChild);
  }

  /**
   * Because the age difference between the siblings can be big (14 years) and the mother can be 50 years during
   * her first (oldest) child's birth, there can be an unrealistic age difference between the youngest child and
   * the mother. In this case, this method will correct the age of the mother and all her children by "compressing"
   * all the age differences between the siblings and between the mother, so that there is an
   * age difference of 50 between the youngest child and the mother.
   */
  public void correctAgeOfPartner2AndAllChildren() {
    /* Now the age difference between mother and youngest child must be checked. */
    int oldAgeOfPartner2 = partner2.getCensusTuple().getAge();
    int ageOfYoungestChild = children[0].getCensusTuple().getAge();
    int overflow = oldAgeOfPartner2 - ageOfYoungestChild;
    if (overflow > maximumAgeForBirth) {
      /* Age difference between partner2 and youngest child is too big. Partner2 mustn't be 50 years older then
       * the youngest child.
       */
      int newAgeOfPartner2 = ageOfYoungestChild + maximumAgeForBirth;
      partner2.getCensusTuple().setAge(newAgeOfPartner2);

      /* Calculate factor the age difference between youngest child and partner2 was decreased.
       * The new age difference is now 50 years.
       */
      double factor = (double) maximumAgeForBirth / (double) overflow;

      /* This factor is now used to decrease the age difference of all children to the youngest child. */
      for (int i = 1; i < numberOfChildren; i++) {

        int oldAgeDifferenceToYoungestChild =
                children[i].getCensusTuple().getAge() - ageOfYoungestChild;

        int newAgeDifferenceToYoungestChild = (int) Math.round(oldAgeDifferenceToYoungestChild * factor);

        if (newAgeDifferenceToYoungestChild == 0) {
          /* The min. age difference between all siblings is defined with 1, so +i. */
          children[i].getCensusTuple().setAge(ageOfYoungestChild + i);
        } else {
          children[i].getCensusTuple().setAge(ageOfYoungestChild + newAgeDifferenceToYoungestChild);
        }
      }
    }
  }

  public void calculate_FederalState_Age_OfPartner1() {
    /* Partner1 possibly deactivated in single parent household. */
    if (partner1.activated()) {
      partner1.getCensusTuple().setFederalState(household.getFederalStateFromPool());

      int ageOfPartner1 = partner2.getCensusTuple().getAge() + ageDifferenceBetweenSpousesGenerator.next_int();
      int ageOfOldestChild = children[indexOfOldestChild].getCensusTuple().getAge();
      /* Partner1 can be 20 years younger than partner2, which can lead to an unrealistic age differences with the
       * children. So some adjustments are made:
       * Youngest age to become a parent is defined with 15 years ( = age difference to oldest child).
       */
      if (ageOfPartner1 - ageOfOldestChild < minimumAgeForBirth) {
        partner1.getCensusTuple().setAge(ageOfOldestChild + minimumAgeForBirth);
      } else {
        partner1.getCensusTuple().setAge(ageOfPartner1);
      }
    }
  }

  public void fixAgeOfPartners() {
  }

  public void fixAgeOfPartnersForMarriage() {
    if (getPartner1().getCensusTuple().getAge() < getMinimumAgeForMarriage()) {
      getPartner1().getCensusTuple().setAge(getMinimumAgeForMarriage());
    }
    if (getPartner2().getCensusTuple().getAge() < getMinimumAgeForMarriage()) {
      getPartner2().getCensusTuple().setAge(getMinimumAgeForMarriage());
    }
  }

  public void fixAgeOfPartnersForAdoption() {
    int ageOfOldestChild = getChildren()[getIndexOfOldestChild()].getCensusTuple().getAge();
    int ageOfPartner1 = getPartner1().getCensusTuple().getAge();
    int ageOfPartner2 = getPartner2().getCensusTuple().getAge();
    if (ageOfPartner1 - ageOfOldestChild < getMinimumAgeForAdoption1()) {
      getPartner1().getCensusTuple().setAge(ageOfOldestChild + getMinimumAgeForAdoption1());
    }
    if (ageOfPartner2 - ageOfOldestChild < getMinimumAgeForAdoption2()) {
      getPartner2().getCensusTuple().setAge(ageOfOldestChild + getMinimumAgeForAdoption2());
    }
  }

  public int determineWhichAndHowManyChildrenLiveWithParents() {
    int howManyChildrenLiveWithParents = 0;
    for (Child child : children) {
      if (child.activated()) {
        if (household.getPool().availableInPool(
                household.getPool().getPool_1_children(), child.getCensusTuple())) {
          /* Child stays activated. */
          howManyChildrenLiveWithParents++;
        } else {
          child.deactivate();
        }
      }
    }
    return howManyChildrenLiveWithParents;
  }

  /**
   * Method checks how many of those activated parents are available in the pool and deactivates those who are
   * not available.
   *
   * @return Number of available parents.
   */
  public int partnersAvailable() {
    int count = 0;
    if (partner1.activated()) {
      if (household.getPool().availableInPool(
              household.getPool().getPool_1_adults(), partner1.getCensusTuple())) {
        count++;
      } else {
        partner1.deactivate();
      }
    }
    if (partner2.activated()) {
      if (household.getPool().availableInPool(
              household.getPool().getPool_1_adults(), partner2.getCensusTuple())) {
        count++;
      } else {
        partner2.deactivate();
      }
    }
    return count;
  }

  public void processChildrenWithoutParents() {
    for (Child child : children) {
      if (child.activated()) {
        if (household.getPool().subtractFromPool(
                household.getPool().getPool_1_children(), child.getCensusTuple())) {
          household.getPool().addToPool(
                  household.getPool().getPool_2(), child.getCensusTuple());
        }
      }
    }
  }

  public void calculateStructureAndSize() {
  }

  public int getNumberOfActiveChildren() {
    int i = 0;
    for (Child c : children) {
      if (c.activated()) {
        i++;
      }
    }
    return i;
  }

  /**
   * This method calculates and sets the age of partner2 depending on the age of partner1. It is assumed that
   * partner2 is at least 18 years old. Minimum age of partner1 is also 18 years, given by
   * the HouseholdStatistics.xml file.
   */
  public void setAgeOfPartner2_DependingOnAgeOfPartner1() {
    int ageOfPartner1 = partner1.getCensusTuple().getAge();
    int ageDifference = ageDifferenceBetweenSpousesGenerator.next_int();
    int ageOfPartner2 = ageOfPartner1 - ageDifference;

    /* Assuming partner2 is at least 18 years old. */
    ageOfPartner2 = Math.max(minimumAgeForMarriage, ageOfPartner2);
    partner2.getCensusTuple().setAge(ageOfPartner2);
  }
}