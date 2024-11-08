/*
 * Copyright © 2018 - 2021 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unileipzig.dbs.pprl.core.common;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordClusterSimple;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RecordUtils {

  public static <R extends Record> Map<String, List<R>> groupById(Collection<R> records, String idType) {
    return records.stream()
      .sequential()
      .filter(r -> r.getId().getOptionalId(idType).isPresent())
      .collect(Collectors.groupingBy(r -> r.getId().getId(idType)));
  }

  public static Map<String, List<RecordId>> groupIdsByType(Collection<RecordId> recordIds,
    String idType) {
    return recordIds.stream()
      .sequential()
      .filter(r -> r.getOptionalId(idType).isPresent())
      .collect(Collectors.groupingBy(r -> r.getId(idType)));
  }

  public static List<RecordCluster> getSourceGroups(Collection<Record> records) {
    return createCluster(records, RecordId.SOURCE_ID);
  }

  public static List<RecordCluster> createCluster(Collection<Record> records, String idType) {
    Map<String, List<Record>> groupedRecords = groupById(records, idType);
    return groupedRecords.values()
      .stream()
      .map(RecordClusterSimple::new)
      .collect(Collectors.toList());
  }

  public static <R extends Record> Map<String, List<Attribute>> groupByAttributeName(Collection<R> records) {
    final List<Map<String, Attribute>> attributesList = records.stream()
      .sequential()
      .map(Record::getAttributes)
      .collect(Collectors.toList());
    //TODO Rewrite as this implementation needs to much memory
    return HelperUtils.listOfMapsToMapOfLists(attributesList);
  }

  public static <R extends Record> Set<String> getAttributeNames(Collection<R> records) {
    return records.stream()
      .flatMap((R r) -> r.getAttributeNames().stream())
      .collect(Collectors.toSet());
  }

  public static <R extends Record> Map<String, Attribute.Type> getAttributeTypes(Collection<R> records) {
    return groupByAttributeName(records).entrySet()
      .stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()
        .getFirst()
        .getType()));
  }

  public static <R extends Record> long numberOfSources(Collection<R> records) {
    return records.stream()
      .map(Record::getId)
      .map(id -> id.getId(RecordId.SOURCE_ID))
      .distinct()
      .count();
  }

  public static void setId(Collection<Record> records, String idName, String value) {
    for (Record record : records) {
      RecordId id = record.getId().addId(idName, value);
      record.setId(id);
    }
  }
  public static void removeId(Collection<Record> records, String idName) {
    for (Record record : records) {
      RecordId id = record.getId().removeId(idName);
      record.setId(id);
    }
  }

  public static String getPairId(String uniqueIdLeft, String uniqueIdRight) {
    if (uniqueIdLeft.compareTo(uniqueIdRight) > 0) {
      return uniqueIdLeft + "##" + uniqueIdRight;
    } else {
      return uniqueIdRight + "##" + uniqueIdLeft;
    }
  }
}
