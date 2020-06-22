package com.constellio.dev;

import com.constellio.data.conf.FoldersLocator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertConstellioToArabic {

	public static final String INFOS_SEPARATOR = "=";
	public static final String DEFAULT_FILE_CHARSET = "UTF-8";

	// TODO NOTE : these files are not submitted into this branch. Find them on branch "arabic-demo" here : https://github.com/doculibre/constellio-dev
	public static final String TEMP_CONVERSION_FOLDER = "temp_files_from_github";

	public static void main(String[] args) {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();

		Map<String, String> arabicInfos = getFileInfos(i18nFolder, TEMP_CONVERSION_FOLDER + File.separator + "i18n_ar.properties");
		writeInfosToFile(i18nFolder, "i18n.properties", "i18n_ar.properties", arabicInfos);
	}

	private static Map<String, String> getFileInfos(File folder, String fileName) {

		Map<String, String> infos = new HashMap<>();

		File file = new File(folder, fileName);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_FILE_CHARSET))) {

			String previousLine = "";
			String currentLine;

			while ((currentLine = br.readLine()) != null) {
				if (isNotClosed(currentLine)) {
					previousLine = currentLine;
				} else {
					currentLine = previousLine + currentLine;
					previousLine = "";

					String currentProperty = getPropertyName(currentLine);

					if (currentProperty != null) {
						infos.put(currentProperty, currentLine.replace(currentProperty + INFOS_SEPARATOR, ""));
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return infos;
	}

	private static void writeInfosToFile(File folder, String inputFileName, String outputFileName,
										 Map<String, String> infos) {

		File inputFile = new File(folder, inputFileName);
		File outputFile = new File(folder, outputFileName);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), DEFAULT_FILE_CHARSET)); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), DEFAULT_FILE_CHARSET));) {

			String previousLine = "";
			String currentLine;

			while ((currentLine = br.readLine()) != null) {
				if (isNotClosed(currentLine)) {
					previousLine = currentLine;
				} else {
					currentLine = previousLine + currentLine;
					previousLine = "";

					String currentProperty = getPropertyName(currentLine);

					String lineToWrite = currentLine;

					if (currentProperty != null && infos.containsKey(currentProperty)) {
						lineToWrite = currentProperty + INFOS_SEPARATOR + infos.get(currentProperty);
					}

					bw.write(lineToWrite);
					bw.newLine();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getPropertyName(String currentLine) {

		String propertyName = null;
		String[] lineParts = currentLine.split(INFOS_SEPARATOR);

		if (lineParts.length == 2) {
			propertyName = lineParts[0];
		}

		return propertyName;
	}

	private static boolean isNotClosed(String currentLine) {
		Pattern pattern = Pattern.compile("\\\\$");
		Matcher matcher = pattern.matcher(currentLine);

		return matcher.find();
	}
}