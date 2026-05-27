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

package de.unileipzig.dbs.pprl.service.common.data;

import de.unileipzig.dbs.pprl.core.common.model.api.DataSet;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagTable;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;

import java.util.Collection;
import java.util.Optional;

public class DefaultDataSet implements DataSet {

  protected Collection<Record> records;
  private TagTable tagTable = null;
  private GroundTruth groundTruth = null;

  public DefaultDataSet(Collection<Record> records) {
    this.records = records;
  }

  public DefaultDataSet(DataSet dataSet) {
    this(dataSet.getAllRecords());
    dataSet.getTagTable().ifPresent(this::setTagTable);
  }

  @Override
  public void addRecord(Record record) {
    records.add(record);
  }

  @Override
  public Optional<Record> getRecord(RecordId id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Collection<Record> getAllRecords() {
    return records;
  }

  @Override
  public Collection<Record> getRecordsBySource(String sourceName) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void setGroundTruth(GroundTruth groundTruth) {
    this.groundTruth = groundTruth;
  }

  public Optional<GroundTruth> getGroundTruth() {
    return Optional.ofNullable(groundTruth);
  }


  @Override
  public void setTagTable(TagTable tagTable) {
    this.tagTable = tagTable;
  }

  @Override
  public Optional<TagTable> getTagTable() {
    if (tagTable == null || tagTable.getTagList().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(tagTable);
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
