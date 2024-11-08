package de.unileipzig.dbs.pprl.core.common.monitoring;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


public class TagTable {

  public static final String ID0 = "ID0";

  public static final String ID1 = "ID1";

  public static final String ATTRIBUTE = "attribute";
  public static final String TAG = "tag";
  public static final String TAG_STRING = "tagString";
  public static final String TAG_NUMERIC = "tagNumeric";

  private final Table table;

  public TagTable() {
    table = Table.create(
      "Tags",
      StringColumn.create(ID0),
      StringColumn.create(ID1),
      StringColumn.create(ATTRIBUTE),
      StringColumn.create(TAG),
      StringColumn.create(TAG_STRING),
      DoubleColumn.create(TAG_NUMERIC)
    );
  }

  private TagTable(Table table) {
    this.table = table;
  }

  public static TagTable create(Table table) {
    if (checkColumnTypes(table)) {
      return new TagTable(table);
    } else {
      throw new RuntimeException("Provided table is not a TagTable: " + table);
    }
  }

  public static TagTable create(List<Tag> tags) {
    TagTable tagTable = new TagTable();
    return tagTable.addTags(tags);
  }

  public TagTable append(TagTable other) {
    table.append(other.getInternalTagTable());
    return this;
  }

  public TagTable duplicate() {
    return new TagTable(table.copy());
  }

  public TagTable addTag(String id0, String id1, String tag) {
    return addTag(id0, id1, null, tag);
  }

  public TagTable addTag(String id0, String id1, String attribute, String tag) {
    return addTag(id0, id1, attribute, tag, null, null);
  }

  public TagTable addTag(String id0, String id1, String attribute, String tag, String stringValue,
    Double numericValue) {
    Row row = table.appendRow();
    row.setString(ID0, id0);
    row.setString(ID1, id1);
    row.setString(ATTRIBUTE, attribute);
    row.setString(TAG, tag);
    row.setString(TAG_STRING, stringValue);
    row.setDouble(TAG_NUMERIC, numericValue == null ? Double.NaN : numericValue);
    return this;
  }

  public TagTable addTags(Collection<Tag> tags) {
    tags.forEach(this::addTag);
    return this;
  }

  public TagTable addTag(Tag tag) {
    return addTag(
      tag.getId0(), tag.getId1(), tag.getAttribute(), tag.getTag(), tag.getStringValue(),
      tag.getNumericValue()
    );
  }

  public List<Tag> getTagList() {
    return table.stream()
      .map(r -> new Tag(
        r.getString(ID0),
        r.getString(ID1),
        r.getString(ATTRIBUTE),
        r.getString(TAG),
        r.getString(TAG_STRING),
        r.getDouble(TAG_NUMERIC)
      ))
      .collect(Collectors.toList());
  }

  public TagTable clear() {
    table.clear();
    return this;
  }

  public Table getAsTable() {
    return table.copy();
  }

  private Table getInternalTagTable() {
    return table;
  }

  private static boolean checkColumnTypes(Table table) {
    return new HashSet<>(table.columnNames()).containsAll(
      List.of(ID0, ID1, ATTRIBUTE, TAG, TAG_STRING, TAG_NUMERIC));
  }
}
