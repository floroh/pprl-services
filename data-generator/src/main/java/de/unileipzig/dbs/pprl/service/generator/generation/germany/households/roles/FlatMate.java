package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles;

import java.util.List;

public class FlatMate extends HouseholdMember {

  public FlatMate(List<String> requestedAttributes) {
    super(requestedAttributes);
  }

  public void flipGender() {
    if (getCensusTuple().getGender().equalsIgnoreCase("M")) {
      getCensusTuple().setGender("F");
    } else {
      getCensusTuple().setGender("M");
    }
  }
}