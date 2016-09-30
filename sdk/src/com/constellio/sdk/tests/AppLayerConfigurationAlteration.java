package com.constellio.sdk.tests;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.conf.PropertiesAppLayerConfiguration.InMemoryAppLayerConfiguration;

public interface AppLayerConfigurationAlteration {

	void alter(InMemoryAppLayerConfiguration configuration);

}
