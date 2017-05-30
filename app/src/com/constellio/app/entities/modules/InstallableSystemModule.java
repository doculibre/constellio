package com.constellio.app.entities.modules;

import com.constellio.app.services.factories.AppLayerFactory;

//TODO : Move in InstallableModule with "default" once compiled with java 8
public interface InstallableSystemModule extends InstallableModule {
	void start(AppLayerFactory appLayerFactory);

	void stop(AppLayerFactory appLayerFactory);

}
