package de.unileipzig.dbs.pprl.service.generator.generation.germany.attributes.person;

import de.unileipzig.dbs.pprl.service.generator.generation.germany.randomgenerator.RandomSingleton;
import de.unileipzig.dbs.pprl.service.generator.generation.germany.records.Person;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import static java.time.Year.isLeap;

public class DateOfBirth extends PersonAttribute {

  Age age;
  Random r = RandomSingleton.getRandom();

  LocalDate today;
  int todayYear;
  int todayMonth;
  int todayDay;

  LocalDate tomorrow;
  int tomorrowYear;
  int tomorrowMonth;
  int tomorrowDay;

  /**
   * There are 2 possible years of birth, when an age is given. Year A is closer to this year,
   * year B is the year before year A.
   */
  int possibleYearOfBirthA;
  int possibleMonthOfBirthA;
  int possibleDayOfBirthA;
  LocalDate possibleDateOfBirthA;

  int possibleYearOfBirthB;
  int possibleMonthOfBirthB;
  int possibleDayOfBirthB;
  LocalDate possibleDateOfBirthB;

  LocalDate dateOfBirth;

  public DateOfBirth() {
    setEvaluationPriority(27);
    setAttributeName("DATE OF BIRTH");

    today = LocalDate.now();

    todayYear = today.getYear();
    todayMonth = today.getMonthValue();
    todayDay = today.getDayOfMonth();

    tomorrow = today.plusDays(1);

    tomorrowYear = tomorrow.getYear();
    tomorrowMonth = tomorrow.getMonthValue();
    tomorrowDay = tomorrow.getDayOfMonth();

  }

  //<editor-fold desc="Standard get and set methods.">
  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setAge(Age age) {
    this.age = age;
  }

  public void setToday(LocalDate today) {
    this.today = today;
  }
  //</editor-fold>

  public String toString() {
    return today.toString();
  }

  @Override
  public boolean connectInfluences(Person person) {
    if (person.getAge() == null) {
      person.setAge(new Age());
      person.getAuxiliaryList().add(person.getAge());
      this.age = person.getAge();
      return true;
    } else {
      this.age = person.getAge();
      return false;
    }
  }

  public void nextValue() {
    possibleYearOfBirthA = todayYear - Integer.parseInt(age.getValue_String());
    possibleYearOfBirthB = tomorrowYear - (Integer.parseInt(age.getValue_String()) + 1);

    if (!isLeap(todayYear)) {
      if (isLeap(possibleYearOfBirthB) && todayMonth == 2 && todayDay == 28) {
        possibleMonthOfBirthB = 2;
        possibleDayOfBirthB = 29;
      } else {
        possibleMonthOfBirthB = tomorrowMonth;
        possibleDayOfBirthB = tomorrowDay;
      }
      possibleMonthOfBirthA = todayMonth;
      possibleDayOfBirthA = todayDay;
    } else if (isLeap(todayYear)) {
      if (!isLeap(possibleYearOfBirthB) && !isLeap(possibleYearOfBirthA)) {
        if (todayMonth == 2 && todayDay == 29) {
          possibleMonthOfBirthA = 2;
          possibleDayOfBirthA = 28;
          possibleMonthOfBirthB = tomorrowMonth;
          possibleDayOfBirthB = tomorrowDay;
        } else if (todayMonth == 2 && todayDay == 28) {
          possibleMonthOfBirthB = 3;
          possibleDayOfBirthB = 1;
          possibleMonthOfBirthA = todayMonth;
          possibleDayOfBirthA = todayDay;
        } else {
          possibleMonthOfBirthB = tomorrowMonth;
          possibleDayOfBirthB = tomorrowDay;
          possibleMonthOfBirthA = todayMonth;
          possibleDayOfBirthA = todayDay;
        }
      } else if (isLeap(possibleYearOfBirthB)) {
        if (todayMonth == 2 && todayDay == 29) {
          possibleMonthOfBirthA = 2;
          possibleDayOfBirthA = 28;
        } else {
          possibleMonthOfBirthA = todayMonth;
          possibleDayOfBirthA = todayDay;
        }
        possibleMonthOfBirthB = tomorrowMonth;
        possibleDayOfBirthB = tomorrowDay;
      } else if (isLeap(possibleYearOfBirthA)) {
        if (todayMonth == 2 && todayDay == 28) {
          possibleMonthOfBirthB = 3;
          possibleDayOfBirthB = 1;
        } else {
          possibleMonthOfBirthB = tomorrowMonth;
          possibleDayOfBirthB = tomorrowDay;
        }
        possibleMonthOfBirthA = todayMonth;
        possibleDayOfBirthA = todayDay;
      }
    }
    possibleDateOfBirthA = LocalDate.of(possibleYearOfBirthA, possibleMonthOfBirthA, possibleDayOfBirthA);
    possibleDateOfBirthB = LocalDate.of(possibleYearOfBirthB, possibleMonthOfBirthB, possibleDayOfBirthB);

    dateOfBirth = possibleDateOfBirthB.plusDays(
            r.nextInt((int) ChronoUnit.DAYS.between(possibleDateOfBirthB, possibleDateOfBirthA) + 1));
    setValue_String(dateOfBirth.toString());
  }

  @Override
  public void nextValue_personInHousehold() {
    nextValue();
  }

  public void nextValue_personInFlatSharingCommunity() {
    nextValue();
  }
}