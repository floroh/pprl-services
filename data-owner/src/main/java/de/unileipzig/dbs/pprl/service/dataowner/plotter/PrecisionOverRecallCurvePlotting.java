package de.unileipzig.dbs.pprl.service.dataowner.plotter;

import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityCheck;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;

public class PrecisionOverRecallCurvePlotting {

  public static Figure plot(Table results) {
    String xCol = QualityCheck.RECALL;
    String yCol = QualityCheck.PRECISION;
    Layout layout = Layout.builder("Precision-Recall-Curve", xCol, yCol).build();
    ScatterTrace trace =
      ScatterTrace.builder(results.numberColumn(xCol), results.numberColumn(yCol))
        .mode(ScatterTrace.Mode.LINE_AND_MARKERS).build();
    return new Figure(layout, trace);
  }
}
