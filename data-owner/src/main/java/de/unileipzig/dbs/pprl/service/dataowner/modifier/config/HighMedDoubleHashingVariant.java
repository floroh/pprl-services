package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HighMedDoubleHashingVariant extends ConfigModifier<String> {
	static final String KEY = "DH";
	private static final String JSONPATH = "$.selectedVariant";

	public HighMedDoubleHashingVariant(Map<String, String> params) {
		super(KEY);
	}

	public HighMedDoubleHashingVariant() {
		super(KEY);
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
		if (!JsonModifier.test(config.getConfig(), JSONPATH)) {
			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
			return new ArrayList<>();
		}

		return applyPropertyVariants(config, Arrays.asList("MD5SHA1", "SHA1SHA2", "SHA2SHA3"));
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
		return "DoubleHashing";
	}
}
