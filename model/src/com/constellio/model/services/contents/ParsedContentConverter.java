package com.constellio.model.services.contents;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.ParsedContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ParsedContentConverter {

	private static String SEPARATOR = "----------";

	public String convertToString(ParsedContent parsedContent) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(parsedContent.getLanguage() + "\n");
		stringBuilder.append(parsedContent.getLength() + "\n");
		stringBuilder.append(parsedContent.getMimeType() + "\n");
		for (Entry<String, Object> mapEntry : parsedContent.getProperties().entrySet()) {
			String parameterKey = encodeString("" + mapEntry.getKey());
			String parameterValue = encodeString("" + mapEntry.getValue());
			stringBuilder.append(parameterKey + "=" + parameterValue + "\n");
		}

		for (Entry<String, List<String>> mapEntry : parsedContent.getStyles().entrySet()) {
			for (String value : mapEntry.getValue()) {
				String parameterKey = "style_" + encodeString("" + mapEntry.getKey());
				String parameterValue = encodeString("" + value);
				stringBuilder.append(parameterKey + "=" + parameterValue + "\n");
			}
		}

		stringBuilder.append(SEPARATOR);
		stringBuilder.append(encodeString(parsedContent.getParsedContent()));
		return stringBuilder.toString();
	}

	private String encodeString(String value) {
		return value == null ? null : value.replace("\n", "__<LB>__").replace("\r", "__<CR>__");
	}

	private String decodeString(String value) {
		return value == null ? null : value.replace("__<LB>__", "\n").replace("__<CR>__", "\r");
	}

	public ParsedContent convertToParsedContent(String string) {
		int separatorIndex = string.indexOf(SEPARATOR);

		KeyListMap<String, String> styles = new KeyListMap<>();
		Map<String, Object> parameters = new HashMap<>();

		String[] attributeLines = string.substring(0, separatorIndex).split("\n");
		String lang = attributeLines[0];
		long length = Long.valueOf(attributeLines[1]);
		String mime = attributeLines[2];
		for (int i = 3; i < attributeLines.length; i++) {
			String attributeLine = attributeLines[i];
			int equalSignIndex = attributeLine.indexOf("=");
			String key = decodeString(attributeLine.substring(0, equalSignIndex));
			String value = decodeString(attributeLine.substring(equalSignIndex + 1));

			if (key.startsWith("style_")) {
				styles.add(key.substring(6), value);
			} else if (key.contains("List:")) {
				putStringListInHashMap(parameters, key, value);
			} else {
				parameters.put(key, value);
			}
		}
		String parsedContent = decodeString(string.substring(separatorIndex + SEPARATOR.length()));
		ParsedContent parsedContentToReturn = new ParsedContent(parsedContent, lang, mime, length, parameters, styles.getNestedMap());
		if (parameters.get("Title") instanceof String) {
			parsedContentToReturn.setTitle((String) parameters.get("Title"));
		}

		return parsedContentToReturn;
	}

	private void putStringListInHashMap(Map<String, Object> parameters, String key, String value) {
		value = value.substring(1, value.length() - 1);
		String[] valuesAfterSplit = value.split(",");
		List<String> valueList = new ArrayList<String>();
		for (String aValue : valuesAfterSplit) {
			valueList.add(aValue.trim());
		}
		parameters.put(key, valueList);
	}


}
