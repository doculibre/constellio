package com.constellio.sdk.tests;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.conf.PropertiesAppLayerConfiguration;
import com.constellio.app.conf.PropertiesAppLayerConfiguration.InMemoryAppLayerConfiguration;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactoriesDecorator;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.data.conf.DigitSeparatorMode.THREE_LEVELS_OF_ONE_DIGITS;
import static com.constellio.data.conf.HashingEncoding.BASE32;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class TestConstellioFactoriesDecorator extends ConstellioFactoriesDecorator {

	File importedSettings;
	boolean backgroundThreadsEnabled;
	boolean mockPluginManager;
	String systemLanguage;
	File setupProperties;
	File importationFolder;
	File configManagerFolder;
	File appTempFolder;
	File contentFolder;
	File pluginsFolder, pluginsToMoveOnStartup;
	List<DataLayerConfigurationAlteration> dataLayerConfigurationAlterations = new ArrayList<>();
	List<ModelLayerConfigurationAlteration> modelLayerConfigurationAlterations = new ArrayList<>();
	List<AppLayerConfigurationAlteration> appLayerConfigurationAlterations = new ArrayList<>();
	final private boolean checkRollback;
	private File transactionLogWorkFolder;

	public TestConstellioFactoriesDecorator(boolean backgroundThreadsEnabled, boolean mockPluginManager,
											boolean checkRollback) {
		this.backgroundThreadsEnabled = backgroundThreadsEnabled;
		this.checkRollback = checkRollback;
		this.mockPluginManager = mockPluginManager;
	}

	@Override
	public DataLayerConfiguration decorateDataLayerConfiguration(DataLayerConfiguration dataLayerConfiguration) {

		InMemoryDataLayerConfiguration inMemoryDataLayerConfiguration = new InMemoryDataLayerConfiguration(
				(PropertiesDataLayerConfiguration) dataLayerConfiguration);

		inMemoryDataLayerConfiguration.setSettingsFileSystemBaseFolder(configManagerFolder);
		inMemoryDataLayerConfiguration.setTempFolder(appTempFolder);
		inMemoryDataLayerConfiguration.setContentDaoFileSystemFolder(contentFolder);
		inMemoryDataLayerConfiguration.setBackgroundThreadsEnabled(backgroundThreadsEnabled);
		inMemoryDataLayerConfiguration.setInRollbackTestMode(checkRollback);
		inMemoryDataLayerConfiguration.setSecondTransactionLogBaseFolder(transactionLogWorkFolder);
		inMemoryDataLayerConfiguration.setContentDaoFileSystemDigitsSeparatorMode(THREE_LEVELS_OF_ONE_DIGITS);
		inMemoryDataLayerConfiguration.setHashingEncoding(BASE32);

		if (transactionLogWorkFolder != null) {
			inMemoryDataLayerConfiguration.setSecondTransactionLogEnabled(true);
		}

		for (DataLayerConfigurationAlteration alteration : dataLayerConfigurationAlterations) {
			alteration.alter(inMemoryDataLayerConfiguration);
		}

		return inMemoryDataLayerConfiguration;
	}

	@Override
	public ModelLayerConfiguration decorateModelLayerConfiguration(ModelLayerConfiguration modelLayerConfiguration) {
		File key = new File(configManagerFolder, "key.txt");

		InMemoryModelLayerConfiguration volatileModelLayerConfiguration = new InMemoryModelLayerConfiguration(
				(PropertiesModelLayerConfiguration) modelLayerConfiguration);

		volatileModelLayerConfiguration.setConstellioEncryptionFile(key);
		volatileModelLayerConfiguration.setImportationFolder(importationFolder);

		if (systemLanguage != null) {
			volatileModelLayerConfiguration.setMainDataLanguage(systemLanguage);
		} else {
			volatileModelLayerConfiguration.setMainDataLanguage("fr");
		}

		for (ModelLayerConfigurationAlteration alteration : modelLayerConfigurationAlterations) {
			alteration.alter(volatileModelLayerConfiguration);
		}

		return volatileModelLayerConfiguration;
	}

	@Override
	public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {

		InMemoryAppLayerConfiguration inMemoryAppLayerConfiguration = new InMemoryAppLayerConfiguration(
				(PropertiesAppLayerConfiguration) appLayerConfiguration);

		inMemoryAppLayerConfiguration.setSetupProperties(setupProperties);
		inMemoryAppLayerConfiguration.setPluginsFolder(pluginsFolder);
		inMemoryAppLayerConfiguration.setPluginsManagementOnStartupFile(pluginsToMoveOnStartup);
		//
		//		doReturn(setupProperties).when(spiedAppLayerConfiguration).getSetupProperties();
		//		doReturn(pluginsFolder).when(spiedAppLayerConfiguration).getPluginsFolder();
		//		doReturn(pluginsToMoveOnStartup).when(spiedAppLayerConfiguration).getPluginsManagementOnStartupFile();

		for (AppLayerConfigurationAlteration alteration : appLayerConfigurationAlterations) {
			alteration.alter(inMemoryAppLayerConfiguration);
		}

		return inMemoryAppLayerConfiguration;
	}

	@Override
	public AppLayerFactory decorateAppServicesFactory(AppLayerFactory appLayerFactory) {

		if (mockPluginManager) {
			appLayerFactory = spy(appLayerFactory);
			ConstellioPluginManager pluginManager = mock(ConstellioPluginManager.class, "pluginManager");
			doReturn(pluginManager).when(appLayerFactory).getPluginManager();
		}
		return appLayerFactory;

	}

	@Override
	public DataLayerFactory decorateDataLayerFactory(DataLayerFactory dataLayerFactory) {
		dataLayerFactory = super.decorateDataLayerFactory(dataLayerFactory);

		if (importedSettings != null) {
			dataLayerFactory.getConfigManager().importFrom(importedSettings);
		}

		return dataLayerFactory;
	}

	@Override
	public FoldersLocator decorateFoldersLocator(FoldersLocator foldersLocator) {

		return new FoldersLocator() {
			@Override
			public File getDefaultTempFolder() {
				return appTempFolder;
			}
		};

	}

	public TestConstellioFactoriesDecorator setSetupProperties(File setupProperties) {
		this.setupProperties = setupProperties;
		return this;
	}

	public TestConstellioFactoriesDecorator setImportationFolder(File importationFolder) {
		this.importationFolder = importationFolder;
		return this;
	}

	public TestConstellioFactoriesDecorator setConfigManagerFolder(File configManagerFolder) {
		this.configManagerFolder = configManagerFolder;
		return this;
	}

	public TestConstellioFactoriesDecorator setAppTempFolder(File appTempFolder) {
		this.appTempFolder = appTempFolder;
		return this;
	}

	public TestConstellioFactoriesDecorator setContentFolder(File contentFolder) {
		this.contentFolder = contentFolder;
		return this;
	}

	public TestConstellioFactoriesDecorator setPluginsFolder(File pluginsFolder) {
		this.pluginsFolder = pluginsFolder;
		return this;
	}

	public TestConstellioFactoriesDecorator setPluginsToMoveOnStartupFile(File pluginsFolder) {
		this.pluginsToMoveOnStartup = pluginsFolder;
		return this;
	}

	public TestConstellioFactoriesDecorator setDataLayerConfigurationAlterations(
			List<DataLayerConfigurationAlteration> dataLayerConfigurationAlterations) {
		this.dataLayerConfigurationAlterations = dataLayerConfigurationAlterations;
		return this;
	}

	public TestConstellioFactoriesDecorator setModelLayerConfigurationAlterations(
			List<ModelLayerConfigurationAlteration> modelLayerConfigurationAlterations) {
		this.modelLayerConfigurationAlterations = modelLayerConfigurationAlterations;
		return this;
	}

	public TestConstellioFactoriesDecorator setAppLayerConfigurationAlterations(
			List<AppLayerConfigurationAlteration> appLayerConfigurationAlterations) {
		this.appLayerConfigurationAlterations = appLayerConfigurationAlterations;
		return this;
	}

	public void setSystemLanguage(String systemLanguage) {
		this.systemLanguage = systemLanguage;
	}

	public void setTransactionLogWorkFolder(File transactionLogWorkFolder) {
		this.transactionLogWorkFolder = transactionLogWorkFolder;
	}

	public void importSettings(File importedSettings) {
		this.importedSettings = importedSettings;
	}
}