package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.cohabitation;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.LivingArrangement;

import java.util.List;

public abstract class Cohabitation extends LivingArrangement {

  public Cohabitation(Household household, int maximumNumberOfChildren, List<String> requestedAttributes) {
    super(household, maximumNumberOfChildren, requestedAttributes);
  }
}