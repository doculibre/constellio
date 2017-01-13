package com.constellio.model.entities.schemas.sort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.constellio.data.utils.AccentApostropheCleaner;

public class DefaultStringSortFieldNormalizer implements StringSortFieldNormalizer {

	static Pattern[] patterns = new Pattern[8];

	static {
		for (int i = 8; i > 0; i--) {
			String regex = "([^0-9]+|^)([0-9]{" + i + "})([^0-9]+|$)";
			patterns[i - 1] = Pattern.compile(regex);
		}
	}

	@Override
	public String normalize(String rawValue) {
		String normalizedText = AccentApostropheCleaner.removeAccents(rawValue.toLowerCase()).trim();
		for (int i = 8; i > 0; i--) {
			//String regex = "([^0-9]+|^)([0-9]{" + i + "})([^0-9]+|$)";
			Matcher matcher = patterns[i - 1].matcher(normalizedText);
			if (matcher.find()) {
				StringBuffer fillers = new StringBuffer();
				int zeros = 9 - i;
				for (int j = 0; j < zeros; j++) {
					fillers.append("\\0");
				}
				String replacement = "$1" + fillers + "$2$3";
				normalizedText = matcher.replaceAll(replacement);//normalizedText.replaceAll(regex, replacement);

			}
		}
		if ("".equals(normalizedText)) {
			normalizedText = null;
		}
		return normalizedText;
	}

	@Override
	public String normalizeNull() {
		return null;
	}
}
