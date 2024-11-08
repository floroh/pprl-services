package de.unileipzig.dbs.pprl.core.common.model.impl;


import java.util.Objects;

/**
 * Table representation for serialization
 */
public class SerializableTable {

  private String name;

  private String[] header;

  private String[] types;

  private String[][] data = new String[0][0];

  public SerializableTable(String name) {
    this.name = name;
  }

  public SerializableTable(String name, String[] header, String[] types, String[][] data) {
    this.name = name;
    this.header = header;
    this.types = types;
    this.data = data;
  }

  public SerializableTable() {
  }

  public static SerializableTableBuilder builder() {
    return new SerializableTableBuilder();
  }

  public String getName() {
    return this.name;
  }

  public String[] getHeader() {
    return this.header;
  }

  public String[] getTypes() {
    return this.types;
  }

  public String[][] getData() {
    return this.data;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setHeader(String[] header) {
    this.header = header;
  }

  public void setTypes(String[] types) {
    this.types = types;
  }

  public void setData(String[][] data) {
    this.data = data;
  }

  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof SerializableTable)) {
      return false;
    }
    final SerializableTable other = (SerializableTable) o;
    if (!other.canEqual((Object) this)) {
      return false;
    }
    final Object this$name = this.getName();
    final Object other$name = other.getName();
    if (!Objects.equals(this$name, other$name)) {
      return false;
    }
    if (!java.util.Arrays.deepEquals(this.getHeader(), other.getHeader())) {
      return false;
    }
    if (!java.util.Arrays.deepEquals(this.getTypes(), other.getTypes())) {
      return false;
    }
    if (!java.util.Arrays.deepEquals(this.getData(), other.getData())) {
      return false;
    }
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof SerializableTable;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $name = this.getName();
    result = result * PRIME + ($name == null ? 43 : $name.hashCode());
    result = result * PRIME + java.util.Arrays.deepHashCode(this.getHeader());
    result = result * PRIME + java.util.Arrays.deepHashCode(this.getTypes());
    result = result * PRIME + java.util.Arrays.deepHashCode(this.getData());
    return result;
  }

  public String toString() {
    return "SerializableTable(name=" + this.getName() + ", header=" +
      java.util.Arrays.deepToString(this.getHeader()) + ", types=" +
      java.util.Arrays.deepToString(this.getTypes()) + ", data=" +
      java.util.Arrays.deepToString(this.getData()) + ")";
  }

  public static class SerializableTableBuilder {
    private String name;
    private String[] header;
    private String[] types;
    private String[][] data;

    SerializableTableBuilder() {
    }

    public SerializableTableBuilder name(String name) {
      this.name = name;
      return this;
    }

    public SerializableTableBuilder header(String[] header) {
      this.header = header;
      return this;
    }

    public SerializableTableBuilder types(String[] types) {
      this.types = types;
      return this;
    }

    public SerializableTableBuilder data(String[][] data) {
      this.data = data;
      return this;
    }

    public SerializableTable build() {
      return new SerializableTable(this.name, this.header, this.types, this.data);
    }

    public String toString() {
      return "SerializableTable.SerializableTableBuilder(name=" + this.name + ", header=" +
        java.util.Arrays.deepToString(this.header) + ", types=" + java.util.Arrays.deepToString(this.types) +
        ", data=" + java.util.Arrays.deepToString(this.data) + ")";
    }
  }
}
