package com.constellio.app.ui.pages.base;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;

import java.io.Serializable;

public interface MainLayout extends Serializable {
	String MAIN_LAYOUT_NAVIGATION = "mainLayoutNavigation";

	@Deprecated
	CoreViews navigateTo();

	Navigation navigate();

	ConstellioHeader getHeader();

	ConstellioMenu getMenu();

	void scrollToTop();

}
