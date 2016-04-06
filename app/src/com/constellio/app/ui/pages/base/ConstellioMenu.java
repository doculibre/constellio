package com.constellio.app.ui.pages.base;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.CoreViews;

public interface ConstellioMenu extends Serializable {

	CoreViews navigateTo();

	SessionContext getSessionContext();

	void updateUIContent();

	void setCollections(List<String> collections);

	ConstellioFactories getConstellioFactories();

}
