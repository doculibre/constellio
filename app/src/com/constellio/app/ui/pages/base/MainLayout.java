package com.constellio.app.ui.pages.base;

import java.io.Serializable;

import com.constellio.app.ui.application.ConstellioNavigator;

public interface MainLayout extends Serializable {
	public static String MAIN_LAYOUT_NAVIGATION1 = "mainLayoutNavigation1";
	public static String MAIN_LAYOUT_NAVIGATION2 = "mainLayoutNavigation2";

	ConstellioNavigator navigateTo();

	ConstellioHeaderImpl getHeader();

}
