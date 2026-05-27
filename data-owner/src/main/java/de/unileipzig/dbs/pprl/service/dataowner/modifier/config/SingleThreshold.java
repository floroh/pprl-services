package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SingleThreshold extends ConfigModifier<BigDecimal> {
	public static final String KEY = "THR";
	private static final String JSONPATH = "$.." +
			JsonModifier.classSelector("classifier", "SingleThresholdClassifier") +
			".threshold";
	private BigDecimal min;
	private BigDecimal max;
	private BigDecimal delta;

	public SingleThreshold(Map<String, String> params) {
		super(KEY);
		this.min = new BigDecimal(params.get("min"));
		this.max = new BigDecimal(params.get("max"));
		this.delta = new BigDecimal(params.get("delta"));
	}

	public SingleThreshold(double min, double max, double delta) {
		super(KEY);
		this.min = BigDecimal.valueOf(min);
		this.max = BigDecimal.valueOf(max);
		this.delta = BigDecimal.valueOf(delta);
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
		if (!JsonModifier.test(config.getConfig(), JSONPATH)) {
			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
			return new ArrayList<>();
		}

		return applyPropertyVariants(config, createNumericVariants(min, max, delta));
	}

	@Override
    String changeProperty(String props, BigDecimal newValue) {
		props = JsonModifier.set(props, JSONPATH, Double.valueOf(format(newValue)));
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
