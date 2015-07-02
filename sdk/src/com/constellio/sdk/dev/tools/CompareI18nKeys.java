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
package com.constellio.sdk.dev.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

		for (String line : FileUtils.readLines(file)) {
			if (line.contains("=")) {
				keys.add(line.split("=")[0]);
			}
		}
		return keys;
	}

}
