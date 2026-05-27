package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NumHashFunctions extends ConfigModifier<Integer> {
	private static final String KEY = "k";
	private String JSONPATH_SINGLE;
	private String JSONPATH_MULTI;
	private String JSONPATH_WEIGHTED_MULTI;
	private String attributeName;
	private Integer min;
	private Integer max;
	private Integer delta;


	public NumHashFunctions(Map<String, String> params) {
		this(
			params.get("attributeName"),
			Integer.valueOf(params.get("min")),
			Integer.valueOf(params.get("max")),
			Integer.valueOf(params.get("delta"))
		);

	}

	public NumHashFunctions(String attributeName, Integer value) {
		this(attributeName, value, value, 3);
	}

	public NumHashFunctions(String attributeName, Integer min, Integer max, Integer delta) {
		super(buildParamKey(attributeName));
		this.attributeName = attributeName;
		this.min = min;
		this.max = max;
		this.delta = delta;
		createJsonPath();
	}

	private void createJsonPath() {
		JSONPATH_SINGLE = "$.." +
			"encoderGroups[?(@.@class=='.SingleAttributeEncoderGroup' && @.attributeId=='" + attributeName + "')]" +
			".attributeEncoder." +
			JsonModifier.classSelector("featureEncoder", "RandomHashing") +
			".numHashFunctions";
		JSONPATH_MULTI = "$.." +
			JsonModifier.classSelector("encoderGroups", "MultiAttributeEncoderGroup") +
			".attributeEncoders." + attributeName + "." +
			JsonModifier.classSelector("featureEncoder", "RandomHashing") +
			".numHashFunctions";
		JSONPATH_WEIGHTED_MULTI = "$.." +
			JsonModifier.classSelector("encoderGroups", "WeightedMergeMultiAttributeEncoderGroup") +
			".attributeEncoders." + attributeName + "." +
			JsonModifier.classSelector("featureEncoder", "RandomHashing") +
			".numHashFunctions";
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
//		if (!JsonModifier.test(config.getConfig(), JSONPATH_MULTI)) {
//			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
//			return new ArrayList<>();
//		}
		return applyPropertyVariants(config, createNumericVariants());
	}

	private List<Integer> createNumericVariants() {
		List<Integer> variants = new ArrayList<>();
		for (Integer cur = min; cur <= max; cur = cur + delta) {
			variants.add(cur);
		}
		return variants;
	}

	@Override
    String changeProperty(String props, Integer newValue) {
		props = JsonModifier.set(props, JSONPATH_MULTI, newValue);
		props = JsonModifier.set(props, JSONPATH_WEIGHTED_MULTI, newValue);
		props = JsonModifier.set(props, JSONPATH_SINGLE, newValue);
		return props;
	}

	@Override
    String format(Integer newValue) {
		return newValue.toString();
	}

	static List<BigDecimal> createNumericVariants(BigDecimal min, BigDecimal max, BigDecimal delta) {
		List<BigDecimal> variants = new ArrayList<>();
		for (BigDecimal cur = min; cur.compareTo(max) <= 0; cur = cur.add(delta)) {
			variants.add(cur);
		}
		return variants;
	}

	private static String buildParamKey(String attributeName) {
		return KEY + attributeName;
	}

	@Override
	public String toString() {
		return "NumHashFunctions{" +
			"attributeName='" + attributeName + '\'' +
			", min=" + min +
			", max=" + max +
			", delta=" + delta +
			'}';
	}
}
