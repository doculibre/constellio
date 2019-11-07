package com.constellio.app.services.factories;

import com.constellio.app.api.cmis.binding.global.CmisCacheManager;
import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.extensions.AppLayerExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.corrector.CorrectorExcluderManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.app.services.records.SystemCheckManager;
import com.constellio.app.services.recovery.UpgradeAppRecoveryService;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.containers.ContainerButtonListener;
import com.constellio.app.ui.pages.base.EnterViewListener;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactoryImpl;
import com.constellio.model.services.factories.ModelLayerFactoryWithRequestCacheImpl;
import com.constellio.model.services.records.cache.RecordsCaches;

import java.util.List;

public class AppLayerFactoryWithRequestCacheImpl implements AppLayerFactory {

	AppLayerFactoryImpl appLayerFactory;
	RecordsCaches requestCache;
	ModelLayerFactory modelLayerFactory;
	String id;

	public AppLayerFactoryWithRequestCacheImpl(AppLayerFactoryImpl appLayerFactory, String id) {
		this.id = id;
		this.appLayerFactory = appLayerFactory;
		this.requestCache = appLayerFactory.getModelLayerFactory().getRecordsCaches();
		this.modelLayerFactory = new ModelLayerFactoryWithRequestCacheImpl(
				(ModelLayerFactoryImpl) appLayerFactory.getModelLayerFactory(), requestCache);
	}

	public void registerSystemWideManager(String module, String id, StatefulService manager) {
		appLayerFactory.registerSystemWideManager(module, id, manager);
	}

	public List<InitUIListener> getInitUIListeners() {
		return appLayerFactory.getInitUIListeners();
	}

	public <T> T getRegisteredManager(String collection, String module, String id) {
		return appLayerFactory.getRegisteredManager(collection, module, id);
	}

	public void postInitialization() {
		appLayerFactory.postInitialization();
	}

	public ConstellioPluginManager getPluginManager() {
		return appLayerFactory.getPluginManager();
	}

	public SchemasDisplayManager getMetadataSchemasDisplayManager() {
		return appLayerFactory.getMetadataSchemasDisplayManager();
	}

	public void registerManager(String collection, String module, String id, StatefulService manager) {
		appLayerFactory.registerManager(collection, module, id, manager);
	}

	public UpgradeAppRecoveryService newUpgradeAppRecoveryService() {
		return appLayerFactory.newUpgradeAppRecoveryService();
	}

	public void close(boolean closeBottomLayers) {
		appLayerFactory.close(closeBottomLayers);
	}

	public CollectionsManager getCollectionsManager() {
		return appLayerFactory.getCollectionsManager();
	}

	@Override
	public short getInstanceId() {
		return appLayerFactory.getInstanceId();
	}

	public String getInstanceName() {
		return appLayerFactory.getInstanceName();
	}

	public String toResourceName(String name) {
		return appLayerFactory.toResourceName(name);
	}

	public PresenterService newPresenterService() {
		return new PresenterService(getModelLayerFactory());
	}

	public AppLayerConfiguration getAppLayerConfiguration() {
		return appLayerFactory.getAppLayerConfiguration();
	}

	public <T> T getSystemWideRegisteredManager(String module, String id) {
		return appLayerFactory.getSystemWideRegisteredManager(module, id);
	}

	public CmisCacheManager getConstellioCmisRepositoriesManager() {
		return appLayerFactory.getConstellioCmisRepositoriesManager();
	}

	public List<EnterViewListener> getEnterViewListeners() {
		return appLayerFactory.getEnterViewListeners();
	}

	public SystemGlobalConfigsManager getSystemGlobalConfigsManager() {
		return appLayerFactory.getSystemGlobalConfigsManager();
	}

	public ConstellioModulesManager getModulesManager() {
		return appLayerFactory.getModulesManager();
	}

	public void restart()
			throws AppManagementServiceException {
		appLayerFactory.restart();
	}

	public AppLayerExtensions getExtensions() {
		return appLayerFactory.getExtensions();
	}

	public MigrationServices newMigrationServices() {
		return appLayerFactory.newMigrationServices();
	}

	public <T extends StatefulService> T add(T statefulService) {
		return appLayerFactory.add(statefulService);
	}

	public AppSchemasServices newSchemasServices() {
		return appLayerFactory.newSchemasServices();
	}

	@Override
	public CorrectorExcluderManager getCorrectorExcluderManager() {
		return appLayerFactory.getCorrectorExcluderManager();
	}

	@Override
	public boolean isInitializationFinished() {
		return appLayerFactory.isInitializationFinished();
	}

	@Override
	public void initialize() {
		appLayerFactory.initialize();
	}

	public List<ContainerButtonListener> getContainerButtonListeners() {
		return appLayerFactory.getContainerButtonListeners();
	}

	public SystemCheckManager getSystemCheckManager() {
		return appLayerFactory.getSystemCheckManager();
	}

	@Override
	public void close() {
		appLayerFactory.close();
	}

	public AppManagementService newApplicationService() {
		return appLayerFactory.newApplicationService();
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public NavigatorConfigurationService getNavigatorConfigurationService() {
		return appLayerFactory.getNavigatorConfigurationService();
	}

	public LabelTemplateManager getLabelTemplateManager() {
		return appLayerFactory.getLabelTemplateManager();
	}

	//	@Override
	//	public String toString() {
	//		return id;
	//	}

	public void disconnect() {
		//requestCache.disconnect();
	}

}
