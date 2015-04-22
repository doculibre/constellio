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
package com.constellio.sdk.tests;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactoriesDecorator;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;

public class TestConstellioFactoriesDecorator extends ConstellioFactoriesDecorator {

	File setupProperties;
	File importationFolder;
	File configManagerFolder;
	File appTempFolder;
	File contentFolder;
	List<DataLayerConfigurationAlteration> dataLayerConfigurationAlterations = new ArrayList<>();
	List<ModelLayerConfigurationAlteration> modelLayerConfigurationAlterations = new ArrayList<>();
	List<AppLayerConfigurationAlteration> appLayerConfigurationAlterations = new ArrayList<>();

	@Override
	public DataLayerConfiguration decorateDataLayerConfiguration(DataLayerConfiguration dataLayerConfiguration) {
		DataLayerConfiguration spiedDataLayerConfiguration = spy(dataLayerConfiguration);
		doReturn(configManagerFolder).when(spiedDataLayerConfiguration).getSettingsFileSystemBaseFolder();
		doReturn(appTempFolder).when(spiedDataLayerConfiguration).getTempFolder();
		doReturn(contentFolder).when(spiedDataLayerConfiguration).getContentDaoFileSystemFolder();

		for (DataLayerConfigurationAlteration alteration : dataLayerConfigurationAlterations) {
			alteration.alter(spiedDataLayerConfiguration);
		}

		return spiedDataLayerConfiguration;
	}

	@Override
	public ModelLayerConfiguration decorateModelLayerConfiguration(ModelLayerConfiguration modelLayerConfiguration) {
		ModelLayerConfiguration spiedModelLayerConfiguration = spy(modelLayerConfiguration);
		doReturn(importationFolder).when(spiedModelLayerConfiguration).getImportationFolder();

		for (ModelLayerConfigurationAlteration alteration : modelLayerConfigurationAlterations) {
			alteration.alter(spiedModelLayerConfiguration);
		}

		return spiedModelLayerConfiguration;
	}

	@Override
	public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {

		AppLayerConfiguration spiedAppLayerConfiguration = spy(appLayerConfiguration);

		doReturn(setupProperties).when(spiedAppLayerConfiguration).getSetupProperties();

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
}