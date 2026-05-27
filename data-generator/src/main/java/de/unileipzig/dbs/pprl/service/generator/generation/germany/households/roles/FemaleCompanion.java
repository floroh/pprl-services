package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles;

import java.util.List;

public class FemaleCompanion extends FamilyMember {

  public FemaleCompanion(List<String> requestedAttributes) {

    super(requestedAttributes);

    if (getCensusTuple() != null) {
      getCensusTuple().setGender("F");
    }
  }
}