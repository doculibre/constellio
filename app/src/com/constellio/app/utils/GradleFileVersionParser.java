package com.constellio.app.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.constellio.model.conf.FoldersLocator;

public class GradleFileVersionParser {

	private static String version;

	public static String getVersion() {

		if (version == null) {

			version = findVersion();

		}

		return version;
	}

	private static String findVersion() {
		File appLayerBuildGradleFile = new File(new FoldersLocator().getAppProject(), "build.gradle");

		try {
			List<String> appLayerBuildGradleFileLines = FileUtils.readLines(appLayerBuildGradleFile);

			for (int i = 0; i < appLayerBuildGradleFileLines.size(); i++) {
				String line = appLayerBuildGradleFileLines.get(i);

				if (line.contains("baseName = 'core-app'")) {
					String nextLine = appLayerBuildGradleFileLines.get(i + 1);
					int firstQuote = nextLine.indexOf("'");
					int secondQuote;
					if(firstQuote == -1){
						firstQuote = nextLine.indexOf("\"");
						secondQuote = nextLine.indexOf("\"", firstQuote + 1);
					}else{
						secondQuote = nextLine.indexOf("'", firstQuote + 1);
					}
					return nextLine.substring(firstQuote + 1, secondQuote);
				}

			}

			return null;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
