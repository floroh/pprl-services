package de.unileipzig.dbs.pprl.service.dataowner.modifier;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.core.common.selector.AttributeIsIn;
import de.unileipzig.dbs.pprl.core.common.selector.AttributePresent;
import de.unileipzig.dbs.pprl.core.common.selector.InverseSelector;
import de.unileipzig.dbs.pprl.core.common.selector.SelectAll;
import de.unileipzig.dbs.pprl.core.common.selector.SelectRandom;
import de.unileipzig.dbs.pprl.core.common.selector.Selector;
import de.unileipzig.dbs.pprl.core.common.selector.SelectorCombination;
import de.unileipzig.dbs.pprl.core.common.selector.YearOfBirthSelector;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.attribute.*;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.dataprovider.DefaultGeneratorDataProvider;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.record.*;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.dataprovider.GeneratorDataProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DataSetModifierConfigProvider {

  public enum GeneratorConfigTag {
    EXACT_DUPLICATE,
    SIMILAR_BUT_OTHER_PLACE,
    SIMILAR_AND_SAME_PLACE,
    NAMECHANGE_BUT_BIRTHNAME,
    MOVE_BUT_BIRTHPLACE,
    NEW_LAST_NAME,
    DOUBLE_FIRST_NAME,
    DOUBLE_LAST_NAME,
    MOVED,
    EMPTY_ADDRESS,
    EMPTY_POB,
    WRONG_YOB,
    EXCHANGED_FIRST_LAST_NAME,
    CHANGE_BIRTH_NAME_PLACE,
    ADD_BIRTH_NAME_PLACE,
    TYPO_NAME,
    TYPO_FN_COMMON_LN,
    TYPO_FN_RARE_LN,
    TYPO_FN_LN_CITY,
    RARE_NAME_MOVE,
    COMMON_NAME_MOVE,
    RARE_NAME_MISSING_ADDR,
    COMMON_NAME_MISSING_ADDR,
    MARRIAGE_RARE_FIRSTNAME_AND_ADDR,
    MARRIAGE_COMMON_FIRSTNAME_AND_ADDR,
    SAME_HOUSEHOLD,
    RARE_DIFFERENT_NAME,
    COMMON_DIFFERENT_NAME
  }

  private GeneratorDataProvider dataProvider;

  public DataSetModifierConfigProvider() {
    this(new DefaultGeneratorDataProvider());
//    this(new AttributeFrequencyLookupBasedDataProvider());
  }

  public DataSetModifierConfigProvider(
    GeneratorDataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  public DataSetModifierConfig nonModifier() {
    return DataSetModifierConfig.builder()
      .tag(GeneratorConfigTag.EXACT_DUPLICATE.name())
      .isTrueDuplicate(true)
      .build();
  }

  public DataSetModifierConfig similarButOtherPlace(boolean similarFirstName) {
    List<String> firstNames = dataProvider.getAllValues(PersonalAttributeType.FIRSTNAME);
    List<String> lastNames = dataProvider.getAllValues(PersonalAttributeType.LASTNAME);
    List<String> plz = dataProvider.getAllValues(PersonalAttributeType.PLZ);
    List<String> city = dataProvider.getAllValues(PersonalAttributeType.CITY);
    DateTypoModifier yearModifier = new DateTypoModifier("y");
    yearModifier.setDatePattern("yyyy");
    return DataSetModifierConfig.builder()
            .isTrueDuplicate(false)
            .tag(GeneratorConfigTag.SIMILAR_BUT_OTHER_PLACE.name())
            .filterRecordsToModify(new SelectorCombination<>(SelectorCombination.Operation.AND,
                            new AttributePresent(PersonalAttributeType.FIRSTNAME.name()),
                            new AttributePresent(PersonalAttributeType.LASTNAME.name()),
                            new SelectorCombination<>(SelectorCombination.Operation.OR,
                                    new AttributePresent(PersonalAttributeType.YEAROFBIRTH.name()),
                                    new AttributePresent(PersonalAttributeType.DATEOFBIRTH.name())
                            ),
                            new AttributePresent(PersonalAttributeType.CITY.name()),
                            new AttributePresent(PersonalAttributeType.PLZ.name())
                    )
            )
      .attributeModifier(
        similarFirstName ? PersonalAttributeType.FIRSTNAME.name() : PersonalAttributeType.LASTNAME.name(),
        List.of(
          new SelectiveAttributeModifier<>(
            new AttributeReplacer(similarFirstName ? firstNames : lastNames, true)
          )
        )
      )
      .attributeModifier(
        PersonalAttributeType.DATEOFBIRTH.name(),
        List.of(
          new SelectiveAttributeModifier<>(new DateTypoModifier("yMd"))
        )
      )
      .attributeModifier(
        PersonalAttributeType.YEAROFBIRTH.name(),
        List.of(
          new SelectiveAttributeModifier<>(yearModifier)
        )
      )
      .attributeModifier(
        PersonalAttributeType.CITY.name(),
        List.of(
          new SelectiveAttributeModifier<>(new AttributeReplacer(city, false, 864))
        )
      )
      .attributeModifier(
        PersonalAttributeType.PLZ.name(),
        List.of(
          new SelectiveAttributeModifier<>(new AttributeReplacer(plz, false, 864))
        )
      )
      .build();
  }


  public DataSetModifierConfig diffNamesAndDobButSamePlace() {
    List<String> firstNames = dataProvider.getAllValues(PersonalAttributeType.FIRSTNAME);
    List<String> lastNames = dataProvider.getAllValues(PersonalAttributeType.LASTNAME);
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(false)
      .tag(GeneratorConfigTag.SIMILAR_AND_SAME_PLACE.name())
      .attributeModifier(
        PersonalAttributeType.FIRSTNAME.name(),
        List.of(new SelectiveAttributeModifier<>(new AttributeReplacer(firstNames, true)))
      )
      .attributeModifier(
        PersonalAttributeType.LASTNAME.name(),
        List.of(new SelectiveAttributeModifier<>(new AttributeReplacer(lastNames, true)))
      )
      .attributeModifier(
        PersonalAttributeType.DATEOFBIRTH.name(),
        List.of(new SelectiveAttributeModifier<>(new DateTypoModifier("yMd")))
      )
      .build();
  }

  public DataSetModifierConfig wrongYOBinDOB() {
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(false)
      .tag(GeneratorConfigTag.WRONG_YOB.name())
      .attributeModifier(
        PersonalAttributeType.DATEOFBIRTH.name(),
        List.of(
          new SelectiveAttributeModifier<>(new DateTypoModifier("y")),
          new SelectiveAttributeModifier<>(new DateTypoModifier("Md"))
        )
      )
      .build();
  }
  public DataSetModifierConfig wrongYOB() {
    List<String> yobs = dataProvider.getAllValues(PersonalAttributeType.YEAROFBIRTH);
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.WRONG_YOB.name())
      .attributeModifier(
        PersonalAttributeType.YEAROFBIRTH.name(),
        List.of(
          new SelectiveAttributeModifier<>(new AttributeReplacer(yobs, true))
        )
      )
      .build();
  }

  public DataSetModifierConfig doubleFirstName(String delimiter) {
    List<String> firstNames = dataProvider.getAllValues(PersonalAttributeType.FIRSTNAME);
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.DOUBLE_FIRST_NAME.name())
      .attributeModifier(
        PersonalAttributeType.FIRSTNAME.name(),
        List.of(new SelectiveAttributeModifier<>(new MultiFieldAdder(firstNames, delimiter)))
      )
      .build();
  }

  public DataSetModifierConfig doubleLastName(String delimiter) {
    List<String> lastNames = dataProvider.getAllValues(PersonalAttributeType.LASTNAME);
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.DOUBLE_LAST_NAME.name())
      .attributeModifier(
        PersonalAttributeType.LASTNAME.name(),
        List.of(new SelectiveAttributeModifier<>(new MultiFieldAdder(lastNames, delimiter)))
      )
      .build();
  }

  public DataSetModifierConfig newLastName(double share, boolean similar) {
    List<String> lastNames = dataProvider.getAllValues(PersonalAttributeType.LASTNAME);
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.NEW_LAST_NAME.name())
      .attributeModifier(
        PersonalAttributeType.LASTNAME.name(),
        List.of(new SelectiveAttributeModifier<>(
          new AttributeReplacer(lastNames, similar),
          getSelector(share)
        ))
      )
      .build();
  }

  public <T> Selector<T> getSelector(double share) {
    if (share == 1.0) {
      return new SelectAll<>();
    }
    return new SelectRandom<>(share);
  }

  public DataSetModifierConfig marryAndAddBirthName() {
    List<String> lastNames = dataProvider.getAllValues(PersonalAttributeType.LASTNAME);
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.NAMECHANGE_BUT_BIRTHNAME.name())
      .recordModifier(
        new SelectiveRecordModifier(
          new AttributeCopyReplace(
            PersonalAttributeType.LASTNAME.name(), PersonalAttributeType.NAMEATBIRTH.name(), lastNames)
        )
      )
      .build();
  }

  public DataSetModifierConfig attributeRemover(Collection<String> attributesToRemove) {
    List<SelectiveRecordModifier> recordModifiers = attributesToRemove.stream()
      .map(AttributeRemover::new)
      .map(SelectiveRecordModifier::new)
      .collect(Collectors.toList());
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag("NO_" + String.join("_", attributesToRemove))
      .recordModifiers(recordModifiers)
      .build();
  }

  public DataSetModifierConfig moved(double share) {
    List<String> city = dataProvider.getAllValues(PersonalAttributeType.CITY);
    List<String> plz = dataProvider.getAllValues(PersonalAttributeType.PLZ);
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.MOVED.name())
      .attributeModifier(
        PersonalAttributeType.CITY.name(),
        List.of(
          new SelectiveAttributeModifier<>(
            new AttributeReplacer(city, false, 147),
            getSelector(share)
          )
        )
      )
      .attributeModifier(
        PersonalAttributeType.PLZ.name(),
        List.of(
          new SelectiveAttributeModifier<>(
            new AttributeReplacer(plz, false, 147),
            getSelector(share)
          )
        )
      )
      .build();
  }

  public DataSetModifierConfig moveAndKeepBirthPlace() {
    List<String> state = dataProvider.getAllValues(PersonalAttributeType.STATE);
    List<String> city = dataProvider.getAllValues(PersonalAttributeType.CITY);
    List<String> plz = dataProvider.getAllValues(PersonalAttributeType.PLZ);
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.MOVE_BUT_BIRTHPLACE.name())
      .filterRecordsToModify(new AttributePresent(PersonalAttributeType.STATE.name()))
      .attributeModifier(
        PersonalAttributeType.PLZ.name(),
        List.of(
          new SelectiveAttributeModifier<>(new AttributeReplacer(plz, false, 753))
        )
      )
      .recordModifier(
        new SelectiveRecordModifier(
          new AttributeCopyReplace(
            PersonalAttributeType.STATE.name(), PersonalAttributeType.PLACEOFBIRTH.name(), state, 753)
        )
      )
      .recordModifier(
        new SelectiveRecordModifier(
          new AttributeCopyReplace(
            PersonalAttributeType.CITY.name(), PersonalAttributeType.CITYOFBIRTH.name(), city, 753)
        )
      )
      .build();
  }

  public DataSetModifierConfig typosName(boolean firstName) {
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.TYPO_NAME.name())
      .attributeModifier(
        (firstName ? PersonalAttributeType.FIRSTNAME : PersonalAttributeType.LASTNAME).name(),
        List.of(new SelectiveAttributeModifier<>(new CharReplacer()))
      )
      .build();
  }

  public DataSetModifierConfig typoFirstNameCommonOrRareLastName(boolean isRare) {
    final List<String> lastNames = rareOrCommonValues(PersonalAttributeType.LASTNAME, isRare, true);

    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(isRare ? GeneratorConfigTag.TYPO_FN_RARE_LN.name() : GeneratorConfigTag.TYPO_FN_COMMON_LN.name())
      .filterRecordsToModify(new AttributeIsIn(PersonalAttributeType.LASTNAME.asString(), lastNames))
      .attributeModifier(PersonalAttributeType.FIRSTNAME.name(),
        List.of(new SelectiveAttributeModifier<>(new CharReplacer()))
      )
      .build();
  }

  public DataSetModifierConfig typos3() {
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .filterRecordsToModify(new SelectorCombination<>(SelectorCombination.Operation.AND,
                      new AttributePresent(PersonalAttributeType.FIRSTNAME.name()),
                      new AttributePresent(PersonalAttributeType.LASTNAME.name()),
                      new AttributePresent(PersonalAttributeType.CITY.name())
              )
      )
      .tag(GeneratorConfigTag.TYPO_FN_LN_CITY.name())
      .attributeModifier(
        PersonalAttributeType.FIRSTNAME.name(),
        List.of(new SelectiveAttributeModifier<>(new CharReplacer()))
      )
      .attributeModifier(
        PersonalAttributeType.LASTNAME.name(),
        List.of(new SelectiveAttributeModifier<>(new CharInserter()))
      )
      .attributeModifier(
        PersonalAttributeType.CITY.name(),
        List.of(new SelectiveAttributeModifier<>(new CharSwapper()))
      )
      .build();
  }

  public DataSetModifierConfig addBirthNameAndPlace(double share) {
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.ADD_BIRTH_NAME_PLACE.name())
      .recordModifier(new SelectiveRecordModifier(
        new AttributeCopy(PersonalAttributeType.CITY.name(), PersonalAttributeType.PLACEOFBIRTH.name()),
        new SelectRandom<>(share)
      ))
      .recordModifier(new SelectiveRecordModifier(
        new AttributeCopy(PersonalAttributeType.LASTNAME.name(), PersonalAttributeType.NAMEATBIRTH.name()),
        new SelectRandom<>(share)
      ))
      .build();
  }

  public DataSetModifierConfig changeBirthNameAndPlace() {
    List<String> lastNames = dataProvider.getAllValues(PersonalAttributeType.LASTNAME);
    List<String> city = dataProvider.getAllValues(PersonalAttributeType.CITY);

    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.CHANGE_BIRTH_NAME_PLACE.name())
      .filterRecordsToModify(new AttributePresent(PersonalAttributeType.NAMEATBIRTH.name()))
      .attributeModifier(
        PersonalAttributeType.NAMEATBIRTH.name(),
        List.of(
          new SelectiveAttributeModifier<>(new AttributeReplacer(lastNames, false), new SelectRandom<>(
            0.5,
            135
          ))
        )
      )
      .attributeModifier(
        PersonalAttributeType.PLACEOFBIRTH.name(),
        List.of(
          new SelectiveAttributeModifier<>(new AttributeReplacer(city, false), new SelectRandom<>(0.5, 246))
        )
      )
      .build();
  }

  public DataSetModifierConfig emptyAddress(double share) {
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.EMPTY_ADDRESS.name())
      .recordModifier(new SelectiveRecordModifier(
        new AttributeEmptier(PersonalAttributeType.CITY.name()),
        new SelectRandom<>(share)
      ))
      .recordModifier(new SelectiveRecordModifier(
        new AttributeEmptier(PersonalAttributeType.PLZ.name()),
        new SelectRandom<>(share)
      ))
      .build();
  }

  public DataSetModifierConfig emptyPlaceOfBirth(double share) {
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .tag(GeneratorConfigTag.EMPTY_POB.name())
      .recordModifier(new SelectiveRecordModifier(
        new AttributeEmptier(PersonalAttributeType.PLACEOFBIRTH.name()),
        new SelectRandom<>(share)
      ))
      .build();
  }

  public DataSetModifierConfig moveForYearRange(double share, int startYearInclusive, int endYearExclusive,
    boolean isInverse) {
//    List<String> city = dataProvider.getAllValues(PersonalAttributeType.CITY);
//    List<String> plz = dataProvider.getAllValues(PersonalAttributeType.PLZ);

    YearOfBirthSelector yearOfBirthSelector =
      new YearOfBirthSelector("yyyy-MM-dd", startYearInclusive, endYearExclusive);

    DataSetModifierConfig config = moved(share);
    config.setFilterRecordsToModify(
      isInverse ?
        new InverseSelector<>(yearOfBirthSelector)
        : yearOfBirthSelector);
    return config;
//    return DataSetModifierConfig.builder()
//      .isTrueDuplicate(true)
//      .modifiedSourceName("dup")
//      .tag(GeneratorConfigTag.MOVED.name())
//      .filterRecordsToModify()
//      .attributeModifier(
//        PersonalAttributeType.CITY.name(),
//        List.of(
//          new SelectiveAttributeModifier<>(
//            new AttributeReplacer(city, false, 147),
//            getSelector(share)
//          )
//        )
//      )
//      .attributeModifier(
//        PersonalAttributeType.PLZ.name(),
//        List.of(
//          new SelectiveAttributeModifier<>(
//            new AttributeReplacer(plz, false, 147),
//            getSelector(share)
//          )
//        )
//      )
//      .build();
  }

//  public DataSetModifierConfig sameHousehold() {
//    return DataSetModifierConfig.builder()
//      .isTrueDuplicate(false)
//      .modifiedSourceName("dup")
//      .tag(GeneratorConfigTag.SAME_HOUSEHOLD.name())
//      .filterRecordsToModify()
//      .build();
//  }

  public DataSetModifierConfig swappedFirstAndLastname() {
    return DataSetModifierConfig.builder()
      .isTrueDuplicate(true)
      .filterRecordsToModify(new SelectorCombination<>(SelectorCombination.Operation.AND,
                      new AttributePresent(PersonalAttributeType.FIRSTNAME.name()),
                      new AttributePresent(PersonalAttributeType.LASTNAME.name())
              )
      )
      .tag(GeneratorConfigTag.EXCHANGED_FIRST_LAST_NAME.name())
      .recordModifier(new SelectiveRecordModifier(
        new AttributeSwapper(PersonalAttributeType.FIRSTNAME.name(), PersonalAttributeType.LASTNAME.name())
      ))
      .build();
  }

  public DataSetModifierConfig rareOrCommonNameMove(boolean isRare) {
    List<String> city = dataProvider.getAllValues(PersonalAttributeType.CITY);
    List<String> plz = dataProvider.getAllValues(PersonalAttributeType.PLZ);

    final List<String> firstNames = rareOrCommonValues(PersonalAttributeType.FIRSTNAME, isRare, true);
    final List<String> lastNames = rareOrCommonValues(PersonalAttributeType.LASTNAME, isRare, true);

    return DataSetModifierConfig.builder()
      .isTrueDuplicate(isRare)
      .tag(isRare ? GeneratorConfigTag.RARE_NAME_MOVE.name() : GeneratorConfigTag.COMMON_NAME_MOVE.name())
      .filterRecordsToModify(new SelectorCombination<>(
          SelectorCombination.Operation.AND,
          new AttributeIsIn(PersonalAttributeType.FIRSTNAME.asString(), firstNames),
          new AttributeIsIn(PersonalAttributeType.LASTNAME.asString(), lastNames)
        )
      )
      .attributeModifier(PersonalAttributeType.CITY.asString(), List.of(new SelectiveAttributeModifier<>(
        new AttributeReplacer(city, false, 123)
      )))
      .attributeModifier(PersonalAttributeType.PLZ.asString(), List.of(new SelectiveAttributeModifier<>(
        new AttributeReplacer(plz, false, 123)
      )))
      .build();
  }

  public DataSetModifierConfig rareOrCommonMarriage(boolean isRare) {
    final List<String> firstNames = rareOrCommonValues(PersonalAttributeType.FIRSTNAME, isRare, true);
    final List<String> cities = rareOrCommonValues(PersonalAttributeType.CITY, isRare, true);

    final List<String> lastNames = dataProvider.getAllValues(PersonalAttributeType.LASTNAME);

    return DataSetModifierConfig.builder()
      .isTrueDuplicate(isRare)
      .tag(
        isRare ?
          GeneratorConfigTag.MARRIAGE_RARE_FIRSTNAME_AND_ADDR.name()
          : GeneratorConfigTag.MARRIAGE_COMMON_FIRSTNAME_AND_ADDR.name())
      .filterRecordsToModify(new SelectorCombination<>(
          SelectorCombination.Operation.AND,
          new AttributeIsIn(PersonalAttributeType.FIRSTNAME.asString(), firstNames),
          new AttributeIsIn(PersonalAttributeType.CITY.asString(), cities)
        )
      )
      .attributeModifier(PersonalAttributeType.LASTNAME.asString(), List.of(new SelectiveAttributeModifier<>(
        new AttributeReplacer(lastNames, false, 123)
      )))
      .build();
  }

  public DataSetModifierConfig rareOrCommonDisagreeingName(boolean isRare) {
    final List<String> firstNames = rareOrCommonValues(PersonalAttributeType.FIRSTNAME, isRare, true);

    return DataSetModifierConfig.builder()
      .isTrueDuplicate(isRare)
      .tag(isRare ? GeneratorConfigTag.RARE_DIFFERENT_NAME.name() :
        GeneratorConfigTag.COMMON_DIFFERENT_NAME.name())
      .filterRecordsToModify(new AttributeIsIn(PersonalAttributeType.FIRSTNAME.asString(), firstNames))
      .attributeModifier(PersonalAttributeType.FIRSTNAME.asString(), List.of(new SelectiveAttributeModifier<>(
        new AttributeReplacer(firstNames, false, 123)
      )))
      .build();
  }

  public DataSetModifierConfig rareOrCommonNameMissingAddress(boolean isRare) {
    final List<String> firstNames = rareOrCommonValues(PersonalAttributeType.FIRSTNAME.asString(), isRare, true, 0.75);
    final List<String> lastNames = rareOrCommonValues(PersonalAttributeType.LASTNAME.asString(), isRare, true, 0.75);

    return DataSetModifierConfig.builder()
      .isTrueDuplicate(isRare)
      .tag(isRare ? GeneratorConfigTag.RARE_NAME_MISSING_ADDR.name() :
        GeneratorConfigTag.COMMON_NAME_MISSING_ADDR.name())
      .filterRecordsToModify(new SelectorCombination<>(
          SelectorCombination.Operation.AND,
          new AttributeIsIn(PersonalAttributeType.FIRSTNAME.asString(), firstNames),
          new AttributeIsIn(PersonalAttributeType.LASTNAME.asString(), lastNames)
        )
      )
      .recordModifier(new SelectiveRecordModifier(
        new AttributeRemover(PersonalAttributeType.CITY.name())
      ))
      .recordModifier(new SelectiveRecordModifier(
        new AttributeRemover(PersonalAttributeType.PLZ.name())
      ))
      .build();
  }

  private List<String> rareOrCommonValues(PersonalAttributeType attributeType, boolean isRare, boolean distinct) {
    return rareOrCommonValues(attributeType.asString(), isRare, distinct);
  }

  private List<String> rareOrCommonValues(String attributeName, boolean isRare, boolean distinct) {
    double share = isRare ? 0.65 : 0.02;
    return rareOrCommonValues(attributeName, isRare, distinct, share);
  }

  private List<String> rareOrCommonValues(String attributeName, boolean isRare, boolean distinct, double share) {
    log.info("Getting values from data provider: attributeName={}, isRare={}, distinct={}, share={}",
            attributeName, isRare, distinct, share);
    return dataProvider.getFrequencyFilteredValues(attributeName, isRare, share, distinct);
  }

//  public DataSetModifierConfig sameHousehold() {
//    return DataSetModifierConfig.builder()
//            .isTrueDuplicate(false)
//            .tag(GeneratorConfigTag.SAME_HOUSEHOLD.name())
//            .filterRecordsToModify(new SelectorCombination<>(
//                            SelectorCombination.Operation.AND,
//                            new AttributeIsIn(PersonalAttributeType..asString(), firstNames),
//                            new AttributeIsIn(PersonalAttributeType.LASTNAME.asString(), lastNames)
//                    )
//            )
//            .build();
//  }
}
