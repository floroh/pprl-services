package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;
import net.minidev.json.JSONArray;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MultiThreshold extends ConfigModifier<BigDecimal> {
	static final String KEY = "THR";
	private static final String JSONPATH_PROBABLE = "$.." +
			JsonModifier.classSelector("classifier", "MultiThresholdClassifier") +
			".probableThreshold";

	private static final String JSONPATH_POSSIBLE = "$.." +
			JsonModifier.classSelector("classifier", "MultiThresholdClassifier") +
			".possibleThreshold";

	private BigDecimal min;
	private BigDecimal max;
	private BigDecimal delta;

	public MultiThreshold(Map<String, String> params) {
		super(KEY);
		this.min = new BigDecimal(params.get("min"));
		this.max = new BigDecimal(params.get("max"));
		this.delta = new BigDecimal(params.get("delta"));
	}

	public MultiThreshold(double threshold) {
		this(threshold, threshold, 0.1);
	}

	public MultiThreshold(double min, double max, double delta) {
		super(KEY);
		this.min = BigDecimal.valueOf(min);
		this.max = BigDecimal.valueOf(max);
		this.delta = BigDecimal.valueOf(delta);
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
		if (!JsonModifier.test(config.getConfig(), JSONPATH_PROBABLE)) {
			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
			return new ArrayList<>();
		}

		return applyPropertyVariants(config, createNumericVariants(min, max, delta));
	}

	@Override
    String changeProperty(String props, BigDecimal newValue) {
//		props = JsonModifier.set(props, JSONPATH_POSSIBLE, Double.valueOf(format(newValue)));
		props = JsonModifier.set(props, JSONPATH_PROBABLE, Double.valueOf(format(newValue)));
		JSONArray readObject = (JSONArray)JsonModifier.read(props, JSONPATH_POSSIBLE);
		BigDecimal oldPossibleThreshold = BigDecimal.valueOf((double) readObject.getFirst());
		// Possible must be equal/greater than probable threshold
		if (oldPossibleThreshold.compareTo(newValue) > 0) {
			props = JsonModifier.set(props, JSONPATH_POSSIBLE, Double.valueOf(format(newValue)));
		}
		return props;
	}

	@Override
    String format(BigDecimal newValue) {
//		return newValue.toString();
		return String.format(Locale.ENGLISH, "%.2f", newValue);
	}

	@Override
	public String toString() {
		return "Threshold{" +
						"min=" + min +
						", max=" + max +
						", delta=" + delta +
						'}';
	}
}
