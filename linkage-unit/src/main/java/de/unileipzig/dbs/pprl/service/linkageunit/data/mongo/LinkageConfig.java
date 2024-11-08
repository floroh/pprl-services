package de.unileipzig.dbs.pprl.service.linkageunit.data.mongo;

import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.MatchingDto;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
public class LinkageConfig {

  @Id
  private ObjectId _id;

  private boolean partial;

  private String componentName;

  private MatchingDto matchingDto;
}
