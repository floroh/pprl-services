package de.unileipzig.dbs.pprl.service.generator.generation.germany.builders;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.Attribute;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person.*;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.Child;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.HouseholdCounter;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.LivingArrangement;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.RelationshipTuplePool;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.SurnameConfigurator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Pool;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class RandomGeneratorBuilder {

  private static RandomGeneratorBuilder INSTANCE;

  private static final ProbabilityDistributionBuilder probabilityDistributionBuilder
          = ProbabilityDistributionBuilder.getInstance();

  /* RandomGenerators that are stores inside this class: */
  private static RandomGenerator censusTupleGenerator;
  private static RandomGenerator surnameGenerator;
  private static Map<Object, RandomGenerator> zipCodeGenerators;
  private static Map<Object, RandomGenerator> streetGenerators;

  private static Map<Object, RandomGenerator> locationGenerators;

  private static Map<Object, RandomGenerator> placeOfBirth_federalStatesGenerator;
  private static Map<Object, RandomGenerator> placeOfBirthGenerators;
  private static Map<Object, RandomGenerator> femaleNameGenerators;
  private static Map<Object, RandomGenerator> maleNameGenerators;

  private static RandomGenerator childSpacingGenerator_2ndChild;
  private static RandomGenerator childSpacingGenerator_3rdChild;
  private static RandomGenerator childSpacingGenerator_4thChild;

  private static RandomGenerator ageDuringFirstBirthGenerator;
  private static RandomGenerator ageDifferenceBetweenSpousesGenerator;
  private static RandomGenerator typeOfLivingArrangement_and_numberOfChildrenGenerator;
  private static RandomGenerator livingArrangementsWithSingleFatherAndChildren;
  private static RandomGenerator livingArrangementsWithSingleMotherAndChildren;

  private static RandomGenerator variationForMarriageGenerator;
  private static RandomGenerator variationForCohabitationGenerator;

  //private static RandomGenerator childlessPartner1Generator;
  private static RandomGenerator childlessPartner1Generator_updated;

  private static RandomGenerator householdSizeGenerator;

  private static RandomGenerator ageGroupsOfOnePersonHouseholdsGenerator;


  private RandomGeneratorBuilder() {
  }

  public RandomGeneratorBuilder getINSTANCE() {
    if (INSTANCE == null) {
      INSTANCE = new RandomGeneratorBuilder();
    }
    return INSTANCE;
  }

  public static void reset() {
    INSTANCE = null;

    censusTupleGenerator = null;
    surnameGenerator = null;
    zipCodeGenerators = null;
    streetGenerators = null;
    locationGenerators = null;
    placeOfBirth_federalStatesGenerator = null;
    placeOfBirthGenerators = null;
    femaleNameGenerators = null;
    maleNameGenerators = null;
    childSpacingGenerator_2ndChild = null;
    childSpacingGenerator_3rdChild = null;
    childSpacingGenerator_4thChild = null;
    ageDuringFirstBirthGenerator = null;
    ageDifferenceBetweenSpousesGenerator = null;
    typeOfLivingArrangement_and_numberOfChildrenGenerator = null;
    livingArrangementsWithSingleFatherAndChildren = null;
    livingArrangementsWithSingleMotherAndChildren = null;
    variationForMarriageGenerator = null;
    variationForCohabitationGenerator = null;
    childlessPartner1Generator_updated = null;
    householdSizeGenerator = null;
    ageGroupsOfOnePersonHouseholdsGenerator = null;
  }

  public static RandomGenerator buildRandomGenerator(Object object, String parameter) {

    switch (object) {
      case Child child -> {

        switch (parameter) {
          case "Child2" -> {
            if (childSpacingGenerator_2ndChild == null) {
              childSpacingGenerator_2ndChild =
                      new RandomGenerator(probabilityDistributionBuilder
                              .buildProbabilityDistribution(object, parameter));
            }
            return childSpacingGenerator_2ndChild;
          }
          case "Child3" -> {
            if (childSpacingGenerator_3rdChild == null) {
              childSpacingGenerator_3rdChild =
                      new RandomGenerator(probabilityDistributionBuilder
                              .buildProbabilityDistribution(object, parameter));
            }
            return childSpacingGenerator_3rdChild;
          }
          case "Child4" -> {
            if (childSpacingGenerator_4thChild == null) {
              childSpacingGenerator_4thChild =
                      new RandomGenerator(probabilityDistributionBuilder
                              .buildProbabilityDistribution(object, parameter));
            }
            return childSpacingGenerator_4thChild;
          }
        }
      }
      case Household household -> {

        if (parameter.equalsIgnoreCase("LivingArrangementTypAndNumberOfChildren")) {
          typeOfLivingArrangement_and_numberOfChildrenGenerator =
                  ifNull_build(typeOfLivingArrangement_and_numberOfChildrenGenerator, object, parameter);
          return typeOfLivingArrangement_and_numberOfChildrenGenerator;
        } else if (parameter.equalsIgnoreCase("LivingArrangementsWithSingleFatherAndChildren")) {
          livingArrangementsWithSingleFatherAndChildren =
                  ifNull_build(livingArrangementsWithSingleFatherAndChildren, object, parameter);
          return livingArrangementsWithSingleFatherAndChildren;
        } else if (parameter.equalsIgnoreCase("LivingArrangementsWithSingleMotherAndChildren")) {
          livingArrangementsWithSingleMotherAndChildren =
                  ifNull_build(livingArrangementsWithSingleMotherAndChildren, object, parameter);
          return livingArrangementsWithSingleMotherAndChildren;
        } else {
          return null;
        }
      }
      case LivingArrangement livingArrangement -> {

            /*
            if(parameter.equalsIgnoreCase("AverageNumberOfChildrenPerMother")){
                numberOfChildrenGenerator = ifNull_build(numberOfChildrenGenerator, object, parameter);
                return numberOfChildrenGenerator;
            }

             */

        if (parameter.equalsIgnoreCase("AgeOfMotherDuringFirstBirth")) {
          ageDuringFirstBirthGenerator = ifNull_build(ageDuringFirstBirthGenerator, object, parameter);
          return ageDuringFirstBirthGenerator;
        } else if (parameter.equals("AgeDifferencesBetweenSpouses")) {
          ageDifferenceBetweenSpousesGenerator =
                  ifNull_build(ageDifferenceBetweenSpousesGenerator, object, parameter);
          return ageDifferenceBetweenSpousesGenerator;
        }
      }
      case SurnameConfigurator surnameConfigurator -> {

        if (parameter.equalsIgnoreCase("SurnameVariationsMarriage")) {
          variationForMarriageGenerator =
                  ifNull_build(variationForMarriageGenerator, object, parameter);
          return variationForMarriageGenerator;
        } else if (parameter.equalsIgnoreCase("SurnameVariationsForCohabitation")) {
          variationForCohabitationGenerator =
                  ifNull_build(variationForCohabitationGenerator, object, parameter);
          return variationForCohabitationGenerator;
        }
      }
      case RelationshipTuplePool relationshipTuplePool -> {

            /*
            childlessPartner1Generator = ifNull_build(childlessPartner1Generator, object, null);
            return childlessPartner1Generator;

             */
      }
      case HouseholdCounter householdCounter -> {

        householdSizeGenerator = ifNull_build(householdSizeGenerator, object, null);
        return householdSizeGenerator;
      }
      case Pool pool -> {

        ageGroupsOfOnePersonHouseholdsGenerator =
                ifNull_build(ageGroupsOfOnePersonHouseholdsGenerator, object, null);
        return ageGroupsOfOnePersonHouseholdsGenerator;
      }
      case null, default ->
              System.out.println("ERROR @ Builders.RandomGeneratorBuilder: Random Generator for HouseholdStructurePool" +
                      " can not be built.");
    }
    return null;
  }

  public static RandomGenerator buildRandomGenerator(Object object, List<String> strings, List<Integer> counts) {
    if (childlessPartner1Generator_updated == null) {
      childlessPartner1Generator_updated = new RandomGenerator(
              probabilityDistributionBuilder
                      .buildProbabilityDistribution(object, strings, counts));
    }
    return childlessPartner1Generator_updated;

  }

  public static RandomGenerator buildRandomGenerator(Attribute attribute) {

    if (attribute instanceof CensusTuple) {
      if (censusTupleGenerator == null) {
        censusTupleGenerator =
                new RandomGenerator(probabilityDistributionBuilder.buildProbabilityDistribution(attribute));
      }
      return censusTupleGenerator;
    } else if (attribute instanceof Surname) {
      if (surnameGenerator == null) {
        surnameGenerator = new RandomGenerator(probabilityDistributionBuilder.buildProbabilityDistribution(attribute));
      }
      return surnameGenerator;
    } else {
      System.out.println("ERROR @ Builders.RandomGeneratorBuilder: Random Generator for "
              + attribute.getAttributeName() + " can not be built.");
      return null;
    }
  }

  public static Map<Object, RandomGenerator> buildRandomGenerators(Attribute attribute, String parameter) {

    Map<Object, RandomGenerator> multipleRandomGenerators = new HashMap<>();

    switch (attribute) {
      case ZipCode zipCode -> {
        if (zipCodeGenerators == null) {
          Map<Object, ProbabilityDistribution> multipleProbabilityDistributions =
                  probabilityDistributionBuilder.buildProbabilityDistributions(attribute, parameter);
          for (Map.Entry<Object, ProbabilityDistribution> entry : multipleProbabilityDistributions.entrySet()) {
            multipleRandomGenerators.put(entry.getKey(), new RandomGenerator(entry.getValue()));
          }
          zipCodeGenerators = multipleRandomGenerators;
        }
        return zipCodeGenerators;
      }
      case Street street -> {
        if (streetGenerators == null) {
          Map<Object, ProbabilityDistribution> multipleProbabilityDistributions =
                  probabilityDistributionBuilder.buildProbabilityDistributions(attribute, parameter);
          for (Map.Entry<Object, ProbabilityDistribution> entry : multipleProbabilityDistributions.entrySet()) {
            multipleRandomGenerators.put(entry.getKey(), new RandomGenerator(entry.getValue()));
          }
          streetGenerators = multipleRandomGenerators;
        }
        return streetGenerators;
      }
      case Location location -> {
        if (locationGenerators == null) {
          Map<Object, ProbabilityDistribution> multipleProbabilityDistributions =
                  probabilityDistributionBuilder.buildProbabilityDistributions(attribute, parameter);
          for (Map.Entry<Object, ProbabilityDistribution> entry : multipleProbabilityDistributions.entrySet()) {
            multipleRandomGenerators.put(entry.getKey(), new RandomGenerator(entry.getValue()));
          }
          locationGenerators = multipleRandomGenerators;
        }
        return locationGenerators;
      }
      case PlaceOfBirth_FederalState placeOfBirthFederalState -> {
        if (placeOfBirth_federalStatesGenerator == null) {
          Map<Object, ProbabilityDistribution> multipleProbabilityDistributions =
                  probabilityDistributionBuilder.buildProbabilityDistributions(attribute, parameter);
          for (Map.Entry<Object, ProbabilityDistribution> entry : multipleProbabilityDistributions.entrySet()) {
            multipleRandomGenerators.put(entry.getKey(), new RandomGenerator(entry.getValue()));
          }
          placeOfBirth_federalStatesGenerator = multipleRandomGenerators;
        }
        return placeOfBirth_federalStatesGenerator;
      }
      case PlaceOfBirth placeOfBirth -> {
        if (placeOfBirthGenerators == null) {
          Map<Object, ProbabilityDistribution> multipleProbabilityDistributions =
                  probabilityDistributionBuilder.buildProbabilityDistributions(attribute, parameter);
          for (Map.Entry<Object, ProbabilityDistribution> entry : multipleProbabilityDistributions.entrySet()) {
            multipleRandomGenerators.put(entry.getKey(), new RandomGenerator(entry.getValue()));
          }
          placeOfBirthGenerators = multipleRandomGenerators;
        }
        return placeOfBirthGenerators;
      }
      case Forename forename when parameter.equalsIgnoreCase("female") -> {
        if (femaleNameGenerators == null) {
          Map<Object, ProbabilityDistribution> multipleProbabilityDistributions =
                  probabilityDistributionBuilder.buildProbabilityDistributions(attribute, parameter);
          for (Map.Entry<Object, ProbabilityDistribution> entry : multipleProbabilityDistributions.entrySet()) {
            multipleRandomGenerators.put(entry.getKey(), new RandomGenerator(entry.getValue()));
          }
          femaleNameGenerators = multipleRandomGenerators;
        }
        return femaleNameGenerators;
      }
      case Forename forename when parameter.equalsIgnoreCase("male") -> {
        if (maleNameGenerators == null) {
          Map<Object, ProbabilityDistribution> multipleProbabilityDistributions =
                  probabilityDistributionBuilder.buildProbabilityDistributions(attribute, parameter);
          for (Map.Entry<Object, ProbabilityDistribution> entry : multipleProbabilityDistributions.entrySet()) {
            multipleRandomGenerators.put(entry.getKey(), new RandomGenerator(entry.getValue()));
          }
          maleNameGenerators = multipleRandomGenerators;
        }
        return maleNameGenerators;
      }
      default -> {
        System.out.println("ERROR @ Builders.RandomGeneratorBuilder: Random Generators for "
                + attribute.getAttributeName() + " can not be built.");
        return null;
      }
    }
  }

  private static RandomGenerator ifNull_build(RandomGenerator randomGenerator, Object object, String parameter) {
    if (randomGenerator == null) {
      randomGenerator =
              new RandomGenerator(
                      probabilityDistributionBuilder
                              .buildProbabilityDistribution(object, parameter));
    }
    return randomGenerator;
  }
}