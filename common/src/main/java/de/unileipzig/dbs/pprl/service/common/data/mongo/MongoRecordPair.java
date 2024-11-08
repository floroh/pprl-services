package de.unileipzig.dbs.pprl.service.common.data.mongo;

import de.unileipzig.dbs.pprl.core.common.RecordUtils;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.AbstractRecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static de.unileipzig.dbs.pprl.core.matcher.model.api.LinkageProcessDataSet.ACTIVE;

@Document
public class MongoRecordPair extends AbstractRecordPair {

  @Id
  private ObjectId _id;

  @Indexed
  private ObjectId projectId;

  @Indexed
  private Set<String> properties = new HashSet<>();

  @Indexed
  private String pairId;

  private MongoRecord leftRecord;

  private MongoRecord rightRecord;

  public MongoRecordPair(MongoRecord leftRecord, MongoRecord rightRecord) {
    super();
    this.leftRecord = leftRecord;
    this.rightRecord = rightRecord;
    properties.add(ACTIVE);
    pairId = RecordUtils.getPairId(leftRecord.getId().getUniqueLikeId(), rightRecord.getId().getUniqueLikeId());
  }

  public MongoRecordPair(MongoRecord leftRecord, MongoRecord rightRecord, double similarity,
    MatchGrade classification) {
    super(similarity, classification);
    this.leftRecord = leftRecord;
    this.rightRecord = rightRecord;
    properties.add(ACTIVE);
    pairId = RecordUtils.getPairId(leftRecord.getId().getUniqueLikeId(), rightRecord.getId().getUniqueLikeId());
  }

  private MongoRecordPair() {
  }

  public ObjectId getProjectId() {
    return projectId;
  }

  public void setProjectId(ObjectId projectId) {
    this.projectId = projectId;
  }

  public void set_id(ObjectId _id) {
    this._id = _id;
  }

  public Set<String> getProperties() {
    if (properties == null) {
      return new HashSet<>();
    }
    return properties;
  }

  public void setProperties(Set<String> properties) {
    this.properties = properties;
  }

  public void addProperty(String property) {
    if (properties == null) {
      properties = new HashSet<>();
    }
    properties.add(property);
  }

  public void removeProperty(String property) {
    if (properties != null) {
      properties.remove(property);
    }
  }

  @Override
  public MongoRecord getLeftRecord() {
    return leftRecord;
  }

  @Override
  public RecordPair setLeftRecord(Record record) {
    leftRecord = (MongoRecord) record;
    return this;
  }

  @Override
  public MongoRecord getRightRecord() {
    return rightRecord;
  }

  @Override
  public RecordPair setRightRecord(Record record) {
    rightRecord = (MongoRecord) record;
    return this;
  }

  @Override
  public String getPairId() {
    return pairId;
  }

  @Override
  public MongoRecordPair duplicate() {
    MongoRecordPair duplicate =
      new MongoRecordPair(leftRecord.duplicate(), rightRecord.duplicate(), similarity, classification);
    getTags().forEach(duplicate::addTag);
    getAttributeSimilarities().ifPresent(as -> duplicate.setAttributeSimilarities(new HashMap<>(as)));
    getProperties().forEach(duplicate::addProperty);
    duplicate.setProjectId(projectId);
    return duplicate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MongoRecordPair that = (MongoRecordPair) o;

    if (Double.compare(that.similarity, similarity) != 0) {
      return false;
    }
    if (!leftRecord.getId().equals(that.leftRecord.getId())) {
      if (leftRecord.getId().equals(that.rightRecord.getId()) &&
        (rightRecord.getId().equals(that.leftRecord.getId()))) {
        return true;
      }
      return false;
    }
    if (!rightRecord.getId().equals(that.rightRecord.getId())) {
      if (rightRecord.getId().equals(that.leftRecord.getId()) &&
        (leftRecord.getId().equals(that.rightRecord.getId()))) {
        return true;
      }
      return false;
    }
    if (classification != that.classification) {
      return false;
    }
    return Objects.equals(attributeSimilarities, that.attributeSimilarities);
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = prime * leftRecord.getId().getUniqueId().hashCode();
    result = result + prime * rightRecord.getId().getUniqueId().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "MongoRecordPair{" +
      "_id=" + _id +
      ", projectId=" + projectId +
      ", properties=" + properties +
      ", similarity=" + similarity +
      ", classification=" + classification +
      ", pairId='" + pairId + '\'' +
      ", attributeSimilarities=" + attributeSimilarities +
      ", leftRecord=" + leftRecord +
      ", rightRecord=" + rightRecord +
      ", tags=" + tags +
      '}';
  }
}
