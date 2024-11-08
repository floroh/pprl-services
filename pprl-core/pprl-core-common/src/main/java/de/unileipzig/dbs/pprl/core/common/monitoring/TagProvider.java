package de.unileipzig.dbs.pprl.core.common.monitoring;

import java.util.List;

public interface TagProvider {

  /**
   * Implementations of this interface must either override this method or {@link #provideTagTable}
   */
  default List<Tag> provideTags() {
    return provideTagTable().getTagList();
  }

  /**
   * Implementations of this interface must either override this method or {@link #provideTags()}
   */
  default TagTable provideTagTable() {
    return TagTable.create(provideTags());
  }

}
