package com.constellio.app.entities.modules;

import com.constellio.app.services.factories.AppLayerFactory;

public interface InstallableSystemModule extends InstallableModule {
	void start(AppLayerFactory appLayerFactory);

	void stop(AppLayerFactory appLayerFactory);

}
