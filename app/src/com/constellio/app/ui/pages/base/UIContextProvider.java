package com.constellio.app.ui.pages.base;

import java.io.Serializable;

import com.constellio.app.services.factories.ConstellioFactories;

public interface UIContextProvider extends Serializable {

	UIContext getUIContext();

	ConstellioFactories getConstellioFactories();
}
