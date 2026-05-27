package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LinkPostprocessor extends ConfigModifier<String> {
	static final String KEY = "POST";
	private static final String JSONPATH = "$..linksPostprocessor.@class";
	private static final String JSONPATH_BEFORE_CLUSTERING = "$..linksPostprocessorBeforeClustering.@class";

	private boolean beforeClustering = false;
	public LinkPostprocessor(Map<String, String> params) {
		super(KEY);
	}

	public LinkPostprocessor() {
		super(KEY);
	}

	public LinkPostprocessor(boolean beforeClustering) {
		super(KEY);
		this.beforeClustering = beforeClustering;
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
		if (!JsonModifier.test(config.getConfig(), getJsonPath())) {
			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
			return new ArrayList<>();
		}

		return applyPropertyVariants(config, Arrays.asList(".MaxBoth", ".Dummy"));
	}

	@Override
    String changeProperty(String props, String newValue) {
		props = JsonModifier.set(props, getJsonPath(), newValue);
		return props;
	}

	@Override
    String format(String newValue) {
		return newValue;
	}

	private String getJsonPath() {
		return beforeClustering ? JSONPATH_BEFORE_CLUSTERING : JSONPATH;
	}
	@Override
	public String toString() {
		return "LinkProcessor";
	}
}
