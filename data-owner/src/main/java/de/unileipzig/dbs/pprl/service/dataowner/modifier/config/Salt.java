package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.service.common.modifier.JsonModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Salt extends ConfigModifier<String> {
	public static final long SEED = 1234567;
	public static final int DEFAULT_LENGTH = 6;
	static final String KEY = "SALT";
	private static final String JSONPATH = "$.." +
			JsonModifier.classSelector("featureEncoder", "RandomHashing") +
			".salt";
	private int number;
	private int length = DEFAULT_LENGTH;

	public Salt(Map<String, String> params) {
		super(KEY);
		this.number = Integer.parseInt(params.get("number"));
	}

	public Salt(int number) {
		super(KEY);
		this.number = number;
	}

	@Override
	public List<ConfigVariant> modify(ConfigVariant config) {
		if (!JsonModifier.test(config.getConfig(), JSONPATH)) {
			ConfigPreparator.logger.warn("Tried to modify property (" + KEY + ") that does not exist");
			return new ArrayList<>();
		}

		return applyPropertyVariants(config, createSalts());
	}

	@Override
    String changeProperty(String props, String newValue) {
		props = JsonModifier.append(props, "$..featureEncoder[?].salt",
				JsonModifier.classFilter(".RandomHashing"),
				"salt",
				newValue
		);
		return props;
	}

	@Override
    String format(String newValue) {
		return newValue;
	}

	private List<String> createSalts() {
		List<String> variants = new ArrayList<>();

		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		Random random = new Random(SEED);

		for (int i = 0; i < number; i++) {
			String generatedString = random.ints(leftLimit, rightLimit + 1)
					.limit(length)
					.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
					.toString();
			variants.add(generatedString);
		}
		return variants;
	}

	@Override
	public String toString() {
		return "Salt{" +
						"number=" + number +
						'}';
	}
}
