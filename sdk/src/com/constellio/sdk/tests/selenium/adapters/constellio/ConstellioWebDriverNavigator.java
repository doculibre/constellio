package com.constellio.sdk.tests.selenium.adapters.constellio;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.ApplicationRuntimeException;
import com.constellio.app.ui.tools.ServerThrowableContext;

public class ConstellioWebDriverNavigator {

	private ConstellioWebDriver webDriver;

	private String url;

	public ConstellioWebDriverNavigator(ConstellioWebDriver webDriver, String url) {
		this.url = url;
		this.webDriver = webDriver;
	}

	public void appManagement() {
		String pageLoadTime = webDriver.getPageLoadTimeAsString(100);
		webDriver.navigate().to(url + "/#!" + NavigatorConfigurationService.APP_MANAGEMENT);
		webDriver.waitForPageReload(20, pageLoadTime);
		checkNothingThrownByServer();
	}

	public void home() {
		String pageLoadTime = webDriver.getPageLoadTimeAsString(100);
		webDriver.navigate().to(url + "/");
		webDriver.waitForPageReload(20, pageLoadTime);
		checkNothingThrownByServer();
	}

	public void url(String pageUrl) {
		String pageLoadTime = webDriver.getPageLoadTimeAsString(100);
		webDriver.navigate().to(url + "/#!" + pageUrl);
		webDriver.waitForPageReload(20, pageLoadTime);
		checkNothingThrownByServer();
	}

	private void checkNothingThrownByServer() {
		Throwable thrownByServer = ServerThrowableContext.LAST_THROWABLE.get();
		if (thrownByServer != null) {
			throw new ApplicationRuntimeException(thrownByServer);
		}
	}
}
