package com.constellio.sdk.tests;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;

public class ModuleEnabler {

	InstallableModule module;
	CollectionsListManager collectionsListManager;
	ConstellioModulesManager constellioModulesManager;

	public ModuleEnabler(InstallableModule module,
			CollectionsListManager collectionsListManager, ConstellioModulesManager constellioModulesManager) {
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

		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, module);

	}

	public static ModuleEnabler givenInstalledModule(AppLayerFactory appLayerFactory,
			Class<? extends InstallableModule> moduleClass) {
		ConstellioModulesManager constellioModulesManager = appLayerFactory.getModulesManager();
		CollectionsListManager collectionsListManager = appLayerFactory.getModelLayerFactory().getCollectionsListManager();
		InstallableModule module;
		try {
			module = moduleClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		if (!appLayerFactory.getPluginManager().isRegistered(module.getId())) {
			appLayerFactory.getPluginManager().registerModule(module);
		}
		constellioModulesManager.installValidModuleAndGetInvalidOnes(module, collectionsListManager);

		return new ModuleEnabler(module, collectionsListManager, constellioModulesManager);
	}
}
