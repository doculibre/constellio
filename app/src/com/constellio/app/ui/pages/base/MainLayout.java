package com.constellio.app.ui.pages.base;

import java.io.Serializable;

import com.constellio.app.ui.application.ConstellioNavigator;

public interface MainLayout extends Serializable {
	String MAIN_LAYOUT_NAVIGATION = "mainLayoutNavigation";

	ConstellioNavigator navigateTo();

	ConstellioHeaderImpl getHeader();
}
