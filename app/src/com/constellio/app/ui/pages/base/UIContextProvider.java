package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.ConstellioFactories;

import java.io.Serializable;

public interface UIContextProvider extends Serializable {

	UIContext getUIContext();

	ConstellioFactories getConstellioFactories();
}
