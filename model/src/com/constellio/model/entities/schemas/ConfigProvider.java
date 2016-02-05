package com.constellio.model.entities.schemas;

import com.constellio.model.entities.configs.SystemConfiguration;

public interface ConfigProvider {

	<T> T get(SystemConfiguration config);

}
