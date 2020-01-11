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
import com.constellio.app.services.systemSetup.SystemLocalConfigsManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.containers.ContainerButtonListener;
import com.constellio.app.ui.pages.base.EnterViewListener;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.factories.LayerFactory;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.List;

public interface AppLayerFactory extends LayerFactory {

	void registerSystemWideManager(String module, String id, StatefulService manager);

	void registerManager(String collection, String module, String id, StatefulService manager);

	<T> T getRegisteredManager(String collection, String module, String id);

	<T> T getSystemWideRegisteredManager(String module, String id);

	AppLayerExtensions getExtensions();

	AppManagementService newApplicationService();

	UpgradeAppRecoveryService newUpgradeAppRecoveryService();

	SystemCheckManager getSystemCheckManager();

	@Override
	void initialize();

	void postInitialization();

	void restart()
			throws AppManagementServiceException;

	@Override
	void close();

	ConstellioPluginManager getPluginManager();

	ModelLayerFactory getModelLayerFactory();

	NavigatorConfigurationService getNavigatorConfigurationService();

	PresenterService newPresenterService();

	CmisCacheManager getConstellioCmisRepositoriesManager();

	List<EnterViewListener> getEnterViewListeners();

	List<InitUIListener> getInitUIListeners();

	List<ContainerButtonListener> getContainerButtonListeners();

	SchemasDisplayManager getMetadataSchemasDisplayManager();

	SystemGlobalConfigsManager getSystemGlobalConfigsManager();

	SystemLocalConfigsManager getSystemLocalConfigsManager();

	CollectionsManager getCollectionsManager();

	MigrationServices newMigrationServices();

	ConstellioModulesManager getModulesManager();

	LabelTemplateManager getLabelTemplateManager();

	AppLayerConfiguration getAppLayerConfiguration();

	AppSchemasServices newSchemasServices();

	CorrectorExcluderManager getCorrectorExcluderManager();

	boolean isInitializationFinished();
}
