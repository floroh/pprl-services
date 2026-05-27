package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.Household;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

import java.util.List;

public abstract class HouseholdMember extends Person {

  private boolean activated = true;

  public HouseholdMember(List<String> requestedAttributes) {

    super(requestedAttributes);
  }

  public boolean activated() {
    return activated;
  }

  public void activate() {
    activated = true;
  }

  public void deactivate() {
    activated = false;
  }

  /**
   * Enables all household members to have
   * 1.) same address,
   * 2.) same Record_Id,
   * 3.) same HouseholdStructure.
   *
   * @param household The household instance, that contains those objects.
   */
  public void configure(Household household) {
    /* Enable all household members to have same address: */
    if (this.getFederalState() != null) {
      getFederalState().setReferenceFederalState(household.getReferenceFederalState());
    }
    if (this.getZipCode() != null) {
      getZipCode().setReferenceZipCode(household.getReferenceZipCode());
    }
    if (this.getLocation() != null) {
      getLocation().setReferenceLocation(household.getReferenceLocation());
    }
    if (this.getStreet() != null) {
      getStreet().setReferenceStreet(household.getReferenceStreet());
    }

    /* All household members use the same Record_Id object and increase the value (ID) of the same object. */
    if (this.getPerson_id() != null) {
      exchangePersonIdInRequestedAttributesList(household.getReferencePerson_Id());
      exchangePersonIdInAuxiliaryList(household.getReferencePerson_Id());
    }
    if (this.getPerson_id() != null) {
      setPerson_id(household.getReferencePerson_Id());
    }

    /* Every household member must have the same marking, in which kind of household structure he/she lives
     * and must have the same household ID.
     */
    if (this.getHouseholdStructure() != null) {
      exchangeObjectInListOfAttributes(getRequestedAttributes(), getHouseholdStructure(), household.getHouseholdStructure());
      setHouseholdStructure(household.getHouseholdStructure());
      //getRequestedAttributes().add(household.getHouseholdStructure());
    }
    if (this.getHousehold_id() != null) {
      exchangeObjectInListOfAttributes(getRequestedAttributes(), getHousehold_id(), household.getHousehold_id());
      setHousehold_id(household.getHousehold_id());
      //getRequestedAttributes().add(household.getHousehold_id());
    }
  }
}