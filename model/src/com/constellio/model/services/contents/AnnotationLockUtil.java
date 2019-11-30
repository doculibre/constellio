package com.constellio.model.services.contents;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AnnotationLockUtil {

	public static String getUserId(String tokenString) {
		StringTokenizer tokenizer = new StringTokenizer(tokenString, ";");

		return tokenizer.nextToken();
	}

	public static List<String> getKeys(String tokenString) {
		StringTokenizer tokeneizer = new StringTokenizer(tokenString, ";");

		tokeneizer.nextToken();

		List<String> keyList = new ArrayList<>();

		while (tokeneizer.hasMoreTokens()) {
			keyList.add(tokeneizer.nextToken());
		}

		return keyList;
	}

	public static String createTokenizedString(String userId, List<String> keyList) {
		StringBuilder tokenizedString = new StringBuilder();

		tokenizedString.append(userId);

		for (String currentKey : keyList) {
			tokenizedString.append(";" + currentKey);
		}

		return tokenizedString.toString();
	}
}
