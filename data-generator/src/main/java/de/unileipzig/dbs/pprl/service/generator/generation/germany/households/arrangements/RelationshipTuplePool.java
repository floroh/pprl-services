package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person.CensusTuple;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Object of this class stores a pool of RelationshipTuple objects
 */
public class RelationshipTuplePool {

  private RandomGenerator childlessPartner1Generator;

  @Setter
  @Getter
  private List<RelationshipTuple> pool;

  public RelationshipTuplePool(int numberOfChildlessRelationships, List<CensusTuple> pool_2) {

    //<editor-fold desc="Take into account the distribution among all federal states.">
    List<String> federalStates = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();
    for (CensusTuple tuple : pool_2) {
      if (!federalStates.contains(tuple.getFederalState())) {
        federalStates.add(tuple.getFederalState());
        counts.add(tuple.getFrequency());
      } else {
        int index = federalStates.indexOf(tuple.getFederalState());
        int oldCount = counts.get(index);
        counts.set(index, oldCount + tuple.getFrequency());
      }
    }
    //</editor-fold>

    childlessPartner1Generator = RandomGeneratorBuilder.buildRandomGenerator(this, federalStates, counts);

    pool = new ArrayList<>();

    /* Get the event space of the respective random generator. The event space has tuples of this form:
     * (federal state, type of relationship, age of partner 1).
     */
    List<String> eventSpace =
            childlessPartner1Generator.getProbabilityDistribution().getEventSpace_String();

    /* Create the respective tuple for every event. */
    for (String event : eventSpace) {
      String[] eventArray = event.split(",");
      pool.add(new RelationshipTuple(
              eventArray[0],
              Integer.parseInt(eventArray[1]),
              Integer.parseInt(eventArray[2])
      ));
    }

    /* Do the random experiments and count the results. */
    for (int i = 0; i < numberOfChildlessRelationships; i++) {
      int index = childlessPartner1Generator.getAliasMethod().next();
      pool.get(index).setFrequency(
              pool.get(index).getFrequency() + 1);
    }

    /* Remove tuples with no frequency. */
    pool.removeIf(tuple -> tuple.getFrequency() == 0);
  }

  public void reduceFrequency(RelationshipTuple tuple) {
    if (pool.contains(tuple)) {
      int index = pool.indexOf(tuple);
      int oldFrequency = pool.get(index).getFrequency();
      if (oldFrequency - 1 <= 0) {
        pool.remove(index);
      } else {
        pool.get(index).setFrequency(oldFrequency - 1);
      }
    }
  }
}