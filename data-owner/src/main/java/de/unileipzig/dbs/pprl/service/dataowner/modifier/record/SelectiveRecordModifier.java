package de.unileipzig.dbs.pprl.service.dataowner.modifier.record;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.selector.SelectAll;
import de.unileipzig.dbs.pprl.core.common.selector.Selector;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "@class")
public class SelectiveRecordModifier {

  private RecordModifier modifier;

  private Selector<Record> selector;

  public SelectiveRecordModifier(RecordModifier modifier) {
    this(modifier, new SelectAll<>());
  }

  public SelectiveRecordModifier(
    RecordModifier modifier, Selector<Record> selector) {
    this.modifier = modifier;
    this.selector = selector;
  }

  private SelectiveRecordModifier() {
  }

  public RecordModifier getModifier() {
    return modifier;
  }

  public void setModifier(RecordModifier modifier) {
    this.modifier = modifier;
  }

  public Selector<Record> getSelector() {
    return selector;
  }

  public void setSelector(Selector<Record> selector) {
    this.selector = selector;
  }
}
