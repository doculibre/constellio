package com.constellio.dev;

import com.constellio.data.conf.FoldersLocator;
import jxl.write.WritableFont;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstellioLanguageTableIO {

	public static final String INFOS_SEPARATOR = "=";
	public static final String DEFAULT_FILE_CHARSET = "UTF-8";
	public static final String PRINCIPAL_LANG_FILE = "i18n.properties";
	public static final String PROPERTIES_FILE_EXTENSION = ".properties";
	public static final String PROPERTIES_FILE_ARABIC_SIGNATURE = "_ar";
	public static final String EXCEL_OUTPUT_FILE_EXTENSION = ".xls";
	public static final WritableFont.FontName FONT = WritableFont.ARIAL;
	public static final int FONT_SIZE = 10;
	public static final int ARABIC_CHARACTER_ASSIGNATION_LIMIT = 1791;
	/* default value. If null, then keys with no values will not be added. TODO set wanted value */
	public static final String PROPERTIES_FILE_NO_TRADUCTION_VALUE = "noTraductionAvailable";

	private static final String EXCEL_OUTPUT_FILE_NAME = "output";
	/* path of received input file : TODO modify path with received file */
	//private static final String EXCEL_INPUT_FILE_PATH = "C:\\Workspace\\dev-constellio\\constellio\\resources_i18n\\excelOutput\\i18n_v2_arabic.xls";

	private static final String EXCEL_INPUT_FILE_PATH = "C:\\Workspace\\dev-constellio\\constellio\\resources_i18n\\excelOutput\\i18n_v2_arabic.xls";
	public static final String VERSION_NUMBER_SEPARATOR = "7_6_3";

	private File[] filesAndFolders;
	private Set<File> filesInPath;
	private String minVersion;
	private String maxVersion;

	private File arabicFile;
	private File outputFile;
	private File inputFile;
	private FileOutputStream fileOutputStream;

	/**
	 * Construct IO and parsing functions.
	 *
	 * @param minVersion  - if null, constraint is ignored
	 * @param maxVersion  - if null, constraint is ignored
	 * @param isWriteMode - for previous files deletion
	 * @throws IOException
	 */
	public ConstellioLanguageTableIO(String minVersion, String maxVersion, boolean isWriteMode) throws IOException {
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
		initSystemFiles(isWriteMode);
		filesInPath = getConversionFiles(getFilesAndFolders(), minVersion, maxVersion);
	}

	/**
	 * Initialises files needed.
	 *
	 * @param deletePreviousInfos - true if previous conversion can be deleted before running
	 * @throws IOException
	 */
	private void initSystemFiles(boolean deletePreviousInfos) throws IOException {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		arabicFile = new File(i18nFolder, "i18n_ar.properties");
		File outputDirectory = new File(i18nFolder + File.separator + "excelOutput");
		outputFile = new File(outputDirectory, EXCEL_OUTPUT_FILE_NAME + EXCEL_OUTPUT_FILE_EXTENSION);
		inputFile = new File(EXCEL_INPUT_FILE_PATH);

		// get files to convert
		filesAndFolders = ArrayUtils.addAll(
				i18nFolder.listFiles(),
				new File(foldersLocator.getPluginsRepository(), "/plugin011/src/com/constellio/agent/i18n/"),
				new File(foldersLocator.getPluginsRepository(), "/plugin029/resources/demos/i18n/"),
				new File(foldersLocator.getPluginsRepository(), "/plugin029/resources/demos/migrations/1_0/"),
				new File(foldersLocator.getPluginsRepository(), "/plugin028/resources/workflows/migrations/7_6_10/"),
				new File(foldersLocator.getPluginsRepository(), "/plugin028/resources/workflows/migrations/7_5_2_7/")
		);

		if (deletePreviousInfos) {
			// deletes previous output so its not in input files
			if (outputDirectory.exists()) {
				FileUtils.deleteDirectory(outputDirectory);
			}

			// creates output files
			outputDirectory.mkdir();
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}

			fileOutputStream = new FileOutputStream(outputFile);
		}
	}

	public File getArabicFile() {
		return arabicFile;
	}

	public Set<File> getFilesInPath() {
		return filesInPath;
	}

	// DATA HOLDERS

	public File[] getFilesAndFolders() {
		return filesAndFolders;
	}

	public FileOutputStream getFileOutputStream() {
		return fileOutputStream;
	}

	public File getOutputFile() {
		return outputFile;
	}

	/**
	 * Parses list of available system files and keeps only needed files (combo files with those not included in combo only).
	 *
	 * @param files
	 * @throws IOException
	 */
	protected Set<File> getConversionFiles(File[] files, String minVersion, String maxVersion) throws IOException {
		Set<File> filteredFileSet = filesInPath = new TreeSet<>(
				new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {

						// o2 gets compared before 01 for A-Z sorting (and not Z-A)
						int result = o2.getName().compareTo(o1.getName());

						// makes principal file first, independently of sort method
						if (o2.getName().equals(PRINCIPAL_LANG_FILE)) {
							result = -1;
						}

						return result;
					}
				}
		);

		addToConversion(files, minVersion, maxVersion, filteredFileSet);

		return filteredFileSet;
	}

	protected Set<File> addToConversion(File[] files, String minVersion, String maxVersion, Set<File> filteredFileSet)
			throws IOException {

		for (File file : files) {

			String fileName = file.getName();
			String filePath = file.getAbsolutePath();

			if (file.isDirectory() && (!isVersionNumber(fileName) || (isVersionNumber(fileName) && isVersionNumberInDefinedInteval(fileName, minVersion, maxVersion)) || isInInclusions(filePath))) {
				addToConversion(file.listFiles(), minVersion, maxVersion, filteredFileSet);
			} else if (!file.isDirectory()) {

				filteredFileSet.add(file);

			}
		}

		return filteredFileSet;
	}

	private boolean isVersionNumberInDefinedInteval(String fileName, String minVersion, String maxVersion) {
		return (minVersion == null || fileName.compareTo(minVersion) > 0) && (maxVersion == null || fileName.compareTo(maxVersion) < 0);
	}

	// INCLUSION/EXCLUSION TOOLS

	protected static boolean isBasePropertyFile(String currentLine) {
		Pattern pattern = Pattern.compile("\\d.properties$");
		Matcher matcher = pattern.matcher(currentLine);

		return matcher.find();
	}

	protected static boolean isRootPropertyFile(String currentLine) {
		Pattern pattern = Pattern.compile("i18n.properties$");
		Matcher matcher = pattern.matcher(currentLine);

		return matcher.find();
	}

	protected static boolean isComboPropertyFile(String currentLine) {
		Pattern pattern = Pattern.compile("combo.properties$");
		Matcher matcher = pattern.matcher(currentLine);

		return matcher.find();
	}

	protected static boolean isAgentPropertyFile(String currentLine) {
		Pattern pattern = Pattern.compile("^agent");
		Matcher matcher = pattern.matcher(currentLine);

		return matcher.find();
	}

	protected static boolean isDemosPropertyFile(String currentLine) {
		Pattern pattern = Pattern.compile("^demos");
		Matcher matcher = pattern.matcher(currentLine);

		return matcher.find();
	}

	protected static boolean isInInclusions(String currentLine) {
		return StringUtils.containsIgnoreCase(currentLine, "workflows") || StringUtils.containsIgnoreCase(currentLine, "agent") || StringUtils.containsIgnoreCase(currentLine, "demos");
	}

	protected boolean isVersionNumber(String currentLine) {
		Pattern pattern = Pattern.compile("^\\d+_\\d");
		Matcher matcher = pattern.matcher(currentLine);

		return matcher.find();
	}

	// FILE READERS

	/**
	 * Get information read from a specific property file while preserving read order.
	 *
	 * @param folder   root of file
	 * @param fileName to append to folder to create final file
	 * @return infos
	 */
	protected static Map<String, String> getFileInfos(File folder, String fileName) {

		Map<String, String> infos = new LinkedHashMap<>();

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

	/**
	 * Get property name from read line. Uses specified pattern.
	 *
	 * @param currentLine - line read
	 * @return the property
	 */
	private static String getPropertyName(String currentLine) {

		String propertyName = null;
		String[] lineParts = currentLine.split(INFOS_SEPARATOR);

		if (lineParts.length == 2) {
			propertyName = lineParts[0];
		}

		return propertyName;
	}

	/**
	 * Checks whether "\" multiline character in property files is used.
	 *
	 * @param currentLine
	 * @return true if multiline
	 */
	private static boolean isNotClosed(String currentLine) {
		Pattern pattern = Pattern.compile("\\\\$");
		Matcher matcher = pattern.matcher(currentLine);

		return matcher.find();
	}

	public File getInputFile() {
		return inputFile;
	}
}