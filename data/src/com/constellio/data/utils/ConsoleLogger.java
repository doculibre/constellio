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
package com.constellio.data.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ConsoleLogger {

	public static String GLOBAL_PREFIX = "";

	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	public static synchronized void log(String text) {

		String linesPrefix = sdf.format(new Date()) + " - ";

		StringBuilder message = new StringBuilder();
		for (String textLine : text.split("\n")) {
			message.append(GLOBAL_PREFIX + " " + linesPrefix + textLine + "\n");
		}

		//if (GLOBAL_PREFIX.equals("BatchProcessControllerWithTaxonomiesAcceptanceTest")) {
		//System.out.println(message.toString());
		//}
	}

	public static synchronized void log(List<String> lines) {

		String linesPrefix = sdf.format(new Date()) + " - ";

		StringBuilder message = new StringBuilder();
		for (String textLine : lines) {
			message.append(GLOBAL_PREFIX + " " + linesPrefix + textLine + "\n");
		}

		//if (GLOBAL_PREFIX.equals("BatchProcessControllerWithTaxonomiesAcceptanceTest")) {
		//System.out.println(message.toString());
		//}
	}

}
