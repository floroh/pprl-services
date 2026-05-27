package de.unileipzig.dbs.pprl.service.dataowner.modifier.dataprovider;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.dataset.DatasetModifier;

import java.util.List;

/**
 * Provides data that can be used by {@link DatasetModifier}
 * e.g. as a source for values that replace original attributes
 */
public interface GeneratorDataProvider {

  /**
   * Get all values available from this provider without any filtering
   *
   * @param attributeName name of the attribute, e.g. FIRSTNAME
   * @param distinct      if true, only unique values are returned
   *                      if false, the returned list can contain the same value multiple times, depending on
   *                      the frequency of this value
   * @return list of attribute values
   */
  List<String> getAllValues(String attributeName, boolean distinct);

  /**
   * Get values filtered based on frequencies
   *
   * @param attributeName name of the attribute, e.g. FIRSTNAME
   * @param isRare        if true, the less common values are returned
   *                      if false, the more common values are returned
   * @param share         relative proportion of (unique) values that are returned, selected either
   *                      from the top or bottom the list sorted by frequency (depending on isRare)
   *                     on isRare
   * @param distinct      if true, only unique values are returned
   *                      if false, the returned list can contain the same value multiple times, depending on
   *                      the frequency of this value
   * @return list of attribute values
   */
  List<String> getFrequencyFilteredValues(String attributeName, boolean isRare, double share,
    boolean distinct);

  default List<String> getAllValues(PersonalAttributeType personalAttributeType) {
    return getAllValues(personalAttributeType, false);
  }

  default List<String> getAllValues(PersonalAttributeType personalAttributeType, boolean distinct) {
    return getAllValues(personalAttributeType.asString(), distinct);
  }

}
