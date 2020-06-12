package com.constellio.app.services.factories;

import com.constellio.data.utils.Factory;

public interface ConstellioFactoriesInstanceProvider {

	ConstellioFactories getInstance(String tenantId, Factory<ConstellioFactories> constellioFactoriesFactory);

	boolean isInitialized(String tenantId);

	void clear(String tenantId);

	void clearAll();
}
