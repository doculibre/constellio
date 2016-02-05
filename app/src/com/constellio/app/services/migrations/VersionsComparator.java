package com.constellio.app.services.migrations;

import java.util.Comparator;

public class VersionsComparator implements Comparator<String> {

	public static boolean isFirstVersionBeforeOrEqualToSecond(String firstVersion, String secondVersion) {
		return new VersionsComparator().compare(firstVersion, secondVersion) != 1;
	}

	public static boolean isFirstVersionBeforeSecond(String firstVersion, String secondVersion) {
		return new VersionsComparator().compare(firstVersion, secondVersion) == -1;
	}

	@Override
	public int compare(String versionOne, String versionTwo) {
		String[] version1VersionAndSubVersion = versionOne.split("-");
		String[] version2VersionAndSubVersion = versionTwo.split("-");

		String[] versionsOneWithoutSubVersion = split(version1VersionAndSubVersion[0]);
		String[] versionsTwoWithoutSubVersion = split(version2VersionAndSubVersion[0]);

		for (int i = 0; i < Math.min(versionsOneWithoutSubVersion.length, versionsTwoWithoutSubVersion.length); i++) {
			if (Integer.parseInt(versionsOneWithoutSubVersion[i]) > Integer.parseInt(versionsTwoWithoutSubVersion[i])) {
				return 1;
			} else if (Integer.parseInt(versionsOneWithoutSubVersion[i]) < Integer.parseInt(versionsTwoWithoutSubVersion[i])) {
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
				return Integer.valueOf(subVersion1).compareTo(Integer.valueOf(subVersion2));
			}
		} else {
			if (subVersion1.isEmpty()) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	private String[] split(String version) {
		int index = 0;
		String[] normalizedVersion = new String[] { "0", "0", "0", "0", "0" };
		for (String part : version.split("\\.")) {
			normalizedVersion[index++] = part;
		}
		return normalizedVersion;
	}

}
