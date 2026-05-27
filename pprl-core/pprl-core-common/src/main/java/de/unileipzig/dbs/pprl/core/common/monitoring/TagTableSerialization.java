package de.unileipzig.dbs.pprl.core.common.monitoring;

import tech.tablesaw.api.Table;

import java.util.Collection;

public class TagTableSerialization {
    public static Table convertToTable(Collection<Tag> tags) {
        TagTable tagTable = TagTable.create(tags);
        return tagTable.getAsTable();
//        StringColumn id0Column = StringColumn.create("id0");
//        StringColumn id1Column = StringColumn.create("id1");
//        StringColumn attributeColumn = StringColumn.create("attribute");
//        StringColumn tagColumn = StringColumn.create("tag");
//        StringColumn stringValueColumn = StringColumn.create("stringValue");
//        DoubleColumn numericValueColumn = DoubleColumn.create("numericValue");
//
//        for (Tag tag : tags) {
//            id0Column.append(tag.getId0());
//            id1Column.append(tag.getId1());
//            attributeColumn.append(tag.getAttribute());
//            tagColumn.append(tag.getTag());
//            stringValueColumn.append(tag.getStringValue());
//            numericValueColumn.append(tag.getNumericValue());
//        }
//
//        return Table.create("Tags Table", id0Column, id1Column, attributeColumn, tagColumn, stringValueColumn, numericValueColumn);
    }

    public static Collection<Tag> convertFromTable(Table table) {
        TagTable tagTable = TagTable.create(table);
        return tagTable.getTagList();
//        Set<Tag> tags = new HashSet<>();
//
//        StringColumn id0Column = table.stringColumn("id0");
//        StringColumn id1Column = table.stringColumn("id1");
//        StringColumn attributeColumn = table.stringColumn("attribute");
//        StringColumn tagColumn = table.stringColumn("tag");
//        StringColumn stringValueColumn = table.stringColumn("stringValue");
//        DoubleColumn numericValueColumn = table.doubleColumn("numericValue");
//
//        for (int i = 0; i < table.rowCount(); i++) {
//            Tag tag = new Tag(
//                    id0Column.get(i),
//                    id1Column.get(1),
//                    attributeColumn.get(i),
//                    tagColumn.get(i),
//                    stringValueColumn.get(i),
//                    numericValueColumn.get(i)
//            );
//            tags.add(tag);
//        }
//
//        return tags;
    }
}
