package com.constellio.app.ui.pages.base;

import java.io.Serializable;

import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.CoreViews;

public interface MainLayout extends Serializable {
	String MAIN_LAYOUT_NAVIGATION = "mainLayoutNavigation";

	@Deprecated
	CoreViews navigateTo();

	Navigation navigate();

	ConstellioHeaderImpl getHeader();
}
