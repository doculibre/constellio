package com.constellio.app.ui.framework.components.breadcrumb;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.application.ConstellioUI.Navigation;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.SessionContextProvider;

public interface BreadcrumbTrail extends Serializable, SessionContextProvider {

	void addItem(BreadcrumbItem item);

	List<BreadcrumbItem> getItems();

	@Deprecated
	CoreViews navigateTo();

	Navigation navigate();
}
