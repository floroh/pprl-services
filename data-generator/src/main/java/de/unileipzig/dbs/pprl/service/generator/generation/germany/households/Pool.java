package de.unileipzig.dbs.pprl.service.generator.generation.germany.households;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person.CensusTuple;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.Constants;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.ProbabilityDistributionBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;

import java.util.*;

public class Pool {

  private final Random random = RandomSingleton.getRandom();

  /**
   * Arrays work like a map, age of the child = index of the array.
   * The content is the probability for a child (of a specific age and a specific gender) for living in parents household.
   *
   */
  private final int[] maleLivingInParentsHousehold;
  private final int[] femaleLivingInParentsHousehold;
  private int r;
  private int index;
  private final int minimumAgeToMoveOut = Constants.MINIMUM_AGE_TO_MOVE_OUT;
  private final int maximumAgeForBeingChild = Constants.MAXIMUM_AGE_FOR_BEING_CHILD;


  private RandomGenerator ageGroupsOfOnePersonHouseholdsGenerator;

  private final List<CensusTuple> pool_1 = new ArrayList<>();
  private final List<CensusTuple> pool_1_children = new ArrayList<>();
  private final List<CensusTuple> pool_1_adults = new ArrayList<>();
  private final List<CensusTuple> pool_2 = new ArrayList<>();
  private HashMap<String, List<CensusTuple>> pool_3;
  private List<List<CensusTuple>> pool_4;
  private List<List<CensusTuple>> pool_4_auxiliary;

  public Pool(int censusTuplePoolSize) {

    RandomGenerator censusTupleGenerator = RandomGeneratorBuilder.buildRandomGenerator(new CensusTuple());

    maleLivingInParentsHousehold = ProbabilityDistributionBuilder.getInstance()
            .buildFrequencyDistribution(
                    this,
                    "childrenLivingInParentsHousehold",
                    "male");

    femaleLivingInParentsHousehold = ProbabilityDistributionBuilder.getInstance()
            .buildFrequencyDistribution(
                    this,
                    "childrenLivingInParentsHousehold",
                    "female");

    List<Integer> frequencyDistribution = new ArrayList<>();

    for (int i = 0; i < censusTupleGenerator.getProbabilityDistribution().getEventSpace_String().size(); i++) {
      frequencyDistribution.add(0);
    }

    /* Do random experiments and count the results. */
    int indexForIncreasingFrequency;
    for (int j = 1; j <= censusTuplePoolSize; j++) {
      indexForIncreasingFrequency = censusTupleGenerator.getAliasMethod().next();
      frequencyDistribution.set(indexForIncreasingFrequency, frequencyDistribution.get(indexForIncreasingFrequency) + 1);
    }

    for (int k = 0; k < censusTupleGenerator.getProbabilityDistribution().getEventSpace_String().size(); k++) {
      /* Only add those census tuples to the pool, that have frequency > 0. */
      if (frequencyDistribution.get(k) > 0) {
        /* Get a census tuple, which is a String inside the RandomGenerator. */
        String event = censusTupleGenerator.getProbabilityDistribution().getEventSpace_String().get(k);
        String[] censusTuple = event.split(",");
        /* Pool 1 contains all persons in the beginning. */
        pool_1.add(
                new CensusTuple(
                        censusTuple[0],
                        censusTuple[1],
                        Integer.parseInt(censusTuple[2]),
                        frequencyDistribution.get(k)));
        /* All children from pool_1 will be moved to pool_1_children. */
        pool_1_children.add(
                new CensusTuple(
                        censusTuple[0],
                        censusTuple[1],
                        Integer.parseInt(censusTuple[2]),
                        0));
        /* All persons don't living with parents will be moved to pool_1_adults. */
        pool_1_adults.add(
                new CensusTuple(
                        censusTuple[0],
                        censusTuple[1],
                        Integer.parseInt(censusTuple[2]),
                        0));
        /* pool_2 is prepared for phase 2. */
        pool_2.add(
                new CensusTuple(
                        censusTuple[0],
                        censusTuple[1],
                        Integer.parseInt(censusTuple[2]),
                        0));
      }
    }

    /* Update, separate pool_1 into pool_1_children and pool_1_adults. */
    String gender;
    int age;
    while (!pool_1.isEmpty()) {
      gender = pool_1.getFirst().getGender();
      age = pool_1.getFirst().getAge();
      if (livingInParentsHousehold(gender, age)) {
        addToPool(pool_1_children, pool_1.getFirst());
      } else {
        addToPool(pool_1_adults, pool_1.getFirst());
      }
      subtractFromPool(pool_1, pool_1.getFirst());
    }
    pool_1_children.removeIf(tuple -> tuple.getFrequency() == 0);
    pool_1_adults.removeIf(tuple -> tuple.getFrequency() == 0);

    /* Census tuples are sorted according to age, ascending. */
    Collections.sort(pool_1_children);
  }

  //<editor-fold desc="Standard get and set methods">
  public List<CensusTuple> getPool_1() {
    return pool_1;
  }

  public List<CensusTuple> getPool_2() {
    return pool_2;
  }

  public RandomGenerator getAgeGroupsOfOnePersonHouseholdsGenerator() {
    return ageGroupsOfOnePersonHouseholdsGenerator;
  }

  public HashMap<String, List<CensusTuple>> getPool_3() {
    return pool_3;
  }

  public List<List<CensusTuple>> getPool_4() {
    return pool_4;
  }

  public List<List<CensusTuple>> getPool_4_auxiliary() {
    return pool_4_auxiliary;
  }

  public List<CensusTuple> getPool_1_children() {
    return pool_1_children;
  }

  public List<CensusTuple> getPool_1_adults() {
    return pool_1_adults;
  }
  //</editor-fold>

  /**
   * This function determines, if a child, depending on it's age and gender, still lives in the parents household or
   * not. It is based on the statistics in the HouseholdStatistics.xml file, tag <ChildrenLivingInParentsHousehold>.
   *
   * @param gender Gender of the child.
   * @param age    Age of the child.
   * @return A boolean value, that states if child lives in parents household or not.
   */
  public boolean livingInParentsHousehold(String gender, int age) {

    r = random.nextInt(100000000);

    if (age < minimumAgeToMoveOut) {
      /* Children younger than 14 live with parents. */
      return true;
    } else if (age > maximumAgeForBeingChild) {
      /* Children older then 45 years don't live with parents. */
      return false;
    } else {
      index = age;
    }

    if (gender.equalsIgnoreCase("M")) {
      return r < maleLivingInParentsHousehold[index];
    } else if (gender.equalsIgnoreCase("F")) {
      return r < femaleLivingInParentsHousehold[index];
    } else {
      return false;
    }
  }

  public CensusTuple getFirstCensusTupleFromPool() {
    return pool_1.getFirst();
  }

  public int getPoolFrequency(List<CensusTuple> pool) {
    if (pool.isEmpty()) {
      return 0;
    } else {
      int frequency = 0;
      for (CensusTuple c : pool) {
        frequency += c.getFrequency();
      }
      return frequency;
    }
  }

  public int getPool_4Frequency(List<List<CensusTuple>> pool) {
    int frequency = 0;
    for (List<CensusTuple> microPool : pool) {
      frequency += getPoolFrequency(microPool);
    }
    return frequency;
  }

  public int getPoolFrequency(Map<String, List<CensusTuple>> pool) {
    int frequency = 0;
    for (Map.Entry<String, List<CensusTuple>> entry : pool.entrySet()) {
      List<CensusTuple> tuples = entry.getValue();
      frequency += getPoolFrequency(tuples);
    }
    return frequency;
  }

  public boolean availableInPool(List<CensusTuple> pool, CensusTuple censusTuple) {
    int index = pool.indexOf(censusTuple);
    return pool.contains(censusTuple) && pool.get(index).getFrequency() > 0;
  }

  public boolean subtractFromPool(List<CensusTuple> pool, CensusTuple censusTuple) {
    if (pool.contains(censusTuple)) {
      int index = pool.indexOf(censusTuple);
      if (pool.get(index).getFrequency() == 1) {
        pool.remove(censusTuple);
        return true;
        /* Successfully subtracted. */
      } else if (pool.get(index).getFrequency() > 1) {
        pool.get(index).reduceFrequencyBy(1);
        return true;
        /* Successfully subtracted. */
      } else {
        System.out.println("Tuple in pool with frequency less/equal 0, not allowed.");
        return false;
      }
    } else {
      /* Tuple is not inside pool. */
      return false;
    }
  }

  public boolean addToPool(List<CensusTuple> pool, CensusTuple censusTuple) {
    if (pool.contains(censusTuple)) {
      int index = pool.indexOf(censusTuple);
      pool.get(index).increaseFrequencyBy(1);
      return true;
    } else {
      /* Tuple is not inside pool. */
      return false;
    }
  }

  public void buildPool_3() {

    pool_3 = new HashMap<>();

    ageGroupsOfOnePersonHouseholdsGenerator = RandomGeneratorBuilder.buildRandomGenerator(this, null);

    /* Contains age groups: "0-24", "25-34", "35-44", etc. */
    List<String> ageGroups =
            ageGroupsOfOnePersonHouseholdsGenerator.getProbabilityDistribution().getEventSpace_String();

    /* Store upper limits of each age group inside an array. */
    int[] upperLimitOfEachAgeGroup = new int[ageGroups.size()];
    for (int i = 0; i < ageGroups.size(); i++) {
      String[] limits = ageGroups.get(i).split("-");
      upperLimitOfEachAgeGroup[i] = Integer.parseInt(limits[1]);
    }

    /* Put all possible age groups inside a map. */
    for (String ageGroup : ageGroups) {
      pool_3.put(ageGroup, new ArrayList<>());
    }

    /* Assign every remaining census tuple to a list inside the map, depending on the age. */
    Iterator<CensusTuple> i = pool_2.iterator();
    while (i.hasNext()) {
      CensusTuple tuple = i.next();
      for (int j = 0; j < upperLimitOfEachAgeGroup.length; j++) {
        if (tuple.getAge() <= upperLimitOfEachAgeGroup[j] && tuple.getFrequency() > 0) {
          pool_3.get(ageGroups.get(j)).add(tuple);
          i.remove();
          break;
        }
      }
    }

    /* Remove entries without without tuples. */
    pool_3.entrySet().removeIf(entry -> entry.getValue().size() <= 0);
  }

  public void moveFromArrayListToArrayList(List<CensusTuple> from, List<CensusTuple> to) {
    while (!from.isEmpty()) {
      if (to.contains(from.getFirst())) {
        int index = to.indexOf(from.getFirst());
        to.get(index).increaseFrequencyBy(from.getFirst().getFrequency());
        from.removeFirst();
      } else {
        to.add(from.removeFirst());
      }
    }
  }

  public void fromMapToPool(Map<String, List<CensusTuple>> map, List<CensusTuple> arrayList) {
    for (Map.Entry<String, List<CensusTuple>> entry : map.entrySet()) {
      List<CensusTuple> tuples = entry.getValue();
      while (!tuples.isEmpty()) {
        arrayList.add(tuples.removeFirst());
      }
    }
  }

  /**
   * Moves all the tuples from pool_3 to pool_4 and instantiates pool_4_auxiliary.
   */
  public void buildPool_4() {

    /* Intermediate step: Move all tuples back to pool_2. */
    fromMapToPool(pool_3, pool_2);

    /* Check how many federal states are left. */
    //int fsCounter = 0;
    List<String> federalStates = new ArrayList<>();
    for (CensusTuple tuple : pool_2) {
      if (!federalStates.contains(tuple.getFederalState())) {
        federalStates.add(tuple.getFederalState());
      }
    }

    /* Intermediate step: Create a map, to separate all remained census tuples according to their federal states. */
    HashMap<String, List<CensusTuple>> pool_4_map = new HashMap<>();

    /* For every federal state, that is left, add a key to this map. */
    for (String federalState : federalStates) {
      pool_4_map.put(federalState, new ArrayList<>());
    }

    /* Sort tuples inside pool_2 ascending according to age. Move all those tuples from pool_2 into the map.
     * This separates all the tuples according to their value for federal state.
     */
    Collections.sort(pool_2);
    for (CensusTuple tuple : pool_2) {
      if (tuple.getFrequency() > 0) {
        pool_4_map.get(tuple.getFederalState()).add(tuple);
      }
    }

    /* Clear pool_2. */
    pool_2.clear();

    /* Move all lists inside the map to pool_4, pool_4_auxiliary stays empty. */
    pool_4 = new ArrayList<>();
    pool_4_auxiliary = new ArrayList<>();
    for (Map.Entry<String, List<CensusTuple>> entry : pool_4_map.entrySet()) {
      pool_4.add(entry.getValue());
    }
  }
}