package com.constellio.sdk.dev.tools;

import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

		List<String> removedItems = new ArrayList<>(comparisonResults.getRemovedItems());
		List<String> newItems = new ArrayList<>(comparisonResults.getNewItems());

		Collections.sort(removedItems);
		Collections.sort(newItems);

		for (String key : removedItems) {
			result.append("\n\t" + key);
		}

		result.append("\n\n\nKeys in " + languageFilename + " that are not in i18n.properties");
		for (String key : newItems) {
			result.append("\n\t" + key);
		}
		return result.toString();
	}

	public static String getComparisonMessage(Language language, ListComparisonResults<String> comparisonResults,
											  String keysFileName) {
		StringBuilder result = new StringBuilder("");
		String languageFilename = keysFileName + "_" + language.getCode() + ".properties";
		result.append("Keys in " + keysFileName + ".properties that are not in " + languageFilename);

		List<String> removedItems = new ArrayList<>(comparisonResults.getRemovedItems());
		List<String> newItems = new ArrayList<>(comparisonResults.getNewItems());

		Collections.sort(removedItems);
		Collections.sort(newItems);

		for (String key : removedItems) {
			result.append("\n\t" + key);
		}

		result.append("\n\n\nKeys in " + languageFilename + " that are not in " + keysFileName + ".properties");
		for (String key : newItems) {
			result.append("\n\t" + key);
		}
		return result.toString();
	}

	public static ListComparisonResults<String> compare(Language language)
			throws Exception {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		String languageFilename = "i18n_" + language.getCode() + ".properties";
		List<String> defaultKeys = loadI18nKeysWithValue(new File(i18nFolder, "i18n.properties"));
		List<String> englishKeys = loadI18nKeysWithValue(new File(i18nFolder, languageFilename));
		return LangUtils.compare(defaultKeys, englishKeys);
	}

	public static ListComparisonResults<String> compareKeys(Language language, String folderName, String keysFileName)
			throws Exception {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		String languageFilename = keysFileName + "_" + language.getCode() + ".properties";
		List<String> defaultKeys = loadI18nKeysWithValue(new File(new File(i18nFolder, folderName), keysFileName + ".properties"));
		List<String> englishKeys = loadI18nKeysWithValue(new File(new File(i18nFolder, folderName), languageFilename));
		return LangUtils.compare(defaultKeys, englishKeys);
	}

	private static List<String> loadI18nKeysWithValue(File file)
			throws IOException {
		List<String> keys = new ArrayList<>();

		Map<String, String> properties = PropertyFileUtils.loadKeyValues(file);

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			if (StringUtils.isNotBlank(entry.getValue())) {
				keys.add(entry.getKey());
			}
		}

		//		Iterator<String> linesIterator = FileUtils.readLines(file).iterator();
		//
		//		while (linesIterator.hasNext()) {
		//			String line = linesIterator.next();
		//			while (line.endsWith("\\")) {
		//				line += linesIterator.next();
		//			}
		//			if (line.contains("=")) {
		//				keys.add(line.split("=")[0]);
		//			}
		//		}
		return keys;
	}

}
