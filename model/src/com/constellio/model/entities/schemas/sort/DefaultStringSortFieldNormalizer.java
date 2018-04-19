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
	public String normalize(String rawValue) { //rawValue = 1.1.1
		int numberOfTriesRemaining = 2;
		String previousValue = rawValue;
		String normalizedValue = "";
		while (numberOfTriesRemaining-- > 0 && !previousValue.equals(normalizedValue = getNormalizedValue(previousValue))) {
			previousValue = normalizedValue;
		}
		return previousValue;
	}

	private String getNormalizedValue(String rawValue) {
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
		return normalizedText;
	}

	@Override
	public String normalizeNull() {
		return null;
	}

	public static String normalizeNullable(String value) {
		if (value == null) {
			return null;
		} else {
			return new DefaultStringSortFieldNormalizer().normalize(value);
		}
	}
}
