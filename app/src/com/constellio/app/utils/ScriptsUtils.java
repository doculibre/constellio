package com.constellio.app.utils;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.factories.ConstellioFactoriesDecorator;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.model.conf.ModelLayerConfiguration;

public class ScriptsUtils {

	public static AppLayerFactory startLayerFactoriesWithoutBackgroundThreads() {
		ConstellioFactoriesDecorator constellioFactoriesDecorator = new ConstellioFactoriesDecorator() {
			@Override
			public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {
				return super.decorateAppLayerConfiguration(appLayerConfiguration);
			}

			@Override
			public ModelLayerConfiguration decorateModelLayerConfiguration(ModelLayerConfiguration modelLayerConfiguration) {
				modelLayerConfiguration.setBatchProcessesEnabled(false);
				return super.decorateModelLayerConfiguration(modelLayerConfiguration);
			}

			@Override
			public DataLayerConfiguration decorateDataLayerConfiguration(DataLayerConfiguration dataLayerConfiguration) {
				dataLayerConfiguration.setBackgroundThreadsEnabled(false);
				return super.decorateDataLayerConfiguration(dataLayerConfiguration);
			}
		};

		return ConstellioFactories.getInstance(constellioFactoriesDecorator).getAppLayerFactory();
	}

}
