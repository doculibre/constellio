package com.constellio.sdk.dev.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.Language;

public class CompareI18nKeys {

	public static void main(String argv[])
			throws Exception {

		ListComparisonResults<String> results = compare(Language.English);
		System.out.println(getComparisonMessage(Language.English, results));

	}

	public static String getComparisonMessage(Language language, ListComparisonResults<String> comparisonResults) {
		StringBuilder result = new StringBuilder("");
		String languageFilename = "i18n_" + language.getCode() + ".properties";
		result.append("Keys in i18n.properties that are not in " + languageFilename);
		for (String key : comparisonResults.getRemovedItems()) {
			result.append("\n\t" + key);
		}

		result.append("\n\n\nKeys in " + languageFilename + " that are not in i18n.properties");
		for (String key : comparisonResults.getNewItems()) {
			result.append("\n\t" + key);
		}
		return result.toString();
	}

	public static ListComparisonResults<String> compare(Language language)
			throws Exception {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		String languageFilename = "i18n_" + language.getCode() + ".properties";
		List<String> defaultKeys = loadI18nKeys(new File(i18nFolder, "i18n.properties"));
		List<String> englishKeys = loadI18nKeys(new File(i18nFolder, languageFilename));
		return LangUtils.compare(defaultKeys, englishKeys);
	}

	private static List<String> loadI18nKeys(File file)
			throws IOException {
		List<String> keys = new ArrayList<>();

		Iterator<String> linesIterator = FileUtils.readLines(file).iterator();

		while(linesIterator.hasNext()) {
			String line = linesIterator.next();
			while(line.endsWith("\\")) {
				line += linesIterator.next();
			}
			if (line.contains("=")) {
				keys.add(line.split("=")[0]);
			}
		}
		return keys;
	}

}
