package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.ConstellioFactories;

import java.io.Serializable;

public interface SessionContextProvider extends Serializable {

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();
}
