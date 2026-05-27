package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HighMedPermutationSeed extends ConfigModifier<Long> {
	static final String KEY = "DH";
	private static final String JSONPATH = "$.permutationSeed";

	public HighMedPermutationSeed(Map<String, String> params) {
		super(KEY);
	}

	public HighMedPermutationSeed() {
		super(KEY);
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
		if (!JsonModifier.test(config.getConfig(), JSONPATH)) {
			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
			return new ArrayList<>();
		}

		return applyPropertyVariants(config, Arrays.asList(1234L, 5678L, 1357L, 157182348321283147L, 8688583188214384L));
	}

	@Override
    String changeProperty(String props, Long newValue) {
		props = JsonModifier.set(props, JSONPATH, newValue);
		return props;
	}

	@Override
	String format(Long newValue) {
		return newValue.toString();
	}

	@Override
	public String toString() {
		return "PermutationSeed";
	}
}
