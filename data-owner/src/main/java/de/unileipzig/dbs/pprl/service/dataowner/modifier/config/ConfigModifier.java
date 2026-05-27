package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class ConfigModifier<T> {
	protected String paramKey;

	public ConfigModifier(String paramKey) {
		this.paramKey = paramKey;
	}

	abstract List<ConfigVariant> modify(ConfigVariant config);

	abstract String changeProperty(String props, T newValue);

	abstract String format(T value);

	List<ConfigVariant> applyPropertyVariants(ConfigVariant baseConfig, List<T> propertyVariants) {
		List<ConfigVariant> variants = new ArrayList<>();
		for (T value : propertyVariants) {
			ConfigVariant curConfig = baseConfig.clone();
			curConfig.setParam(paramKey, format(value));
			curConfig.setConfig(changeProperty(curConfig.getConfig(), value));
			variants.add(curConfig);
		}
		return variants;
	}

	static List<BigDecimal> createNumericVariants(BigDecimal min, BigDecimal max, BigDecimal delta) {
		List<BigDecimal> variants = new ArrayList<>();
		for (BigDecimal cur = min; cur.compareTo(max) <= 0; cur = cur.add(delta)) {
			variants.add(cur);
		}
		return variants;
	}
}
