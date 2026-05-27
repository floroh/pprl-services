package de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person.Surname;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.builders.RandomGeneratorBuilder;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.roles.Child;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.cohabitation.Cohabitation;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.households.arrangements.marriage.Marriage;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomGenerator;

public class SurnameConfigurator {

  private final RandomGenerator surnameGenerator;
  private final RandomGenerator variationForMarriageGenerator;
  private final RandomGenerator variationForCohabitationGenerator;

  LivingArrangement livingArrangement;

  //<editor-fold desc="Old surnames of partner1 and partner2.">
  String N1;
  String N2;
  //</editor-fold>

  int variationForMarriage;
  int variationForCohabitation;

  public SurnameConfigurator(LivingArrangement l) {
    //<editor-fold desc="Initialise random generators.">
    surnameGenerator = RandomGeneratorBuilder.buildRandomGenerator(new Surname());

    variationForMarriageGenerator =
            RandomGeneratorBuilder.buildRandomGenerator(this, "SurnameVariationsMarriage");

    variationForCohabitationGenerator =
            RandomGeneratorBuilder.buildRandomGenerator(this, "SurnameVariationsForCohabitation");
    //</editor-fold>

    livingArrangement = l;
  }

  public void generateTwoSurnames() {
    N1 = surnameGenerator.next_String();
    do {
      N2 = surnameGenerator.next_String();
    } while ((N1).equals(N2));
  }

  public void giveSurnames() {

    if (livingArrangement instanceof Marriage) {
      giveSurnamesMarriage((Marriage) livingArrangement);
    } else if (livingArrangement instanceof Cohabitation) {
      giveSurnamesCohabitation((Cohabitation) livingArrangement);
    } else {
      System.out.println("Wrong type for selected living arrangement.");
    }
  }

  public void giveSurnamesMarriage(Marriage marriage) {

    variationForMarriage = variationForMarriageGenerator.next_int();

    /* Variations described as: (name of partner1, name of partner 2, name for all children). */

    /* Variation 1 is the most common case: (N1, N1, N1). */
    if (variationForMarriage == 1) {
      N1 = surnameGenerator.next_String();
      marriage.getPartner1().getSurname().setValue_String(N1);
      marriage.getPartner2().getSurname().setValue_String(N1);
      for (Child c : marriage.getChildren()) {
        if (c.activated()) {
          c.getSurname().setValue_String(N1);
        }
      }
      return;
    }

    generateTwoSurnames();

    /* (N1, N2, N1) */
    if (variationForMarriage == 2) {
      marriage.getPartner1().getSurname().setValue_String(N1);
      marriage.getPartner2().getSurname().setValue_String(N2);
      for (Child c : marriage.getChildren()) {
        if (c.activated()) {
          c.getSurname().setValue_String(N1);
        }
      }
    }

    /* (N1, N2, N2) */
    else if (variationForMarriage == 3) {
      marriage.getPartner1().getSurname().setValue_String(N1);
      marriage.getPartner2().getSurname().setValue_String(N2);
      for (Child c : marriage.getChildren()) {
        if (c.activated()) {
          c.getSurname().setValue_String(N2);
        }
      }
    }

    /* (N1, N1-N2, N1) */
    else if (variationForMarriage == 4) {
      marriage.getPartner1().getSurname().setValue_String(N1);
      marriage.getPartner2().getSurname().setValue_String(N1 + "-" + N2);
      for (Child c : marriage.getChildren()) {
        if (c.activated()) {
          c.getSurname().setValue_String(N1);
        }
      }
    }

    /* (N1, N2-N1, N1) */
    else if (variationForMarriage == 5) {
      marriage.getPartner1().getSurname().setValue_String(N1);
      marriage.getPartner2().getSurname().setValue_String(N2 + "-" + N1);
      for (Child c : marriage.getChildren()) {
        if (c.activated()) {
          c.getSurname().setValue_String(N1);
        }
      }
    }

    /* (N1-N2, N2, N2) */
    else if (variationForMarriage == 6) {
      marriage.getPartner1().getSurname().setValue_String(N1 + "-" + N2);
      marriage.getPartner2().getSurname().setValue_String(N2);
      for (Child c : marriage.getChildren()) {
        if (c.activated()) {
          c.getSurname().setValue_String(N2);
        }
      }
    }

    /* (N2-N1, N2, N2) */
    else if (variationForMarriage == 7) {
      marriage.getPartner1().getSurname().setValue_String(N2 + "-" + N1);
      marriage.getPartner2().getSurname().setValue_String(N2);
      for (Child c : marriage.getChildren()) {
        if (c.activated()) {
          c.getSurname().setValue_String(N2);
        }
      }
    } else {
      System.out.println("This surname variation for marriage does not exist.");
    }
  }

  public void giveSurnamesCohabitation(Cohabitation cohabitation) {

    generateTwoSurnames();

    variationForCohabitation = variationForCohabitationGenerator.next_int();

    cohabitation.getPartner1().getSurname().setValue_String(N1);
    cohabitation.getPartner2().getSurname().setValue_String(N2);

    if (variationForCohabitation == 1) {
      for (Child c : cohabitation.getChildren()) {
        if (c.activated()) {
          c.getSurname().setValue_String(N1);
        }
      }
    } else if (variationForCohabitation == 2) {
      for (Child c : cohabitation.getChildren()) {
        if (c.activated()) {
          c.getSurname().setValue_String(N2);
        }
      }
    } else {
      System.out.println("This surname variation for cohabitation does not exist.");
    }
  }
}