package com.constellio.sdk.tests;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactoriesDecorator;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;

public class TestConstellioFactoriesDecorator extends ConstellioFactoriesDecorator {

	boolean backgroundThreadsEnabled;
	String systemLanguage;
	File setupProperties;
	File importationFolder;
	File configManagerFolder;
	File appTempFolder;
	File contentFolder;
	File pluginsFolder;
	List<DataLayerConfigurationAlteration> dataLayerConfigurationAlterations = new ArrayList<>();
	List<ModelLayerConfigurationAlteration> modelLayerConfigurationAlterations = new ArrayList<>();
	List<AppLayerConfigurationAlteration> appLayerConfigurationAlterations = new ArrayList<>();

	public TestConstellioFactoriesDecorator(boolean backgroundThreadsEnabled) {
		this.backgroundThreadsEnabled = backgroundThreadsEnabled;
	}

	@Override
	public DataLayerConfiguration decorateDataLayerConfiguration(DataLayerConfiguration dataLayerConfiguration) {
		DataLayerConfiguration spiedDataLayerConfiguration = spy(dataLayerConfiguration);
		doReturn(configManagerFolder).when(spiedDataLayerConfiguration).getSettingsFileSystemBaseFolder();
		doReturn(appTempFolder).when(spiedDataLayerConfiguration).getTempFolder();
		doReturn(contentFolder).when(spiedDataLayerConfiguration).getContentDaoFileSystemFolder();
		doReturn(backgroundThreadsEnabled).when(spiedDataLayerConfiguration).isBackgroundThreadsEnabled();

		for (DataLayerConfigurationAlteration alteration : dataLayerConfigurationAlterations) {
			alteration.alter(spiedDataLayerConfiguration);
		}

		return spiedDataLayerConfiguration;
	}

	@Override
	public ModelLayerConfiguration decorateModelLayerConfiguration(ModelLayerConfiguration modelLayerConfiguration) {
		File key = new File(configManagerFolder, "key.txt");
		ModelLayerConfiguration spiedModelLayerConfiguration = spy(modelLayerConfiguration);
		doReturn(key).when(spiedModelLayerConfiguration).getConstellioEncryptionFile();
		doReturn(importationFolder).when(spiedModelLayerConfiguration).getImportationFolder();
		if (systemLanguage != null) {
			doReturn(systemLanguage).when(spiedModelLayerConfiguration).getMainDataLanguage();
		} else {
			doReturn("fr").when(spiedModelLayerConfiguration).getMainDataLanguage();
		}

		for (ModelLayerConfigurationAlteration alteration : modelLayerConfigurationAlterations) {
			alteration.alter(spiedModelLayerConfiguration);
		}

		return spiedModelLayerConfiguration;
	}

	@Override
	public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {

		AppLayerConfiguration spiedAppLayerConfiguration = spy(appLayerConfiguration);

		doReturn(setupProperties).when(spiedAppLayerConfiguration).getSetupProperties();
		doReturn(pluginsFolder).when(spiedAppLayerConfiguration).getPluginsFolder();

		for (AppLayerConfigurationAlteration alteration : appLayerConfigurationAlterations) {
			alteration.alter(spiedAppLayerConfiguration);
		}

		return spiedAppLayerConfiguration;
	}

	@Override
	public AppLayerFactory decorateAppServicesFactory(AppLayerFactory appLayerFactory) {
		AppLayerFactory spiedAppLayerFactory = spy(appLayerFactory);

		ConstellioPluginManager pluginManager = mock(ConstellioPluginManager.class, "pluginManager");
		when(spiedAppLayerFactory.getPluginManager()).thenReturn(pluginManager);

		return spiedAppLayerFactory;
	}

	@Override
	public FoldersLocator decorateFoldersLocator(FoldersLocator foldersLocator) {
		FoldersLocator spiedFoldersLocator = spy(foldersLocator);
		doReturn(appTempFolder).when(spiedFoldersLocator).getDefaultTempFolder();
		return spiedFoldersLocator;
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
}