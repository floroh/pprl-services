/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unileipzig.dbs.pprl.core.matcher.evaluation;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class QualityResult extends EvaluationResult {

	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String CSV_HEADER_RECALL = "recall";
	public static final String CSV_HEADER_RECALL_WEIGHTED = "recall (w)";
	public static final String CSV_HEADER_PRECISION = "precision";
	public static final String CSV_HEADER_PRECISION_WEIGHTED = "precision(p)";
	public static final String CSV_HEADER_F1 = "F1-score";
	public static final String CSV_HEADER_F1_WEIGHTED = "F1-score (w)";
	public static final String CSV_HEADER_PR_AUC = "PR-AUC";
	public static final String CSV_HEADER_ACCURACY = "Accuracy";
	public static final String CSV_HEADER_ACCURACY_WEIGHTED = "Accuracy (w)";
	public static final String CSV_HEADER_MCC = "MCC";
	public static final String CSV_HEADER_TP = "TP";
	public static final String CSV_HEADER_TN = "TN";
	public static final String CSV_HEADER_FP = "FP";
	public static final String CSV_HEADER_FN = "FN";
	public static final String CSV_HEADER_FP_Singleton = "FPs";
	public static final String CSV_HEADER_FP_Duplicates = "FPd";

	private long truePos;
	private long trueNeg;
	private long falsePos;
	private long falseNeg;
	private long falsePosSingleton;
	private long falsePosDuplicates;

	private double weightedTruePos;
	private double weightedTrueNeg;
	private double weightedFalsePos;
	private double weightedFalseNeg;
	private double areaUnderCurveOfPrecisionRecall;

	public QualityResult(long truePos, long falsePos, long falseNeg) {
		super();
		this.truePos = truePos;
		this.falsePos = falsePos;
		this.falseNeg = falseNeg;
	}

	public QualityResult() {
	}

	@Override
	public Map<String, BigDecimal> getMetrics() {
		Map<String, BigDecimal> metrics = new HashMap<>();
		metrics.put(CSV_HEADER_RECALL, getMetric(CSV_HEADER_RECALL).orElse(BigDecimal.valueOf(getRecall())));
		metrics.put(CSV_HEADER_PRECISION, getMetric(CSV_HEADER_PRECISION).orElse(BigDecimal.valueOf(getPrecision())));
		metrics.put(CSV_HEADER_F1, getMetric(CSV_HEADER_PRECISION).orElse(BigDecimal.valueOf(getF1Score())));
		metrics.put(CSV_HEADER_PR_AUC, getMetric(CSV_HEADER_PR_AUC).orElse(BigDecimal.valueOf(getAreaUnderCurveOfPrecisionRecall())));
		metrics.put(CSV_HEADER_TP, getMetric(CSV_HEADER_TP).orElse(BigDecimal.valueOf(getTruePos())));
		metrics.put(CSV_HEADER_FP, getMetric(CSV_HEADER_FP).orElse(BigDecimal.valueOf(getFalsePos())));
		metrics.put(CSV_HEADER_FN, getMetric(CSV_HEADER_FN).orElse(BigDecimal.valueOf(getFalseNeg())));
		metrics.put(CSV_HEADER_FP_Singleton, getMetric(CSV_HEADER_FP_Singleton).orElse(BigDecimal.valueOf(getFalsePosSingleton())));
		metrics.put(CSV_HEADER_FP_Duplicates, getMetric(CSV_HEADER_FP_Duplicates).orElse(BigDecimal.valueOf(getFalsePosDuplicates())));
		return metrics;
	}

	private Optional<BigDecimal> getMetric(String name) {
		if (metrics.containsKey(name)) {
			return Optional.ofNullable(metrics.get(name));
		} else {
			return Optional.empty();
		}
	}

	public Double getWeightedPrecision() {
		double precision = (double) weightedTruePos / (weightedTruePos + weightedFalsePos);
		return Double.isNaN(precision) ? 0.0 : precision;
	}
	public Double getPrecision() {
		double precision = (double) truePos / (truePos + falsePos);
		return Double.isNaN(precision) ? 0.0 : precision;
	}

	public Double getWeightedRecall() {
		double recall = weightedTruePos / (weightedTruePos + weightedFalseNeg);
		return Double.isNaN(recall) ? 0.0 : recall;
	}
	public Double getRecall() {
		double recall = (double) truePos / (truePos + falseNeg);
		return Double.isNaN(recall) ? 0.0 : recall;
	}

	public Double getWeightedF1Score() {
		double r = getWeightedRecall();
		double p = getWeightedPrecision();
		return (p + r) == 0 ? 0.0 : (2*p*r) / (p + r);
	}

	public Double getF1Score() {
		double r = getRecall();
		double p = getPrecision();
		return (p + r) == 0 ? 0.0 : (2*p*r) / (p + r);
	}

	public Double getWeightedAccuracy() {
		double accuracy =
			(weightedTruePos + weightedTrueNeg) / (weightedTruePos + weightedTrueNeg + weightedFalsePos + weightedFalseNeg);
		return Double.isNaN(accuracy) ? 0.0 : accuracy;
	}

	public Double getAccuracy() {
		double accuracy = ((double)(truePos + trueNeg)) / (truePos + trueNeg + falsePos + falseNeg);
		return Double.isNaN(accuracy) ? 0.0 : accuracy;
	}

	public Double getMatthewsCorrelationCoefficient() {
		double numerator = (truePos * trueNeg) - (falsePos * falseNeg);
		double denominator = Math.sqrt((truePos + falsePos) * (truePos + falseNeg) * (trueNeg + falsePos) * (trueNeg + falseNeg));
		return denominator == 0 ? 0.0 : numerator / denominator;
	}
	public void setTruePos(long truePos) {
		this.truePos = truePos;
	}

	public void setTrueNeg(long trueNeg) {
		this.trueNeg = trueNeg;
	}

	public void setFalsePos(long falsePos) {
		this.falsePos = falsePos;
	}

	public void setFalseNeg(long falseNeg) {
		this.falseNeg = falseNeg;
	}

	public long getFalsePosSingleton() {
		return falsePosSingleton;
	}

	public void setFalsePosSingleton(long falsePosSingleton) {
		this.falsePosSingleton = falsePosSingleton;
	}

	public long getFalsePosDuplicates() {
		return falsePosDuplicates;
	}

	public void setFalsePosDuplicates(long falsePosDuplicates) {
		this.falsePosDuplicates = falsePosDuplicates;
	}

	public long getTruePos() {
		return truePos;
	}

	public long getTrueNeg() {
		return trueNeg;
	}

	public long getFalsePos() {
		return falsePos;
	}

	public long getFalseNeg() {
		return falseNeg;
	}

	public double getWeightedTruePos() {
		return weightedTruePos;
	}

	public void setWeightedTruePos(double weightedTruePos) {
		this.weightedTruePos = weightedTruePos;
	}

	public double getWeightedTrueNeg() {
		return weightedTrueNeg;
	}

	public void setWeightedTrueNeg(double weightedTrueNeg) {
		this.weightedTrueNeg = weightedTrueNeg;
	}

	public double getWeightedFalsePos() {
		return weightedFalsePos;
	}

	public void setWeightedFalsePos(double weightedFalsePos) {
		this.weightedFalsePos = weightedFalsePos;
	}

	public double getWeightedFalseNeg() {
		return weightedFalseNeg;
	}

	public void setWeightedFalseNeg(double weightedFalseNeg) {
		this.weightedFalseNeg = weightedFalseNeg;
	}

	public double getAreaUnderCurveOfPrecisionRecall() {
		return areaUnderCurveOfPrecisionRecall;
	}

	public void setAreaUnderCurveOfPrecisionRecall(double areaUnderCurveOfPrecisionRecall) {
		this.areaUnderCurveOfPrecisionRecall = areaUnderCurveOfPrecisionRecall;
	}

	public Table getAsTable() {
		return Table.create("Quality result",
				DoubleColumn.create(CSV_HEADER_RECALL, getRecall()),
				DoubleColumn.create(CSV_HEADER_PRECISION, getPrecision()),
				DoubleColumn.create(CSV_HEADER_F1, getF1Score()),
				DoubleColumn.create(CSV_HEADER_F1_WEIGHTED, getWeightedF1Score()),
				DoubleColumn.create(CSV_HEADER_ACCURACY, getAccuracy()),
				DoubleColumn.create(CSV_HEADER_ACCURACY_WEIGHTED, getWeightedAccuracy()),
//				DoubleColumn.create(CSV_HEADER_MCC, getMatthewsCorrelationCoefficient()),
//				DoubleColumn.create(CSV_HEADER_PR_AUC, getAreaUnderCurveOfPrecisionRecall()),
				LongColumn.create(CSV_HEADER_TP, getTruePos()),
				LongColumn.create(CSV_HEADER_FP, getFalsePos()),
				LongColumn.create(CSV_HEADER_FN, getFalseNeg()),
				LongColumn.create(CSV_HEADER_TN, getTrueNeg()),
				LongColumn.create(CSV_HEADER_FP_Singleton, getFalsePosSingleton()),
				LongColumn.create(CSV_HEADER_FP_Duplicates, getFalsePosDuplicates())
		);
	}

	public Table getAsTableWithDescription(String description) {
		Table table = getAsTable();
		table.insertColumn(0, StringColumn.create(DESCRIPTION_COLUMN));
		table.row(0).setString(DESCRIPTION_COLUMN, description);
		return table;
	}

	public static QualityResult fromFile(String path) throws IOException {
		Table qTab = Table.read()
			.usingOptions(
				CsvReadOptions.builder(path)
					.columnTypes(
						new ColumnType[]{ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE,
						ColumnType.LONG, ColumnType.LONG, ColumnType.LONG, ColumnType.LONG, ColumnType.LONG}
					)
			);
		return QualityResult.fromTable(qTab);
	}
	public static QualityResult fromTable(Table qualityTable) {
		QualityResult qualRes = new QualityResult(
				qualityTable.longColumn(CSV_HEADER_TP).get(0),
				qualityTable.longColumn(CSV_HEADER_FP).get(0),
				qualityTable.longColumn(CSV_HEADER_FN).get(0)
		);
		qualRes.setFalsePosSingleton(qualityTable.longColumn(CSV_HEADER_FP_Singleton).get(0));
		qualRes.setFalsePosDuplicates(qualityTable.longColumn(CSV_HEADER_FP_Duplicates).get(0));
		qualRes.setAreaUnderCurveOfPrecisionRecall(qualityTable.doubleColumn(CSV_HEADER_PR_AUC).get(0));
		return qualRes;
	}

	public static Double roundAsDouble(Double d) {
		return Double.parseDouble(round(d));
	}


//	@Override
//	public String toString() {
//		return "QualityResult{" +
//						"Params=" + getParams() +
//						", Precision=" + round(getPrecision()) +
//						", Recall=" + round(getRecall()) +
//						", F1=" + round(getF1Score()) +
//						", Acc=" + round(getAccuracy()) +
//						", MCC=" + round(getMatthewsCorrelationCoefficient()) +
//						", TruePos=" + truePos +
//						", TrueNeg=" + trueNeg +
//						", FalsePos=" + falsePos +
//						", FalseNeg=" + falseNeg +
//						", FalsePosSingleton=" + falsePosSingleton +
//						", FalsePosDuplicates=" + falsePosDuplicates +
//						'}';
//	}

	@Override
	public String toString() {
		return "QualityResult{" +
						", Precision=" + round(getPrecision()) +
			", Recall=" + round(getRecall()) +
			", F1=" + round(getF1Score()) +
			", F1W=" + round(getWeightedF1Score()) +
			", Acc=" + round(getAccuracy()) +
			", MCC=" + round(getMatthewsCorrelationCoefficient()) +
			", truePos=" + truePos +
			", trueNeg=" + trueNeg +
			", falsePos=" + falsePos +
			", falseNeg=" + falseNeg +
			", falsePosSingleton=" + falsePosSingleton +
			", falsePosDuplicates=" + falsePosDuplicates +
			", weightedTruePos=" + weightedTruePos +
			", weightedTrueNeg=" + weightedTrueNeg +
			", weightedFalsePos=" + weightedFalsePos +
			", weightedFalseNeg=" + weightedFalseNeg +
			", areaUnderCurveOfPrecisionRecall=" + areaUnderCurveOfPrecisionRecall +
			"} " + super.toString();
	}
}
