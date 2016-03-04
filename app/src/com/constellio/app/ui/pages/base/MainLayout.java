package com.constellio.app.ui.pages.base;

import java.io.Serializable;

import com.constellio.app.ui.application.ConstellioUI.Navigation;
import com.constellio.app.ui.application.CoreViews;

public interface MainLayout extends Serializable {
	String MAIN_LAYOUT_NAVIGATION = "mainLayoutNavigation";

	CoreViews navigateTo();

	Navigation navigate();

	ConstellioHeaderImpl getHeader();
}
