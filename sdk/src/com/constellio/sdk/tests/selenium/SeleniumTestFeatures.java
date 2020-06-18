package com.constellio.sdk.tests.selenium;

import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.client.cmis.client.CmisSessionBuilder;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.services.tenant.TenantService;
import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTestSession;
import com.constellio.sdk.tests.FactoriesTestFeatures;
import com.constellio.sdk.tests.SkipTestsRule;
import com.constellio.sdk.tests.ZeUltimateFirefoxDriver;
import com.constellio.sdk.tests.ZeUltimateFirefoxProfile;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.util.Date;
import java.util.Map;

import static com.constellio.sdk.tests.SDKConstellioFactoriesInstanceProvider.DEFAULT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class SeleniumTestFeatures {

	private static boolean applicationStarted = false;
	private static ConstellioWebDriver openedWebDriver;
	private boolean waitUntilICloseTheBrowsers = false;
	private FactoriesTestFeatures factoriesTestFeatures;
	SkipTestsRule skipTestsRule;

	private static boolean resetServletsAndFiltersAfterTest = true;

	private Map<String, String> sdkProperties;

	private int port = 8080;
	private int portSSL = 8443;

	public static void afterAllTests() {
		if (applicationStarted) {
			ApplicationStarter.stopApplication();
			applicationStarted = false;
		}

		if (openedWebDriver != null) {
			closeOpenedWebDriver();
		}
		resetServletsAndFiltersAfterTest = true;
	}

	private static void closeOpenedWebDriver() {
		openedWebDriver.quit();
		openedWebDriver = null;
	}

	public void afterTest(boolean failed) {
		if (waitUntilICloseTheBrowsers) {
			String phantomJSBinaryDir = sdkProperties.get("phantomJSBinary");
			String firefoxBinaryDir = sdkProperties.get("firefoxBinary");

			if (firefoxBinaryDir == null && phantomJSBinaryDir == null) {
				try {
					Object lock = new Object();
					synchronized (lock) {
						while (true) {
							lock.wait();
						}
					}
				} catch (InterruptedException ex) {
				}
			} else {
				waitForWebDriversToClose();
			}
		} else if (openedWebDriver != null) {
			if (failed) {
				Dimension dimension = openedWebDriver.manage().window().getSize();
				openedWebDriver.manage().window().setSize(new Dimension(dimension.getWidth(), 1800));
				openedWebDriver.snapshot("failure");
			}
			closeOpenedWebDriver();
		}

		if (resetServletsAndFiltersAfterTest) {
			ApplicationStarter.resetServlets();
			ApplicationStarter.resetFilters();
		}
		waitUntilICloseTheBrowsers = false;
	}

	public void beforeTest(Map<String, String> theSdkProperties, FactoriesTestFeatures factoriesTestFeatures,
						   SkipTestsRule skipTestsRule) {
		this.sdkProperties = theSdkProperties;
		this.factoriesTestFeatures = factoriesTestFeatures;
		this.skipTestsRule = skipTestsRule;
		if (sdkProperties.containsKey("port")) {
			try {
				port = Integer.valueOf(sdkProperties.get("port"));
			} catch (Exception e) {
				port = 8080;
			}
		}
	}

	public CmisSessionBuilder newCmisSessionBuilder() {
		resetServletsAndFiltersAfterTest = false;
		disableAllServices();
		System.setProperty("cmisEnabled", "true");
		if (!applicationStarted) {
			startApplication();

		}
		String url;

		if (TenantUtils.isSupportingTenants()) {
			url = "http://" + TenantService.getInstance().getTenantByCode(TenantUtils.getTenantId()).getHostnames().get(0) + ":" + port + "/constellio/";
		} else {
			url = "http://localhost:" + port + "/constellio/";
		}


		return CmisSessionBuilder.forAppUrl(url);

	}

	public AdminServicesSession newRestClient(String serviceKey, String username, String password) {
		disableAllServices();
		System.setProperty("driverEnabled", "true");
		resetServletsAndFiltersAfterTest = false;
		if (!applicationStarted) {
			startApplication();
		}
		String url = "http://localhost:" + port + "/constellio/rest";
		return AdminServicesSession.connect(url, serviceKey, username, password);
	}

	public WebTarget newWebTarget() {
		return newWebTarget("/rest");
	}

	public WebTarget newWebTarget(String path) {
		return newWebTarget(path, null);
	}

	public WebTarget newWebTarget(String path, ObjectMapper objectMapper) {

		if (!path.isEmpty() && !path.startsWith("/")) {
			path = "/" + path;
		}

		disableAllServices();
		System.setProperty("driverEnabled", "true");
		if (!applicationStarted) {
			startApplication();
		}
		String url = "http://localhost:" + port + "/constellio/rest" + path;

		ObjectMapper mapper = objectMapper != null ? objectMapper : new ObjectMapper();
		mapper.registerModule(new JodaModule());

		JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
		jsonProvider.setMapper(mapper);

		ClientConfig config = new ClientConfig();
		config.property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE_CLIENT, true);
		config.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
		config.register(jsonProvider);
		config.register(MultiPartFeature.class);

		return ClientBuilder.newClient(config).target(url);
	}

	public SolrClient newSearchClient() {
		disableAllServices();
		resetServletsAndFiltersAfterTest = false;
		if (!applicationStarted) {
			startApplication();
		}
		String url = "http://localhost:" + port + "/constellio";
		SolrClient solrServer = new HttpSolrClient.Builder(url).withResponseParser(new XMLResponseParser()).build();

		return solrServer;

	}

	public ConstellioWebDriver newWebDriver(boolean preferFirefox) {
		return newWebDriver(preferFirefox, false);
	}

	public ConstellioWebDriver newWebDriver(boolean preferFirefox, boolean useSSL) {
		disableAllServices();
		if (!applicationStarted) {
			startApplication();
		}

		String url;
		if (useSSL) {
			url = "https://localhost:" + portSSL + "/constellio";
		} else {
			url = "http://localhost:" + port + "/constellio";
		}

		String phantomJSBinaryDir = sdkProperties.get("phantomJSBinary");
		String firefoxBinaryDir = sdkProperties.get("firefoxBinary");

		String currentPageLoadTime;
		if (openedWebDriver == null) {
			WebDriver webDriver;
			if (firefoxBinaryDir == null && phantomJSBinaryDir == null) {
				return null;
				//throw new RuntimeException(
				//		"You need to configure 'phantomJSBinary' or 'firefoxBinary' properties in sdk.properties file");
			} else if (phantomJSBinaryDir == null || (firefoxBinaryDir != null && preferFirefox)) {
				webDriver = newFirefoxWebDriver(firefoxBinaryDir);

			} else {
				webDriver = newPhantomJSWebDriver(phantomJSBinaryDir);
			}
			FoldersLocator foldersLocator = factoriesTestFeatures.getFoldersLocator(DEFAULT_NAME);
			openedWebDriver = new ConstellioWebDriver(webDriver, url, foldersLocator, skipTestsRule);
			currentPageLoadTime = "";

		} else {
			currentPageLoadTime = openedWebDriver.getPageLoadTimeAsString(2000);
			openedWebDriver.manage().deleteAllCookies();
		}

		boolean customWindowPosition = ConstellioTestSession.get().isDeveloperTest();

		if (customWindowPosition) {
			String positionXConfig = sdkProperties.get("window.position.x");
			String positionYConfig = sdkProperties.get("window.position.y");
			String widthConfig = sdkProperties.get("window.width");
			String heightConfig = sdkProperties.get("window.height");

			if (StringUtils.isNotBlank(positionXConfig) && StringUtils.isNotBlank(positionYConfig)) {
				int positionX = Integer.valueOf(positionXConfig);
				int positionY = Integer.valueOf(positionYConfig);
				openedWebDriver.manage().window().setPosition(new Point(positionX, positionY));
			}

			if (StringUtils.isNotBlank(widthConfig) && StringUtils.isNotBlank(heightConfig)) {
				int width = Integer.valueOf(widthConfig);
				int height = Integer.valueOf(heightConfig);
				openedWebDriver.manage().window().setSize(new Dimension(width, height));
			} else {
				openedWebDriver.manage().window().setSize(new Dimension(1200, 1024));
			}

		} else {
			openedWebDriver.manage().window().setSize(new Dimension(1200, 1024));
		}

		boolean ready = false;

		Exception exception = null;
		for (int i = 0; i < 10 && !ready; i++) {
			try {
				openedWebDriver.gotoConstellio();
				openedWebDriver.waitForPageReload(20, currentPageLoadTime);
				ready = true;
			} catch (Exception e) {
				exception = e;
			}
		}
		if (!ready) {
			throw new RuntimeException(exception);
		}

		return openedWebDriver;
	}

	public ConstellioWebDriver newWebDriverSSL(boolean preferFirefox) {
		disableAllServices();
		if (!applicationStarted) {
			startApplication();
		}

		String url = "http://localhost:" + portSSL + "/constellio";

		String phantomJSBinaryDir = sdkProperties.get("phantomJSBinary");
		String firefoxBinaryDir = sdkProperties.get("firefoxBinary");

		String currentPageLoadTime;
		if (openedWebDriver == null) {
			WebDriver webDriver;
			if (firefoxBinaryDir == null && phantomJSBinaryDir == null) {
				throw new RuntimeException(
						"You need to configure 'phantomJSBinary' or 'firefoxBinary' properties in sdk.properties file");
			} else if (phantomJSBinaryDir == null || (firefoxBinaryDir != null && preferFirefox)) {
				webDriver = newFirefoxWebDriver(firefoxBinaryDir);

			} else {
				webDriver = newPhantomJSWebDriver(phantomJSBinaryDir);
			}
			FoldersLocator foldersLocator = factoriesTestFeatures.getFoldersLocator(DEFAULT_NAME);
			openedWebDriver = new ConstellioWebDriver(webDriver, url, foldersLocator, skipTestsRule);
			currentPageLoadTime = "";

		} else {
			currentPageLoadTime = openedWebDriver.getPageLoadTimeAsString(2000);
			openedWebDriver.manage().deleteAllCookies();
		}

		openedWebDriver.manage().window().setSize(new Dimension(1200, 1024));

		boolean ready = false;

		Exception exception = null;
		for (int i = 0; i < 10 && !ready; i++) {
			try {
				openedWebDriver.gotoConstellio();
				openedWebDriver.waitForPageReload(20, currentPageLoadTime);
				ready = true;
			} catch (Exception e) {
				exception = e;
			}
		}
		if (!ready) {
			throw new RuntimeException(exception);
		}

		return openedWebDriver;
	}

	private WebDriver newPhantomJSWebDriver(String phantomJSBinaryDir) {
		System.setProperty("phantomjs.binary.path", phantomJSBinaryDir);

		try {
			return null;
			//return new PhantomJSDriver(DesiredCapabilities.phantomjs());
		} catch (Exception e) {
			throw new RuntimeException("Could not start PhantomJS in directory '" + phantomJSBinaryDir + "'", e);
		}
	}

	private WebDriver newFirefoxWebDriver(String firefoxBinaryDir) {
		WebDriver webDriver;
		if (firefoxBinaryDir.equals("detect")) {
			webDriver = newDefaultFirefoxWebDriver();
		} else {
			webDriver = newFirefoxWebDriverWithSpecificBinDirectory(firefoxBinaryDir);
		}
		return webDriver;
	}

	private ZeUltimateFirefoxDriver newDefaultFirefoxWebDriver() {
		try {
			return new ZeUltimateFirefoxDriver();
		} catch (Exception e) {
			throw new RuntimeException("Could not detect or start Firefox", e);
		}
	}

	private ZeUltimateFirefoxDriver newFirefoxWebDriverWithSpecificBinDirectory(String firefoxBinaryDir) {
		try {
			FirefoxBinary firefoxBinary = new FirefoxBinary(new File(firefoxBinaryDir));
			ZeUltimateFirefoxProfile firefoxSchema = new ZeUltimateFirefoxProfile();
			return new ZeUltimateFirefoxDriver(firefoxBinary, firefoxSchema);
		} catch (Exception e) {
			throw new RuntimeException("Could not start Firefox in directory '" + firefoxBinaryDir + "'", e);
		}
	}

	public void waitUntilICloseTheBrowsers() {
		waitUntilICloseTheBrowsers = true;
	}

	public String startApplication() {
		File webContent = new FoldersLocator().getAppProjectWebContent();
		long time = new Date().getTime();

		assertThat(webContent).exists().isDirectory();

		File webInf = new File(webContent, "WEB-INF");
		assertThat(webInf).exists().isDirectory();
		assertThat(new File(webInf, "web.xml")).exists();
		assertThat(new File(webInf, "sun-jaxws.xml")).exists();

		File cmis11 = new File(webInf, "cmis11");
		assertThat(cmis11).exists().isDirectory();
		assertThat(cmis11.listFiles()).isNotEmpty();

		ApplicationStarter.startApplication(false, webContent, port);

		applicationStarted = true;
		System.out.println("Application started in " + (new Date().getTime() - time) + "ms");

		return "http://localhost:" + port + "/constellio/";
	}

	public String startApplicationWithSSL(boolean keepAlive) {
		File webContent = new FoldersLocator().getAppProjectWebContent();
		long time = new Date().getTime();

		assertThat(webContent).exists().isDirectory();

		File webInf = new File(webContent, "WEB-INF");
		assertThat(webInf).exists().isDirectory();
		assertThat(new File(webInf, "web.xml")).exists();
		assertThat(new File(webInf, "sun-jaxws.xml")).exists();

		File cmis11 = new File(webInf, "cmis11");
		assertThat(cmis11).exists().isDirectory();
		assertThat(cmis11.listFiles()).isNotEmpty();

		ApplicationStarter.startApplication(keepAlive, webContent, Integer.valueOf(SDKPasswords.sslPort()), SDKPasswords.sslKeystorePassword());

		applicationStarted = true;
		System.out.println("Application started in " + (new Date().getTime() - time) + "ms");

		return "http://localhost:" + portSSL + "/constellio/";
	}

	public void stopApplication() {
		ApplicationStarter.stopApplication();
		applicationStarted = false;
	}

	private void waitForWebDriversToClose() {

		try {
			while (true) {
				openedWebDriver.getCurrentUrl();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		} catch (RuntimeException e) {
			return;
			// Web driver closed
		}
	}

	public void disableAllServices() {
		System.setProperty("driverEnabled", "false");
		System.setProperty("cmisEnabled", "false");
		System.setProperty("benchmarkServiceEnabled", "false");

	}

	public ConstellioWebDriver getLastWebDriver() {
		return openedWebDriver;
	}

	public String getHttpApplicationRootUrl() {
		return "http://localhost:" + port + "/constellio/";
	}

	public String getHttpsApplicationRootUrl() {
		return "http://localhost:" + portSSL + "/constellio/";
	}
}
