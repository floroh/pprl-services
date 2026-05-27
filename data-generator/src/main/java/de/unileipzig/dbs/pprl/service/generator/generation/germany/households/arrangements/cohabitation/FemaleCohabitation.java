package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.cohabitation;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.FemaleCompanion;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;

import java.util.List;

public class FemaleCohabitation extends Cohabitation {

  public FemaleCohabitation(Household household, int maximumNumberOfChildren, List<String> requestedAttributes) {

    super(household, maximumNumberOfChildren, requestedAttributes);

    setPartner1(new FemaleCompanion(requestedAttributes));
    setPartner2(new FemaleCompanion(requestedAttributes));

    getPartner1().configure(household);
    getPartner2().configure(household);

    if (getPartner1().getHouseholdRole() != null) {
      getPartner1().getHouseholdRole().setValue_String("fC1");
    }
    if (getPartner2().getHouseholdRole() != null) {
      getPartner2().getHouseholdRole().setValue_String("fC2");
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
    int i4 = 0, c;
    if (getPartner1().activated()) {
      i4++;
    }
    if (getPartner2().activated()) {
      i4++;
    }
    c = getNumberOfActiveChildren();
    setLivingArrangementStructure(String.format("0-0-0-%s-%s", i4, c));
    setLivingArrangementSize(i4 + c);
  }
}