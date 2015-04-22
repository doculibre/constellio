/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
