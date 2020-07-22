package com.constellio.sdk.tests;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.sdk.tests.SDKConstellioFactoriesInstanceProvider.DEFAULT_NAME;
import static java.util.Arrays.asList;

public class SaveStateFeature {

	FileSystemTestFeatures fileSystemTestFeatures;
	FactoriesTestFeatures factoriesTestFeatures;
	String afterTestSaveTitle;
	File afterTestSaveFile;

	public SaveStateFeature(FactoriesTestFeatures factoriesTestFeatures,
							FileSystemTestFeatures fileSystemTestFeatures) {
		this.factoriesTestFeatures = factoriesTestFeatures;
		this.fileSystemTestFeatures = fileSystemTestFeatures;
	}

	public void saveCurrentStateTo(File file)
			throws Exception {

		File tempFolder = fileSystemTestFeatures.newTempFolder();
		File tempContentFolder = new File(tempFolder, "content");
		File tempSettingsFolder = new File(tempFolder, "settings");

		DataLayerFactory dataLayerFactory = factoriesTestFeatures.newDaosFactory(DEFAULT_NAME);
		DataLayerConfiguration dataLayerConfiguration = dataLayerFactory.getDataLayerConfiguration();

		if (!dataLayerConfiguration.isSecondTransactionLogEnabled()) {
			throw new RuntimeException("Save state requires second transaction log");
		}

		dataLayerFactory.getSecondTransactionLogManager().regroupAndMove();
		File settingsFolder = dataLayerConfiguration.getSettingsFileSystemBaseFolder();
		File contentFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();

		dataLayerFactory.getConfigManager().exportTo(tempSettingsFolder);
		FileUtils.copyDirectory(contentFolder, tempContentFolder);
		//plugins are not saved during tests since we have to restart system

		List<File> files = asList(tempSettingsFolder, tempContentFolder);

		dataLayerFactory.getIOServicesFactory().newZipService().zip(file, files);

	}

	public static File loadStateFrom(File file, File tempFolder, File settingsFolder, File contentFolder,
									 File pluginsFolder,
									 File tlogWorkFolder, boolean resetPasswords)
			throws Exception {

		File folder;
		if (!file.isDirectory()) {
			folder = new File(tempFolder, "tempUnzipFolder");
			folder.mkdirs();
			IOServices ioServices = new IOServices(tempFolder);
			new ZipService(ioServices).unzip(file, folder);
		} else {
			folder = file;
		}

		File tempUnzipContentFolder = new File(folder, "content");
		File tempUnzipSettingsFolder = new File(folder, "settings");
		File tempUnzipPluginsFolder = new File(folder, "plugins");
		File tempUnzipTlogWorkFolder = new File(folder, "tlog-work");

		if (resetPasswords) {
			changePasswordsToPassword(tempUnzipSettingsFolder);

			File ldapConfigsFile = new File(tempUnzipSettingsFolder, "ldapConfigs.properties");
			//String newLDAPConfigs = "ldap.authentication.active=false";
			//FileUtils.write(ldapConfigsFile, newLDAPConfigs, false);
			FileUtils.deleteQuietly(ldapConfigsFile);

		}

		//FileUtils.copyDirectory(tempUnzipSettingsFolder, settingsFolder);
		FileUtils.copyDirectory(tempUnzipContentFolder, contentFolder);
		if (tempUnzipPluginsFolder.exists()) {
			FileUtils.copyDirectory(tempUnzipPluginsFolder, pluginsFolder);
		}


		if (tempUnzipTlogWorkFolder.exists()) {
			FileUtils.copyDirectory(tempUnzipTlogWorkFolder, tlogWorkFolder);
		}

		return tempUnzipSettingsFolder;
	}

	public static void changePasswordsToPassword(File settingsFolder)
			throws IOException {
		File authenticationFile = new File(settingsFolder, "authentification.properties");
		List<String> lines = FileUtils.readLines(authenticationFile);

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			int indexOfEqual = line.indexOf("=");
			if (indexOfEqual != -1) {
				line = line.substring(0, indexOfEqual) + "=W6ph5Mm5Pz8GgiULbPgzG37mj9g\\=";
				lines.set(i, line);
			}
		}
		FileUtils.writeLines(authenticationFile, lines);
	}

	public void saveStateAfterTestWithTitle(String title) {
		this.afterTestSaveTitle = title;
	}

	public void saveStateAfterTestInFile(File file) {
		this.afterTestSaveFile = file;
	}

	public void afterTest() {
		if (afterTestSaveTitle != null) {
			try {
				saveCurrentStateToInitialStatesFolder(afterTestSaveTitle);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void saveCurrentStateToInitialStatesFolder()
			throws Exception {
		saveCurrentStateToInitialStatesFolder(null);
	}

	public void saveCurrentStateToInitialStatesFolder(String title)
			throws Exception {
		ConstellioModulesManager modulesManager = factoriesTestFeatures.newAppServicesFactory(DEFAULT_NAME).getModulesManager();
		String version = factoriesTestFeatures.newAppServicesFactory(DEFAULT_NAME).newMigrationServices()
				.getCurrentVersion("zeCollection");

		List<String> moduleIds = new ArrayList<>();
		for (Module module : modulesManager.getInstalledModules()) {
			moduleIds.add(module.getId());
		}
		String modulesStr = StringUtils.join(moduleIds, ",");

		File file;
		if (afterTestSaveFile == null) {
			String name;

			name = "given_system_in_" + version + "_with_" + modulesStr + "_module";
			if (modulesStr.contains(",")) {
				name += "s";
			}

			if (title != null) {
				name = name + "__" + title;
			}

			file = new File(new SDKFoldersLocator().getInitialStatesFolder(), name + ".zip");
		} else {
			file = afterTestSaveFile;
		}
		if (file.exists()) {
			file.delete();
		}
		saveCurrentStateTo(file);
		System.out.println("Savestate saved in '" + file.getAbsolutePath() + "'");
	}
}
