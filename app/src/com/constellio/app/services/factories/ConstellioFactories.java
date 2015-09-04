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

import java.io.File;
import java.util.Map;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.conf.PropertiesAppLayerConfiguration;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfiguration;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConstellioFactories {

	private static ConstellioFactories instance;

	private ConstellioFactoriesDecorator decorator;
	private DataLayerConfiguration dataLayerConfiguration;
	private ModelLayerConfiguration modelLayerConfiguration;
	private AppLayerConfiguration appLayerConfiguration;

	private FoldersLocator foldersLocator;

	private IOServicesFactory ioServicesFactory;

	private DataLayerFactory dataLayerFactory;

	private ModelLayerFactory modelLayerFactory;

	private AppLayerFactory appLayerFactory;

	private ConstellioFactories() {

	}

	public static synchronized ConstellioFactories getInstance() {
		File propertyFile = new FoldersLocator().getConstellioProperties();
		ConstellioFactoriesDecorator constellioFactoriesDecorator = new ConstellioFactoriesDecorator();
		return getInstance(propertyFile, constellioFactoriesDecorator);
	}

	public static synchronized ConstellioFactories getInstance(File propertyFile, ConstellioFactoriesDecorator decorator) {
		if (instance == null) {
			instance = buildFor(propertyFile, decorator);
			start();
		}

		return instance;
	}

	public static void start() {
		instance.appLayerFactory.initialize();
	}

	public static void clear() {
		if (instance != null) {
			instance.appLayerFactory.close();
			instance = null;
		}
	}

	private static ConstellioFactories buildFor(File propertyFile, ConstellioFactoriesDecorator decorator) {
		ConstellioFactories factories = new ConstellioFactories();

		Map<String, String> configs = PropertyFileUtils.loadKeyValues(propertyFile);

		factories.decorator = decorator;
		factories.foldersLocator = decorator.decorateFoldersLocator(new FoldersLocator());
		factories.buildConfiguration(propertyFile, configs);
		factories.buildLayers();
		return factories;
	}

	private void buildLayers() {
		Delayed<ConstellioModulesManager> modulesManager = new Delayed<>();
		ioServicesFactory = new IOServicesFactory(dataLayerConfiguration.getTempFolder());

		dataLayerFactory = decorator.decorateDataLayerFactory(
				new DataLayerFactory(ioServicesFactory, dataLayerConfiguration, decorator.getStatefullServiceDecorator()));

		modelLayerFactory = decorator
				.decorateModelServicesFactory(new ModelLayerFactory(dataLayerFactory, foldersLocator, modelLayerConfiguration,
						decorator.getStatefullServiceDecorator(), modulesManager));

		appLayerFactory = decorator
				.decorateAppServicesFactory(new AppLayerFactory(appLayerConfiguration, modelLayerFactory, dataLayerFactory,
						decorator.getStatefullServiceDecorator()));

		modulesManager.set(appLayerFactory.getModulesManager());

	}

	private void buildConfiguration(File propertyFile, Map<String, String> configs) {
		File defaultTempFolder = foldersLocator.getDefaultTempFolder();
		File defaultFileSystemBaseFolder = new File(foldersLocator.getConfFolder(), "settings");

		this.dataLayerConfiguration = decorator
				.decorateDataLayerConfiguration(new PropertiesDataLayerConfiguration(configs, defaultTempFolder,
						defaultFileSystemBaseFolder, propertyFile));
		this.modelLayerConfiguration = decorator
				.decorateModelLayerConfiguration(
						new PropertiesModelLayerConfiguration(configs, dataLayerConfiguration, foldersLocator, propertyFile));
		this.appLayerConfiguration = decorator
				.decorateAppLayerConfiguration(
						new PropertiesAppLayerConfiguration(configs, modelLayerConfiguration, foldersLocator, propertyFile));
	}

	public IOServicesFactory getIoServicesFactory() {
		return ioServicesFactory;
	}

	public DataLayerFactory getDataLayerFactory() {
		return dataLayerFactory;
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public DataLayerConfiguration getDataLayerConfiguration() {
		return dataLayerConfiguration;
	}

	public ModelLayerConfiguration getModelLayerConfiguration() {
		return modelLayerConfiguration;
	}

	public AppLayerConfiguration getAppLayerConfiguration() {
		return appLayerConfiguration;
	}

	public FoldersLocator getFoldersLocator() {
		return foldersLocator;
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}
}
