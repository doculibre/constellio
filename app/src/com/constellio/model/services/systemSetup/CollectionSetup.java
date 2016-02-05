package com.constellio.model.services.systemSetup;

import com.constellio.app.services.factories.AppLayerFactory;

public interface CollectionSetup {

	void setup(String collection, AppLayerFactory appLayerFactory);
}
