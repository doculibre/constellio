package com.constellio.app.services.factories;

import com.constellio.data.utils.Factory;

public interface ConstellioFactoriesInstanceProvider {

	ConstellioFactories getInstance(Factory<ConstellioFactories> constellioFactoriesFactory);

	boolean isInitialized();

	void clear();

}
