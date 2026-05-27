package de.unileipzig.dbs.pprl.service.generator.generation.germany.records;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.Attribute;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person.*;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.HouseholdRole;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.HouseholdStructure;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Person extends Record {

  Person_Id person_id;
  CensusTuple censusTuple;
  FederalState federalState;
  Gender gender;
  Age age;
  DateOfBirth dateOfBirth;
  PlaceOfBirth_FederalState placeOfBirth_FederalState;
  PlaceOfBirth placeOfBirth;
  Forename forename;
  ZipCode zipCode;
  Location location;
  Street street;
  Surname surname;

  HouseholdRole householdRole;
  HouseholdStructure householdStructure;
  Household_Id household_id;

  public Person(List<String> requestedAttributesFromUser) {

    super();

    for (String requestedAttribute : requestedAttributesFromUser) {

      if (requestedAttribute.equalsIgnoreCase("PersonId")) {
        setPerson_id(new Person_Id());
        getRequestedAttributes().add(getPerson_id());

      } else if (requestedAttribute.equalsIgnoreCase("CensusTuple")) {
        setCensusTuple(new CensusTuple());
        getRequestedAttributes().add(getCensusTuple());

      } else if (requestedAttribute.equalsIgnoreCase("FederalState")) {
        setFederalState(new FederalState());
        getRequestedAttributes().add(getFederalState());

      } else if (requestedAttribute.equalsIgnoreCase("Gender")) {
        setGender(new Gender());
        getRequestedAttributes().add(getGender());

      } else if (requestedAttribute.equalsIgnoreCase("Age")) {
        setAge(new Age());
        getRequestedAttributes().add(getAge());

      } else if (requestedAttribute.equalsIgnoreCase("DateOfBirth")) {
        setDateOfBirth(new DateOfBirth());
        getRequestedAttributes().add(getDateOfBirth());

      } else if (requestedAttribute.equalsIgnoreCase("Surname")) {
        setSurname(new Surname());
        getRequestedAttributes().add(getSurname());

      } else if (requestedAttribute.equalsIgnoreCase("Forename")) {
        setForename(new Forename());
        getRequestedAttributes().add(getForename());

      } else if (requestedAttribute.equalsIgnoreCase("ZipCode")) {
        setZipCode(new ZipCode());
        getRequestedAttributes().add(getZipCode());

      } else if (requestedAttribute.equalsIgnoreCase("Location")) {
        setLocation(new Location());
        getRequestedAttributes().add(getLocation());

      } else if (requestedAttribute.equalsIgnoreCase("Street")) {
        setStreet(new Street());
        getRequestedAttributes().add(getStreet());

      } else if (requestedAttribute.equalsIgnoreCase("PlaceOfBirth-FederalState")) {
        setPlaceOfBirth_FederalState(new PlaceOfBirth_FederalState());
        getRequestedAttributes().add(getPlaceOfBirth_FederalState());

      } else if (requestedAttribute.equalsIgnoreCase("PlaceOfBirth")) {
        setPlaceOfBirth(new PlaceOfBirth());
        getRequestedAttributes().add(getPlaceOfBirth());

      } else if (requestedAttribute.equalsIgnoreCase("HouseholdRole")) {
        setHouseholdRole(new HouseholdRole());
        getRequestedAttributes().add(getHouseholdRole());

      } else if (requestedAttribute.equalsIgnoreCase("HouseholdStructure")) {
        setHouseholdStructure(new HouseholdStructure());
        getRequestedAttributes().add(getHouseholdStructure());

      } else if (requestedAttribute.equalsIgnoreCase("HouseholdId")) {
        setHousehold_id(new Household_Id());
        getRequestedAttributes().add(getHousehold_id());

      } else {
        System.out.printf("'%s' is not an allowed attribute. \n", requestedAttribute);
        return;
      }
    }

    configureDependencies();
  }

  /**
   * This method connects those attribute objects, that are dependant on each other.
   */
  public void configureDependencies() {

    /* Add all attributes, which where requested, to the auxiliary list. */
    getAuxiliaryList().addAll(getRequestedAttributes());

    /* Add additional attributes to the auxiliary list, if the requested attributes
     * directly/indirectly depend on them.
     */
    boolean iterationWithAttributeAdded = true;
    while (iterationWithAttributeAdded) {
      iterationWithAttributeAdded = false;
      int listSize = getAuxiliaryList().size();
      for (int i = 0; i < listSize; i++) {
        if (getAuxiliaryList().get(i).connectInfluences(this)) {
          iterationWithAttributeAdded = true;
        }
      }
    }
    getAuxiliaryList().sort(new PersonAttributesComparator());
  }

  /**
   * Method is used, when household structures are requested. Used during phase 1 & 2 instead of the nextValue()
   * method. Will avoid the generation of separate address and family names for all household members in phase 1 & 2.
   */
  public void nextValues_personInHousehold() {
    for (Attribute a : getAuxiliaryList()) {
      a.nextValue_personInHousehold();
    }
  }

  /**
   * Method is used, when household structures are requested. Used during phase 1 & 2 instead of the nextValue()
   * method. Will avoid the generation of separate address for all household members in phase 1 & 2.
   */
  public void nextValues_personInFlatSharingCommunity() {
    for (Attribute a : getAuxiliaryList()) {
      a.nextValue_personInFlatSharingCommunity();
    }
  }

  /**
   * Replace the old Record_Id object of the requestedAttributes List with a new one. Method is used by a
   * Household object, to avoid that every household member increases their own Record_Id objects. Instead,
   * only one Record_Id is needed, that is increased by all household members.
   *
   * @param new_person_id The new Record_id object which replaces the old one.
   */
  public void exchangePersonIdInRequestedAttributesList(Person_Id new_person_id) {
    int i = getRequestedAttributes().indexOf(person_id);
    getRequestedAttributes().set(i, new_person_id);
  }

  /**
   * Replace the old Record_Id object of the auxiliaryList with a new one. Method is used by a
   * Household object, to avoid that every household member increases their own Record_Id objects. Instead,
   * only one Record_Id is needed, that is increased by all household members.
   *
   * @param new_person_id The new Record_id object which replaces the old one.
   */
  public void exchangePersonIdInAuxiliaryList(Person_Id new_person_id) {
    int i = getAuxiliaryList().indexOf(person_id);
    getAuxiliaryList().set(i, new_person_id);
  }

  public void exchangeObjectInListOfAttributes(List<Attribute> attributes, PersonAttribute oldAttribute, PersonAttribute newAttribute) {
    int i = attributes.indexOf(oldAttribute);
    attributes.set(i, newAttribute);
  }

}