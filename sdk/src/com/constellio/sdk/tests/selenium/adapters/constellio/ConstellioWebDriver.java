/**
 * Constellio
 * Copyright (C) 2010 DocuLibre inc.
 * <p>
 * This program is free software: you can redistribute it and/or modifyTo
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.sdk.tests.selenium.adapters.constellio;

import com.constellio.app.ui.tools.ApplicationRuntimeException;
import com.constellio.app.ui.tools.ServerThrowableContext;
import com.constellio.app.ui.tools.pageloadtime.PageLoadTimeReader;
import com.constellio.app.ui.tools.pageloadtime.PageLoadTimeWriter;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.sdk.tests.SkipTestsRule;
import com.constellio.sdk.tests.selenium.adapters.base.WebDriverAdapter;
import com.constellio.sdk.tests.selenium.adapters.base.WebElementFinder;
import com.constellio.sdk.tests.selenium.conditions.ActionWithTimeout;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConstellioWebDriver extends WebDriverAdapter<ConstellioWebElement> {

	private static boolean firstBefore = true;

	private static File snapshotFolder;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioWebElement.class);

	private String url;

	private FoldersLocator foldersLocator;

	private SkipTestsRule skipTestsRule;

	private AtomicInteger index = new AtomicInteger(0);

	public ConstellioWebDriver(WebDriver driver, String url, FoldersLocator foldersLocator,
							   SkipTestsRule skipTestsRule) {
		super(driver);
		this.url = url;
		this.foldersLocator = foldersLocator;
		this.skipTestsRule = skipTestsRule;

		createSnapshotFolderIfRequired();
	}

	private void createSnapshotFolderIfRequired() {
		if (firstBefore) {
			snapshotFolder = new File(foldersLocator.getSDKProject(), "snapshots");
			if (snapshotFolder.exists()) {
				File[] files = snapshotFolder.listFiles();
				if (files != null) {
					for (File file : files) {
						try {
							FileUtils.forceDelete(file);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			snapshotFolder.mkdirs();
			firstBefore = false;
		}
	}

	@Override
	protected ConstellioWebElement adapt(WebElementFinder<WebElement> adapted) {
		return new ConstellioWebElement(this, adapted);
	}

	@Override
	protected void ensureNoApplicationException() {
		Throwable thrownByServer = ServerThrowableContext.LAST_THROWABLE.get();
		if (thrownByServer != null) {
			throw new ApplicationRuntimeException(thrownByServer);
		}
	}

	public String getBaseUrl() {
		return url;
	}

	public String getCurrentPage() {
		String currentPage = getCurrentUrl();
		currentPage = currentPage.substring(currentPage.indexOf("#!") + 2);
		return currentPage;
	}

	public Date getPageLoadTimeAsDate() {
		String pageLoadTimeString = getPageLoadTimeAsString(100);
		try {
			return pageLoadTimeString == null ?
				   null :
				   new SimpleDateFormat(PageLoadTimeWriter.DATE_FORMAT).parse(pageLoadTimeString);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public String getPageLoadTimeAsString(long timeoutMS) {
		try {
			String pageLoadTime = new ActionWithTimeout<String>() {
				@Override
				protected String execute()
						throws Exception {
					return new PageLoadTimeReader().readAsString(ConstellioWebDriver.this);
				}
			}.tryExecute(timeoutMS);

			//		    if (pageLoadTime == null) {
			//			    throw new RuntimeException("No page load time");
			//		    }

			return pageLoadTime;

		} catch (Exception e) {
			if (!e.getMessage().contains("timeout")) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public void gotoConstellio() {
		get(url);
	}

	public void waitForPageReload(int timeoutInSeconds, final String lastPageDateString) {
		try {
			new ActionWithTimeout<String>() {
				@Override
				protected String execute()
						throws Exception {
					String result;
					String time = getPageLoadTimeAsString(100);
					if (time == null || time.equals(lastPageDateString)) {
						result = null;
					} else {
						result = time;
					}
					Throwable serverThrowable = ServerThrowableContext.LAST_THROWABLE.get();
					if (serverThrowable != null) {
						throw new ApplicationRuntimeException(serverThrowable);
					}
					return result;
				}
			}.executeUntilNotNull(timeoutInSeconds * 1000);
		} catch (Exception e) {
			ApplicationRuntimeException applicationException;
			if (e instanceof ApplicationRuntimeException) {
				applicationException = (ApplicationRuntimeException) e;
			} else {
				applicationException = new ApplicationRuntimeException("Timeout waiting for page reload", e);
			}
			throw applicationException;
		}

	}

	public ConstellioWebDriverNavigator navigateTo() {
		return new ConstellioWebDriverNavigator(this, getBaseUrl());
	}

	public void logUserInCollection(String username, String collection) {
		//TODO Vincent login in application

	}

	public String getCurrentUserTitle() {
		//TODO Vincent login in application
		return null;
	}

	public void logout() {

	}

	public void printHierarchy() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Constellio web driver");
		List<ConstellioWebElement> elements = getChildren();
		for (ConstellioWebElement element : elements) {
			element.printHierarchy(stringBuilder, 1);
		}
		LOGGER.info(stringBuilder.toString());
	}

	public void snapshot(String name) {

		String className = skipTestsRule.getCurrentTestClass().getSimpleName();
		String methodName = skipTestsRule.getCurrentTestName();

		if (!name.endsWith(".png")) {
			name += ".png";
		}

		name = className + "#" + methodName + "_" + index.incrementAndGet() + "_" + name;

		File scrFile = ((TakesScreenshot) adapted).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File(snapshotFolder, name));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
