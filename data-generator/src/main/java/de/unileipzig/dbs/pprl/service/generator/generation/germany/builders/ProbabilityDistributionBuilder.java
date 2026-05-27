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
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.ProbabilityDistribution;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

public class ProbabilityDistributionBuilder {

  private static ProbabilityDistributionBuilder INSTANCE;

  Document census;
  Document federalStates_zipCodes_location;
  Document federalStates_zipCodes_population;
  Document zipCodes_streetNames;
  Document births_years_federalStates;
  Document placesOfBirth_federalStates_placesOfBirth;
  Document forenames_gender_decades;
  Document surnames;

  Document householdStatistics;

  private ProbabilityDistributionBuilder() {
    String basePath = "generator-germany/probabilityDistributions/";

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


    try {
      DocumentBuilder documentbuilder = factory.newDocumentBuilder();
      census = documentbuilder.parse(new ClassPathResource(basePath + "Census - Federal states, gender, age.xml").getInputStream());
      federalStates_zipCodes_location = documentbuilder.parse(new ClassPathResource(basePath + "FederalStates - ZipCodes,Location.xml").getInputStream());
      federalStates_zipCodes_population = documentbuilder.parse(new ClassPathResource(basePath + "FederalStates - ZipCodes,Population.xml").getInputStream());
      zipCodes_streetNames = documentbuilder.parse(new ClassPathResource(basePath + "ZipCodes - StreetNames.xml").getInputStream());
      births_years_federalStates = documentbuilder.parse(new ClassPathResource(basePath + "Births - Years, FederalStates.xml").getInputStream());
      placesOfBirth_federalStates_placesOfBirth = documentbuilder.parse(new ClassPathResource(basePath + "PlacesOfBirth - FederalStates, PlacesOfBirth.xml").getInputStream());
      forenames_gender_decades = documentbuilder.parse(new ClassPathResource(basePath + "Forenames - Gender, Decades.xml").getInputStream());
      surnames = documentbuilder.parse(new ClassPathResource(basePath + "Surnames.xml").getInputStream());
      householdStatistics = documentbuilder.parse(new ClassPathResource(basePath + "HouseholdStatistics.xml").getInputStream());

    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
  }

  public static ProbabilityDistributionBuilder getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ProbabilityDistributionBuilder();
    }
    return INSTANCE;
  }

  public ProbabilityDistribution buildProbabilityDistribution(Object object, String parameter) {

    List<String> eventSpace = new ArrayList<>();
    List<String> counts = new ArrayList<>();

    switch (object) {
      case Child child1 -> {

        if (parameter.equals("Child2") || parameter.equals("Child3") || parameter.equals("Child4")) {
          /* Get the Element node for that respective child. */
          Element child = getElementsByTagNameUDF(householdStatistics, parameter).getFirst();
          List<Element> ageDifferences = getChildElementsUDF(child);
          List<Integer> probabilityLimits = new ArrayList<>();
          for (Element e : ageDifferences) {
            eventSpace.add(String.valueOf(Integer.parseInt(e.getAttribute("year")) - 1));
            probabilityLimits.add(Integer.valueOf(e.getTextContent()));
          }
          /* Transform the probabilityLimits to a frequency distribution.
           * And transform eventSpace to eventSpace_int[].
           */
          int[] eventSpace_int = new int[eventSpace.size()];
          for (int i = 0; i < eventSpace.size(); i++) {
            eventSpace_int[i] = Integer.parseInt(eventSpace.get(i));
            if (i == 0) {
              counts.add(probabilityLimits.getFirst().toString());
            } else {
              counts.add(String.valueOf(probabilityLimits.get(i) - probabilityLimits.get(i - 1)));
            }
          }
          return new ProbabilityDistribution(eventSpace_int, counts);
        }
      }
      case Household household -> {

        if (parameter.equalsIgnoreCase("LivingArrangementTypAndNumberOfChildren")) {
          Element node =
                  getElementsByTagNameUDF(householdStatistics, parameter).getFirst();
          List<Element> combinations = getChildElementsUDF(node);
          int[][] eventSpace_intArray = new int[combinations.size()][];
          for (int i = 0; i < combinations.size(); i++) {
            int type = Integer.parseInt(combinations.get(i).getAttribute("type"));
            int number = Integer.parseInt(combinations.get(i).getAttribute("number"));
            int[] event = new int[]{type, number};
            eventSpace_intArray[i] = event;
            counts.add(combinations.get(i).getTextContent());
          }
          return new ProbabilityDistribution(eventSpace_intArray, counts);
        }

        if (parameter.equalsIgnoreCase("LivingArrangementsWithSingleFatherAndChildren")) {
          Element node =
                  getElementsByTagNameUDF(householdStatistics, parameter).getFirst();
          List<Element> typesOfLivingArrangements = getChildElementsUDF(node);
          return eventSpace_int_counts_String(typesOfLivingArrangements, "number");
        }

        if (parameter.equalsIgnoreCase("LivingArrangementsWithSingleMotherAndChildren")) {
          Element node =
                  getElementsByTagNameUDF(householdStatistics, parameter).getFirst();
          List<Element> typesOfLivingArrangements = getChildElementsUDF(node);
          return eventSpace_int_counts_String(typesOfLivingArrangements, "number");
        }
      }
      case LivingArrangement livingArrangement -> {

        if (parameter.equalsIgnoreCase("AgeOfMotherDuringFirstBirth")) {
          Element element = getElementsByTagNameUDF(householdStatistics, parameter).getFirst();
          List<Element> mothers = getChildElementsUDF(element);
          int[] eventSpace_int = new int[mothers.size()];
          for (int i = 0; i < eventSpace_int.length; i++) {
            eventSpace_int[i] = Integer.parseInt(mothers.get(i).getAttribute("age"));
            counts.add(mothers.get(i).getTextContent());
          }
          return new ProbabilityDistribution(eventSpace_int, counts);
        } else if (parameter.equals("AgeDifferencesBetweenSpouses")) {
          /* Load the relevant data from the relevant document: */
          Element element = getElementsByTagNameUDF(
                  householdStatistics,
                  "AgeDifferencesBetweenSpouses").getFirst();
          double factor = Double.parseDouble(element.getAttribute("factor"));
          List<Element> differences = getChildElementsUDF(element);
          for (Element difference : differences) {
            eventSpace.add(difference.getAttribute("years"));
            counts.add(difference.getTextContent());
          }
          /* Processing of loaded data to instantiate a probability distribution: */
          int[] eventSpace_int = new int[differences.size() * 2 - 1];
          List<String> finalCounts = new ArrayList<>();
          for (int i = 0; i < eventSpace_int.length; ) {
            if (i == 0) {
              eventSpace_int[0] = Integer.parseInt(eventSpace.removeFirst());
              finalCounts.add(counts.removeFirst());
              i++;
            } else {
              eventSpace_int[i] = Integer.parseInt(eventSpace.removeFirst());
              eventSpace_int[i + 1] = eventSpace_int[i] * (-1);
              int count = Integer.parseInt(counts.removeFirst());
              finalCounts.add(String.valueOf(count * factor));
              finalCounts.add(String.valueOf(count * (1 - factor)));
              i = i + 2;
            }
          }
          return new ProbabilityDistribution(eventSpace_int, finalCounts);
        }
      }
      case SurnameConfigurator surnameConfigurator -> {

        if (parameter.equalsIgnoreCase("SurnameVariationsMarriage")) {
          Element node = getElementsByTagNameUDF(householdStatistics, parameter).getFirst();
          List<Element> variations = getChildElementsUDF(node);
          return eventSpace_int_counts_String(variations, "number");
        } else if (parameter.equalsIgnoreCase("SurnameVariationsForCohabitation")) {
          Element node = getElementsByTagNameUDF(householdStatistics, parameter).getFirst();
          List<Element> variations = getChildElementsUDF(node);
          return eventSpace_int_counts_String(variations, "number");
        }
      }
      case RelationshipTuplePool relationshipTuplePool -> {

        //<editor-fold desc="A uniform distribution among all federal states is assumed.">
        String[] federalStates = new String[]{
                "Baden-Württemberg",
                "Bayern",
                "Berlin",
                "Brandenburg",
                "Bremen",
                "Hamburg",
                "Hessen",
                "Mecklenburg-Vorpommern",
                "Niedersachsen",
                "Nordrhein-Westfalen",
                "Rheinland-Pfalz",
                "Saarland",
                "Sachsen",
                "Sachsen-Anhalt",
                "Schleswig-Holstein",
                "Thüringen"};
        //</editor-fold>

        List<Double> counts_Double = new ArrayList<>();

        Element element =
                getElementsByTagNameUDF(householdStatistics, "LivingArrangementsWithoutChildren").getFirst();
        List<Element> typesOfLivingArrangements = getChildElementsUDF(element);
        for (String federalState : federalStates) {
          for (Element e : typesOfLivingArrangements) {
            int type = Integer.parseInt(e.getAttribute("number"));
            List<Element> ageGroups = getChildElementsUDF(e);
            for (Element ageGroup : ageGroups) {
              int from = Integer.parseInt(ageGroup.getAttribute("from"));
              int to = Integer.parseInt(ageGroup.getAttribute("to"));
              int persons = Integer.parseInt(ageGroup.getTextContent());
              double counts_double = (double) persons / (double) (to - from);
              for (int i = from; i < to; i++) {
                eventSpace.add(federalState + "," + type + "," + i);
                counts_Double.add(counts_double);
                /* test probability distribution builder*/
                //System.out.println("1424Event: " + federalState + "," + type + "," + i + "     prob.: "+ counts_double);
              }
            }
          }
        }
        return ProbabilityDistribution.fromDoubleCounts(eventSpace, counts_Double);
      }
      case HouseholdCounter householdCounter -> {
        Element node = getElementsByTagNameUDF(householdStatistics, "HouseholdSizes").getFirst();
        List<Element> householdSizes = getChildElementsUDF(node);
        return eventSpace_int_counts_String(householdSizes, "size");
      }
      case Pool pool -> {
        Element node = getElementsByTagNameUDF(householdStatistics, "AgeGroupsOfOnePersonHouseholds").getFirst();
        List<Element> ageGroups = getChildElementsUDF(node);
        for (Element ageGroup : ageGroups) {
          eventSpace.add(ageGroup.getAttribute("range"));
          counts.add(ageGroup.getTextContent());
        }
        return ProbabilityDistribution.fromStringCounts(eventSpace, counts);
      }
      case null, default ->
              System.out.println("ERROR @ Builders.ProbabilityDistributionBuilder, HouseholdStructurePool.");
    }
    return null;
  }

  public ProbabilityDistribution buildProbabilityDistribution(Object object, List<String> strings, List<Integer> integers) {

    List<String> eventSpace = new ArrayList<>();

    if (object instanceof RelationshipTuplePool) {

      List<Double> counts_Double = new ArrayList<>();

      Element element =
              getElementsByTagNameUDF(householdStatistics, "LivingArrangementsWithoutChildren").getFirst();
      List<Element> typesOfLivingArrangements = getChildElementsUDF(element);
      /* Iterate through all federal states. */
      for (int i = 0; i < strings.size(); i++) {
        for (Element e : typesOfLivingArrangements) {
          int type = Integer.parseInt(e.getAttribute("number"));
          List<Element> ageGroups = getChildElementsUDF(e);
          for (Element ageGroup : ageGroups) {
            int from = Integer.parseInt(ageGroup.getAttribute("from"));
            int to = Integer.parseInt(ageGroup.getAttribute("to"));
            int persons = Integer.parseInt(ageGroup.getTextContent());
            /* Add dependencies from different federal states: */
            double counts_double = (integers.get(i) * (double) persons) / (double) (to - from);
            for (int j = from; j < to; j++) {
              eventSpace.add(strings.get(i) + "," + type + "," + j);
              counts_Double.add(counts_double);
              /* test probability distribution builder*/
              //System.out.println("1424Event: " + federalState + "," + type + "," + i + "     prob.: "+ counts_double);
            }
          }
        }
      }
      return ProbabilityDistribution.fromDoubleCounts(eventSpace, counts_Double);

    }

    return null;
  }

  public ProbabilityDistribution buildProbabilityDistribution(Attribute attribute) {
    if (attribute instanceof CensusTuple) {
      List<String> eventSpace = new ArrayList<>();
      List<String> counts = new ArrayList<>();
      String federalStateName;
      String gender = "";

      /* Get a list that contains all the <FederalState> nodes. */
      List<Element> federalStates = getElementsByTagNameUDF(census, "FederalState");
      for (Element federalState : federalStates) {
        federalStateName = federalState.getAttribute("name");
        List<Element> maleAndFemalePopulationElements = getChildElementsUDF(federalState);
        for (Element genderGroup : maleAndFemalePopulationElements) {
          if (genderGroup.getNodeName().equalsIgnoreCase("MalePopulation")) {
            gender = "M";
          } else if (genderGroup.getNodeName().equalsIgnoreCase("FemalePopulation")) {
            gender = "F";
          }
          List<Element> ages = getChildElementsUDF(genderGroup);
          for (Element age : ages) {
            eventSpace.add(federalStateName + "," + gender + "," + age.getAttribute("age"));
            counts.add(age.getTextContent());
          }
        }
      }
      return ProbabilityDistribution.fromStringCounts(eventSpace, counts);


    } else if (attribute instanceof Surname) {
      List<String> eventSpace = new ArrayList<>();
      List<String> counts = new ArrayList<>();
      List<Element> surnames = getElementsByTagNameUDF(this.surnames, "Surname");
      for (Element surname : surnames) {
        eventSpace.add(surname.getAttribute("value"));
        counts.add(surname.getAttribute("count"));
      }
      return ProbabilityDistribution.fromStringCounts(eventSpace, counts);

    } else {
      System.out.println("ERROR @ Builders.ProbabilityDistributionBuilder, requested by " + attribute.getClass().getName());
      return null;
    }
  }

  /**
   * Methods builds multiple RandomGenerator.ProbabilityDistribution objects, that are stored inside a map.
   *
   * @param attribute The Attribute object, that invokes this method.
   * @param parameter The Attribute object can hand over a parameter, in case it needs multiple maps.
   * @return A map that stores different RandomGenerator.ProbabilityDistribution objects for different keys.
   */
  public Map<Object, ProbabilityDistribution> buildProbabilityDistributions(Attribute attribute, String parameter) {

    switch (attribute) {
      case ZipCode code -> {
        Map<Object, ProbabilityDistribution> zipCodeDistributions = new HashMap<>();
        List<Element> federalStates = getElementsByTagNameUDF(federalStates_zipCodes_population, "FederalState");
        for (Element federalState : federalStates) {
          List<String> zipCodes = new ArrayList<>();
          List<String> population = new ArrayList<>();
          List<Element> zipCodesList = getChildElementsUDF(federalState);
          for (Element zipCode : zipCodesList) {
            zipCodes.add(zipCode.getAttribute("value"));
            population.add(zipCode.getAttribute("population"));
          }
          zipCodeDistributions.put(federalState.getAttribute("name"), ProbabilityDistribution.fromStringCounts(zipCodes, population));
        }
        return zipCodeDistributions;
      }
      case Street street1 -> {
        Map<Object, ProbabilityDistribution> results = new HashMap<>();
        List<Element> zipCodes = getElementsByTagNameUDF(zipCodes_streetNames, "ZipCode");
        for (Element zipCode : zipCodes) {
          List<String> eventSpace = new ArrayList<>();
          List<Element> streets = getChildElementsUDF(zipCode);
          for (Element street : streets) {
            eventSpace.add(street.getTextContent());
          }
          results.put(zipCode.getAttribute("value"), new ProbabilityDistribution(eventSpace, true));
        }
        return results;
      }
      case Location location1 -> {
        Map<Object, ProbabilityDistribution> result = new HashMap<>();
        Map<String, List<String>> zipCodesAndLocations = new HashMap<>();
        List<Element> locations = getElementsByTagNameUDF(federalStates_zipCodes_location, "Location");
        for (Element location : locations) {
          String zipCode = location.getAttribute("zipCode");
          if (zipCodesAndLocations.containsKey(zipCode)) {
            zipCodesAndLocations.get(zipCode).add(location.getAttribute("name"));
          } else {
            zipCodesAndLocations.put(zipCode,
                    new ArrayList<>(List.of(location.getAttribute("name"))));
          }
        }
        for (Map.Entry<String, List<String>> entry : zipCodesAndLocations.entrySet()) {
          result.put(entry.getKey(), new ProbabilityDistribution(entry.getValue(), true));
        }
        return result;
      }
      case PlaceOfBirth_FederalState placeOfBirthFederalState -> {
        Map<Object, ProbabilityDistribution> result = new HashMap<>();
        List<Element> years = getElementsByTagNameUDF(births_years_federalStates, "Year");
        for (Element year : years) {
          List<String> eventSpace = new ArrayList<>();
          List<String> counts = new ArrayList<>();
          List<Element> federalStates = getChildElementsUDF(year);
          for (Element federalState : federalStates) {
            eventSpace.add(federalState.getAttribute("name"));
            counts.add(federalState.getAttribute("births"));
          }
          result.put(year.getAttribute("year"), ProbabilityDistribution.fromStringCounts(eventSpace, counts));
        }
        return result;
      }
      case PlaceOfBirth ofBirth -> {
        Map<Object, ProbabilityDistribution> result = new HashMap<>();
        List<Element> federalStates =
                getElementsByTagNameUDF(placesOfBirth_federalStates_placesOfBirth, "FederalState");
        for (Element federalState : federalStates) {
          List<String> eventSpace = new ArrayList<>();
          List<String> counts = new ArrayList<>();
          List<Element> placesOfBirth = getChildElementsUDF(federalState);
          for (Element placeOfBirth : placesOfBirth) {
            eventSpace.add(placeOfBirth.getAttribute("location"));
            counts.add(placeOfBirth.getAttribute("population"));
          }
          result.put(federalState.getAttribute("name"), ProbabilityDistribution.fromStringCounts(eventSpace, counts));
        }
        return result;
      }
      case Forename forename1 -> {
        Map<Object, ProbabilityDistribution> result = new HashMap<>();
        List<Element> genderForenames = new ArrayList<>();
        if (parameter.equalsIgnoreCase("female")) {
          genderForenames = getElementsByTagNameUDF(forenames_gender_decades, "FemaleForenames");
        } else if (parameter.equalsIgnoreCase("male")) {
          genderForenames = getElementsByTagNameUDF(forenames_gender_decades, "MaleForenames");
        }
        List<Element> decades = getChildElementsUDF(genderForenames.getFirst());
        for (Element decade : decades) {
          List<String> eventSpace = new ArrayList<>();
          List<String> counts = new ArrayList<>();
          List<Element> forenames = getChildElementsUDF(decade);
          for (Element forename : forenames) {
            eventSpace.add(forename.getAttribute("value"));
            counts.add(forename.getAttribute("count"));
          }
          result.put(Integer.valueOf(decade.getAttribute("start")), ProbabilityDistribution.fromStringCounts(eventSpace, counts));
        }
        return result;
      }
      default -> {
        System.out.println("ERROR @ Builders.ProbabilityDistributionBuilder: Probability Distributions for "
                + attribute.getAttributeName() + " can not be build.");
        return null;
      }
    }
  }

  public int[] buildFrequencyDistribution(Object object, String parameter1, String parameter2) {

    if (object instanceof Pool) {

      if (parameter1.equals("childrenLivingInParentsHousehold")) {
        List<Element> list = getElementsByTagNameUDF(householdStatistics, "ChildrenLivingInParentsHousehold");
        Element malesOrFemales = null;
        if (parameter2.equals("male")) {
          malesOrFemales = getChildElementsUDF(list.getFirst()).getFirst();
        } else if (parameter2.equals("female")) {
          malesOrFemales = getChildElementsUDF(list.getFirst()).get(1);
        }
        List<Element> ages = getChildElementsUDF(malesOrFemales);
        List<Integer> values = new ArrayList<>();
        List<Integer> textContent = new ArrayList<>();
        for (Element age : ages) {
          values.add(Integer.valueOf(age.getAttribute("value")));
          textContent.add(Integer.valueOf(age.getTextContent()));
        }
        /* Get the last (=greatest) value and add 1. */
        int[] result = new int[values.getLast() + 1];
        for (int i = 0; i < values.size(); i++) {
          result[values.get(i)] = textContent.get(i);
        }
        return result;
      }
    }

    return null;
  }

  private List<Element> getElementsByTagNameUDF(Document document, String tagName) {
    List<Element> result = new ArrayList<>();
    NodeList nodeList = document.getElementsByTagName(tagName);
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        result.add((Element) node);
      }
    }
    return result;
  }

  private List<Element> getChildElementsUDF(Element element) {
    List<Element> result = new ArrayList<>();
    NodeList nodeList = element.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        result.add((Element) node);
      }
    }
    return result;
  }

  private ProbabilityDistribution eventSpace_int_counts_String(List<Element> list, String attributeName) {
    int[] eventSpace_int = new int[list.size()];
    List<String> counts = new ArrayList<>();
    for (int i = 0; i < eventSpace_int.length; i++) {
      eventSpace_int[i] = Integer.parseInt(list.get(i).getAttribute(attributeName));
      counts.add(list.get(i).getTextContent());
    }
    return new ProbabilityDistribution(eventSpace_int, counts);
  }
}