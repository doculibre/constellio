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
package com.constellio.sdk.tests;

public class TestClassFinder {

	private TestClassFinder() {

	}

	@SuppressWarnings("unchecked")
	public static Class<? extends ConstellioTest> findCurrentTest() {

		String lastConstellioClassName = null;
		for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
			String className = stackTraceElement.getClassName();

			if ((className.startsWith("com.constellio") || className.startsWith("sct.services")) && !className.contains("$")) {
				lastConstellioClassName = className;
			}
		}

		if (lastConstellioClassName.contains(".ConstellioTest")) {
			throw new RuntimeException("Cannot use this class from ConstellioTest");
		}

		try {
			return (Class<? extends ConstellioTest>) Class.forName(lastConstellioClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

	}

}
