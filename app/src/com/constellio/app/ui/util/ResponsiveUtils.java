package com.constellio.app.ui.util;

import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;

public class ResponsiveUtils {

	public static boolean isPhone() {
		boolean phone;
		Page page = Page.getCurrent();
		if (page == null) {
			return false;
		}

		WebBrowser webBrowser = page.getWebBrowser();
		if (webBrowser.isIPhone()) {
			phone = true;
		} else {
			phone = page.getBrowserWindowWidth() <= 800;
		}
		return phone;
	}

	public static boolean isTablet() {
		boolean tablet;
		Page page = Page.getCurrent();

		if (page == null) {
			return false;
		}

		WebBrowser webBrowser = page.getWebBrowser();
		if (webBrowser.isIPad()) {
			tablet = true;
		} else {
			tablet = page.getBrowserWindowWidth() > 800 && page.getBrowserWindowWidth() <= 1100;
		}
		return tablet;
	}

	public static boolean isMobile() {
		return isPhone() || isTablet();
	}

	public static boolean isDesktop() {
		boolean desktop;
		if (isPhone() || isTablet()) {
			desktop = false;
		} else {
			if (Page.getCurrent() == null) {
				return false;
			}
			desktop = Page.getCurrent().getBrowserWindowWidth() > 1100;
		}
		return desktop;
	}

	public static boolean isFileDropSupported() {
		boolean supportsFileDrops;
		Page page = Page.getCurrent();
		WebBrowser browser = page.getWebBrowser();
		if (browser.isWindowsPhone() || browser.isIOS() || browser.isAndroid()) {
			supportsFileDrops = false;
		} else {
			supportsFileDrops = true;
		}
		return supportsFileDrops;
	}

}
