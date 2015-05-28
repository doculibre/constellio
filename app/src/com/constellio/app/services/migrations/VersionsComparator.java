/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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

		String[] versionsOne = split(versionOne);
		String[] versionsTwo = split(versionTwo);

		for (int i = 0; i < Math.min(versionsOne.length, versionsTwo.length); i++) {
			if (Integer.parseInt(versionsOne[i]) > Integer.parseInt(versionsTwo[i])) {
				return 1;
			} else if (Integer.parseInt(versionsOne[i]) < Integer.parseInt(versionsTwo[i])) {
				return -1;
			}
		}

		if (versionsOne.length > versionsTwo.length) {
			return 1;
		} else if (versionOne.length() < versionTwo.length()) {
			return -1;
		}
		return 0;

	}

	private String[] split(String version) {
		int index = 0;
		String[] normalizedVersion = new String[] { "0", "0", "0", "0" };
		for (String part : version.split("\\.")) {
			normalizedVersion[index++] = part;
		}
		return normalizedVersion;
	}

}
