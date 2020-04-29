package com.constellio.data.utils;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class XmlUtils {
	static Map<String, String> invalidCharactersTable;

	static {
		invalidCharactersTable = new HashMap<>();
		invalidCharactersTable.put("/", "__FSL__");
		invalidCharactersTable.put("\\\\", "__BSL__");
		invalidCharactersTable.put(" ", "__SPC__");
	}

	public static String escapeAttributeName(String value) {
		if (!Strings.isNullOrEmpty(value)) {
			for (Map.Entry invalidCharEntry : invalidCharactersTable.entrySet()) {
				String unescapedChars = (String) invalidCharEntry.getKey();
				if (Pattern.compile(unescapedChars).matcher(value).find()) {
					value = value.replaceAll(unescapedChars, (String) invalidCharEntry.getValue());
				}
			}
		}

		return value;
	}

	public static String unescapeAttributeName(String value) {
		if (!Strings.isNullOrEmpty(value)) {
			for (Map.Entry invalidCharEntry : invalidCharactersTable.entrySet()) {
				String escapeChars = (String) invalidCharEntry.getValue();
				if (value.contains(escapeChars)) {
					value = value.replaceAll(escapeChars, (String) invalidCharEntry.getKey());
				}
			}
		}

		return value;
	}
}
