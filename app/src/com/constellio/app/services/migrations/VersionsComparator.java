package com.constellio.app.services.migrations;

import java.util.Comparator;

public class VersionsComparator implements Comparator<String> {

	public static boolean isFirstVersionBeforeOrEqualToSecond(String firstVersion, String secondVersion) {
		return new VersionsComparator().compare(firstVersion, secondVersion) != 1;
	}

	public static boolean isFirstVersionBeforeSecond(String firstVersion, String secondVersion) {
		try {
			return new VersionsComparator().compare(firstVersion, secondVersion) == -1;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public int compare(String versionOne, String versionTwo) {

		if (versionOne.contains("beta")) {
			versionOne = versionOne.replace("beta.", "beta").replace("beta", "beta.");
		}

		if (versionTwo.contains("beta")) {
			versionTwo = versionTwo.replace("beta.", "beta").replace("beta", "beta.");
		}

		String[] version1VersionAndSubVersion = versionOne.split("-");
		String[] version2VersionAndSubVersion = versionTwo.split("-");

		String[] versionsOneWithoutSubVersion = split(version1VersionAndSubVersion[0]);
		String[] versionsTwoWithoutSubVersion = split(version2VersionAndSubVersion[0]);

		for (int i = 0; i < Math.min(versionsOneWithoutSubVersion.length, versionsTwoWithoutSubVersion.length); i++) {
			if (parseVersionDigit(versionsOneWithoutSubVersion[i]) > parseVersionDigit(versionsTwoWithoutSubVersion[i])) {
				return 1;
			} else if (parseVersionDigit(versionsOneWithoutSubVersion[i]) < parseVersionDigit(versionsTwoWithoutSubVersion[i])) {
				return -1;
			}
		}

		if (versionsOneWithoutSubVersion.length > versionsTwoWithoutSubVersion.length) {
			return 1;
		} else if (versionOne.length() < versionTwo.length()) {
			return -1;
		}

		String subVersion1 = "", subVersion2 = "";
		if (version1VersionAndSubVersion.length > 1) {
			subVersion1 = version1VersionAndSubVersion[1];
		}
		if (version2VersionAndSubVersion.length > 1) {
			subVersion2 = version2VersionAndSubVersion[1];
			if (subVersion1.isEmpty()) {
				return -1;
			} else {
				return Integer.compare(parseVersionDigit(subVersion1), parseVersionDigit(subVersion2));
			}
		} else {
			if (subVersion1.isEmpty()) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	private int parseVersionDigit(String str) {
		if ("beta".equals(str)) {
			return -1;
		} else {
			return Integer.parseInt(str);
		}
	}

	private String[] split(String version) {
		int index = 0;
		String[] normalizedVersion = new String[]{"0", "0", "0", "0", "0"};
		for (String part : version.split("\\.")) {
			normalizedVersion[index++] = part;
		}
		return normalizedVersion;
	}

}
