package de.unileipzig.dbs.pprl.service.dataowner.data.mongo;

import de.unileipzig.dbs.pprl.service.common.data.dto.EncodingDto;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
public class MongoEncodingConfig {
  @Id
  private ObjectId _id;

  private EncodingDto encodingDto;
}
