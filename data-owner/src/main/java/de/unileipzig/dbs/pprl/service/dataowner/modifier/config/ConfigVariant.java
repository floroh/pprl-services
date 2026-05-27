package de.unileipzig.dbs.pprl.service.dataowner.modifier.config;

import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ConfigVariant implements Serializable {
	public static final String PARAM_SEPERATOR = "&";
	public static final String PARAM_KEY_VALUE_SEPERATOR = "=";
	public static final String PARAM_NAME_COLUMN = "PARAM";
	public static final String PARAM_VALUE_COLUMN = "VALUE";

	private String path;
	private String config;
	private Map<String, String> params;

	public ConfigVariant(String config) {
		params = new HashMap<>();
		this.config = config;
	}

	public ConfigVariant clone() {
		ConfigVariant dup = new ConfigVariant(config);
		params.forEach(dup::setParam);
		return dup;
	}

//	public void store(String outputPath, String outputBaseName) {
//		path = createPath(outputPath, outputBaseName, ".json");
//		try {
//			FileUtils.storePlainText(path, config, false);
//		} catch (IOException e) {
//			throw new RuntimeException("Could not write config to + " + path);
//		}
//	}

	public String createPath(String outputPath, String outputBaseName, String fileExtension) {
		return outputPath + File.separator + outputBaseName + (outputPath.isEmpty()? "" : "_") + buildParamString(params) + fileExtension;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getPath() {
		return path;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParam(String key, String val) {
		this.params.put(key, val);
	}

	public Table getParamTable() {
		return getParamTable(params);
	}

	public static Table getParamTable(Map<String, String> params) {
		StringColumn colKeys = StringColumn.create(PARAM_NAME_COLUMN);
		StringColumn colValues = StringColumn.create(PARAM_VALUE_COLUMN);
		params.forEach((k,v) -> {
			colKeys.append(k);
			colValues.append(v);
		});
		return Table.create("Parameter", colKeys, colValues);
	}

	public static String buildParamString(Map<String, String> params) {
		StringBuilder suffix = new StringBuilder();
		if (!params.isEmpty()) {
			boolean first = true;
			for (Map.Entry<String, String> param : params.entrySet()) {
				if (!first) suffix.append(PARAM_SEPERATOR);
				suffix.append(formatParamForFileName(param.getKey(), param.getValue()));
				first = false;
			}
		}
		return suffix.toString();
	}

	public static Map<String, String> parseParamString(String paramString) {
		Map<String, String> params = new HashMap<>();

		if (paramString == null || paramString.isEmpty()) {
			return params;
		}

		String[] paramSubStrings = paramString.split(PARAM_SEPERATOR);
		for (String paramSubString : paramSubStrings) {
			String[] kv = paramSubString.split(PARAM_KEY_VALUE_SEPERATOR);
			params.put(kv[0], kv[1]);
		}
		return params;
	}

	private static String formatParamForFileName(String key, String value) {
		String formattedValue = switch (key) {
//			case Threshold.KEY:
//				try {
//					double d = Double.parseDouble(value);
//					if (d > 0 && d <= 1) {
//						d *= 100;
//					}
//					formattedValue = String.valueOf((int)d);
//				} catch (NumberFormatException e) {
//					formattedValue = value;
//				}
//				break;
      default ->
//			case LshLength.KEY:
//			case LshKeys.KEY:
              value;
    };
    for (PersonalAttributeType type : PersonalAttributeType.values()) {
			if (key.contains(type.asString())) {
				key = key.replaceAll(type.asString(), type.getShortName());
			}
		}
		return key + PARAM_KEY_VALUE_SEPERATOR + formattedValue;
	}

//	public void write(String path) throws IOException {
//		FileUtils.storePlainText(path, JsonModifier.prettify(config), false);
//	}
}
