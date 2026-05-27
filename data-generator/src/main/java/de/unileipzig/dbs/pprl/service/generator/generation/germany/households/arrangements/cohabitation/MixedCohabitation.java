package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.cohabitation;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.*;

import java.util.List;

public class MixedCohabitation extends Cohabitation {

  public MixedCohabitation(Household household, int maximumNumberOfChildren, List<String> requestedAttributes) {

    super(household, maximumNumberOfChildren, requestedAttributes);

    setPartner1(new MaleCompanion(requestedAttributes));
    setPartner2(new FemaleCompanion(requestedAttributes));

    getPartner1().configure(household);
    getPartner2().configure(household);

    if (getPartner1().getHouseholdRole() != null) {
      getPartner1().getHouseholdRole().setValue_String("mC");
    }
    if (getPartner2().getHouseholdRole() != null) {
      getPartner2().getHouseholdRole().setValue_String("fC");
    }

    getAllPersons().add(getPartner1());
    getAllPersons().add(getPartner2());
  }

  public void singleMale() {
    getPartner2().deactivate();
  }

  public void singleFemale() {
    getPartner1().deactivate();
  }

  public void calculateStructureAndSize() {
    int i3 = 0, i4 = 0, c;
    if (getPartner1().activated()) {
      i3++;
    }
    if (getPartner2().activated()) {
      i4++;
    }
    c = getNumberOfActiveChildren();

    setLivingArrangementStructure(String.format("0-0-%s-%s-%s", i3, i4, c));
    setLivingArrangementSize(i3 + i4 + c);
  }
}