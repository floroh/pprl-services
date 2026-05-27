package de.unileipzig.dbs.pprl.service.common.csv;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordIdDto;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvRowConverter {

  public RecordDto rowToRecordDto(Map<String, String> row) {
    if (row == null || row.isEmpty()) {
      return null;
    }

    RecordIdDto id = buildRecordId(row);

    RecordDto.RecordDtoBuilder builder = RecordDto.builder();
    if (id != null) {
      builder.id(id);
    }

    for (Map.Entry<String, String> e : row.entrySet()) {
      String col = e.getKey();
      String raw = e.getValue();
      String val = emptyToNull(raw);
      if (val == null) continue; // skip empty

      // skip id.* since already handled
      if (col != null && col.startsWith("id.")) continue;

      String attrKey = tryResolveToEnumName(col);
      builder.attribute(attrKey, toAttributeDto(val));
    }

    return builder.build();
  }


  /**
   * Build a RecordIdDto from columns like:
   * id.local, id.source, id.global, id.unique, id.blocks
   * (blocks may be comma-separated list)
   */
  private RecordIdDto buildRecordId(Map<String, String> row) {
    RecordIdDto.RecordIdDtoBuilder idBuilder = RecordIdDto.builder();
    boolean hasAny = false;

    String local = emptyToNull(row.get("id.local"));
    String source = emptyToNull(row.get("id.source"));
    String global = emptyToNull(row.get("id.global"));
    String unique = emptyToNull(row.get("id.unique"));
    String blocks = emptyToNull(row.get("id.blocks"));

    if (local != null) { idBuilder.local(local); hasAny = true; }
    if (source != null) { idBuilder.source(source); hasAny = true; }
    if (global != null) { idBuilder.global(global); hasAny = true; }
    if (unique != null) { idBuilder.unique(unique); hasAny = true; }

    if (blocks != null) {
      List<String> blockList = Arrays.stream(blocks.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .collect(Collectors.toList());
      idBuilder.blocks(blockList);
      if (!blockList.isEmpty()) hasAny = true;
    }

    return hasAny ? idBuilder.build() : null;
  }

  /**
   * Try to convert column name to PersonalAttributeType enum.
   * If successful, return the enum name (PersonalAttributeType.NAME.name()).
   * Otherwise return the original column name.
   *
   * Normalization strategy:
   *  - trim
   *  - uppercase
   *  - replace non-alphanumeric characters with underscore
   *
   * This keeps the attempt robust for column names like "firstName", "FIRST-NAME", "FIRST_NAME".
   */
  private static String tryResolveToEnumName(String columnName) {
    if (columnName == null) return columnName;

    String normalized = columnName.trim()
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9]", "_"); // non-alphanumerics -> underscore

    try {
      PersonalAttributeType pat = PersonalAttributeType.valueOf(normalized);
      return pat.name(); // use enum name as attribute key
    } catch (IllegalArgumentException ex) {
      // not an enum constant -> use original column name unchanged
      return columnName;
    }
  }

  private static String emptyToNull(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }

  private static AttributeDto toAttributeDto(String value) {
    return AttributeDto.builder().type("STRING").value(value).build();
  }
}