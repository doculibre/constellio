package com.constellio.app.ui.framework.components.breadcrumb;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.pages.base.SessionContextProvider;

import java.io.Serializable;
import java.util.List;

public interface BreadcrumbTrail extends Serializable, SessionContextProvider {

	void addItem(BreadcrumbItem item);

	List<BreadcrumbItem> getItems();

	List<BreadcrumbItem> getItems(Class<? extends BreadcrumbItem> clazz);
	
	@Deprecated
	CoreViews navigateTo();

	Navigation navigate();
}
