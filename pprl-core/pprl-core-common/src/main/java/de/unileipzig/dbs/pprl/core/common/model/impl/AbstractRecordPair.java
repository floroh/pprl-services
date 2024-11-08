package de.unileipzig.dbs.pprl.core.common.model.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractRecordPair implements RecordPair {

  protected double similarity;

  protected MatchGrade classification;

  protected Map<String, Double> attributeSimilarities;

  protected Set<Tag> tags;

  public AbstractRecordPair(double similarity, MatchGrade classification) {
    this.similarity = similarity;
    this.classification = classification;
  }

  protected AbstractRecordPair() {
    this(-1.0, MatchGrade.UNKNOWN);
  }

  @Override
  public abstract Record getRightRecord();

  @Override
  public abstract Record getLeftRecord();

  @Override
  public RecordId getLeftRecordId() {
    return getLeftRecord().getId();
  }

  @Override
  public RecordId getRightRecordId() {
    return getRightRecord().getId();
  }

  @Override
  public double getSimilarity() {
    return similarity;
  }

  @Override
  public RecordPair setSimilarity(double similarity) {
    this.similarity = similarity;
    return this;
  }

  @Override
  public MatchGrade getClassification() {
    return classification;
  }

  @Override
  public RecordPair setClassification(MatchGrade classification) {
    this.classification = classification;
    return this;
  }

  @Override
  public Optional<Map<String, Double>> getAttributeSimilarities() {
    return Optional.ofNullable(attributeSimilarities);
  }

  @Override
  public RecordPair setAttributeSimilarities(Map<String, Double> attributeSimilarities) {
    this.attributeSimilarities = attributeSimilarities;
    return this;
  }

  @Override
  public RecordPair addTag(Tag tag) {
    if (tags == null) {
      tags = new HashSet<>();
    }
    boolean isAdded = tags.add(tag);
    if (!isAdded) {
      //Replace tag if already exists
      tags.remove(tag);
      tags.add(tag);
    }
    return this;
  }

  @Override
  public RecordPair removeTag(Tag tag) {
    if (tags == null) {
      return this;
    }
    tags.remove(tag);
    return this;
  }

  @Override
  public Collection<Tag> getTags() {
    if (tags == null) {
      tags = new HashSet<>();
    }
    return tags;
  }
}
