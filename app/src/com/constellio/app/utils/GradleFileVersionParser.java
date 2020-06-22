package com.constellio.app.utils;

import com.constellio.data.conf.FoldersLocator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GradleFileVersionParser {

	private static String version;

	public static String getVersion() {

		if (version == null) {

			version = findVersion();

		}

		return version;
	}

	private static String findVersion() {
		File appLayerBuildGradleFile = new File(new FoldersLocator().getConstellioWebappFolder(), "build.gradle");

		try {
			List<String> appLayerBuildGradleFileLines = FileUtils.readLines(appLayerBuildGradleFile);

			for (int i = 0; i < appLayerBuildGradleFileLines.size(); i++) {
				String line = appLayerBuildGradleFileLines.get(i);

				if (line.contains("version = ")) {
					int firstQuote = line.indexOf("'");
					int secondQuote;
					if (firstQuote == -1) {
						firstQuote = line.indexOf("\"");
						secondQuote = line.indexOf("\"", firstQuote + 1);
					} else {
						secondQuote = line.indexOf("'", firstQuote + 1);
					}
					return line.substring(firstQuote + 1, secondQuote);
				}

			}

			return null;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
