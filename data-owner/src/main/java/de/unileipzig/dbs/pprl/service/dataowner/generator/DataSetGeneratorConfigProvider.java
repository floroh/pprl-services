package de.unileipzig.dbs.pprl.service.dataowner.generator;

import de.unileipzig.dbs.pprl.core.common.frequencies.AttributesFrequencyLookup;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.DataSetModifierConfig;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.DataSetModifierConfigProvider;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.dataprovider.AttributeFrequencyLookupBasedDataProvider;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.dataprovider.DefaultGeneratorDataProvider;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.dataprovider.GeneratorDataProvider;
import de.unileipzig.dbs.pprl.core.common.selector.AttributeIsIn;
import de.unileipzig.dbs.pprl.core.common.selector.SourceSelector;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class DataSetGeneratorConfigProvider {

  private GeneratorDataProvider generatorDataProvider;

  private final Map<String, Supplier<DataSetGeneratorConfig>> nameToGeneratorConfigGetter =
          Map.ofEntries(
                  Map.entry("TEST", this::getTestConfig),

                  Map.entry("BaWue_TIME", this::getBaWue_TIME),
                  Map.entry("BaWue_DIRTY", this::getBaWue_DIRTY),
                  Map.entry("BaWue_FRQ", this::getBaWue_FRQ),

                  Map.entry("NCVR_TIME", this::getNCVR_TIME),
                  Map.entry("NCVR_DIRTY", this::getNCVR_DIRTY),
                  Map.entry("NCVR_FRQ", this::getNCVR_FRQ),

                  Map.entry("TIME", this::getTIME),
                  Map.entry("DIRTY", this::getDIRTY),
                  Map.entry("FRQ", this::getFRQ)
          );

  public DataSetGeneratorConfigProvider() {}

  public DataSetGeneratorConfigProvider(AttributesFrequencyLookup afl) {
    this.generatorDataProvider = new AttributeFrequencyLookupBasedDataProvider(afl);
  }
  public DataSetGeneratorConfigProvider(AttributesFrequencyLookup afl, long seed) {
    this.generatorDataProvider = new AttributeFrequencyLookupBasedDataProvider(afl, seed);
  }

  public DataSetGeneratorConfig getByName(String name) {
    Supplier<DataSetGeneratorConfig> supplier = nameToGeneratorConfigGetter.get(name);
    if (supplier == null) {
      throw new IllegalArgumentException("Unknown dataset generation configuration name: " + name);
    }
    DataSetGeneratorConfig dataSetGeneratorConfig = supplier.get();
    dataSetGeneratorConfig.setName(name);
    return dataSetGeneratorConfig;
  }

  public Set<String> getAllConfigNames() {
    return nameToGeneratorConfigGetter.keySet();
  }


  private DataSetGeneratorConfig getTestConfig() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(100)
      .modifiedSize(100)
      .sourceOverlap(0.2)
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.emptyAddress(1.0)) //TM
      .duplicateModifierConfig(provider.typos3()) //TM
      .duplicateModifierConfig(provider.swappedFirstAndLastname()) //TM
      .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
      .build();
  }

  private DataSetGeneratorConfig getBaWue_DIRTY_1() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(10000)
      .modifiedSize(10000)
      .sourceOverlap(0.2)
      .inputFilter(new AttributeIsIn(PersonalAttributeType.STATE.name(), List.of("Baden-Württemberg")))
//      .sourceModifierConfig(provider.emptyAddress(0.2))
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.emptyAddress(1.0)) //TM
      .duplicateModifierConfig(provider.typos3()) //TM
      .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
      .build();
  }

  private DataSetGeneratorConfig getBaWue_DIRTY_2() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(10000)
      .modifiedSize(10000)
      .sourceOverlap(0.2)
      .inputFilter(new AttributeIsIn(PersonalAttributeType.STATE.name(), List.of("Baden-Württemberg")))
//      .sourceModifierConfig(provider.emptyAddress(0.2))
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.emptyAddress(1.0)) //TM
      .duplicateModifierConfig(provider.swappedFirstAndLastname()) //TM
      .duplicateModifierConfig(provider.typos3()) //TM
      .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
      .build();
  }
  private DataSetGeneratorConfig getBaWue_DIRTY_3() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(10000)
      .modifiedSize(10000)
      .sourceOverlap(0.2)
      .inputFilter(new AttributeIsIn(PersonalAttributeType.STATE.name(), List.of("Baden-Württemberg")))
//      .sourceModifierConfig(provider.emptyAddress(0.2))
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.emptyAddress(1.0)) //TM
      .duplicateModifierConfig(provider.swappedFirstAndLastname()) //TM
      .duplicateModifierConfig(provider.typos3()) //TM
      .duplicateModifierConfig(provider.rareOrCommonNameMove(true)) //TM
      .duplicateModifierConfig(provider.rareOrCommonNameMove(false)) //TNM
      .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
      .build();
  }

  private DataSetGeneratorConfig getBaWue_DIRTY_2_100() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    return DataSetGeneratorConfig.builder()
            .originalSize(100)
            .modifiedSize(100)
            .sourceOverlap(0.2)
            .inputFilter(new AttributeIsIn(PersonalAttributeType.STATE.name(), List.of("Baden-Württemberg")))
//      .sourceModifierConfig(provider.emptyAddress(0.2))
            .duplicateModifierConfig(provider.nonModifier()) //TM
            .duplicateModifierConfig(provider.emptyAddress(1.0)) //TM
            .duplicateModifierConfig(provider.swappedFirstAndLastname()) //TM
            .duplicateModifierConfig(provider.typos3()) //TM
            .duplicateModifierConfig(provider.newLastName(1.0, false)) //TM
            .duplicateModifierConfig(provider.doubleLastName("-")) //TM
            .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
            .build();
  }

  private DataSetGeneratorConfig getBaWue_FRQ() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder = DataSetGeneratorConfig.builder()
            .originalSize(10000)
            .modifiedSize(10000)
            .sourceOverlap(0.2)
            .inputFilter(new AttributeIsIn(PersonalAttributeType.STATE.name(), List.of("Baden-Württemberg")));
    builder = addFRQ(builder, provider);
    DataSetGeneratorConfig config = builder.build();
    for (DataSetModifierConfig duplicateModifierConfig : config.getDuplicateModifierConfigs()) {
      duplicateModifierConfig.setOriginalSourceName("A");
      duplicateModifierConfig.setModifiedSourceName("B");
    }
    return config;
  }

  private DataSetGeneratorConfig getNCVR_FRQ() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder = DataSetGeneratorConfig.builder()
            .originalSize(10000)
            .modifiedSize(10000)
            .sourceOverlap(0.2)
            .inputFilter(new SourceSelector("A"));
    builder = addFRQ(builder, provider);
    DataSetGeneratorConfig config = builder.build();
    for (DataSetModifierConfig duplicateModifierConfig : config.getDuplicateModifierConfigs()) {
      duplicateModifierConfig.setOriginalSourceName("A");
      duplicateModifierConfig.setModifiedSourceName("B");
    }
    return config;
  }

  private DataSetGeneratorConfig getFRQ() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder = DataSetGeneratorConfig.builder()
            .originalSize(10000)
            .modifiedSize(10000)
            .sourceOverlap(0.2);
    builder = addFRQ(builder, provider);
    return builder.build();
  }


  private DataSetGeneratorConfig.DataSetGeneratorConfigBuilder addFRQ(DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder,
                                                                      DataSetModifierConfigProvider provider) {
    return builder
            .duplicateModifierConfig(provider.nonModifier()) //TM
            .duplicateModifierConfig(provider.typos3()) //TM
            .duplicateModifierConfig(provider.rareOrCommonNameMove(true)) //TM
            .duplicateModifierConfig(provider.rareOrCommonNameMissingAddress(true)) //TM
            .duplicateModifierConfig(provider.rareOrCommonMarriage(true)) //TM
            .duplicateModifierConfig(provider.rareOrCommonNameMove(false)) //TNM
            .duplicateModifierConfig(provider.rareOrCommonNameMissingAddress(false)) //TNM
            .duplicateModifierConfig(provider.rareOrCommonMarriage(false)) //TNM
            .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
            .duplicateModifierConfig(provider.similarButOtherPlace(false)) //TNM
            ;
  }
  private DataSetGeneratorConfig getBaWue_DIRTY() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder = DataSetGeneratorConfig.builder()
            .originalSize(10000)
            .modifiedSize(10000)
            .sourceOverlap(0.2)
            .inputFilter(new AttributeIsIn(PersonalAttributeType.STATE.name(), List.of("Baden-Württemberg")));
    builder = addDIRTY(builder, provider);
    DataSetGeneratorConfig config = builder.build();
    for (DataSetModifierConfig duplicateModifierConfig : config.getDuplicateModifierConfigs()) {
      duplicateModifierConfig.setOriginalSourceName("A");
      duplicateModifierConfig.setModifiedSourceName("B");
    }
    return config;
  }

  private DataSetGeneratorConfig getNCVR_DIRTY() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder = DataSetGeneratorConfig.builder()
            .originalSize(10000)
            .modifiedSize(10000)
            .sourceOverlap(0.2)
            .inputFilter(new SourceSelector("A"));
    builder = addDIRTY(builder, provider);
    DataSetGeneratorConfig config = builder.build();
    for (DataSetModifierConfig duplicateModifierConfig : config.getDuplicateModifierConfigs()) {
      duplicateModifierConfig.setOriginalSourceName("A");
      duplicateModifierConfig.setModifiedSourceName("B");
    }
    return config;
  }

  private DataSetGeneratorConfig getDIRTY() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder = DataSetGeneratorConfig.builder()
            .originalSize(10000)
            .modifiedSize(10000)
            .sourceOverlap(0.2);
    builder = addDIRTY(builder, provider);
    DataSetGeneratorConfig config = builder.build();
    for (DataSetModifierConfig duplicateModifierConfig : config.getDuplicateModifierConfigs()) {
      duplicateModifierConfig.setOriginalSourceName("A");
      duplicateModifierConfig.setModifiedSourceName("B");
    }
    return config;
  }

  private DataSetGeneratorConfig.DataSetGeneratorConfigBuilder addDIRTY(DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder,
                                                                            DataSetModifierConfigProvider provider) {
    return builder
//            .sourceModifierConfig(provider.emptyAddress(0.2))
            .duplicateModifierConfig(provider.nonModifier()) //TM
            .duplicateModifierConfig(provider.swappedFirstAndLastname()) //TM
            .duplicateModifierConfig(provider.emptyPlaceOfBirth(1.0)) //TM
            .duplicateModifierConfig(provider.emptyAddress(1.0)) //TM
            .duplicateModifierConfig(provider.doubleFirstName(" ")) //TM
            .duplicateModifierConfig(provider.doubleFirstName("-")) //TM
            .duplicateModifierConfig(provider.typosName(true)) //TM
            .duplicateModifierConfig(provider.typosName(false)) //TM
            .duplicateModifierConfig(provider.typos3()) //TM
            .duplicateModifierConfig(provider.wrongYOB()) //TM
            .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
            .duplicateModifierConfig(provider.similarButOtherPlace(false)) //TNM
            ;
  }

  private DataSetGeneratorConfig getBaWue_TIME() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder = DataSetGeneratorConfig.builder()
            .originalSize(10000)
            .modifiedSize(10000)
            .sourceOverlap(0.2)
            .inputFilter(new AttributeIsIn(PersonalAttributeType.STATE.name(), List.of("Baden-Württemberg")));
    builder = addTIME(builder, provider);
    DataSetGeneratorConfig config = builder.build();
    for (DataSetModifierConfig duplicateModifierConfig : config.getDuplicateModifierConfigs()) {
      duplicateModifierConfig.setOriginalSourceName("A");
      duplicateModifierConfig.setModifiedSourceName("B");
    }
    return config;
  }

  private DataSetGeneratorConfig getNCVR_TIME() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder = DataSetGeneratorConfig.builder()
            .originalSize(10000)
            .modifiedSize(10000)
            .sourceOverlap(0.2)
            .inputFilter(new SourceSelector("A"));
    builder = addTIME(builder, provider);
    DataSetGeneratorConfig config = builder.build();
    for (DataSetModifierConfig duplicateModifierConfig : config.getDuplicateModifierConfigs()) {
      duplicateModifierConfig.setOriginalSourceName("A");
      duplicateModifierConfig.setModifiedSourceName("B");
    }
    return config;
  }

  private DataSetGeneratorConfig getTIME() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder = DataSetGeneratorConfig.builder()
            .originalSize(10000)
            .modifiedSize(10000)
            .sourceOverlap(0.2);
    builder = addTIME(builder, provider);
    return builder.build();
  }

  private DataSetGeneratorConfig.DataSetGeneratorConfigBuilder addTIME(DataSetGeneratorConfig.DataSetGeneratorConfigBuilder builder,
                                                                            DataSetModifierConfigProvider provider) {
    return builder
//      .sourceModifierConfig(provider.addBirthNameAndPlace(0.2))
//      .sourceModifierConfig(provider.changeBirthNameAndPlace())
            .duplicateModifierConfig(provider.nonModifier()) //TM
//            .duplicateModifierConfig(provider.newLastName(1.0, true)) //TM
            .duplicateModifierConfig(provider.newLastName(1.0, false)) //TM
            .duplicateModifierConfig(provider.doubleLastName("-")) //TM
            .duplicateModifierConfig(provider.doubleLastName(" ")) //TM
//            .duplicateModifierConfig(provider.marryAndAddBirthName()) //TM
            .duplicateModifierConfig(provider.moveAndKeepBirthPlace()) //TM
            .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
            .duplicateModifierConfig(provider.similarButOtherPlace(false)) //TNM
            ;
  }

  private DataSetGeneratorConfig getDIRTY_1() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(10000)
      .modifiedSize(10000)
      .sourceOverlap(0.2)
      .inputFilter(new SourceSelector("A"))
//      .sourceModifierConfig(provider.addBirthNameAndPlace(0.2))
//      .sourceModifierConfig(provider.changeBirthNameAndPlace())
//      .sourceModifierConfig(provider.emptyAddress(0.2))
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.emptyAddress(1.0)) //TM
//      .duplicateModifierConfig(provider.swappedFirstAndLastname()) //TM
//      .duplicateModifierConfig(provider.doubleFirstName()) //TM
      .duplicateModifierConfig(provider.typos3()) //TM
      .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
      .build();
  }

  private DataSetGeneratorConfig getDIRTY_2() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(10000)
      .modifiedSize(10000)
      .sourceOverlap(0.2)
      .inputFilter(new SourceSelector("A"))
//      .sourceModifierConfig(provider.addBirthNameAndPlace(0.2))
//      .sourceModifierConfig(provider.changeBirthNameAndPlace())
//      .sourceModifierConfig(provider.emptyAddress(0.2))
      .duplicateModifierConfig(provider.nonModifier()) //TM
      .duplicateModifierConfig(provider.emptyAddress(1.0)) //TM
      .duplicateModifierConfig(provider.swappedFirstAndLastname()) //TM
//      .duplicateModifierConfig(provider.doubleFirstName()) //TM
      .duplicateModifierConfig(provider.typos3()) //TM
      .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
      .build();
  }

//  private DataSetGeneratorConfig getDIRTY() {
//    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider(generatorDataProvider);
//    return DataSetGeneratorConfig.builder()
//      .originalSize(10000)
//      .modifiedSize(10000)
//      .sourceOverlap(0.2)
//      .inputFilter(new SourceSelector("A"))
//      .sourceModifierConfig(provider.addBirthNameAndPlace(0.2))
//      .sourceModifierConfig(provider.changeBirthNameAndPlace())
//      .sourceModifierConfig(provider.emptyAddress(0.2))
//      .duplicateModifierConfig(provider.nonModifier()) //TM
//      .duplicateModifierConfig(provider.emptyAddress(1.0)) //TM
//      .duplicateModifierConfig(provider.swappedFirstAndLastname()) //TM
//      .duplicateModifierConfig(provider.doubleFirstName()) //TM
//      .duplicateModifierConfig(provider.typos3()) //TM
//      .duplicateModifierConfig(provider.similarButOtherPlace(true)) //TNM
//      .build();
//  }



  private static DataSetGeneratorConfig getFBWv1() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider();
    return DataSetGeneratorConfig.builder()
      .originalSize(100000)
      .modifiedSize(100000)
      .sourceOverlap(0.2)
      .sourceModifierConfig(provider.addBirthNameAndPlace(0.2))
      .sourceModifierConfig(provider.changeBirthNameAndPlace())
      .sourceModifierConfig(provider.emptyAddress(0.2))
      .duplicateModifierConfigs(List.of(
        provider.nonModifier(), //TM
        provider.nonModifier(), // TM
        provider.nonModifier(), // TM
        provider.nonModifier(), // TM
        provider.typosName(true), // TM
        provider.typosName(false), // TM
        provider.similarButOtherPlace(true), // TNM
        provider.similarButOtherPlace(false), // TNM
        provider.diffNamesAndDobButSamePlace() // TNM
      ))
      .build();
  }

  private static DataSetGeneratorConfig getFBWtimeNoMiss() {
    return getFBWtimeNoMiss(new DefaultGeneratorDataProvider());
  }

  private static DataSetGeneratorConfig getFBWtimeNoMiss(GeneratorDataProvider dataProvider) {
    int sourceSize = 10000;
    DataSetModifierConfigProvider modifierConfigProvider = new DataSetModifierConfigProvider(dataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(sourceSize)
      .modifiedSize(sourceSize)
      .sourceOverlap(0.2)
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonNameMove(true)) //TM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonMarriage(true)) //TM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonNameMove(false)) //TNM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonMarriage(false)) //TNM
      .build();
  }

  private static DataSetGeneratorConfig getFbwMoveOnly() {
    return getFbwMoveOnly(new DefaultGeneratorDataProvider());
  }

  private static DataSetGeneratorConfig getFbwMoveOnly(GeneratorDataProvider dataProvider) {
    DataSetModifierConfigProvider modifierConfigProvider = new DataSetModifierConfigProvider(dataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(10000)
      .modifiedSize(10000)
      .sourceOverlap(0.2)
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonNameMove(true)) //TM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonNameMove(false)) //TNM
      .build();
  }

  private static DataSetGeneratorConfig getFbwMarriageOnly() {
    return getFbwMoveOnly(new DefaultGeneratorDataProvider());
  }

  private static DataSetGeneratorConfig getFbwMarriageOnly(GeneratorDataProvider dataProvider) {
    DataSetModifierConfigProvider modifierConfigProvider = new DataSetModifierConfigProvider(dataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(10000)
      .modifiedSize(10000)
      .sourceOverlap(0.2)
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonMarriage(true)) //TM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonMarriage(false)) //TNM
      .build();
  }


  private static DataSetGeneratorConfig getBigDataPraktikum2022() {
    DataSetModifierConfigProvider provider = new DataSetModifierConfigProvider();
    DataSetModifierConfig moveYoung = provider.moveForYearRange(0.8, 1990, 2005, true);
    DataSetModifierConfig moveOther = provider.moveForYearRange(0.4, 1990, 2005, false);

    DataSetModifierConfig newLastNameFemale = provider.newLastName(0.8, false);
    newLastNameFemale.setFilterRecordsToModify(new AttributeIsIn(
      PersonalAttributeType.SEX.asString(),
      Collections.singleton("F")
    ));
    DataSetModifierConfig newLastNameMale = provider.newLastName(0.2, false);
    newLastNameMale.setFilterRecordsToModify(new AttributeIsIn(
      PersonalAttributeType.SEX.asString(),
      Collections.singleton("M")
    ));
    return DataSetGeneratorConfig.builder()
      .originalSize(200000)
      .modifiedSize(200000)
      .sourceOverlap(0.3)
      .sourceModifierConfig(provider.attributeRemover(List.of(
        PersonalAttributeType.STREET.asString(),
        PersonalAttributeType.STATE.asString(),
        PersonalAttributeType.PLACEOFBIRTH.asString()
      )))
      .duplicateModifierConfigs(List.of(
//        provider.nonModifier(), // TM
        provider.typosName(true), // TM
        provider.typosName(false), // TM
        moveYoung, // TM
        moveOther, // TM
        newLastNameMale, // TM
        newLastNameFemale, // TM
        provider.similarButOtherPlace(true), // TNM
        provider.similarButOtherPlace(false), // TNM
        provider.diffNamesAndDobButSamePlace() // TNM
      ))
      .build();
  }

  private static DataSetGeneratorConfig getCommonAndRareDiffNames(GeneratorDataProvider dataProvider) {
    int sourceSize = 10000;
    DataSetModifierConfigProvider modifierConfigProvider = new DataSetModifierConfigProvider(dataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(sourceSize)
      .modifiedSize(sourceSize)
      .sourceOverlap(0.5)
      .sourceModifierConfig(modifierConfigProvider.attributeRemover(List.of(
        PersonalAttributeType.STREET.asString(),
        PersonalAttributeType.STATE.asString(),
        PersonalAttributeType.PLACEOFBIRTH.asString()
      )))
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonDisagreeingName(true)) //TM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonDisagreeingName(false)) //TNM
      .build();
  }

  private static DataSetGeneratorConfig getAllMove(GeneratorDataProvider dataProvider) {
    int sourceSize = 10000;
    DataSetModifierConfigProvider modifierConfigProvider = new DataSetModifierConfigProvider(dataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(sourceSize)
      .modifiedSize(sourceSize)
      .sourceOverlap(1.0)
//      .duplicateModifierConfig(modifierConfigProvider.moved(1.0)) //TM
      .duplicateModifierConfig(modifierConfigProvider.nonModifier()) //TM
      .build();
  }

  private static DataSetGeneratorConfig getFbwMixed() {
    return getFbwMixed(new DefaultGeneratorDataProvider());
  }

  private static DataSetGeneratorConfig getFbwMixed(GeneratorDataProvider dataProvider) {
    int sourceSize = 10000;
    DataSetModifierConfigProvider modifierConfigProvider = new DataSetModifierConfigProvider(dataProvider);
    return DataSetGeneratorConfig.builder()
      .originalSize(sourceSize)
      .modifiedSize(sourceSize)
      .sourceOverlap(0.3)
      .sourceModifierConfig(modifierConfigProvider.attributeRemover(List.of(
        PersonalAttributeType.STREET.asString(),
        PersonalAttributeType.STATE.asString(),
        PersonalAttributeType.PLACEOFBIRTH.asString()
      )))
      .duplicateModifierConfig(modifierConfigProvider.nonModifier()) //TM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonNameMove(true)) //TM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonMarriage(true)) //TM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonNameMissingAddress(true)) //TM
      .duplicateModifierConfig(modifierConfigProvider.typoFirstNameCommonOrRareLastName(true)) //TM
      .duplicateModifierConfig(modifierConfigProvider.typoFirstNameCommonOrRareLastName(false)) //TM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonNameMove(false)) //TNM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonMarriage(false)) //TNM
      .duplicateModifierConfig(modifierConfigProvider.rareOrCommonNameMissingAddress(false)) //TNM
      .duplicateModifierConfig(modifierConfigProvider.similarButOtherPlace(true)) //TNM
      .duplicateModifierConfig(modifierConfigProvider.diffNamesAndDobButSamePlace()) //TNM
      .build();
  }

}
