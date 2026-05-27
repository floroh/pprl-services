package de.unileipzig.dbs.pprl.core.matcher;

import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.monitoring.Tag;

public class TagUtils {

  public static final String TAG_MATCHING_METHOD = "MATCHING_METHOD";

  public static Tag create(RecordPair pair, String attribute, String tag, String stringValue,
                           Double numericValue) {
    return new Tag(pair.getLeftRecord().getId().getUniqueLikeId(), pair.getRightRecord().getId().getUniqueLikeId(),
      attribute,
      tag,
      stringValue,
      numericValue,
            null, null
    );
  }

  public static Tag create(RecordPair pair, String attribute, String tag) {
    return new Tag(pair.getLeftRecord().getId().getUniqueLikeId(), pair.getRightRecord().getId().getUniqueLikeId(),
      attribute,
      tag,
      null,
      null,
            null, null
    );
  }

  public static Tag addIDs(Tag tag, RecordPair pair) {
    tag.setId0(pair.getLeftRecord().getId().getUniqueLikeId());
    tag.setId1(pair.getRightRecord().getId().getUniqueLikeId());
    return tag;
  }

  public static boolean isFromClericalReview(RecordPair rp) {
    return rp.getTags().stream()
            .filter(tag -> tag.getTag().equals(TAG_MATCHING_METHOD))
            .anyMatch(tag -> tag.getStringValue().contains("CR"));
  }
}
