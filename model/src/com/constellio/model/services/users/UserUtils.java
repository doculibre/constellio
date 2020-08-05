package com.constellio.model.services.users;

import com.constellio.data.utils.AccentApostropheCleaner;

public class UserUtils {
	public static String cleanUsername(String username) {
		return username == null ? null : AccentApostropheCleaner.removeAccents(username.toLowerCase()).replace(" ", "");
	}
}
