package com.constellio.app.start;

import com.constellio.app.services.extensions.plugins.utils.PluginManagementUtils;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.appManagement.InstallationService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.services.appManagement.AppManagementService.RESTART_COMMAND;

public final class MainConstellio {
	private static final Logger LOGGER = LoggerFactory.getLogger(MainConstellio.class);

	private MainConstellio() {

	}

	public static void main(String[] args)
			throws IOException, InterruptedException, ZipServiceException {
		changeTemporaryDirectory();
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
			PluginManagementUtils utils = new PluginManagementUtils(folderLocator);
			Set<String> pluginsToMove = utils.getPluginsToMove();
			if (pluginsToMove.isEmpty()) {
				Map<String, String> configs = PropertyFileUtils.loadKeyValues(folderLocator.getConstellioProperties());
				boolean initOnStartup = true;
				if ("false".equals(configs.get("init.startup"))) {
					initOnStartup = false;
				}
				runApplication(initOnStartup);
			} else {
				utils.movePlugins(pluginsToMove);
				ensureApplicationWillRestartInCorrectState(utils);
				LOGGER.info("Restarting app after plugins copy");
				restartApplication();
			}
		}
	}

	private static void changeTemporaryDirectory() {
		FoldersLocator foldersLocator = new FoldersLocator();
		File tempFile = foldersLocator.getDefaultTempFolder();
		try {
			FileUtils.deleteDirectory(tempFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tempFile.mkdirs();
		System.setProperty("java.io.tmpdir", tempFile.getAbsolutePath());
	}

	private static void ensureApplicationWillRestartInCorrectState(PluginManagementUtils utils)
			throws IOException {
		Set<String> pluginsToMove = utils.getPluginsToMove();
		if (!pluginsToMove.isEmpty()) {
			throw new RuntimeException(
					"Avoiding application restart in an invalid state, please check that plugins are in lib and that "
					+ "plugins management file is empty: " + new FoldersLocator().getPluginsToMoveOnStartupFile());
		}
	}

	private static void restartApplication()
			throws InterruptedException {
		File commandFile = new FoldersLocator().getWrapperCommandFile();
		try {
			FileUtils.writeStringToFile(commandFile, RESTART_COMMAND, false);
			waitForWrapperRestart();
		} catch (IOException e) {
			throw new MainConstellioRuntimeException("Cannot write in command file " + commandFile.getAbsolutePath(), e);
		}
	}

	private static void waitForWrapperRestart()
			throws InterruptedException {
		//FIXME waiting wrapper to restart
		for (int i = 1; i < 100; i++) {
			Thread.sleep(60000);
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

	private static void runApplication(boolean initOnStartup)
			throws IOException {
		//		if (initOnStartup) {
		//			ConstellioFactories.getInstance();
		//		}

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

		Boolean isForceSecuredCookies = new Boolean(properties.get("server.forceSecuredCookies"));
		params.setForceSecuredCookies(isForceSecuredCookies);

		ApplicationStarter.startApplication(params);
	}

	private static Map<String, String> readProperties() {
		return PropertyFileUtils.loadKeyValues(new FoldersLocator().getConstellioProperties());
	}

}