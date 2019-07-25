package com.constellio.model.conf;

import com.constellio.data.utils.ImpossibleRuntimeException;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FoldersLocator {

	public static final String CONSTELLIO_TMP = "constellio_tmp";
	private static boolean CONTEXT_PRINTED = false;

	private static FoldersLocatorMode foldersLocatorModeCached;

	private static String currentClassPath;

	private File defaultTempFolder;

	public FoldersLocator() {
	}


	public File getJavaRootFolder() {
		String fullPath = getCurrentClassPath();
		File finalPath;

		if (fullPath.contains("file:") && fullPath.contains("!")) {
			String path = fullPath.split("!")[0];

			if (path.contains("constellio-plugins/sdk")) {
				// is the plugin sdk test
				path = path.split("file:")[0];

				File classFolder = new File(path);
				finalPath = classFolder;
			} else {
				// is the webapp
				path = path.split("file:")[1];
				File classFolder = new File(path);
				finalPath = classFolder.getParentFile();

				while (!finalPath.getName().equals("model") && !finalPath.getName().equals("WEB-INF")) {
					finalPath = finalPath.getParentFile();
				}

				finalPath = finalPath.getParentFile().getAbsoluteFile();
			}
		} else {
			File classFolder = new File(fullPath);

			if (classFolder.getParentFile().getParentFile().getParentFile().getParentFile().getName().equals("bin")) {
				finalPath = classFolder.getParentFile().getParentFile().getParentFile().getParentFile()
						.getParentFile().getAbsoluteFile();
			} else if (classFolder.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile()
					.getName().equals("bin")) {
				// eclipse
				finalPath = classFolder.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile()
						.getParentFile().getAbsoluteFile();
			} else {
				finalPath = classFolder.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile()
						.getParentFile().getParentFile().getParentFile().getAbsoluteFile();
			}

		}

		return finalPath;
	}

	String getCurrentClassPath() {
		if (currentClassPath == null) {
			currentClassPath = new File(FoldersLocator.class.getResource("").getFile()).getAbsoluteFile().getPath();
		}
		return currentClassPath;
	}

	public File getWrapperInstallationFolder() {
		if (getFoldersLocatorMode() != FoldersLocatorMode.WRAPPER) {
			throw new FoldersLocatorRuntimeException("getWrapperInstallationFolder requires wrapper mode");
		}
		return getConstellioWebappFolder().getParentFile();
	}

	public File getTomcatInstallationFolder() {
		return getConstellioWebappFolder().getParentFile().getParentFile();
	}

	public boolean isWrapperInstallationFolder(File file) {
		if (!file.exists()) {
			return false;
		}

		List<String> fileChildren = Arrays.asList(file.list());
		List<String> fileChildrenExpectedList = Arrays.asList("bin", "conf", "webapp");

		return fileChildren.containsAll(fileChildrenExpectedList);
	}

	public File getKeystoreFile() {
		return new File(getConfFolder(), "keystore.jks");
	}

	public File getWrapperConf() {
		return new File(getConfFolder(), "wrapper.conf");
	}

	public File getBPMNsFolder() {
		return new File(getConfFolder(), "bpmns");
	}

	public File getSmtpMailFolder() {
		return new File(getConfFolder(), "smtpMail");
	}

	public File getConfFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getWrapperInstallationFolder(), "conf");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getTomcatInstallationFolder(), "conf");

		} else {
			return new File(getConstellioWebappFolder(), "conf");
		}
	}

	public File getReindexingFolder() {
		return new File(getWorkFolder(), "reindexing");
	}

	public File getReindexingAggregatedValuesFolder() {
		return new File(getReindexingFolder(), "aggregatedValues");
	}

	public File getReindexationLock() {
		return new File(getReindexingFolder(), "reindexation.lock");
	}

	public File getVAADINFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConstellioWebappFolder(), "VAADIN");
		} else {
			return new File(getAppProjectWebContent(), "VAADIN");
		}
	}

	public File getThemes() {
		return new File(getVAADINFolder(), "themes");
	}

	public File getConstellioTheme() {
		return new File(getThemes(), "constellio");
	}

	public File getConstellioThemeImages() {
		return new File(getConstellioTheme(), "images");
	}

	public File getImportationProject() {
		return new File(getPluginsRepository(), "importation");
	}

	public File getAppProject() {
		return new File(getConstellioWebappFolder(), "app");
	}

	public File getAppProjectWebContent() {
		return new File(getAppProject(), "WebContent");
	}

	public File getConstellioProperties() {
		return new File(getConfFolder(), "constellio.properties");
	}

	public File getConstellioSetupProperties() {
		return new File(getConfFolder(), "constellio.setup.properties");
	}

	public File getBinFolder() {
		return new File(getWrapperInstallationFolder(), "bin");
	}

	public File getBatFolder() {
		return new File(getWrapperInstallationFolder(), "bat");
	}

	public File getSolrHomeConfFolder(double solrVersion) {
		if (solrVersion < 6) {
			return new File(getConstellioWebappFolder(), "solrHome5");
		}
		return new File(getConstellioWebappFolder(), "solrHome7");
	}

	public File getWrapperCommandFolder() {
		return new File(getConstellioWebinfFolder(), "command");
	}

	public File getWrapperCommandFile() {
		File commandFile;
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			commandFile = new File(getWrapperCommandFolder(), "cmd");
		} else {
			commandFile = new File(getDefaultTempFolder(), "cmd");
		}
		return commandFile;
	}

	// TODO Not supporting custom temp folder
	/*private File getTempUpdateClientFolder(File tempFolder) {
		return new File(tempFolder, "update-client");
	}*/

	public File getDefaultTempFolder() {

		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			File file = new File(getWrapperInstallationFolder().getParentFile(), CONSTELLIO_TMP);
			if (file.exists() && file.isDirectory()) {
				return file;
			} else {
				return new File(getWrapperInstallationFolder(), "temp");
			}
		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getTomcatInstallationFolder(), "temp");
		} else {
			return new File(getConstellioProject(), "temp");
		}
	}

	public File getConstellioProject() {
		return getConstellioWebappFolder();
	}

	public File getConstellioWebappFolder() {
		File javaRootFolder = getJavaRootFolder();

		File webappFolder;
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			webappFolder = getWrapperWebappFolder(javaRootFolder);
		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			webappFolder = getTomcatWebappFolder(javaRootFolder);
		} else {
			webappFolder = getGitWebappFolder(javaRootFolder);
		}
		return webappFolder;
	}

	private File getGitWebappFolder(File javaRootFolder) {
		List<String> gitSubProjects = Arrays.asList("app", "data", "model", "custom", "sdk");
		String lowercaseJavaRootFolder = javaRootFolder.getName().toLowerCase();

		// TODO Remove intelligid
		if (!"constellio-plugins".equals(lowercaseJavaRootFolder) && ("constellio".equals(lowercaseJavaRootFolder) || "intelligid"
				.equals(lowercaseJavaRootFolder) || lowercaseJavaRootFolder.startsWith("constellio-") || lowercaseJavaRootFolder
																			  .startsWith("constellio_") || lowercaseJavaRootFolder.startsWith("intelligid-"))) {
			return javaRootFolder;

		} else if (gitSubProjects.contains(lowercaseJavaRootFolder)) {
			if (javaRootFolder.getParentFile().getName().equals("constellio-plugins")) {
				return new File(javaRootFolder.getParentFile().getParentFile(), "constellio");
			} else {
				return javaRootFolder.getParentFile();
			}

		} else {
			throw new IllegalStateException("Cannot find project folder for java root folder '" + javaRootFolder.getName()
											+ "' path = '" + javaRootFolder.getAbsolutePath() + "'");
		}
	}

	private File getTomcatWebappFolder(File javaRootFolder) {
		File parentJavaRootFolder = javaRootFolder.getAbsoluteFile().getParentFile();
		if (parentJavaRootFolder.getName().equals("webapps")) {
			return javaRootFolder;

		} else {

			File webappsFolder;
			if (javaRootFolder.getName().contains("tomcat")) {
				webappsFolder = new File(javaRootFolder, "webapps");
			} else {
				webappsFolder = new File(parentJavaRootFolder, "webapps");
			}
			File intelligidWebapp = new File(webappsFolder, "intelligid");
			if (intelligidWebapp.exists()) {
				return intelligidWebapp;
			}

			File constellioWebapp = new File(webappsFolder, "constellio");
			if (constellioWebapp.exists()) {
				return constellioWebapp;
			}
			throw new ImpossibleRuntimeException(
					"Cannot detect application. Need a 'constellio' or 'intelligid' application in webapps folder");
		}

	}

	private File getWrapperWebappFolder(File javaRootFolder) {
		return javaRootFolder;
	}

	public FoldersLocatorMode getFoldersLocatorMode() {
		if (foldersLocatorModeCached == null) {
			foldersLocatorModeCached = detectFoldersLocatorMode();

			if (!CONTEXT_PRINTED) {
				System.out.println("========================================================================");
				System.out.println("CLASS FOLDER  : '" + getCurrentClassPath() + "'");
				System.out.println("i18N FOLDER   : '" + getI18nFolder() + "'");
				System.out.println("CATALINA_HOME : '" + System.getenv("CATALINA_HOME") + "'");
				System.out.println("ROOT FOLDER   : '" + getJavaRootFolder().getAbsolutePath() + "'");
				System.out.println("LOCATOR MODE  : '" + foldersLocatorModeCached + "'");
				System.out.println("WEBAPP FOLDER : '" + getConstellioWebappFolder().getAbsolutePath() + "'");
				System.out.println("CONF FOLDER   : '" + getConfFolder().getAbsolutePath() + "'");
				System.out.println("TEMP FOLDER   : '" + getDefaultTempFolder().getAbsolutePath() + "'");
				if (foldersLocatorModeCached == FoldersLocatorMode.TOMCAT) {
					System.out.println("TOMCAT INSTALL: '" + getTomcatInstallationFolder() + "'");
				}
				System.out.println("========================================================================");
				CONTEXT_PRINTED = true;
			}
		}
		return foldersLocatorModeCached;
	}

	public FoldersLocatorMode detectFoldersLocatorMode() {
		File javaRootFolder = getJavaRootFolder();
		FoldersLocatorMode mode;
		if (isTomcat()) {
			mode = FoldersLocatorMode.TOMCAT;
		} else {
			List<String> possibleWrapperJavaRootFolders = Arrays.asList("bin", "conf", "jdoc");

			boolean isWrapper = isWrapperInstallationFolder(javaRootFolder) || possibleWrapperJavaRootFolders
					.contains(javaRootFolder.getName()) || javaRootFolder.getName().contains("webapp");
			mode = isWrapper ? FoldersLocatorMode.WRAPPER : FoldersLocatorMode.PROJECT;
		}

		return mode;
	}

	private boolean isTomcat() {
		File javaRootFolder = getJavaRootFolder().getAbsoluteFile();
		File parentJavaRootFolder = javaRootFolder.getParentFile();
		return javaRootFolder.getName().contains("tomcat") || (parentJavaRootFolder != null && (
				parentJavaRootFolder.getName().toLowerCase().contains("tomcat")
				|| parentJavaRootFolder.getName().equals("webapps")));
	}

	public File getConstellioWebinfFolder() {
		//		String webappFolder = getConstellioWebappFolder().getAbsolutePath();
		return new File(getConstellioWebappFolder(), "WEB-INF");
	}

	public File getUploadConstellioWarFile() {
		File warFile;
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			warFile = new File(getWrapperInstallationFolder(), "constellio.war");
		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			warFile = new File(getTomcatInstallationFolder(), "constellio.war");
		} else {
			warFile = new File(getDefaultTempFolder(), "constellio.war");
		}
		return warFile;
	}

	public File getLastAlertFile() {
		return new File(getDefaultTempFolder(), "lastAlert.pdf");
	}

	public File getDefaultImportationFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConstellioWebinfFolder(), "to-import");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getTomcatInstallationFolder(), "to-import");

		} else {
			return new File(getConstellioProject(), "to-import");
		}
	}

	public File getCustomProject() {
		return new File(getConstellioProject(), "custom");
	}

	public File getBuildLibs() {
		return new File(getConstellioProject(), "build" + File.separator + "libs");
	}

	public File getDefaultSettingsFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConfFolder(), "settings");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getTomcatInstallationFolder(), "settings");

		} else {
			return new File(getConfFolder(), "settings");
		}
	}

	public File getLibFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER || getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			File webinfFolder = getConstellioWebinfFolder();
			return new File(webinfFolder, "lib");

		} else {
			throw new FoldersLocatorRuntimeException.NotAvailableInGitMode("WEB-INF/lib");
		}
	}

	public File getLibFolder(File webAppFolder) {
		File webInf = new File(webAppFolder, "WEB-INF");
		return new File(webInf, "lib");
	}

	public File getSDKProject() {
		return new File(getConstellioProject(), "sdk");
	}

	public File getSDKResourcesProject() {
		return new File(getSDKProject(), "sdk-resources");
	}

	public File getPluginsRepository() {
		return new File(getConstellioProject().getParentFile(), "constellio-plugins");
	}

	public File getPluginsSDKProject() {
		return new File(getPluginsRepository(), "sdk");
	}

	public File getLanguageProfiles() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConstellioWebinfFolder(), "languageProfiles");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getConstellioWebinfFolder(), "languageProfiles");

		} else {
			return new File(getConstellioWebappFolder(), "languageProfiles");
		}
	}

	public File getModulesResourcesFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConstellioWebinfFolder(), "modules-resources");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getConstellioWebinfFolder(), "modules-resources");

		} else {
			return new File(getConstellioWebappFolder(), "modules-resources");
		}
	}

	public File getModuleResourcesFolder(String module) {
		return new File(getModulesResourcesFolder() + File.separator + module);
	}

	public File getDict() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConstellioWebinfFolder(), "dict");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getConstellioWebinfFolder(), "dict");

		} else {
			return new File(getConstellioWebappFolder(), "dict");
		}
	}

	public File getI18nFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConstellioWebinfFolder(), "resources_i18n");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getConstellioWebinfFolder(), "resources_i18n");

		} else {
			return new File(getConstellioWebappFolder(), "resources_i18n");
		}
	}

	public File getPluginsResourcesFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConstellioWebinfFolder(), "plugins-modules-resources");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getConstellioWebinfFolder(), "plugins-modules-resources");

		} else {
			return new File(getConstellioWebappFolder(), "plugins-modules-resources");
		}
	}

	public File getReportsResourceFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConstellioWebinfFolder(), "resources" + File.separator + "reports");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getConstellioWebinfFolder(), "resources" + File.separator + "reports");

		} else {
			return new File(getConstellioWebappFolder(), "resources" + File.separator + "reports");
		}
	}

	public File getResourcesFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConstellioWebinfFolder(), "resources");

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getConstellioWebinfFolder(), "resources");

		} else {
			return new File(getConstellioWebappFolder(), "resources");
		}
	}

	public static void invalidateCaches() {
		foldersLocatorModeCached = null;
		currentClassPath = null;
	}

	public File getConstellioEncryptionFile() {
		String encryptionFileName = "key.txt";
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return new File(getConfFolder(), encryptionFileName);

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getConfFolder(), encryptionFileName);

		} else {
			return new File(getDefaultSettingsFolder(), encryptionFileName);
		}
	}

	public File getPluginsJarsFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER || getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			return new File(getConstellioWebinfFolder(), "plugins");

		} else {
			return new File(getConstellioWebappFolder(), "plugins");
		}
	}

	public File getPluginsToMoveOnStartupFile() {
		File pluginManagementFolder;
		if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER || getFoldersLocatorMode() == FoldersLocatorMode.TOMCAT) {
			pluginManagementFolder = new File(getConstellioWebinfFolder(), "pluginsManagement");

		} else {
			pluginManagementFolder = new File(getConstellioWebappFolder(), "pluginsManagement");
		}
		return new File(pluginManagementFolder, "toMoveOnStartup");
	}

	public File getPluginsJarsFolder(File webAppFolder) {
		File webInf = new File(webAppFolder, "WEB-INF");
		return new File(webInf, "plugins");
	}

	public File getPluginsToMoveOnStartupFile(File webAppFolder) {
		File webInf = new File(webAppFolder, "WEB-INF");
		File pluginManagementFolder = new File(webInf, "pluginsManagement");
		return new File(pluginManagementFolder, "toMoveOnStartup");
	}

	public File getUploadLicenseFile() {
		return new File(getDefaultTempFolder(), "license.xml");
	}

	public File getLicenseFile() {
		return new File(getConfFolder(), "license.xml");
	}

	public File getWorkFolder() {
		if (getFoldersLocatorMode() == FoldersLocatorMode.PROJECT) {
			//File workFolder = new File("/raid/francis/work"); //new File(getSDKProject(), "work");
			File workFolder = new File(getSDKProject(), "work");
			workFolder.mkdirs();
			return workFolder;

		} else if (getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			File workFolder = new File(getWrapperInstallationFolder(), "work");
			workFolder.mkdirs();
			return workFolder;

		} else {
			throw new UnsupportedOperationException("Unsupported on tomcat");
		}
	}

	/*
	public File getBuildDataFile() {
		return new File(getConstellioWebappFolder(), "data.txt");
	}
	*/
}
