package com.constellio.app.modules.restapi.core.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthorizationUtils {

	public static final String SCHEME = "Bearer";

	public static String getToken(String header) {
		return header.replace(SCHEME.concat(" "), "");
	}

	public static String getScheme(String header) {
		int spaceIdx = header.indexOf(" ");
		if (spaceIdx == -1) {
			return null;
		}

		return header.substring(0, spaceIdx);
	}

}
