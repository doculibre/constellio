package com.constellio.model.entities.calculators;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class JEXLCalculatorUtils {

	public String combine(String separator, String... params) {
		StringBuilder stringBuilder = new StringBuilder();

		for (String part : params) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(separator);
			}
			if (StringUtils.isNotBlank(part)) {
				stringBuilder.append(part);
			}
		}

		return stringBuilder.toString();
	}

	public String firstOrEmptyString(List<String> values) {
		if (values == null || values.isEmpty()) {
			return "";

		} else {
			return values.get(0);
		}
	}
}
