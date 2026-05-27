package de.unileipzig.dbs.pprl.service.generator.generation.germany.households;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.Attribute;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person.*;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.Constants;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.*;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.*;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.cohabitation.*;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.marriage.*;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.PersonAttributesComparator;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Household {

  private final int requestedNumberOfRecords;
  private final boolean headerIncluded;
  private final String destinationFolder;
  private final String fileName;

  /* All persons must use the same Person_Id object. */
  @Getter
  private final Person_Id referencePerson_Id;

  @Getter
  private final Household_Id household_id;
  @Getter
  private final HouseholdStructure householdStructure;
  @Getter
  private final HouseholdCounter hc;

  int maximumFlatSize = Constants.getMaximumFlatSharingSize();
  int maximumNumberOfChildren = Constants.getMaximumNumberOfChildren();
  @Setter
  @Getter
  double ratio_phase2_to_phase1 = Constants.getRatioPhase2ToPhase1();

  //<editor-fold desc="Random generators and their stored results">
  private final Random random = RandomSingleton.getRandom();

  /**
   * Random generator to determine the type of living arrangement and number of children in it at the same time.
   * There will be always at least 1 child.
   * Types of living arrangements:
   * 1 = mixed marriage
   * 2 = male marriage
   * 3 = female marriage
   * 4 = mixed cohabitation
   * 5 = male cohabitation
   * 6 = female cohabitation
   * 7 = single father
   * 8 = single mother
   */
  private final RandomGenerator typeOfLivingArrangement_and_numberOfChildrenGenerator;

  /**
   * Random generator determines 1 of the following possible types for single fathers:
   * 1 = mixed marriage
   * 2 = male marriage
   * 4 = mixed cohabitation
   * 5 = male cohabitation
   * One partner will be deactivated to get a single father with children.
   */
  private final RandomGenerator livingArrangementsWithSingleFatherAndChildren;

  /**
   * Random generator determines 1 of the following possible types for single mothers:
   * 1 = mixed marriage
   * 3 = female marriage
   * 4 = mixed cohabitation
   * 6 = female cohabitation
   */
  private final RandomGenerator livingArrangementsWithSingleMotherAndChildren;
  //</editor-fold>

  //<editor-fold desc="Class attributes which refer to pools.">
  @Getter
  private final Pool pool;
  private CensusTuple censusTupleFromPool;
  @Getter
  private String federalStateFromPool;
  @Getter
  private String genderFromPool;
  @Getter
  private int ageFromPool;

  @Setter
  @Getter
  private RelationshipTuplePool relationshipTuplePool;
  @Getter
  private RelationshipTuple relationshipTupleFromPool;
  @Setter
  private String federalState_fromRelationshipTuple;
  @Setter
  @Getter
  private int typeOfRelationship_fromRelationshipTuple;
  @Setter
  @Getter
  private int ageOfPartner1_fromRelationshipTuple;

  /* During phase 4: */
  /**
   * For every remained federal state in phase 4, there is a different micro pool.
   * All census tuples inside a micro pool have the same federal state.
   */
  @Getter
  private List<CensusTuple> selectedMicroPool;

  private final boolean surnameRequested;

  //<editor-fold desc="Class attributes for describing the address of the household">
  @Getter
  private final FederalState referenceFederalState;
  @Getter
  private final ZipCode referenceZipCode;
  @Getter
  private final Location referenceLocation;
  @Getter
  private final Street referenceStreet;
  @Getter
  private final List<Attribute> address = new ArrayList<>();
  //</editor-fold>

  //<editor-fold desc="Defining various types of forms, how people can live together in a household. ">
  /* FlatSharingCommunity: */
  @Getter
  private final FlatSharingCommunity flatSharingCommunity;

  /* Marriages: */
  private final MixedMarriage mixedMarriage;
  private final MaleMarriage maleMarriage;
  private final FemaleMarriage femaleMarriage;

  /* Cohabitation: */
  private final MixedCohabitation mixedCohabitation;
  private final MaleCohabitation maleCohabitation;
  private final FemaleCohabitation femaleCohabitation;

  /* This class attributes are used to select one of the above mentioned forms of how people live together. */
  private int[] typeOfLivingArrangement_and_numberOfChildren;
  @Getter
  int typeOfLivingArrangement;
  @Getter
  int numberOfChildren;
  @Getter
  private LivingArrangement selectedLivingArrangement;
  //</editor-fold>

  /* For Tests of phase 2 only: */
  List<RelationshipTuple> timesPartner1DidNotExistsInPool = new ArrayList<>();

  /**
   * Constructor
   *
   * @param requestedNumberOfRecords  Specifies the number of records that have to be produced.
   * @param requestedPersonAttributes Specifies all the attributes that are requested by the user.
   */
  public Household(
          List<String> requestedPersonAttributes,
          int requestedNumberOfRecords,
          boolean headerIncluded,
          String destinationFolder,
          String fileName) {

    this.requestedNumberOfRecords = requestedNumberOfRecords;
    this.headerIncluded = headerIncluded;
    this.destinationFolder = destinationFolder;
    this.fileName = fileName;

    referencePerson_Id = new Person_Id();
    household_id = new Household_Id();
    householdStructure = new HouseholdStructure();
    householdStructure.setHousehold(this);
    hc = new HouseholdCounter(requestedNumberOfRecords);

    typeOfLivingArrangement_and_numberOfChildren = new int[2];

    //<editor-fold desc="Initialisation of random generators">
    typeOfLivingArrangement_and_numberOfChildrenGenerator =
            RandomGeneratorBuilder.buildRandomGenerator(this, "LivingArrangementTypAndNumberOfChildren");

    livingArrangementsWithSingleFatherAndChildren =
            RandomGeneratorBuilder.buildRandomGenerator(this, "LivingArrangementsWithSingleFatherAndChildren");

    livingArrangementsWithSingleMotherAndChildren =
            RandomGeneratorBuilder.buildRandomGenerator(this, "LivingArrangementsWithSingleMotherAndChildren");
    //</editor-fold>

    //<editor-fold desc="Initialisation of pool">
    pool = new Pool(requestedNumberOfRecords);
    //</editor-fold>

    //<editor-fold desc="Configure the address for household.">
    /* Adding to the address list: */
    address.add(referenceFederalState = new FederalState());
    address.add(referenceZipCode = new ZipCode());
    address.add(referenceLocation = new Location());
    address.add(referenceStreet = new Street());

    /* Sorting of the attributes objects according to their evaluation priority: */
    address.sort(new PersonAttributesComparator());

    /* Setting of their influences:
     * (setCensusTuple for referenceFederalState during sampling from pool) */
    referenceZipCode.setFederalState(referenceFederalState);
    referenceLocation.setZipCode(referenceZipCode);
    referenceStreet.setZipCode(referenceZipCode);

    /* After adding to the address list, sorting and setting of the influences, the address for all household
     * members will be calculated as follow:
     * 1.) Based on the drawn CensusTuple from pool, the federal state is determined.
     * 2.) Based on that federal state, a appropriate zip code will be generated.
     * 3.) Based on that zip code, an appropriate location and street will be generated. */
    //</editor-fold>

    //<editor-fold desc="Initialisation of the different forms of living. ">
    flatSharingCommunity = new FlatSharingCommunity(this, maximumFlatSize, requestedPersonAttributes);

    mixedMarriage = new MixedMarriage(this, maximumNumberOfChildren, requestedPersonAttributes);
    maleMarriage = new MaleMarriage(this, maximumNumberOfChildren, requestedPersonAttributes);
    femaleMarriage = new FemaleMarriage(this, maximumNumberOfChildren, requestedPersonAttributes);
    mixedCohabitation = new MixedCohabitation(this, maximumNumberOfChildren, requestedPersonAttributes);
    maleCohabitation = new MaleCohabitation(this, maximumNumberOfChildren, requestedPersonAttributes);
    femaleCohabitation = new FemaleCohabitation(this, maximumNumberOfChildren, requestedPersonAttributes);
    //</editor-fold>


    /* Check with an arbitrary person, if surname attribute was requested. */
    surnameRequested = mixedMarriage.getPartner1().getSurname() != null;
  }

  public void setRelationshipTupleFromPool(RelationshipTuple relationshipTupleFromPool) {
    this.relationshipTupleFromPool = relationshipTupleFromPool;
  }

  /**
   * Based on a type of living arrangement, the respective LivingArrangement object is selected:
   * 1 = mixed marriage,
   * 2 = male marriage,
   * 3 = female marriage,
   * 4 = mixed cohabitation,
   * 5 = male cohabitation,
   * 6 = female cohabitation,
   * 7 = single father,
   * 8 = single mother.
   * For type 7, a selection between type 1,2,4,5 and
   * for type 8, a selection between type 1,3,4,6
   * is made and one of the partners is deactivated.
   *
   * @param typeOfLivingArrangement Value which specifies the type of living arrangement.
   */
  public void selectLivingArrangement(int typeOfLivingArrangement) {
    if (typeOfLivingArrangement == 1) {
      selectedLivingArrangement = mixedMarriage;
    } else if (typeOfLivingArrangement == 2) {
      selectedLivingArrangement = maleMarriage;
    } else if (typeOfLivingArrangement == 3) {
      selectedLivingArrangement = femaleMarriage;
    } else if (typeOfLivingArrangement == 4) {
      selectedLivingArrangement = mixedCohabitation;
    } else if (typeOfLivingArrangement == 5) {
      selectedLivingArrangement = maleCohabitation;
    } else if (typeOfLivingArrangement == 6) {
      selectedLivingArrangement = femaleCohabitation;
    } else if (typeOfLivingArrangement == 7) {
      /* single father, possible types: 1,2,4,5 */
      selectLivingArrangement(
              determineTypeOfLivingArrangement(livingArrangementsWithSingleFatherAndChildren));
      selectedLivingArrangement.singleMale();
    } else if (typeOfLivingArrangement == 8) {
      /*single mother, possible types: 1,3,4,6 */
      selectLivingArrangement(
              determineTypeOfLivingArrangement(livingArrangementsWithSingleMotherAndChildren));
      selectedLivingArrangement.singleFemale();
    } else {
      selectedLivingArrangement = null;
      System.out.println("Undefined type for living arrangement with children.");
    }
  }

  public void subtractFamilyMembersFromPool() {
    for (HouseholdMember hm : selectedLivingArrangement.getAllPersons()) {
      if (hm.activated()) {
        if (hm instanceof Child) {
          if (!pool.subtractFromPool(pool.getPool_1_children(), hm.getCensusTuple())) {
            /* Deactivate, if subtraction was not successful. */
            hm.deactivate();
          }
        } else {
          if (!pool.subtractFromPool(pool.getPool_1_adults(), hm.getCensusTuple())) {
            /* Deactivate, if subtraction was not successful. */
            hm.deactivate();
          }
        }
      }
    }
  }

  public void subtractPartnersFromPool(List<CensusTuple> poolList) {
    for (HouseholdMember hm : selectedLivingArrangement.getAllPersons()) {
      if (hm.activated()) {
        if (!pool.subtractFromPool(poolList, hm.getCensusTuple())) {
          /* Deactivate, if subtraction was not successful. */
          hm.deactivate();
        }
      }
    }
  }

  //<editor-fold desc="Methods for phase 1.">
  public CensusTuple getFirstCensusTupleFromCensusTuplePool() {
    /* Return the CensusTuple with youngest age, because the completeCensusTuplePool ist sorted by age.
     * This method is uses in a while loop, where it is already checked, whether the pool is empty or not.
     */
    return pool.getPool_1().getFirst();
  }

  public void setCensusTupleFromPool(CensusTuple censusTuple) {
    this.censusTupleFromPool = censusTuple;
    this.federalStateFromPool = censusTupleFromPool.getFederalState();
    this.genderFromPool = censusTupleFromPool.getGender();
    this.ageFromPool = censusTupleFromPool.getAge();

    /* Census tuple from pool determines the later address of the household. */
    this.referenceFederalState.setCensusTuple(censusTupleFromPool);
  }

  public int determineTypeOfLivingArrangement(RandomGenerator randomGenerator) {
    typeOfLivingArrangement = randomGenerator.next_int();
    return typeOfLivingArrangement;
  }

  /**
   * This method determines both the type of living arrangement and number of children, because there is a
   * correlation between both quantities.
   */
  public void determine_and_set_Type_Of_Living_Arrangement_And_Number_Of_Children() {
    typeOfLivingArrangement_and_numberOfChildren =
            typeOfLivingArrangement_and_numberOfChildrenGenerator.next_intArray();

    typeOfLivingArrangement = typeOfLivingArrangement_and_numberOfChildren[0];
    numberOfChildren = typeOfLivingArrangement_and_numberOfChildren[1];
  }
  //</editor-fold>

  //<editor-fold desc="Methods for phase 2.">
  public void deactivateAllChildren() {
    for (Child c : mixedMarriage.getChildren()) {
      c.deactivate();
    }
    for (Child c : maleMarriage.getChildren()) {
      c.deactivate();
    }
    for (Child c : femaleMarriage.getChildren()) {
      c.deactivate();
    }
    for (Child c : mixedCohabitation.getChildren()) {
      c.deactivate();
    }
    for (Child c : maleCohabitation.getChildren()) {
      c.deactivate();
    }
    for (Child c : femaleCohabitation.getChildren()) {
      c.deactivate();
    }
  }

  public void initialiseRelationshipTuplePool(int numberOfChildlessRelationships, List<CensusTuple> pool_2) {
    relationshipTuplePool = new RelationshipTuplePool(numberOfChildlessRelationships, pool_2);
  }

  public void setRelationshipTupleFromPool() {
    /* Order for the tuples is not considered in phase 2, always take tuple with index 0. */
    relationshipTupleFromPool = relationshipTuplePool.getPool().getFirst();
    federalState_fromRelationshipTuple = relationshipTupleFromPool.getFederalState();
    typeOfRelationship_fromRelationshipTuple = relationshipTupleFromPool.getTypeOfLivingArrangement();
    ageOfPartner1_fromRelationshipTuple = relationshipTupleFromPool.getAgeOfPartner1();
  }
  //</editor-fold>

  public void generateHouseholds() throws IOException {

    try (BufferedWriter logWriter = new BufferedWriter(
            new FileWriter(destinationFolder + fileName + ".csv"))
    ) {
      //<editor-fold desc="Write to log file - BEFORE PHASE 1.">
      logWriter.write("Requested number of records   : " + requestedNumberOfRecords);
      logWriter.newLine();
      logWriter.write(hc.printOutInitialisationInfo());
      logWriter.newLine();
      logWriter.write("************************* BEFORE PHASE 1 *************************");
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("People in pool_1_children: "
              + pool.getPoolFrequency(pool.getPool_1_children()));
      logWriter.newLine();
      logWriter.write("People in pool_1_adults: "
              + pool.getPoolFrequency(pool.getPool_1_adults()));
      logWriter.newLine();
      logWriter.write("People in pool_2: "
              + pool.getPoolFrequency(pool.getPool_2()));
      logWriter.newLine();
      logWriter.newLine();
      //</editor-fold>
      hc.setGeneratedRecordsInPhaseX(0);
      hc.setGeneratedHouseholdsInPhaseX(0);

      generateHouseholds_phase1();

      //<editor-fold desc="Write to log file - AFTER PHASE 1 / BEFORE PHASE 2.">
      logWriter.write("************************** AFTER PHASE 1 **************************");
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("People in pool_1_children: "
              + pool.getPoolFrequency(pool.getPool_1_children()));
      logWriter.newLine();
      logWriter.write("People in pool_1_adults: "
              + pool.getPoolFrequency(pool.getPool_1_adults()));
      logWriter.newLine();
      logWriter.write("People in pool_2: "
              + pool.getPoolFrequency(pool.getPool_2()));
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("Generated records: " + hc.getGeneratedRecordsInPhaseX());
      logWriter.newLine();
      logWriter.write("Generated households: " + hc.getGeneratedHouseholdsInPhaseX());
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write(hc.toString());
      logWriter.newLine();
      logWriter.newLine();
      //</editor-fold>
      hc.setGeneratedRecordsInPhaseX(0);
      hc.setGeneratedHouseholdsInPhaseX(0);

      generateHouseholds_phase2();

      //<editor-fold desc="Write to log file - AFTER PHASE 2 / BEFORE PHASE 3.">
      logWriter.write("************************** AFTER PHASE 2 **************************");
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("People in pool_2: "
              + pool.getPoolFrequency(pool.getPool_2()));
      logWriter.newLine();
      logWriter.write("People in pool_3: "
              + pool.getPoolFrequency(pool.getPool_3()));
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("Generated records: " + hc.getGeneratedRecordsInPhaseX());
      logWriter.newLine();
      logWriter.write("Generated households: " + hc.getGeneratedHouseholdsInPhaseX());
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write(hc.toString());
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("Planned households: " + hc.getPhase2_plannedHouseholds());
      logWriter.newLine();
      logWriter.write("Times, partner1 didn't exists in pool: " + hc.getPhase2_timesPartner1DidNotExistsInPool());
      logWriter.newLine();
      logWriter.write("Times, partner2 didn't exists in pool: " + hc.getPhase2_failedAttemptsToFindPartner2());
      logWriter.newLine();
      logWriter.newLine();
      //</editor-fold>
      hc.setGeneratedRecordsInPhaseX(0);
      hc.setGeneratedHouseholdsInPhaseX(0);

      generateHouseholds_phase3();

      //<editor-fold desc="Write to log file - AFTER PHASE 3 / BEFORE PHASE 4.">
      logWriter.write("************************** AFTER PHASE 3 **************************");
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("People in pool_3: "
              + pool.getPoolFrequency(pool.getPool_3()));
      logWriter.newLine();
      logWriter.write("People in pool_4: "
              + pool.getPool_4Frequency(pool.getPool_4()));
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("Generated records: " + hc.getGeneratedRecordsInPhaseX());
      logWriter.newLine();
      logWriter.write("Generated households: " + hc.getGeneratedHouseholdsInPhaseX());
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write(hc.toString());
      logWriter.newLine();
      logWriter.newLine();
      //</editor-fold>
      hc.setGeneratedRecordsInPhaseX(0);
      hc.setGeneratedHouseholdsInPhaseX(0);

      generateHouseholds_phase4();

      //<editor-fold desc="Write to log file - AFTER PHASE 4.">
      logWriter.write("************************** AFTER PHASE 4 **************************");
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("People in pool_4: "
              + pool.getPool_4Frequency(pool.getPool_4()));
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write("Generated records: " + hc.getGeneratedRecordsInPhaseX());
      logWriter.newLine();
      logWriter.write("Generated households: " + hc.getGeneratedHouseholdsInPhaseX());
      logWriter.newLine();
      logWriter.newLine();
      logWriter.write(hc.toString());

      logWriter.newLine();
      logWriter.newLine();
      //</editor-fold>
    }
  }

  public void generateHouseholds_phase1() throws IOException {

    /* START PHASE 1: Create households with at least 1 child and at least 1 parent.*/

    try (BufferedWriter recordWriter = new BufferedWriter(
            new FileWriter(destinationFolder + fileName + "-phase1.csv"))
    ) {
      if (headerIncluded) {
        recordWriter.write(mixedMarriage.getPartner1().attributeNamesToString());
        recordWriter.newLine();
      }

      while (!pool.getPool_1_children().isEmpty()) {

        /* Pull the most upper census tuple (= youngest person) out of the pool_1_children.
         * Extract the relevant data from this CensusTuple.
         */
        setCensusTupleFromPool(pool.getPool_1_children().getFirst());

        /* Determine the type of living arrangement and number of children at the same time. */
        determine_and_set_Type_Of_Living_Arrangement_And_Number_Of_Children();

        /* Select and set type of living arrangement. */
        selectLivingArrangement(typeOfLivingArrangement);


        /* At this point, if a single parent household was selected, 1 partner was deactivated. */


        /* Determine and set how many children live in this living arrangement. */
        selectedLivingArrangement.determineNumberOfChildren();

        /* Activate that number of children, deactivate the other children. */
        selectedLivingArrangement.activateChildren();

        /* Calculate the federal state, gender and age of all children. */
        selectedLivingArrangement.calculate_FederalState_Gender_Age_OfAllActivatedChildren();

        /* Calculate federal state and age of partner2 with the age of the oldest child. */
        selectedLivingArrangement.calculate_FederalState_Age_OfPartner2();

        /* Correct the age of partner2 and all children if necessary. */
        selectedLivingArrangement.correctAgeOfPartner2AndAllChildren();

        /* Calculate federal state and age of partner1. */
        selectedLivingArrangement.calculate_FederalState_Age_OfPartner1();

        /* For marriage: both partners must be at least 18.
         * For adoption: both partners must have a minimum age difference to the oldest child.
         */
        selectedLivingArrangement.fixAgeOfPartners();

        /* Determine, which and how many children live in the household.
         * Subtract Children who are not available.
         */
        if (selectedLivingArrangement.determineWhichAndHowManyChildrenLiveWithParents() == 0) {
          /* No child lives in this household. Ignore who parents are, build next household. */

          /* In case a partner inside a single parent household was deactivated. */
          selectedLivingArrangement.activateBothPartners();
          continue;
        }

        /* At this point, there is AT LEAST 1 child inside the household.
         * Check who of those calculated parents are available, if not available, deactivate.
         */
        if (selectedLivingArrangement.partnersAvailable() == 0) {

          /* Children have no parents. Move all active children to the pool_2. */
          selectedLivingArrangement.processChildrenWithoutParents();

        } else {
          /* At this point, there is at least 1 child and at least 1 parent available. */

          /* Subtract all remaining household members from the pool. */
          subtractFamilyMembersFromPool();

          /* The true household structure can now be calculated. After subtraction!!! - because in rare cases,
           * when there are same gender couples, their census tuples could be equal, and if they try to
           * subtract from the same tuple in pool_1 with frequency = 1 (they both passed the availability check),
           * subtraction could be unsuccessful for one of them (that person will be deactivated then). */
          householdStructure.determineHouseholdStructure();

          /* The referenceAddress and referenceSurname for all household members can now be calculated. */
          for (Attribute a : address) {
            a.nextValue();
          }

          /* Set the surnames for all persons. Check to avoid NullPointerEx. */
          if (surnameRequested) {
            selectedLivingArrangement.getSurnameConfigurator().giveSurnames();
          }

          /* Write household to file (and count generated records). */
          for (HouseholdMember member : selectedLivingArrangement.getAllPersons()) {
            if (member.activated()) {
              member.nextValues_personInHousehold();
              recordWriter.write(member.attributeValuesToString()); // uses the value_String field
              recordWriter.newLine();
              hc.countRecord();
            }
          }
          /* Counts. */
          hc.countHousehold();
          hc.generatedHouseholdOfSize(selectedLivingArrangement.getLivingArrangementSize());

          /* Increase value of household_id. */
          household_id.setValue_int(household_id.getValue_int() + 1);
        }
        /* In case 1 partner was deactivated in a single parents household. */
        selectedLivingArrangement.activateBothPartners();
      }
    }
    /* Move all tuples from pool_1_children and pool_1_adults to pool_2. */
    pool.moveFromArrayListToArrayList(pool.getPool_1_children(), pool.getPool_2());
    pool.moveFromArrayListToArrayList(pool.getPool_1_adults(), pool.getPool_2());
    pool.getPool_2().removeIf(censusTuple -> censusTuple.getFrequency() <= 0);
    System.out.println("Phase 1 completed.");
  }

  public void generateHouseholds_phase2() throws IOException {

    try (BufferedWriter recordWriter = new BufferedWriter(
            new FileWriter(destinationFolder + fileName + "-phase2.csv"))
    ) {
      if (headerIncluded) {
        recordWriter.write(mixedMarriage.getPartner1().attributeNamesToString());
        recordWriter.newLine();
      }

      /* Deactivate all children. */
      deactivateAllChildren();

      /* With the amount of generated households in phase1, determine how many households of childless
       * couples must now be generated in phase 2, which depends on a given ratio.
       */
      int numberOfChildlessRelationships =
              (int) Math.round(getRatio_phase2_to_phase1() * hc.getGeneratedHouseholdsSUM());
      hc.setPhase2_plannedHouseholds(numberOfChildlessRelationships);

      /* Create a pool that describes, how many childless couples must be generated. */
      initialiseRelationshipTuplePool(numberOfChildlessRelationships, pool.getPool_2());

      while (!relationshipTuplePool.getPool().isEmpty()) {

        /* Sample and set a tuple, order does not matter. */
        setRelationshipTupleFromPool();

        /* Select type of living arrangement dependent on this Relationship tuple. */
        selectLivingArrangement(typeOfRelationship_fromRelationshipTuple);

        /* Set federal state and age for partner1, gender is already given with the type of living arrangement. */
        selectedLivingArrangement.getPartner1().getCensusTuple().setFederalState(
                federalState_fromRelationshipTuple);
        selectedLivingArrangement.getPartner1().getCensusTuple().setAge(
                ageOfPartner1_fromRelationshipTuple);

        /* Check if such a census tuple for partner1 exists in the census tuple pool.
         * If no, remove the recently drawn relationship tuple, if yes, calculate partner2.
         */
        if (!pool.availableInPool(
                pool.getPool_2(),
                selectedLivingArrangement.getPartner1().getCensusTuple())) {
          /* Tuple does not exist. */
          hc.timesPartner1DidNotExistsInPool(relationshipTuplePool.getPool().getFirst().getFrequency());

          /* For Testing, list 'timesPartner1DidNotExistsInPool' can be deleted, but
           * tuple MUST be removed from relationshipTuplePool!
           */
          timesPartner1DidNotExistsInPool.add(relationshipTuplePool.getPool().removeFirst());

        } else {
          /* Tuple exists. Calculate partner2, age of partner2 will be minimum 18. */
          selectedLivingArrangement.getPartner2().getCensusTuple()
                  .setFederalState(federalState_fromRelationshipTuple);
          selectedLivingArrangement.setAgeOfPartner2_DependingOnAgeOfPartner1();

          /* Search if census tuple of partner2 exists inside census tuple pool. */
          if (pool.availableInPool(
                  pool.getPool_2(),
                  selectedLivingArrangement.getPartner2().getCensusTuple())) {
            /* Partner2 exists too. */

            /* IN SAME GENDER RELATION SHIPS, PARTNERS CAN HAVE THE SAME CENSUS TUPLE! THIS CAN LEAD TO
             * THE GENERATION OF MORE RECORDS THEN THE SUBTRACTION FROM THE POOL. THIS CASE MUST
             * BE AVOIDED:
             */
            if (selectedLivingArrangement.getPartner1().getCensusTuple().equals(
                    selectedLivingArrangement.getPartner2().getCensusTuple())) {
              int index = pool.getPool_2().indexOf(
                      selectedLivingArrangement.getPartner1().getCensusTuple());
              if (pool.getPool_2().get(index).getFrequency() <= 1) {
                continue;
              }
            }
            /* Subtract both partners from the right pool. */
            subtractPartnersFromPool(pool.getPool_2());

            /* Now household can be generated. */
            householdStructure.determineHouseholdStructure();

            /* The referenceAddress and referenceSurname for all household members can now be calculated. */
            referenceFederalState.setCensusTuple(selectedLivingArrangement.getPartner1().getCensusTuple());
            for (Attribute a : address) {
              a.nextValue();
            }

            /* Set the surnames for all persons. Check to avoid NullPointerEx. */
            if (surnameRequested) {
              selectedLivingArrangement.getSurnameConfigurator().giveSurnames();
            }

            /* Write household to file (and count generated records). */
            for (HouseholdMember member : selectedLivingArrangement.getAllPersons()) {
              if (member.activated()) {
                member.nextValues_personInHousehold();
                recordWriter.write(member.attributeValuesToString());
                recordWriter.newLine();
                hc.countRecord();
              }
            }
            /* Counts. */
            hc.countHousehold();
            hc.generatedHouseholdOfSize(selectedLivingArrangement.getLivingArrangementSize());

            /* Increase value of household_id. */
            household_id.setValue_int(household_id.getValue_int() + 1);

          } else {
            hc.increaseFailedAttemptsToFindPartner2();
          }
          /* Frequency of this relationship is reduced, too. */
          relationshipTuplePool.reduceFrequency(relationshipTupleFromPool);
        }
      }
    }
    /* Divide census tuple pool into age groups, prepare for phase 3. */
    pool.buildPool_3();
    System.out.println("Phase 2 completed.");
  }

  public void generateHouseholds_phase3() throws IOException {

    try (BufferedWriter recordWriter = new BufferedWriter(
            new FileWriter(destinationFolder + fileName + "-phase3.csv"))) {
      if (headerIncluded) {
        recordWriter.write(mixedMarriage.getPartner1().attributeNamesToString());
        recordWriter.newLine();
      }

      /* Deactivate all flat mates, except one. */
      for (int i = 0; i < flatSharingCommunity.getFlatmates().length; i++) {
        if (i == 0) {
          flatSharingCommunity.getFlatmates()[i].activate();
        } else {
          flatSharingCommunity.getFlatmates()[i].deactivate();
        }
      }

      /* Define fields, that are needed during every loop. */
      String ageGroup;
      HashMap<String, List<CensusTuple>> ageGroupMap = pool.getPool_3();
      List<CensusTuple> tuplesInThatAgeGroup;
      int listSize;
      int index;

      /* While there are no more tuples (keys) inside the map, or as long as more single households are needed. */
      while (!ageGroupMap.isEmpty() && hc.getPlannedSizes().get(1) > 0) {

        /* Search a census tuple in this map, based on the probabilities for age groups of one person households. */
        ageGroup = pool.getAgeGroupsOfOnePersonHouseholdsGenerator().next_String();

        if (ageGroupMap.containsKey(ageGroup)) {

          /* Get a census tuple randomly out of that age group and set it as censusTupleFromPool. */
          tuplesInThatAgeGroup = pool.getPool_3().get(ageGroup);
          listSize = tuplesInThatAgeGroup.size();
          index = random.nextInt(listSize);
          setCensusTupleFromPool(tuplesInThatAgeGroup.get(index));

          /* Set federal state, gender, age of the single person accordingly. */
          flatSharingCommunity.calculate_FederalState_Gender_Age_OfYoungestFlatMate(
                  flatSharingCommunity.getFlatmates()[0]);

          /* Subtract that tuple from the pool (removes tuple, if frequency is 0). */
          pool.subtractFromPool(tuplesInThatAgeGroup, censusTupleFromPool);

          /* Remove key (age group), if list has size 0 (has no census tuples). */
          if (tuplesInThatAgeGroup.isEmpty()) {
            ageGroupMap.remove(ageGroup);
          }

          householdStructure.determineHouseholdStructure_FlatSharingCommunity();

          /* The referenceAddress can now be calculated. */
          for (Attribute a : address) {
            a.nextValue();
          }

          /* Calculate other attributes with random generator and write to file. */
          flatSharingCommunity.getFlatmates()[0].nextValues_personInFlatSharingCommunity();
          recordWriter.write(flatSharingCommunity.getFlatmates()[0].attributeValuesToString());
          recordWriter.newLine();

          hc.countRecord();
          hc.countHousehold();

          hc.generatedHouseholdOfSize(flatSharingCommunity.getFlatSize());

          /* Increase value of household_id. */
          household_id.setValue_int(household_id.getValue_int() + 1);
        }

        /* If key doesn't exist, update the random generator. */
        else {
          pool.getAgeGroupsOfOnePersonHouseholdsGenerator().update(ageGroup);
        }
      }
    }
    /* Build pool_4 and pool_4_auxiliary. */
    pool.buildPool_4();
    System.out.println("Phase 3 completed.");
  }

  public void generateHouseholds_phase4() throws IOException {

    try (BufferedWriter recordWriter = new BufferedWriter(
            new FileWriter(destinationFolder + fileName + "-phase4.csv"))) {
      if (headerIncluded) {
        recordWriter.write(mixedMarriage.getPartner1().attributeNamesToString());
        recordWriter.newLine();
      }

      /* Calculate, how many households of each size, still has to be generated. */
      hc.calculateStillNeededSizes();

      int greatestNeedHouseholdSize;
      int pool_4_size;
      int pool_4_index;

      /* As long as there are microPools (tuples) inside pool_4 and pool_4_auxiliary. */
      while (!(pool.getPool_4Frequency(pool.getPool_4()) <= 0)
              || !(pool.getPool_4Frequency(pool.getPool_4_auxiliary()) <= 0)) {

        /* Do households still need to be generated for any household size? */
        if (hc.getPlannedSizes().isEmpty()) {

          /* Generate for the rest special one-person-households with household structure 'R'. */
          householdStructure.setValue_String("R");

          /* Using FlatSharingCommunity. */
          for (int i = 0; i < flatSharingCommunity.getFlatmates().length; i++) {
            if (i == 0) {
              flatSharingCommunity.getFlatmates()[i].activate();
            } else {
              flatSharingCommunity.getFlatmates()[i].deactivate();
            }
          }
          /* For  counting, flatSize is always 1. */
          flatSharingCommunity.calculateFlatSize();

          /* Add all microPools to pool_4. */
          pool.getPool_4().addAll(pool.getPool_4_auxiliary());
          pool.getPool_4_auxiliary().clear();

          for (List<CensusTuple> microPool : pool.getPool_4()) {
            for (CensusTuple tuple : microPool) {
              while (tuple.getFrequency() > 0) {

                flatSharingCommunity.getFlatmates()[0].setCensusTuple(tuple);

                referenceFederalState.setCensusTuple(tuple);
                for (Attribute a : address) {
                  a.nextValue();
                }

                flatSharingCommunity.getFlatmates()[0].nextValues_personInFlatSharingCommunity();
                recordWriter.write(flatSharingCommunity.getFlatmates()[0].attributeValuesToString());
                recordWriter.newLine();

                hc.countRecord();
                hc.countHousehold();
                hc.generatedHouseholdOfSize(flatSharingCommunity.getFlatSize());

                /* Reduce frequency, otherwise endless loop. */
                tuple.reduceFrequencyBy(1);

                /* Increase value of household_id. */
                household_id.setValue_int(household_id.getValue_int() + 1);
              }
            }
          }
          pool.getPool_4().clear();
          break;
        }

        /* Get greatest, still needed household size. */
        greatestNeedHouseholdSize =
                Collections.max(hc.getPlannedSizes().entrySet(), Map.Entry.comparingByKey()).getKey();

        /* Are there still microPools inside pool_4? */
        if (!pool.getPool_4().isEmpty()) {

          /* Select a microPool inside pool_4 randomly. */
          pool_4_size = pool.getPool_4().size();
          pool_4_index = random.nextInt(pool_4_size);
          selectedMicroPool = pool.getPool_4().get(pool_4_index);

          /* Does the selected micro pool have a minimum number of tuples inside it? */
          if (pool.getPoolFrequency(selectedMicroPool) < greatestNeedHouseholdSize) {

            /* Not enough tuples to generate a household of this size in that federal state. */

            /* Move the selected micro pool to pool_4_auxiliary. */
            pool.getPool_4_auxiliary().add(pool.getPool_4().remove(pool_4_index));

            continue;
          }

          /* Enough tuples in selected micro pool.
           * Generate a flat-sharing community household of size = 'greatestNeedHouseholdSize'.
           * MicroPool is automatically reduced by 'greatestNeedHouseholdSize' tuples.
           */
          flatSharingCommunity.generateFlat(greatestNeedHouseholdSize);

          /* Completely remove microPool, if frequency is 0. */
          if (pool.getPoolFrequency(selectedMicroPool) <= 0) {
            pool.getPool_4().remove(pool_4_index);
          }

          /* Calculate other attribute values and write flat to file: */
          householdStructure.determineHouseholdStructure_FlatSharingCommunity();
          referenceFederalState.setCensusTuple(flatSharingCommunity.getFlatmates()[0].getCensusTuple());
          for (Attribute a : address) {
            a.nextValue();
          }
          for (FlatMate mate : flatSharingCommunity.getFlatmates()) {
            if (mate.activated()) {
              mate.nextValues_personInFlatSharingCommunity();
              recordWriter.write(mate.attributeValuesToString());
              recordWriter.newLine();

              hc.countRecord();
            }
          }
          hc.countHousehold();

          /* FlatSize was calculated during the determination of the household structure.
           * One less household of this size must be generated.
           */
          hc.generatedHouseholdOfSize(flatSharingCommunity.getFlatSize());

          /* Increase value of household_id. */
          household_id.setValue_int(household_id.getValue_int() + 1);

          /* More households of size = 'greatestNeedHouseholdSize' needed? */
          if (hc.getPlannedSizes().containsKey(greatestNeedHouseholdSize)
                  && hc.getPlannedSizes().get(greatestNeedHouseholdSize) > 0) {
            continue;
          }
        }
        /* Remove the greatest needed household size, no more households of this size will be generated.*/
        hc.getPlannedSizes().remove(greatestNeedHouseholdSize);

        /* Move all microPools in pool_4_auxiliary to pool_4. */
        pool.getPool_4().addAll(pool.getPool_4_auxiliary());
        pool.getPool_4_auxiliary().clear();
      }
    }
    System.out.println("Phase 4 completed.");
  }
}