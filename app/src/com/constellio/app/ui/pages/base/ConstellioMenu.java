package com.constellio.app.ui.pages.base;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;

public interface ConstellioMenu extends Serializable {

	ConstellioNavigator navigateTo();

	SessionContext getSessionContext();
	
	void updateUIContent();
	
	void setCollections(List<String> collections);

	ConstellioFactories getConstellioFactories();
	
	void setSignOutLinkVisible(boolean visible);

}
