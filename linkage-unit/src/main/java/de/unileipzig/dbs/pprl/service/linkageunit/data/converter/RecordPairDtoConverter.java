package de.unileipzig.dbs.pprl.service.linkageunit.data.converter;

import de.unileipzig.dbs.pprl.core.common.model.impl.MatchGrade;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordPairSimple;
import de.unileipzig.dbs.pprl.service.common.data.converter.AbstractRecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.converter.RecordConverter;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecord;
import de.unileipzig.dbs.pprl.service.common.data.mongo.MongoRecordPair;
import de.unileipzig.dbs.pprl.service.linkageunit.data.dto.RecordPairDto;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class RecordPairDtoConverter {

  public static RecordPairDto convertRecordPairToDto(RecordPair recordPair) {
    Map<String, Double> attributeSimilarities = recordPair.getAttributeSimilarities().orElse(null);
    if (attributeSimilarities != null) {
      Set<String> attrNames = new HashSet<>(attributeSimilarities.keySet());
      for (String attrName : attrNames) {
        if (attributeSimilarities.get(attrName).isNaN()) {
          attributeSimilarities.remove(attrName);
        }
      }
    }
    RecordPairDto dto = RecordPairDto.builder()
      .id0(AbstractRecordConverter.fromRecordId(recordPair.getLeftRecord().getId()))
      .id1(AbstractRecordConverter.fromRecordId(recordPair.getRightRecord().getId()))
      .matchGrade(recordPair.getClassification().toString())
      .similarity(recordPair.getSimilarity())
      .tags(new ArrayList<>(recordPair.getTags()))
      .attributeSimilarities(attributeSimilarities)
      .build();
    if (recordPair instanceof MongoRecordPair) {
      Optional.ofNullable(((MongoRecord) recordPair.getLeftRecord()).getObjectId())
        .ifPresent(objectId -> dto.getId0().setUnique(objectId.toHexString()));
      Optional.ofNullable(((MongoRecord) recordPair.getRightRecord()).getObjectId())
        .ifPresent(objectId -> dto.getId1().setUnique(objectId.toHexString()));
//      dto.getId0().setUnique(objectId.toHexString());
//      dto.getId1().setUnique(((MongoRecord)recordPair.getRightRecord()).getObjectId().toHexString());
      dto.setProperties(((MongoRecordPair) recordPair).getProperties());
      dto.setProjectId(((MongoRecordPair) recordPair).getProjectId().toHexString());
    }
    return dto;
  }

  public static RecordPair convertDtoToRecordPair(RecordPairDto dto) {
    return new RecordPairSimple(
      AbstractRecordConverter.emptyRecord(dto.getId0()),
      AbstractRecordConverter.emptyRecord(dto.getId1()),
      dto.getSimilarity(),
      MatchGrade.valueOf(dto.getMatchGrade())
    );
  }

  public static MongoRecordPair convertDtoToMongoRecordPair(RecordPairDto dto) {
    MongoRecordPair mongoRecordPair = new MongoRecordPair(
      new MongoRecord(-1, RecordConverter.toRecordId(dto.getId0())),
      new MongoRecord(-1, RecordConverter.toRecordId(dto.getId1()))
    );
    if (dto.getMatchGrade() != null) {
      mongoRecordPair.setClassification(MatchGrade.valueOf(dto.getMatchGrade()));
    }
    mongoRecordPair.setProjectId(new ObjectId(dto.getProjectId()));
    mongoRecordPair.setSimilarity(dto.getSimilarity());
    Optional.ofNullable(dto.getProperties()).ifPresent(mongoRecordPair::setProperties);
    Optional.ofNullable(dto.getTags()).ifPresent(tags -> tags.forEach(mongoRecordPair::addTag));
    Optional.ofNullable(dto.getAttributeSimilarities()).ifPresent(mongoRecordPair::setAttributeSimilarities);
    return mongoRecordPair;
  }
}
