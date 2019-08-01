package com.constellio.app.services.factories;

import com.constellio.data.utils.Factory;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ModelLayerFactoryFactory implements Factory<ModelLayerFactory> {

	@Override
	public ModelLayerFactory get() {
		return ConstellioFactories.getInstance().getModelLayerFactory();
	}
}
