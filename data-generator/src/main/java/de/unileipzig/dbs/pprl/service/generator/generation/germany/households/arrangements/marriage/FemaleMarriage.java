package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.marriage;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.Wife;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;

import java.util.List;

public class FemaleMarriage extends Marriage {

  public FemaleMarriage(Household household, int maximumNumberOfChildren, List<String> requestedAttributes) {

    super(household, maximumNumberOfChildren, requestedAttributes);

    setPartner1(new Wife(requestedAttributes));
    setPartner2(new Wife(requestedAttributes));

    getPartner1().configure(household);
    getPartner2().configure(household);

    if (getPartner1().getHouseholdRole() != null) {
      getPartner1().getHouseholdRole().setValue_String("W1");
    }
    if (getPartner2().getHouseholdRole() != null) {
      getPartner2().getHouseholdRole().setValue_String("W2");
    }

    getAllPersons().add(getPartner1());
    getAllPersons().add(getPartner2());
  }

  public void singleFemale() {
    if (RandomSingleton.getRandom().nextBoolean()) {
      getPartner1().deactivate();
    } else {
      getPartner2().deactivate();
    }
  }

  public void fixAgeOfPartners() {
    fixAgeOfPartnersForAdoption();
  }

  public void calculateStructureAndSize() {
    int i2 = 0, c;
    if (getPartner1().activated()) {
      i2++;
    }
    if (getPartner2().activated()) {
      i2++;
    }
    c = getNumberOfActiveChildren();

    setLivingArrangementStructure(String.format("0-%s-0-0-%s", i2, c));
    setLivingArrangementSize(i2 + c);
  }
}