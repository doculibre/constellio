package com.constellio.sdk.tests;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.extensions.ConstellioModulesManagerException.ConstellioModulesManagerException_ModuleInstallationFailed;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ModuleEnabler {

	private static Map<String, File> pluginsBundlesCache = new HashMap<>();

	InstallableModule module;
	CollectionsListManager collectionsListManager;
	ConstellioModulesManager constellioModulesManager;

	public ModuleEnabler(InstallableModule module,
						 CollectionsListManager collectionsListManager,
						 ConstellioModulesManager constellioModulesManager) {
		this.module = module;
		this.collectionsListManager = collectionsListManager;
		this.constellioModulesManager = constellioModulesManager;
	}

	public void enabledInEveryCollections() {
		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			enabledIn(collection);
		}
	}

	public void enabledIn(String collection) {
		if (!module.isComplementary()) {
			try {
				constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, module);
			} catch (ConstellioModulesManagerException_ModuleInstallationFailed constellioModulesManagerException_moduleInstallationFailed) {
				throw new RuntimeException(constellioModulesManagerException_moduleInstallationFailed);
			}
		}

	}

	public static ModuleEnabler givenInstalledModule(AppLayerFactory appLayerFactory,
													 Class<? extends InstallableModule> installableModuleClass) {
		ConstellioModulesManager constellioModulesManager = appLayerFactory.getModulesManager();
		CollectionsListManager collectionsListManager = appLayerFactory.getModelLayerFactory().getCollectionsListManager();
		InstallableModule module;
		try {
			module = installableModuleClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		(appLayerFactory.getPluginManager()).registerPluginOnlyForTests(module);

		//		if (!getAppLayerFactory().getPluginManager().isRegistered(module.getId())) {
		//			getAppLayerFactory().getPluginManager().registerPluginOnlyForTests(module);
		//		}

		//		FoldersLocator foldersLocator = new FoldersLocator();
		//		File constellioPlugins = foldersLocator.getPluginsRepository();
		//		String moduleId = module.getId();
		//		String bundleName = moduleId + "_i18n";
		//
		//		File value = pluginsBundlesCache.get(bundleName);
		//		if (value == null && constellioPlugins.exists() && constellioPlugins.listFiles() != null) {
		//			for (File subFolder : constellioPlugins.listFiles()) {
		//				if (subFolder.getName().startsWith("plugin")) {
		//					File resourcesFolder = new File(subFolder, "resources");
		//					if (resourcesFolder.exists()) {
		//						File i18nfolder = new File(resourcesFolder, "grics" + File.separator + "i18n");
		//
		//						if (new File(i18nfolder, bundleName + ".properties").exists()) {
		//							value = i18nfolder;
		//							break;
		//						}
		//					}
		//				}
		//			}
		//		}
		//		if (value != null) {
		//			i18n.registerBundle(value, bundleName);
		//		}

		try {
			constellioModulesManager.installValidModuleAndGetInvalidOnes(module, collectionsListManager);


			if (module.isComplementary()) {
				((ConstellioModulesManagerImpl) constellioModulesManager).enableComplementaryModules();
			}
		} catch (ConstellioModulesManagerException_ModuleInstallationFailed constellioModulesManagerException_moduleInstallationFailed) {
			throw new RuntimeException(constellioModulesManagerException_moduleInstallationFailed);
		}

		return new ModuleEnabler(module, collectionsListManager, constellioModulesManager);
	}
}
