package com.constellio.app.services.factories;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.Serializable;

public class ConstellioFactoriesDecorator implements Serializable {

	public DataLayerConfiguration decorateDataLayerConfiguration(DataLayerConfiguration dataLayerConfiguration) {
		return dataLayerConfiguration;
	}

	public ModelLayerConfiguration decorateModelLayerConfiguration(ModelLayerConfiguration modelLayerConfiguration) {
		return modelLayerConfiguration;
	}

	public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {
		return appLayerConfiguration;
	}

	public DataLayerFactory decorateDataLayerFactory(DataLayerFactory dataLayerFactory) {
		return dataLayerFactory;
	}

	public ModelLayerFactory decorateModelServicesFactory(ModelLayerFactory modelLayerFactory) {
		return modelLayerFactory;
	}

	public AppLayerFactory decorateAppServicesFactory(AppLayerFactory appLayerFactory) {
		return appLayerFactory;
	}

	public FoldersLocator decorateFoldersLocator(FoldersLocator foldersLocator) {
		return foldersLocator;
	}

	public StatefullServiceDecorator getStatefullServiceDecorator() {
		return new StatefullServiceDecorator();
	}
}
