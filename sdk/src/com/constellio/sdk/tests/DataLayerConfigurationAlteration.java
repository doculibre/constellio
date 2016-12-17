package com.constellio.sdk.tests;

import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;

public interface DataLayerConfigurationAlteration {

	void alter(InMemoryDataLayerConfiguration configuration);

}
