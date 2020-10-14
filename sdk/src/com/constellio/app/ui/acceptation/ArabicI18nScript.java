package com.constellio.app.ui.acceptation;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.entities.Language;
import com.constellio.sdk.dev.tools.CompareI18nKeys;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class ArabicI18nScript {

	public static void main(String[] args) {
		addMissingI18nForArabicLanguageFiles(Language.Arabic);
		addMissingI18nForArabicLanguageCoreFile();
		addMissingI18nForArabicLanguageEsFile();
		addMissingI18nForArabicLanguageRmFile();
		addMissingI18nForArabicLanguageRobotsFile();
		addMissingI18nForArabicLanguageTasksFile();
		addMissingI18nForArabicLanguageMigrationFiles();
	}

	private static void addMissingI18nForArabicLanguageFiles(Language language) {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		String languageFilename = "i18n_" + language.getCode() + ".properties";
		List<String> defaultKeys = loadI18nKeysWithValue(new File(i18nFolder, "i18n.properties"));
		List<String> englishKeys = loadI18nKeysWithValue(new File(i18nFolder, languageFilename));
		ListComparisonResults<String> missingI18ns = LangUtils.compare(defaultKeys, englishKeys);
		for (String missingI18n : missingI18ns.getRemovedItems()) {
			addMissingI18n(missingI18n, i18nFolder, languageFilename, new File(i18nFolder, "i18n.properties"));
		}
	}

	private static void addMissingI18n(String missingI18n, File i18nParentFolder, String languageFilename,
									   File defaultKeysFile) {
		System.out.println("****La clé en Français de: " + missingI18n + " est : \n" + loadI18nMapWithValue(defaultKeysFile).get(missingI18n));
		System.out.println("****Veuillez saisir la clé en arabe : ");
		String arabicValue = readResponse();
		System.out.println("****Veuillez confirmer : (y/n)");
		String confirm = readResponse();
		if ("y".equals(confirm)) {
			writeInFile(missingI18n, arabicValue, new File(i18nParentFolder, languageFilename));
		} else {
			addMissingI18n(missingI18n, i18nParentFolder, languageFilename, defaultKeysFile);
		}
	}

	private static void writeInFile(String missingI18n, String arabicValue, File file) {
		try (FileWriter fileWriter = new FileWriter(file, true)) {
			fileWriter.append("\n" + missingI18n + "=" + arabicValue);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String readResponse() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			return br.readLine();
		} catch (IOException ioe) {
			return "n";
		}
	}

	private static List<String> loadI18nKeysWithValue(File file) {
		List<String> keys = new ArrayList<>();

		Map<String, String> properties = PropertyFileUtils.loadKeyValues(file);

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			if (StringUtils.isNotBlank(entry.getValue())) {
				keys.add(entry.getKey());
			}
		}


		return keys;
	}

	private static Map<String, String> loadI18nMapWithValue(File file) {
		return PropertyFileUtils.loadKeyValues(file);
	}


	private static void addMissingI18nForArabicLanguageCoreFile() {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		File core = new File(i18nFolder, "core");
		addMissingI18n(Language.Arabic, core, "baseView");
		addMissingI18n(Language.Arabic, core, "imports");
		addMissingI18n(Language.Arabic, core, "managementViews");
		//		addMissingI18n(Language.Arabic, core, "model");
		addMissingI18n(Language.Arabic, core, "schemasManagementViews");
		addMissingI18n(Language.Arabic, core, "search");
		addMissingI18n(Language.Arabic, core, "security");
		addMissingI18n(Language.Arabic, core, "usersAndGroupsManagementViews");
		addMissingI18n(Language.Arabic, core, "userViews");
		addMissingI18n(Language.Arabic, core, "webServices");
	}

	private static void addMissingI18nForArabicLanguageEsFile() {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		File es = new File(i18nFolder, "es");
		addMissingI18n(Language.Arabic, es, "i18n");
	}

	private static void addMissingI18nForArabicLanguageRmFile() {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		File rm = new File(i18nFolder, "rm");
		addMissingI18n(Language.Arabic, rm, "audits");
		addMissingI18n(Language.Arabic, rm, "decommissioningViews");
		addMissingI18n(Language.Arabic, rm, "demo");
		addMissingI18n(Language.Arabic, rm, "foldersAndDocuments");
		addMissingI18n(Language.Arabic, rm, "managementViews");
		addMissingI18n(Language.Arabic, rm, "model");
		addMissingI18n(Language.Arabic, rm, "reports");
		addMissingI18n(Language.Arabic, rm, "storageAndContainers");
		addMissingI18n(Language.Arabic, rm, "userViews");
	}

	private static void addMissingI18nForArabicLanguageRobotsFile() {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		File robots = new File(i18nFolder, "robots");
		addMissingI18n(Language.Arabic, robots, "i18n");
	}

	private static void addMissingI18nForArabicLanguageTasksFile() {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		File robots = new File(i18nFolder, "tasks");
		addMissingI18n(Language.Arabic, robots, "model");
		addMissingI18n(Language.Arabic, robots, "views");
		addMissingI18n(Language.Arabic, robots, "workflowBeta");
	}

	private static void addMissingI18nForArabicLanguageMigrationFiles() {
		addMissingI18nForArabicLanguageMigrationFiles("core");
		addMissingI18nForArabicLanguageMigrationFiles("es");
		addMissingI18nForArabicLanguageMigrationFiles("es_rm_robots");
		addMissingI18nForArabicLanguageMigrationFiles("rm");
		addMissingI18nForArabicLanguageMigrationFiles("robots");
		addMissingI18nForArabicLanguageMigrationFiles("tasks");
		//				addMissingI18nForArabicLanguageMigrationFiles("sharepointGraphAPI");
	}

	private static void addMissingI18n(Language language, File i18nParentFolder, String keysFileName) {
		System.out.println("*****************Adding missing i18n for " + keysFileName + " module*****************");
		String languageFilename = keysFileName + "_" + language.getCode() + ".properties";
		List<String> defaultKeys = loadI18nKeysWithValue(new File(i18nParentFolder, keysFileName + ".properties"));
		List<String> englishKeys = loadI18nKeysWithValue(new File(i18nParentFolder, languageFilename));
		ListComparisonResults<String> missingI18ns = LangUtils.compare(defaultKeys, englishKeys);
		for (String missingI18n : missingI18ns.getRemovedItems()) {
			addMissingI18n(missingI18n, i18nParentFolder, languageFilename, new File(i18nParentFolder, keysFileName + ".properties"));
		}
		System.out.println("*****************Finished adding missing i18n for " + keysFileName + " module*****************");
	}

	private static void addMissingI18n(File i18nArabicFolder, File i18nFrenchFolder) {
		ListComparisonResults<String> missingI18ns = CompareI18nKeys.compareKeys(i18nArabicFolder, i18nFrenchFolder);
		if (!missingI18ns.getRemovedItems().isEmpty()) {
			System.out.println("****Chemin du fichier de destination? :");
			String destinationFilePath = readResponse();
			for (String missingI18n : missingI18ns.getRemovedItems()) {
				addMissingI18nToMigrationFile(missingI18n, i18nFrenchFolder, destinationFilePath);
			}
		}
	}

	private static void addMissingI18nToMigrationFile(String missingI18n, File defaultKeysFile,
													  String destinationFilePath) {
		System.out.println("****La clé en Français de: " + missingI18n + " est : \n" + loadI18nMapWithValue(defaultKeysFile).get(missingI18n));
		System.out.println("****Veuillez saisir la clé en arabe : ");
		String arabicValue = readResponse();
		System.out.println("****Veuillez confirmer : (y/n)");
		String confirm = readResponse();
		if ("y".equals(confirm)) {
			writeInFile(missingI18n, arabicValue, new File(destinationFilePath));
		} else {
			addMissingI18nToMigrationFile(missingI18n, defaultKeysFile, destinationFilePath);
		}
	}

	private static void addMissingI18nForArabicLanguageMigrationFiles(String module) {
		FoldersLocator foldersLocator = new FoldersLocator();
		File i18nFolder = foldersLocator.getI18nFolder();
		File[] tasksMigrationFiles = new File(new File(i18nFolder, "migrations"), module).listFiles();
		IOServices ioServices = new IOServices(new File("C:\\Workspace\\constellio-dev\\constellio\\sdk\\src\\com\\constellio\\app\\ui\\acceptation\\temo"));
		File frenchDestinationFile = ioServices.newTemporaryFile("arabicTempFile");
		File arabicDestinationFile = ioServices.newTemporaryFile("frenchTempFile");

		try {
			for (File file : tasksMigrationFiles) {
				String keysFileName = module + "_" + file.getName();

				if (Arrays.asList(file.list()).contains(keysFileName + ".properties")) {
					String frenchFilename = keysFileName + ".properties";
					File frenchI18n = new File(file.getAbsolutePath() + "\\" + frenchFilename);
					appendFile(frenchI18n, frenchDestinationFile);
				}
				if (Arrays.asList(file.list()).contains(keysFileName + "_ar.properties")) {
					String arabicFilename = keysFileName + "_ar.properties";

					File arabicI18n = new File(file.getAbsolutePath() + "\\" + arabicFilename);
					appendFile(arabicI18n, arabicDestinationFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("*****************Adding missing i18n in migration files for " + module + " module*****************");
		addMissingI18n(arabicDestinationFile, frenchDestinationFile);
		System.out.println("*****************Finished adding missing i18n in migration files for " + module + " module*****************");
	}

	private static void appendFile(File sourceFile, File destinationFile) throws IOException {
		BufferedReader bufferedReader = null;
		FileWriter fileWriter = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(sourceFile));
			fileWriter = new FileWriter(destinationFile, true);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				fileWriter.append(line);
				fileWriter.append("\n");
			}
		} finally {
			bufferedReader.close();
			fileWriter.close();
		}
	}

}
