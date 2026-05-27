package de.unileipzig.dbs.pprl.service.protocol.analysis;

import de.unileipzig.dbs.pprl.core.matcher.evaluation.GroundTruth;
import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityCheck;
import tech.tablesaw.aggregate.StringAggregateFunction;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import static de.unileipzig.dbs.pprl.core.common.monitoring.TagTable.LINK_TAG;

public class AnalyzerUtils {

  public static final String OVERALL = "OVERALL";
  public static final String PAIRSIM = "PAIRSIM";
  public static final String COMBINED_TAGS = "tag-list";

  public static Table addCombinedTag(Table results) {
    Table tab = results.summarize(LINK_TAG, stringConcat)
      .by(GroundTruth.LEFT_ID, GroundTruth.RIGHT_ID, QualityCheck.LINK_LABEL);
    tab.column(stringConcat.functionName() + " [" + LINK_TAG + "]")
      .setName(COMBINED_TAGS);
    return tab;
  }

  public static final StringAggregateFunction stringConcat = new StringAggregateFunction("String Concat") {
    @Override
    public String summarize(StringColumn column) {
      return String.join("|", column.asList());
    }
  };
}
