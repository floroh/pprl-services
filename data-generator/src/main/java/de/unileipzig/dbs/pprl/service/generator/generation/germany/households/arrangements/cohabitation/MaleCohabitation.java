package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.cohabitation;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.MaleCompanion;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;

import java.util.List;

public class MaleCohabitation extends Cohabitation {

  public MaleCohabitation(Household household, int maximumNumberOfChildren, List<String> requestedAttributes) {

    super(household, maximumNumberOfChildren, requestedAttributes);

    setPartner1(new MaleCompanion(requestedAttributes));
    setPartner2(new MaleCompanion(requestedAttributes));

    getPartner1().configure(household);
    getPartner2().configure(household);

    if (getPartner1().getHouseholdRole() != null) {
      getPartner1().getHouseholdRole().setValue_String("mC1");
    }
    if (getPartner2().getHouseholdRole() != null) {
      getPartner2().getHouseholdRole().setValue_String("mC2");
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
    int i3 = 0, c;
    if (getPartner1().activated()) {
      i3++;
    }
    if (getPartner2().activated()) {
      i3++;
    }
    c = getNumberOfActiveChildren();

    setLivingArrangementStructure(String.format("0-0-%s-0-%s", i3, c));
    setLivingArrangementSize(i3 + c);
  }
}