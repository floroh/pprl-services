/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
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

package de.unileipzig.dbs.pprl.core.matcher.evaluation;

import de.unileipzig.dbs.pprl.core.common.comparators.ComposedIdComparator;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordIdPair;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdPairSimple;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordPairSimple;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordSimple;
import de.unileipzig.dbs.pprl.core.matcher.clustering.ConnectedComponents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.table.TableSlice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GroundTruth {

  public static final String COMPOSED_ID = "ID";
  public static final String LEFT_ID = "ID0";
  public static final String RIGHT_ID = "ID1";
  public static final String ASSIGNED_ID = "ASSIGNED_ID";

  protected Table links;

  private static Logger logger = LogManager.getLogger();

  protected GroundTruth() {
  }

  protected GroundTruth(Table links) {
    this.links = links;
  }

  public static GroundTruth createFromGlobalIds(Collection<Record> records) {
    return createFromRecordId(records.stream()
      .map(Record::getId)
      .collect(Collectors.toList()));
  }

  public static GroundTruth createFromRecordId(Collection<RecordId> recordIds) {
    return new GroundTruth(linksFromGlobalRecordIds(recordIds));
  }

  public static GroundTruth createFromLinksTable(Table links) {
    if (!links.columnNames().containsAll(List.of(LEFT_ID, RIGHT_ID))) {
      return createEmptyGroundTruth();
    }
    return new GroundTruth(links);
  }

  public static GroundTruth createFromLinks(Collection<RecordIdPair> links) {
    return new GroundTruth(linksFromRecordPairs(links));
  }

  public static GroundTruth createEmptyGroundTruth() {
    Table links = Table.create(
      "Links",
      StringColumn.create(GroundTruth.LEFT_ID),
      StringColumn.create(GroundTruth.RIGHT_ID)
    );
    return new GroundTruth(links);
  }

  public List<RecordIdPair> getIdPairs() {
    return getExpectedLinks().stream()
      .map(GroundTruth::getRecordIdPair)
      .collect(Collectors.toList());
  }

  public Table getExpectedLinks() {
    return sortLinks(links);
  }

  public Table getAssignedIdTable() {
    return assignedIdTableFromExpectedLinks(
      getExpectedLinks()
    );
  }

  public static Table assignedIdTableFromExpectedLinks(Table links) {
    logger.debug("Creating AssignedId table from RecordIds");
    List<RecordIdPair> recordPairs = links.stream()
      .map(GroundTruth::getRecordIdPair)
      .collect(Collectors.toList());

    return assignedIdTableFromRecordIds(assignNewGlobalId(recordPairs));
  }

  private static RecordIdPairSimple getRecordIdPair(Row r) {
    return new RecordIdPairSimple(
      RecordIdComposed.ofComposed(r.getString(LEFT_ID)),
      RecordIdComposed.ofComposed(r.getString(RIGHT_ID))
    );
  }

  private static Collection<RecordId> assignNewGlobalId(Collection<RecordIdPair> idPairs) {
    List<RecordPair> recordPairs = idPairs.stream()
      .map(idPair -> new RecordPairSimple(
        new RecordSimple(idPair.getLeftRecordId()),
        new RecordSimple(idPair.getRightRecordId())
      ))
      .collect(Collectors.toList());
    Collection<Record> recordsWithGlobalIds = new ConnectedComponents().assignGlobalId(recordPairs);

    return recordsWithGlobalIds.stream()
      .map(Record::getId)
      .collect(Collectors.toList());
  }

  public static Table assignedIdTableFromRecordIds(Collection<RecordId> recordIds) {
    logger.debug("Creating AssignedId table from RecordIds");
    StringColumn colId = StringColumn.create(COMPOSED_ID);
    StringColumn colAssignedId = StringColumn.create(ASSIGNED_ID);
    recordIds.forEach(curId -> {
      Optional<String> assignedId = curId.getOptionalId(RecordId.GLOBAL_ID);
      if (assignedId.isPresent()) {
        colId.append(RecordIdComposed.toComposedId(curId.getLocalId(), curId.getSourceId()));
        colAssignedId.append(assignedId.get());
      } else {
        throw new RuntimeException("Missing GlobalId");
      }
    });
    return Table.create("Groups", colId, colAssignedId);
  }

  public static List<Record> applyAssignedIdTable(Collection<Record> records, Table groups) {
    Map<String, String> assignedIds = groups.stream()
      .collect(Collectors.toMap(r -> r.getString(COMPOSED_ID), r -> r.getString(ASSIGNED_ID)));
    return records.stream()
      .peek(r -> r.getId()
        .addId(
          RecordId.GLOBAL_ID,
          assignedIds.get(
            r.getId().getUniqueId()
          )
        )
      )
      .collect(Collectors.toList());
  }

  public static Table linksFromGlobalRecordIds(Collection<RecordId> recordIds) {
    return linksFromAssignedIdTable(
      assignedIdTableFromRecordIds(recordIds)
    );
  }

  public static Table linksFromRecordPairs(Collection<RecordIdPair> idPairs) {
    StringColumn colId0 = StringColumn.create(LEFT_ID);
    StringColumn colId1 = StringColumn.create(RIGHT_ID);
    idPairs.forEach(rp -> {
      List<String> idList = new ArrayList<>();
      idList.add(getComposedId(rp.getLeftRecordId()));
      idList.add(getComposedId(rp.getRightRecordId()));
      idList.sort(new ComposedIdComparator());
      colId0.append(idList.get(0));
      colId1.append(idList.get(1));
    });
    return Table.create("Links", colId0, colId1);
  }

  private static String getComposedId(RecordId id) {
    return RecordIdComposed.toComposedId(id.getLocalId(), id.getSourceId());
  }

  public static Table linksFromAssignedIdTable(Table raw) {
    logger.debug("Creating links table from AssignedId table");
    StringColumn colId0 = StringColumn.create(LEFT_ID);
    StringColumn colId1 = StringColumn.create(RIGHT_ID);

    List<TableSlice> slices = raw.splitOn(ASSIGNED_ID).getSlices();
    long c = 0;
    for (TableSlice slice : slices) {
      if (c % 1000 == 1) {
        logger.debug("Parsing slice " + c + "/" + slices.size());
      }
      int numDuplicates = slice.rowCount();
      if (numDuplicates > 100) {
        logger.debug("Num of duplicates: " + numDuplicates);
      }
      if (numDuplicates == 1) {
        continue;
      }
      for (int i = 0; i < numDuplicates; i++) {
        for (int j = i + 1; j < numDuplicates; j++) {
          List<String> idList = Arrays.asList(
            slice.getString(i, 0), slice.getString(j, 0)
          );
          idList.sort(new ComposedIdComparator());
          colId0.append(idList.get(0));
          colId1.append(idList.get(1));
        }
      }
      c++;
    }
    return Table.create("Links", colId0, colId1);
  }

  public static Table sortLinks(Table links) {
    Table sorted = links.emptyCopy();
    links.stream()
      .map(r -> new RowSorter().sort(r))
      .forEach(sorted::addRow);
//        sorted = sorted.sortOn(new GroundTruth.RowComparator());
    return sorted;
  }

  public static class RowComparator implements Comparator<Row> {
    private ComposedIdComparator comparator = new ComposedIdComparator();

    public int compare(Row r0, Row r1) {
      String id0 = r0.getString(GroundTruth.LEFT_ID);
      String id1 = r1.getString(GroundTruth.LEFT_ID);
      int ret = comparator.compare(id0, id1);
      if (ret == 0) {
        id0 = r0.getString(GroundTruth.RIGHT_ID);
        id1 = r1.getString(GroundTruth.RIGHT_ID);
        ret = comparator.compare(id0, id1);
      }
      return ret;
    }
  }

  public static class RowSorter {
    private ComposedIdComparator comparator = new ComposedIdComparator();

    public Row sort(Row r) {
      String id0 = r.getString(GroundTruth.LEFT_ID);
      String id1 = r.getString(GroundTruth.RIGHT_ID);
      if (comparator.compare(id0, id1) > 0) {
        r.setString(GroundTruth.LEFT_ID, id1);
        r.setString(GroundTruth.RIGHT_ID, id0);
      }
      return r;
    }
  }
}
