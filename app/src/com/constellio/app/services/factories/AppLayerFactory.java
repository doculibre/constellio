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
package com.constellio.app.services.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.constellio.app.api.cmis.binding.global.CmisCacheManager;
import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.extensions.AppLayerExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.app.services.extensions.JSPFConstellioPluginManager;
import com.constellio.app.services.migrations.ConstellioEIM;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.app.services.systemSetup.SystemSetupService;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.containers.ContainerButtonListener;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.EnterViewListener;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.factories.LayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexationMode;

public class AppLayerFactory extends LayerFactory {

	private FoldersLocator foldersLocator;

	private ConstellioPluginManager pluginManager;

	private ConstellioModulesManagerImpl modulesManager;

	private ModelLayerFactory modelLayerFactory;

	private DataLayerFactory dataLayerFactory;

	private CmisCacheManager cmisRepositoriesManager;

	private List<EnterViewListener> enterViewListeners;

	private List<InitUIListener> initUIListeners;

	private SystemGlobalConfigsManager systemGlobalConfigsManager;

	private List<ContainerButtonListener> containerButtonListeners;
	private SchemasDisplayManager metadataSchemasDisplayManager;

	private final AppLayerExtensions appLayerExtensions;

	private final CollectionsManager collectionsManager;

	private final LabelTemplateManager labelTemplateManager;

	public AppLayerFactory(AppLayerConfiguration appLayerConfiguration, ModelLayerFactory modelLayerFactory,
			DataLayerFactory dataLayerFactory, StatefullServiceDecorator statefullServiceDecorator) {
		super(modelLayerFactory, statefullServiceDecorator);

		this.appLayerExtensions = new AppLayerExtensions();
		this.modelLayerFactory = modelLayerFactory;
		this.dataLayerFactory = dataLayerFactory;
		this.enterViewListeners = new ArrayList<>();
		this.initUIListeners = new ArrayList<>();
		this.containerButtonListeners = new ArrayList<>();
		this.foldersLocator = new FoldersLocator();
		this.setDefaultLocale();
		this.metadataSchemasDisplayManager = add(new SchemasDisplayManager(dataLayerFactory.getConfigManager(),
				modelLayerFactory.getCollectionsListManager(), modelLayerFactory.getMetadataSchemasManager()));

		pluginManager = add(new JSPFConstellioPluginManager(appLayerConfiguration.getPluginsFolder(), modelLayerFactory,
				dataLayerFactory));

		pluginManager.register(InstallableModule.class, new ConstellioRMModule());

		Delayed<MigrationServices> migrationServicesDelayed = new Delayed<>();
		this.modulesManager = add(new ConstellioModulesManagerImpl(this, pluginManager, migrationServicesDelayed));

		SystemSetupService systemSetupService = new SystemSetupService(this, appLayerConfiguration);
		this.systemGlobalConfigsManager = add(
				new SystemGlobalConfigsManager(modelLayerFactory.getDataLayerFactory().getConfigManager(), systemSetupService));
		String mainDataLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
		this.collectionsManager = add(
				new CollectionsManager(modelLayerFactory, modulesManager, migrationServicesDelayed, mainDataLanguage));
		migrationServicesDelayed.set(newMigrationServices());
		try {
			newMigrationServices().migrate(null);
		} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
			throw new RuntimeException(optimisticLockingConfiguration);
		}
		labelTemplateManager = new LabelTemplateManager(dataLayerFactory.getConfigManager());

		dataLayerFactory.getBackgroundThreadsManager().onSystemStarted();
	}

	private void setDefaultLocale() {
		Locale locale;
		String mainDataLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
		if ("en".equals(mainDataLanguage)) {
			locale = Locale.ENGLISH;
		} else if ("fr".equals(mainDataLanguage)) {
			locale = Locale.FRENCH;
		} else {
			throw new ImpossibleRuntimeException("Invalid language " + mainDataLanguage);
		}
		i18n.setLocale(locale);

	}

	public AppLayerExtensions getExtensions() {
		return appLayerExtensions;
	}

	public AppManagementService newApplicationService() {
		IOServicesFactory ioServicesFactory = dataLayerFactory.getIOServicesFactory();
		return new AppManagementService(ioServicesFactory, foldersLocator, systemGlobalConfigsManager);
	}

	@Override
	public void initialize() {

		this.pluginManager.detectPlugins();
		super.initialize();

		String warVersion = newApplicationService().getWarVersion();
		if (warVersion != null && !"5.0.0".equals(warVersion)) {
			System.out.println("----------- STARTING APPLICATION IN VERSION " + warVersion + " -----------");
		}

		try {
			newMigrationServices().migrate(null);
		} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
			throw new RuntimeException(optimisticLockingConfiguration);
		}

		if (systemGlobalConfigsManager.isMarkedForReindexing()) {
			modelLayerFactory.newReindexingServices().reindexCollections(ReindexationMode.REWRITE);
			systemGlobalConfigsManager.setMarkedForReindexing(false);
		}

	}

	@Override
	public void close() {
		super.close();
	}

	public ConstellioPluginManager getPluginManager() {
		return pluginManager;
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public NavigatorConfigurationService getNavigatorConfigurationService() {
		return new NavigatorConfigurationService();
	}

	public PresenterService newPresenterService() {
		return new PresenterService(modelLayerFactory);
	}

	public CmisCacheManager getConstellioCmisRepositoriesManager() {
		if (cmisRepositoriesManager == null) {
			cmisRepositoriesManager = add(new CmisCacheManager(this));
			cmisRepositoriesManager.initialize();
		}
		return cmisRepositoriesManager;
	}

	public List<EnterViewListener> getEnterViewListeners() {
		return enterViewListeners;
	}

	public List<InitUIListener> getInitUIListeners() {
		return initUIListeners;
	}

	public List<ContainerButtonListener> getContainerButtonListeners() {
		return containerButtonListeners;
	}

	public SchemasDisplayManager getMetadataSchemasDisplayManager() {
		return metadataSchemasDisplayManager;
	}

	public SystemGlobalConfigsManager getSystemGlobalConfigsManager() {
		return this.systemGlobalConfigsManager;
	}

	public CollectionsManager getCollectionsManager() {
		return collectionsManager;
	}

	public MigrationServices newMigrationServices() {
		return new MigrationServices(new ConstellioEIM(), this, modulesManager, pluginManager);
	}

	public ConstellioModulesManager getModulesManager() {
		return modulesManager;
	}

	public LabelTemplateManager getLabelTemplateManager() {
		return labelTemplateManager;
	}
}
