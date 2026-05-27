package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.marriage;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.Husband;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.Wife;

import java.util.List;

public class MixedMarriage extends Marriage {

  public MixedMarriage(Household household, int maximumNumberOfChildren, List<String> requestedAttributes) {

    super(household, maximumNumberOfChildren, requestedAttributes);

    setPartner1(new Husband(requestedAttributes));
    setPartner2(new Wife(requestedAttributes));

    getPartner1().configure(household);
    getPartner2().configure(household);

    if (getPartner1().getHouseholdRole() != null) {
      getPartner1().getHouseholdRole().setValue_String("H");
    }
    if (getPartner2().getHouseholdRole() != null) {
      getPartner2().getHouseholdRole().setValue_String("W");
    }

    getAllPersons().add(getPartner1());
    getAllPersons().add(getPartner2());
  }

  @Override
  public void singleMale() {
    getPartner2().deactivate();
  }

  public void singleFemale() {
    getPartner1().deactivate();
  }

  public void fixAgeOfPartners() {
    fixAgeOfPartnersForMarriage();
  }

  public void calculateStructureAndSize() {
    int i1 = 0, i2 = 0, c;
    if (getPartner1().activated()) {
      i1++;
    }
    if (getPartner2().activated()) {
      i2++;
    }
    c = getNumberOfActiveChildren();

    setLivingArrangementStructure(String.format("%s-%s-0-0-%s", i1, i2, c));
    setLivingArrangementSize(i1 + i2 + c);
  }
}