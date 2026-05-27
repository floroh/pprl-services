package de.unileipzig.dbs.pprl.service.dataowner.plotter;

import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.LinePlot;
import tech.tablesaw.plotly.api.ScatterPlot;
import tech.tablesaw.plotly.api.VerticalBarPlot;
import tech.tablesaw.plotly.components.*;
import tech.tablesaw.plotly.traces.HistogramTrace;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.table.TableSliceGroup;

import java.util.List;

public class GenericPlotting {

  public static Figure plotHistogram(String title, Table table, String numericColumnName,
    String groupCol) {
    TableSliceGroup tables = table.splitOn(table.categoricalColumn(groupCol));
    Layout layout = Layout.builder(title).showLegend(true).build();
    HistogramTrace[] traces = new HistogramTrace[tables.size()];
    List<Table> tableList = tables.asTableList();
    for (int i = 0; i < tables.size(); i++) {
      traces[i] =
        HistogramTrace.builder(tableList.get(i).numberColumn(numericColumnName).asDoubleArray())
          .showLegend(true)
          .name(tableList.get(i).name())
          .build();
    }
    return new Figure(layout, traces);
  }

  public static void plotScatter(String name, Table table, String xCol, String yCol, String groupCol) {
    Plot.show(
      ScatterPlot.create(name,
        table.sortAscendingOn(groupCol)
        , xCol, yCol, groupCol
      )
    );
  }

  public static void plotLine(String name, Table table, String xCol, String yCol) {
    Figure figure = LinePlot.create(name,
      table
//        table.sortAscendingOn(groupCol)
      , xCol, yCol
    );
    Plot.show(figure);
  }

  public static void plotLineAndMarkers(String name, Table table, String xCol, String yCol, String groupCol) {
    TableSliceGroup tables = table.splitOn(table.categoricalColumn(groupCol));
    Layout layout = Layout.builder(name, xCol, yCol).showLegend(true).build();

    ScatterTrace[] traces = new ScatterTrace[tables.size()];
    for (int i = 0; i < tables.size(); i++) {
      List<Table> tableList = tables.asTableList();
      traces[i] =
        ScatterTrace.builder(
            tableList.get(i).numberColumn(xCol), tableList.get(i).numberColumn(yCol))
          .showLegend(true)
          .name(tableList.get(i).name())
          .mode(ScatterTrace.Mode.LINE_AND_MARKERS)
          .build();
    }
    Figure figure = new Figure(layout, traces);
    Plot.show(figure);
  }

  public static void plotLineRecall(String title, Table table, String xCol, String yCol, String groupCol) {
    TableSliceGroup tables = table.splitOn(table.categoricalColumn(groupCol));

    Layout layout = Layout.builder(title, xCol, yCol)
      .showLegend(true)
      .width(900)
      .height(600)
      .xAxis(Axis.builder()
          .showGrid(true)
          .title("")
          .gridWidth(3)
          .tickSettings(
            TickSettings.builder()
              .font(Font.builder().size(20).build())
              .tickMode(TickSettings.TickMode.AUTO)
//            .dTick(0.05)
              .build()
          ).build()
      )
      .yAxis(Axis.builder()
        .showGrid(true)
        .title("")
        .gridWidth(3)
        .range(0.3, 1.0)
        .tickSettings(
          TickSettings.builder()
            .font(Font.builder().size(20).build())
            .tickMode(TickSettings.TickMode.AUTO)
            .build()
        )
        .build()
      ).build();

    ScatterTrace[] traces = new ScatterTrace[tables.size()];
    for (int i = 0; i < tables.size(); i++) {
      List<Table> tableList = tables.asTableList();
      traces[i] =
        ScatterTrace.builder(
            tableList.get(i).numberColumn(xCol), tableList.get(i).numberColumn(yCol))
          .showLegend(true)
          .name(tableList.get(i).name())
          .mode(ScatterTrace.Mode.LINE)
          .build();
    }
    Figure figure = new Figure(layout, traces);

//    Figure figure = LinePlot.create(name,
//      table
////        table.sortAscendingOn(groupCol)
//      , xCol, yCol, groupCol
//    );
    Plot.show(figure);
  }

  public static void plotBar(String name, Table table, String xCol, String yCol) {
    Plot.show(
      VerticalBarPlot.create(name, table, xCol, yCol)
    );
  }
}
