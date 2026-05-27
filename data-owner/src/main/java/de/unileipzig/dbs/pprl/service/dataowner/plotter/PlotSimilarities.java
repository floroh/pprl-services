package de.unileipzig.dbs.pprl.service.dataowner.plotter;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.components.*;
import tech.tablesaw.plotly.traces.HistogramTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.util.List;
import java.util.stream.IntStream;

import static de.unileipzig.dbs.pprl.core.matcher.evaluation.EvalConstants.*;


public class PlotSimilarities {

  public static void plotSimByLen(String name, List<List<DoubleColumn>> in) {
    Layout layout = Layout.builder()
      .title("Similarities for " + name)
      .xAxis(Axis.builder()
//        .range(-0.02, 1.02)
          .range(0.58, 1.02)
          .title("Similarity value")
          .gridWidth(2)
          .gridColor("black")
          .font(Font.builder()
            .size(30)
            .build()
          )
          .build()
      )
      .yAxis(Axis.builder()
//										.title("#Pairs")
          .gridWidth(1)
          .gridColor("dark gray")
          .font(Font.builder()
            .size(30)
            .build())
          .build()
      )
      .grid(Grid.builder()
        .columns(1)
        .rows(in.size())
        .pattern(Grid.Pattern.COUPLED)
        .build()
      )
//						.showLegend(false)
      .height(700)
      .width(768)
      .build();
    Trace[] traces = IntStream.range(0, in.size())
      .boxed()
      .flatMap(i -> in.get(i).stream()
        .map(curCol -> buildHistogramTrace(curCol, String.valueOf(i + 1))))
      .toArray(Trace[]::new);
    Plot.show(new Figure(layout, traces));
  }

  public static void plotSimilarities(String name, List<DoubleColumn> in) {
    Layout layout = Layout.builder()
      .title("Similarities for " + name)
      .xAxis(Axis.builder()
        .range(0.58, 1)
        .title("Similarity value")
        .build()
      )
      .height(600)
      .width(900)
      .xAxis(Axis.builder()
        .showGrid(true)
        .title("")
        .gridWidth(3)
        .tickSettings(
          TickSettings.builder()
            .tickMode(TickSettings.TickMode.AUTO)
            .font(Font.builder().size(20).build())
            .build()
        ).build()
      )
      .yAxis(Axis.builder()
        .showGrid(true)
        .title("")
        .gridWidth(3)
//        .type(Axis.Type.LOG)
//        .range(0, 300000)
        .tickSettings(
          TickSettings.builder()
            .font(Font.builder().size(20).build())
            .tickMode(TickSettings.TickMode.AUTO)
            .build()
        )
        .build()
      )
      .build();
    Trace[] traces = in.stream()
      .map(curCol -> buildHistogramTrace(curCol, ""))
      .toArray(Trace[]::new);
    Plot.show(new Figure(layout, traces));
  }

  private static Trace buildHistogramTrace(DoubleColumn column, String iy) {
    String color = null;
    if (column.name().contains(PAIR_TYPE_NON_MATCH_RANDOM)) {
      color = "red";
    } else if (column.name().contains(PAIR_TYPE_NON_MATCH_BLOCKED)) {
      color = "blue";
    } else if (column.name().contains(PAIR_TYPE_MATCH)) {
      color = "green";
    }
    Marker.MarkerBuilder markerB = Marker.builder();
    if (color != null) {
      markerB.color(color);
    }

    return HistogramTrace.builder(column)
      .name(column.name())
      .nBinsX(20)
//						.nBinsY(20)
      .xAxis("x")
      .yAxis("y" + iy)
      .marker(markerB.build())
      .build();
  }
}
