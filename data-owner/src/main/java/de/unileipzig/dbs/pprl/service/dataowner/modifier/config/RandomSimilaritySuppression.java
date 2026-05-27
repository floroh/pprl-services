package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RandomSimilaritySuppression extends ConfigModifier<Boolean> {
	static final String KEY = "RSS";
	private static final String JSONPATH = "$.." +
			JsonModifier.classSelector("similarityCalculator", "BitVectorSimilarityCalculator") +
			".useWeightCurve";

	public RandomSimilaritySuppression(Map<String, String> params) {
		super(KEY);
	}

	public RandomSimilaritySuppression() {
		super(KEY);
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
		if (!JsonModifier.test(config.getConfig(), JSONPATH)) {
			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
			return new ArrayList<>();
		}

		return applyPropertyVariants(config, Arrays.asList(true, false));
	}

	@Override
    String changeProperty(String props, Boolean newValue) {
		props = JsonModifier.set(props, JSONPATH, newValue);
		return props;
	}

	@Override
    String format(Boolean newValue) {
		return Boolean.toString(newValue);
//		return newValue ? "Y" : "N";
	}

	@Override
	public String toString() {
		return "RandomSimilaritySuppression";
	}
}
