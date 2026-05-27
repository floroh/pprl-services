package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.marriage;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.LivingArrangement;

import java.util.List;

public abstract class Marriage extends LivingArrangement {

  public Marriage(Household household, int maximumNumberOfChildren, List<String> requestedAttributes) {
    super(household, maximumNumberOfChildren, requestedAttributes);
  }
}