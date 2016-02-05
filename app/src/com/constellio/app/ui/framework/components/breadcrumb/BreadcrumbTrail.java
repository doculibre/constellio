package com.constellio.app.ui.framework.components.breadcrumb;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.pages.base.SessionContextProvider;

public interface BreadcrumbTrail extends Serializable, SessionContextProvider {

	void addItem(BreadcrumbItem item);
	
	List<BreadcrumbItem> getItems();

	ConstellioNavigator navigateTo();
	
}
