package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles;

import java.util.List;

public class Husband extends FamilyMember {

  public Husband(List<String> requestedAttributes) {

    super(requestedAttributes);

    if (getCensusTuple() != null) {
      getCensusTuple().setGender("M");
    }
  }
}