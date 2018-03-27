package com.constellio.model.services.taxonomies;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class TaxonomiesTestsUtils {

	public static boolean ajustIfBetterThanExpected(StackTraceElement[] stackTraceElements, String current, String expected) {

		String filePath = "/Users/francisbaril/Constellio/IdeaProjects/constellio-dev/constellio/sdk/src/com/constellio/model/services/taxonomies/";

		boolean betterThanExpected = isBetterThanExpected(current, expected);
		if (betterThanExpected) {

			int lineNumber;
			String filename;
			for (StackTraceElement element : stackTraceElements) {

				if (element.getClassName().endsWith("AcceptTest")
						&& !(element.getMethodName().equals("solrQueryCounts")
						|| element.getMethodName().equals("secondSolrQueryCounts")
						|| element.getMethodName().equals("secondCallQueryCounts"))) {
					filename = element.getFileName();
					lineNumber = element.getLineNumber();

					File file = new File(filePath + filename);
					if (file.exists()) {
						System.out.println(filename + ":" + lineNumber + " is changed from " + expected + " to " + current);
						try {
							List<String> lines = FileUtils.readLines(file, "UTF-8");
							System.out.println(lines.size());
							String line = lines.get(lineNumber - 1);
							if (line.contains("solrQueryCounts") || line.contains("secondSolrQueryCounts") || line
									.contains("secondCallQueryCounts")) {
								String modifiedLine = line.replace(toCommaSeparatedArgs(expected), toCommaSeparatedArgs(current));
								lines.set(lineNumber - 1, modifiedLine);

								FileUtils.writeLines(file, "UTF-8", lines);
								System.out.println(lines.size());
								System.out.println(line + " > " + modifiedLine);
							}

						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
					break;
				}
			}
		}
		return betterThanExpected;
	}

	private static String toCommaSeparatedArgs(String str) {
		String[] parts = str.split("-");
		return "(" + parts[0] + ", " + parts[1] + ", " + parts[2] + ")";
	}

	private static boolean isBetterThanExpected(String current, String expected) {
//		if (!current.equals(expected)) {
		//			int[] currentParts = toInts(current.split("-"));
		//			int[] expectedParts = toInts(expected.split("-"));
		//
		//			if (currentParts[0] <= expectedParts[0]
		//					&& currentParts[1] <= expectedParts[1]
		//					&& currentParts[2] <= expectedParts[2]) {
		//				return true;
		//			}
		//		}
		return false;
	}

	private static int[] toInts(String[] split) {

		int[] intParts = new int[split.length];

		for (int i = 0; i < split.length; i++) {
			intParts[i] = Integer.parseInt(split[i]);
		}

		return intParts;
	}
}
