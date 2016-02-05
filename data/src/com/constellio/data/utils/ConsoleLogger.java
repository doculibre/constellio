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
