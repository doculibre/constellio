package com.constellio.app.start;

import static com.constellio.app.services.appManagement.AppManagementService.RESTART_COMMAND;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.extensions.plugins.utils.PluginManagementUtils;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.appManagement.InstallationService;

public final class MainConstellio {

	private MainConstellio() {

	}

	public static void main(String[] args)
			throws IOException, InterruptedException, ZipServiceException {
		File constellioInstallationDir = new FoldersLocator().getWrapperInstallationFolder();

		FileService fileService = new FileService(null);

		try {
			fileService.ensureWritePermissions(constellioInstallationDir);
		} catch (IOException e) {
			throw new MainConstellioRuntimeException("No write permissions in installation dir '" + constellioInstallationDir
					+ "'", e);
		}

		if (isInInstallationMode()) {
			installApplication(constellioInstallationDir);
		} else {
			FoldersLocator folderLocator = new FoldersLocator();
			PluginManagementUtils utils = new PluginManagementUtils(folderLocator.getPluginsJarsFolder(),
					folderLocator.getLibFolder(), folderLocator.getPluginsToMoveOnStartupFile());
			Set<String> pluginsToMove = utils.getPluginsToMove();
			if (pluginsToMove.isEmpty()) {
				runApplication();
			} else {
				utils.
						movePluginsAndSetNoPluginToMove(pluginsToMove);
				restartApplication();
			}
		}
	}


	private static void restartApplication() {
		File commandFile = new FoldersLocator().getWrapperCommandFile();
		try {
			FileUtils.writeStringToFile(commandFile, RESTART_COMMAND, false);
		} catch (IOException e) {
			throw new MainConstellioRuntimeException("Cannot write in command file " + commandFile.getAbsolutePath(), e);
		}
	}

	private static boolean isInInstallationMode() {
		File configFile = new FoldersLocator().getWrapperConf();
		return configFile.exists() && configFile.length() == 0;
	}

	private static void installApplication(File constellioInstallationDir)
			throws IOException, InterruptedException, ZipServiceException {

		InstallationService installationService = new InstallationService(constellioInstallationDir);
		installationService.launchInstallation();
	}

	private static void runApplication()
			throws IOException {

		Map<String, String> properties = readProperties();

		ApplicationStarterParams params = new ApplicationStarterParams();
		params.setJoinServerThread(true);
		params.setWebContentDir(new FoldersLocator().getConstellioWebappFolder());

		String keyStorePassword = properties.get("server.keystorePassword");
		if (StringUtils.isNotBlank(keyStorePassword)) {
			params.setSSLWithKeystorePassword(keyStorePassword);
		}

		String serverPortConfig = properties.get("server.port");
		params.setPort(StringUtils.isNotBlank(serverPortConfig) ? Integer.valueOf(serverPortConfig) : 8080);

		ApplicationStarter.startApplication(params);
	}

	private static Map<String, String> readProperties() {
		return PropertyFileUtils.loadKeyValues(new FoldersLocator().getConstellioProperties());
	}

}