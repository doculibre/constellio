package com.constellio.model.services.contents;

import java.util.List;
import java.util.StringTokenizer;

public class AnnotationLockUtil {

	public static String getUserId(String tokenString) {
		StringTokenizer tokenizer = new StringTokenizer(tokenString, ";");

		return tokenizer.nextToken();
	}

	public static String getKey(String tokenString) {
		StringTokenizer tokeneizer = new StringTokenizer(tokenString, ";");

		tokeneizer.nextToken();


		return tokeneizer.nextToken();
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
