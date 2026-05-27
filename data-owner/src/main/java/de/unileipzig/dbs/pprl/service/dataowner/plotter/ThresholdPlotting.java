package de.unileipzig.dbs.pprl.service.dataowner.plotter;

import de.unileipzig.dbs.pprl.core.matcher.evaluation.QualityCheck;
import de.unileipzig.dbs.pprl.service.dataowner.modifier.config.SingleThreshold;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.*;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ThresholdPlotting {

  public static Figure getTracePlot(Table tab) {

    if (tab.column(SingleThreshold.KEY).type() == ColumnType.STRING) {
      DoubleColumn col = DoubleColumn.create(SingleThreshold.KEY);
      tab.stringColumn(SingleThreshold.KEY).asList().stream()
        .map(Double::parseDouble)
        .forEach(col::append);
      tab.replaceColumn(col);
    }
    Double[] x = getArray(tab.doubleColumn(SingleThreshold.KEY));

    Layout layout = Layout.builder(tab.name(), "Threshold", "Quality")
      .width(800)
      .height(600)
      .xAxis(Axis.builder()
        .showGrid(true)
        .title("")
        .gridWidth(3)
        .tickSettings(
          TickSettings.builder()
            .tickMode(TickSettings.TickMode.ARRAY)
            .font(Font.builder().size(30).build())
            .arrayTicks(toDouble(x))
            .build()
        ).build()
      )
      .yAxis(Axis.builder()
        .showGrid(true)
        .title("")
        .gridWidth(3)
        .tickSettings(
          TickSettings.builder()
            .font(Font.builder().size(30).build())
            .tickMode(TickSettings.TickMode.AUTO)
            .build()
        )
        .build()
      )
//						.grid(Grid.builder()
//										.columns(2)
//										.rows(2)
//										.xSide(Grid.XSide.BOTTOM)
//										.ySide(Grid.YSide.LEFT)
//										.pattern(Grid.Pattern.INDEPENDENT)
//										.build()
//						)
      .build();

    Trace[] traces = tab.columnNames().stream()
      .filter(s -> !s.contains(SingleThreshold.KEY))
      .map(s -> getLineTrace(tab, s))
      .toArray(Trace[]::new);

    return new Figure(layout, traces);
  }

  private static Trace getLineTrace(Table tab, String yCol) {
    Line.Dash dash = Line.Dash.SOLID;
    Symbol marker = Symbol.CIRCLE;
//    String color = "blue";
    double width = 8;
    if (yCol.contains(QualityCheck.RECALL)) {
      dash = Line.Dash.LONG_DASH;
      marker = Symbol.TRIANGLE_UP;
      width /= 2;
    }
    if (yCol.contains(QualityCheck.PRECISION)) {
      dash = Line.Dash.DOT;
      marker = Symbol.SQUARE;
      width /= 2;
    }
//		if (yCol.contains("F-Measure")) dash = Line.Dash.SOLID;
    String color = yCol.contains("(BF)") ? "red" : "blue";
    return ScatterTrace.builder(
      tab.numberColumn(SingleThreshold.KEY),
      tab.numberColumn(yCol)
    )
      .name(yCol)
      .marker(
        Marker.builder()
          .symbol(marker)
          .build())
      .line(Line.builder()
        .dash(dash)
        .color(color)
        .width(width)
        .build()
      )
//						.xAxis("x2")
//						.yAxis("y2")
      .mode(ScatterTrace.Mode.LINE_TEXT_AND_MARKERS)
      .build();
  }

  private static Double[] getArray(DoubleColumn col) {
    return col.asList().stream().distinct().collect(Collectors.toList()).toArray(new Double[] {});
  }

  private static double[] toDouble(Integer[] arr) {
    return Arrays.stream(arr).mapToDouble(i -> (double) i).toArray();
  }


  private static double[] toDouble(Double[] arr) {
    return Arrays.stream(arr).mapToDouble(i -> (double) i).toArray();
  }
}
