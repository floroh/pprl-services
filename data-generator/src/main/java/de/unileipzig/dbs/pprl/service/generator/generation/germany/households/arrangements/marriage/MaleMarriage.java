package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.marriage;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.Husband;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;

import java.util.List;

public class MaleMarriage extends Marriage {

  public MaleMarriage(Household household, int maximumNumberOfChildren, List<String> requestedAttributes) {

    super(household, maximumNumberOfChildren, requestedAttributes);

    setPartner1(new Husband(requestedAttributes));
    setPartner2(new Husband(requestedAttributes));

    getPartner1().configure(household);
    getPartner2().configure(household);

    if (getPartner1().getHouseholdRole() != null) {
      getPartner1().getHouseholdRole().setValue_String("H1");
    }
    if (getPartner2().getHouseholdRole() != null) {
      getPartner2().getHouseholdRole().setValue_String("H2");
    }

    getAllPersons().add(getPartner1());
    getAllPersons().add(getPartner2());
  }

  public void singleMale() {
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
    int i1 = 0, c;
    if (getPartner1().activated()) {
      i1++;
    }
    if (getPartner2().activated()) {
      i1++;
    }
    c = getNumberOfActiveChildren();

    setLivingArrangementStructure(String.format("%s-0-0-0-%s", i1, c));
    setLivingArrangementSize(i1 + c);
  }
}