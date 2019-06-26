package com.constellio.app.ui.util;

import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;

public class PlatformDetectionUtils {
	public static boolean isMobile(){
		Page page = Page.getCurrent();
		WebBrowser webBrowser = page.getWebBrowser();

		return webBrowser.isWindowsPhone() || webBrowser.isAndroid() || webBrowser.isIOS();
	}

	public static boolean isDesktop(){
		Page page = Page.getCurrent();
		WebBrowser webBrowser = page.getWebBrowser();

		return webBrowser.isWindows() || webBrowser.isLinux() || webBrowser.isMacOSX();
	}
}
