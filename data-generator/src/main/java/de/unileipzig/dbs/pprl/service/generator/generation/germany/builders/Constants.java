package de.unileipzig.dbs.pprl.service.generator.generation.germany.builders;

public class Constants {

  public static int MINIMUM_AGE = 0;
  public static int MAXIMUM_AGE = 110;

  public static int MINIMUM_AGE_FOR_MARRIAGE = 18;

  public static int MINIMUM_AGE_FOR_ADOPTION_1 = 25;

  public static int MINIMUM_AGE_FOR_ADOPTION_2 = 21;

  public static int MINIMUM_AGE_FOR_BIRTH = 15;
  public static int MAXIMUM_AGE_FOR_BIRTH = 50;

  public static int MINIMUM_AGE_TO_MOVE_OUT = 14;
  public static int MAXIMUM_AGE_FOR_BEING_CHILD = 45;


  public static int MAXIMUM_FLAT_SHARING_SIZE = 5;
  public static int MAXIMUM_LIVING_ARRANGEMENT_SIZE = 7;
  public static int MAXIMUM_NUMBER_OF_CHILDREN = 5;
  public static double RATIO_PHASE2_TO_PHASE1 = 1.03269;


  public static int PROBABILITY_FLATMATE_SAME_GENDER = 75; // 75,0%
  public static int STANDARD_DEVIATION_FLATMATE = 4;


  public static int PROBABILITY_MALE_NEW_BORN = 55;


  public static int getMaximumFlatSharingSize() {
    return MAXIMUM_FLAT_SHARING_SIZE;
  }

  public static int getMaximumLivingArrangementSize() {
    return MAXIMUM_LIVING_ARRANGEMENT_SIZE;
  }

  public static int getMaximumNumberOfChildren() {
    return MAXIMUM_NUMBER_OF_CHILDREN;
  }

  public static double getRatioPhase2ToPhase1() {
    return RATIO_PHASE2_TO_PHASE1;
  }

  public static int getProbabilityFlatmateSameGender() {
    return PROBABILITY_FLATMATE_SAME_GENDER;
  }
}