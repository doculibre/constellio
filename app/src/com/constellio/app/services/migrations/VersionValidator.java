package com.constellio.app.services.migrations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionValidator {
	public static boolean isValidVersion(String version) {
		Pattern pattern = Pattern.compile("^(\\d+\\.)*(\\d+)(-\\d+)?$");
		Matcher matcher = pattern.matcher(version);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}

	}
}
