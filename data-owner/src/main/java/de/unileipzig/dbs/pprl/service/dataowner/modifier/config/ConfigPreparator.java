package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigPreparator {
	private String originalConfig;
	private List<ConfigModifier> configModifiers;

	static Logger logger = LogManager.getLogger(ConfigPreparator.class);

//	public static ConfigPreparator fromPath(String path) {
//		try {
//			String originalConfig = FileUtils.readPlainText(path);
//			return new ConfigPreparator(originalConfig);
//		} catch (IOException e) {
//			throw new RuntimeException("Cannot read from " + path);
//		}
//	}

	public ConfigPreparator(String originalConfig) {
		this.originalConfig = originalConfig;
		configModifiers = new ArrayList<>();
	}

	public void addModifier(ConfigModifier modifier) {
		configModifiers.add(modifier);
	}

	public List<ConfigVariant> createConfigs() {
		List<ConfigVariant> variants = Arrays.asList(new ConfigVariant(originalConfig));

		for (ConfigModifier mod : configModifiers) {
			List<ConfigVariant> nextVariants = new ArrayList<>();
			for (ConfigVariant curVariant : variants) {
				nextVariants.addAll(mod.modify(curVariant));
			}
			variants = nextVariants;
		}
		return variants;
	}

	public List<ConfigModifier> getModifiers() {
		return configModifiers;
	}

}
