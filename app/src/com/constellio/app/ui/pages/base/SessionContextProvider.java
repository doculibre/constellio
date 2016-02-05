package com.constellio.app.ui.pages.base;

import java.io.Serializable;

import com.constellio.app.services.factories.ConstellioFactories;

public interface SessionContextProvider extends Serializable {

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();
}
