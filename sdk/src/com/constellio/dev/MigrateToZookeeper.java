package com.constellio.dev;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ZooKeeperConfigManager;
import com.constellio.model.conf.FoldersLocator;

import java.io.File;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;

public class MigrateToZookeeper {

	static AppLayerFactory appLayerFactory;

	private static void startBackend() {
		//Only enable this line to run in production
		appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

		//Only enable this line to run on developer workstation
		//appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();
	}

	public static void main(String argv[])
			throws Exception {
		if (argv.length != 1) {
			System.out.println("Usage: MigrateToZookeeper <zkAddress>");
		}
		startBackend();
		File settingsFolder = new FoldersLocator().getDefaultSettingsFolder();
		System.out.println("Connecting to : " + argv[0]);
		ZooKeeperConfigManager configManager = new ZooKeeperConfigManager(argv[0], "/", appLayerFactory.getModelLayerFactory().getDataLayerFactory().getIOServicesFactory().newIOServices());
		configManager.importFrom(settingsFolder);

		System.out.println("Import to Zookeeper completed");
	}
}
