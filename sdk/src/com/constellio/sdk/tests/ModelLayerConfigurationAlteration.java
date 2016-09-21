package com.constellio.sdk.tests;

import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;

public interface ModelLayerConfigurationAlteration {

	void alter(InMemoryModelLayerConfiguration configuration);
}
