package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles;

import java.util.List;

public class MaleCompanion extends FamilyMember {

  public MaleCompanion(List<String> requestedAttributes) {

    super(requestedAttributes);

    if (getCensusTuple() != null) {
      getCensusTuple().setGender("M");
    }
  }
}