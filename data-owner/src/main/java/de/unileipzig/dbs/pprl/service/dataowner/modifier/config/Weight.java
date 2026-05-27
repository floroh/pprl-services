package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Weight extends ConfigModifier<BigDecimal> {
	public static final String KEY = "W";
	private String JSONPATH_WEIGHTED_SIMILARITY_AGGREGATOR;
	private String JSONPATH_SCALED_WEIGHT_CALCULATOR;
	private String attributeName;
	private BigDecimal min;
	private BigDecimal max;
	private BigDecimal delta;

	public Weight(Map<String, String> params) {
		this(
			params.get("attributeName"),
		  Double.parseDouble(params.get("min")),
			Double.parseDouble(params.get("max")),
			Double.parseDouble(params.get("delta"))
			);
	}

	public Weight(String attributeName, double weight) {
		this(attributeName, weight, weight, 3);
	}

	public Weight(String attributeName, double min, double max, double delta) {
		super(buildParamKey(attributeName));
		this.attributeName = attributeName;
		this.min = BigDecimal.valueOf(min);
		this.max = BigDecimal.valueOf(max);
		this.delta = BigDecimal.valueOf(delta);
		createJsonPath();
	}

	private void createJsonPath() {
		JSONPATH_WEIGHTED_SIMILARITY_AGGREGATOR = "$.." +
			JsonModifier.classSelector("similarityAggregator", "WeightedSimilarityAggregator") +
			".weights";
		JSONPATH_SCALED_WEIGHT_CALCULATOR = "$.." +
			JsonModifier.classSelector("weightCalculator", "ScaledWeightCalculator") +
			".defaultWeights";
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
		if (!JsonModifier.test(config.getConfig(), JSONPATH_WEIGHTED_SIMILARITY_AGGREGATOR)) {
			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
			return new ArrayList<>();
		}

		return applyPropertyVariants(config, createNumericVariants(min, max, delta));
	}

	@Override
    String changeProperty(String props, BigDecimal newValue) {
		props = JsonModifier.put(props, JSONPATH_WEIGHTED_SIMILARITY_AGGREGATOR, attributeName,
			Double.valueOf(format(newValue)));
		props = JsonModifier.put(props, JSONPATH_SCALED_WEIGHT_CALCULATOR, attributeName,
			Double.valueOf(format(newValue)));
		return props;
	}

	@Override
    String format(BigDecimal newValue) {
//		return newValue.toString();
		return String.format(Locale.ENGLISH, "%.2f", newValue);
	}

	private static String buildParamKey(String attributeName) {
		return KEY + attributeName;
	}

	@Override
	public String toString() {
		return "Weight{" +
						"attributeName=" + attributeName +
						", min=" + min +
						", max=" + max +
						", delta=" + delta +
						'}';
	}
}
