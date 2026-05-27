package de.unileipzig.dbs.pprl.service.common.data.mongo;

import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MongoTag {

  @Id
  private ObjectId objectId;

  @Indexed
  private long datasetId;

  @Indexed
  private String type;

  @Indexed
  private String origin;

  @NonNull
  private Tag tag;

  public static MongoTag create(long datasetId, @NonNull Tag tag) {
    MongoTag mongoTag = new MongoTag();
    mongoTag.datasetId = datasetId;
    mongoTag.setTag(tag);
    return mongoTag;
  }

  public void setTag(Tag tag) {
    this.tag = tag;
    this.type = this.tag.getType();
    this.origin = this.tag.getOrigin();
  }

  //  @JsonIgnore
//  public TagTable getTagTable() {
//    return TagTable.create(TableSerialization.fromDefaultSerializableTable(serializableTable));
//  }
//
//  @JsonIgnore
//  public void setTagTable(TagTable tagTable) {
//    this.serializableTable = TableSerialization.toDefaultSerializableTable(tagTable.getAsTable());
//  }
}
