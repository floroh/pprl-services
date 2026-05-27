package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles;

import java.util.List;

public class Wife extends FamilyMember {

  public Wife(List<String> requestedPersonAttributes) {

    super(requestedPersonAttributes);

    if (getCensusTuple() != null) {
      getCensusTuple().setGender("F");
    }
  }
}