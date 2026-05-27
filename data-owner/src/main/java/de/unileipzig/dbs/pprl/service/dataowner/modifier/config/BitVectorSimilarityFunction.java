package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.util.ArrayList;
import java.util.List;

public class BitVectorSimilarityFunction extends ConfigModifier<String> {
	public static final String KEY = "SIM";
	private static final String JSONPATH = "$.." +
			JsonModifier.classSelector("similarityCalculator", "BitVectorSimilarityCalculator") +
			".similarityMethod";
	private List<String> values;

	public BitVectorSimilarityFunction(List<String> values) {
		super(KEY);
		this.values = values;
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
		if (!JsonModifier.test(config.getConfig(), JSONPATH)) {
			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
			return new ArrayList<>();
		}

		return applyPropertyVariants(config, values);
	}

	@Override
    String changeProperty(String props, String newValue) {
		props = JsonModifier.set(props, JSONPATH, newValue);
		return props;
	}

	@Override
    String format(String newValue) {
		return newValue;
	}

	@Override
	public String toString() {
		return "BitVectorSimilarityFunction{" +
			"values=" + values +
			"} " + super.toString();
	}
}
