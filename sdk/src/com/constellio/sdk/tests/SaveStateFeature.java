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
package com.constellio.sdk.tests;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.services.extensions.ConstellioModulesManager;

public class SaveStateFeature {

	FileSystemTestFeatures fileSystemTestFeatures;
	FactoriesTestFeatures factoriesTestFeatures;
	String afterTestSaveTitle;

	public SaveStateFeature(FactoriesTestFeatures factoriesTestFeatures, FileSystemTestFeatures fileSystemTestFeatures) {
		this.factoriesTestFeatures = factoriesTestFeatures;
		this.fileSystemTestFeatures = fileSystemTestFeatures;
	}

	public void saveCurrentStateTo(File file)
			throws Exception {

		File tempFolder = fileSystemTestFeatures.newTempFolder();
		File tempContentFolder = new File(tempFolder, "content");
		File tempSettingsFolder = new File(tempFolder, "settings");

		DataLayerFactory dataLayerFactory = factoriesTestFeatures.newDaosFactory();
		DataLayerConfiguration dataLayerConfiguration = dataLayerFactory.getDataLayerConfiguration();

		if (!dataLayerConfiguration.isSecondTransactionLogEnabled()) {
			throw new RuntimeException("Save state requires second transaction log");
		}

		dataLayerFactory.getSecondTransactionLogManager().regroupAndMoveInVault();
		File settingsFolder = dataLayerConfiguration.getSettingsFileSystemBaseFolder();
		File contentFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();

		FileUtils.copyDirectory(settingsFolder, tempSettingsFolder);
		FileUtils.copyDirectory(contentFolder, tempContentFolder);

		List<File> files = asList(tempSettingsFolder, tempContentFolder);

		dataLayerFactory.getIOServicesFactory().newZipService().zip(file, files);

	}

	public void loadStateFrom(File file)
			throws Exception {

		File tempUnzipFolder = fileSystemTestFeatures.newTempFolder();

		DataLayerFactory dataLayerFactory = factoriesTestFeatures.newDaosFactory();
		DataLayerConfiguration dataLayerConfiguration = dataLayerFactory.getDataLayerConfiguration();
		dataLayerFactory.getIOServicesFactory().newZipService().unzip(file, tempUnzipFolder);

		File tempUnzipContentFolder = new File(tempUnzipFolder, "content");
		File tempUnzipSettingsFolder = new File(tempUnzipFolder, "settings");
		File settingsFolder = dataLayerConfiguration.getSettingsFileSystemBaseFolder();
		File contentFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();

		FileUtils.copyDirectory(tempUnzipSettingsFolder, settingsFolder);
		FileUtils.copyDirectory(tempUnzipContentFolder, contentFolder);

		dataLayerFactory.getSecondTransactionLogManager().destroyAndRebuildSolrCollection();
	}

	public static void loadStateFrom(File file, File tempFolder, File settingsFolder, File contentFolder, boolean resetPasswords)
			throws Exception {

		File tempUnzipFolder = new File(tempFolder, "tempUnzipFolder");
		tempUnzipFolder.mkdirs();

		IOServices ioServices = new IOServices(tempFolder);
		new ZipService(ioServices).unzip(file, tempUnzipFolder);

		File tempUnzipContentFolder = new File(tempUnzipFolder, "content");
		File tempUnzipSettingsFolder = new File(tempUnzipFolder, "settings");

		if (resetPasswords) {
			File authenticationFile = new File(tempUnzipSettingsFolder, "authentification.properties");
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

		FileUtils.copyDirectory(tempUnzipSettingsFolder, settingsFolder);
		FileUtils.copyDirectory(tempUnzipContentFolder, contentFolder);

	}

	public void saveStateAfterTestWithTitle(String title) {
		this.afterTestSaveTitle = title;
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
		ConstellioModulesManager modulesManager = factoriesTestFeatures.newAppServicesFactory().getModulesManager();
		String version = factoriesTestFeatures.newAppServicesFactory().newMigrationServices().getCurrentVersion("zeCollection");

		List<String> moduleIds = new ArrayList<>();
		for (Module module : modulesManager.getInstalledModules()) {
			moduleIds.add(module.getId());
		}
		String modulesStr = StringUtils.join(moduleIds, ",");

		String name = "given_system_in_" + version + "_with_" + modulesStr + "_module";
		if (modulesStr.contains(",")) {
			name += "s";
		}

		if (title != null) {
			name = name + "__" + title;
		}

		File file = new File(new SDKFoldersLocator().getInitialStatesFolder(), name + ".zip");
		if (file.exists()) {
			file.delete();
		}
		saveCurrentStateTo(file);

	}
}
