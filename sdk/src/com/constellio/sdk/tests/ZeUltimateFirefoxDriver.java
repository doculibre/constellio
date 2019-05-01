package com.constellio.sdk.tests;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.io.Zip;
import org.openqa.selenium.logging.LocalLogs;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.logging.NeedsLocalLogs;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.Platform.WINDOWS;
import static org.openqa.selenium.remote.CapabilityType.ACCEPT_SSL_CERTS;
import static org.openqa.selenium.remote.CapabilityType.LOGGING_PREFS;
import static org.openqa.selenium.remote.CapabilityType.SUPPORTS_WEB_STORAGE;

public class ZeUltimateFirefoxDriver extends RemoteWebDriver implements TakesScreenshot {

	public static final String BINARY = "firefox_binary";
	public static final String PROFILE = "firefox_profile";

	// For now, only enable native events on Windows
	public static final boolean DEFAULT_ENABLE_NATIVE_EVENTS = Platform.getCurrent().is(WINDOWS);

	// For now, only enable native events on Windows
	public static final boolean USE_MARIONETTE = Boolean.parseBoolean(
			System.getProperty("webdriver.firefox.marionette"));

	// Accept untrusted SSL certificates.
	@Deprecated
	public static final boolean ACCEPT_UNTRUSTED_CERTIFICATES = true;
	// Assume that the untrusted certificates will come from untrusted issuers
	// or will be self signed.
	@Deprecated
	public static final boolean ASSUME_UNTRUSTED_ISSUER = true;

	protected FirefoxBinary binary;

	public ZeUltimateFirefoxDriver() {
		this(newFirefoxBinary(), null);
	}

	private static FirefoxBinary newFirefoxBinary() {
		long startTime = new Date().getTime();
		FirefoxBinary firefoxBinary = new FirefoxBinary();
		System.out.println("Create FirefoxBinary took " + (new Date().getTime() - startTime) + "ms");
		return firefoxBinary;
	}

	public ZeUltimateFirefoxDriver(ZeUltimateFirefoxProfile profile) {
		this(newFirefoxBinary(), profile);
	}

	public ZeUltimateFirefoxDriver(Capabilities desiredCapabilities) {
		this(getBinary(desiredCapabilities), extractProfile(desiredCapabilities, null),
				desiredCapabilities);
	}

	public ZeUltimateFirefoxDriver(Capabilities desiredCapabilities, Capabilities requiredCapabilities) {
		this(getBinary(desiredCapabilities), extractProfile(desiredCapabilities, requiredCapabilities),
				desiredCapabilities, requiredCapabilities);
	}

	private static ZeUltimateFirefoxProfile extractProfile(Capabilities desiredCapabilities,
														   Capabilities requiredCapabilities) {
		long startTime = new Date().getTime();

		ZeUltimateFirefoxProfile profile = null;
		Object raw = null;
		if (desiredCapabilities != null && desiredCapabilities.getCapability(PROFILE) != null) {
			raw = desiredCapabilities.getCapability(PROFILE);
		}
		if (requiredCapabilities != null && requiredCapabilities.getCapability(PROFILE) != null) {
			raw = requiredCapabilities.getCapability(PROFILE);
		}
		if (raw != null) {
			if (raw instanceof ZeUltimateFirefoxProfile) {
				profile = (ZeUltimateFirefoxProfile) raw;
			} else if (raw instanceof String) {
				try {

					File dir = TemporaryFilesystem.getDefaultTmpFS().createTempDir("webdriver", "duplicated");
					new Zip().unzip((String) raw, dir);
					profile = new ZeUltimateFirefoxProfile(dir);

				} catch (IOException e) {
					throw new WebDriverException(e);
				}
			}
		}
		profile = getProfile(profile);

		populateProfile(profile, desiredCapabilities);
		populateProfile(profile, requiredCapabilities);
		System.out.println("Extract profile took " + (new Date().getTime() - startTime) + "ms");
		return profile;
	}

	static void populateProfile(ZeUltimateFirefoxProfile profile, Capabilities capabilities) {
		if (capabilities == null) {
			return;
		}
		if (capabilities.getCapability(SUPPORTS_WEB_STORAGE) != null) {
			Boolean supportsWebStorage = (Boolean) capabilities.getCapability(SUPPORTS_WEB_STORAGE);
			profile.setPreference("dom.storage.enabled", supportsWebStorage.booleanValue());
		}
		if (capabilities.getCapability(ACCEPT_SSL_CERTS) != null) {
			Boolean acceptCerts = (Boolean) capabilities.getCapability(ACCEPT_SSL_CERTS);
			profile.setAcceptUntrustedCertificates(acceptCerts);
		}
		if (capabilities.getCapability(LOGGING_PREFS) != null) {
			LoggingPreferences logsPrefs =
					(LoggingPreferences) capabilities.getCapability(LOGGING_PREFS);
			for (String logtype : logsPrefs.getEnabledLogTypes()) {
				profile.setPreference("webdriver.log." + logtype,
						logsPrefs.getLevel(logtype).intValue());
			}
		}

		/*
		if (capabilities.getCapability(HAS_NATIVE_EVENTS) != null) {
			Boolean nativeEventsEnabled = (Boolean) capabilities.getCapability(HAS_NATIVE_EVENTS);
			profile.setEnableNativeEvents(nativeEventsEnabled);
		}
		*/
	}

	private static FirefoxBinary getBinary(Capabilities capabilities) {
		long startTime = new Date().getTime();

		if (capabilities != null && capabilities.getCapability(BINARY) != null) {
			Object raw = capabilities.getCapability(BINARY);
			if (raw instanceof FirefoxBinary) {
				return (FirefoxBinary) raw;
			}
			File file = new File((String) raw);
			return new FirefoxBinary(file);
		}
		System.out.println("getBinary took " + (new Date().getTime() - startTime) + "ms");
		return newFirefoxBinary();
	}

	public ZeUltimateFirefoxDriver(FirefoxBinary binary, ZeUltimateFirefoxProfile profile) {
		//this(binary, profile, DesiredCapabilities.firefox());
	}

	public ZeUltimateFirefoxDriver(FirefoxBinary binary, ZeUltimateFirefoxProfile profile, Capabilities capabilities) {
		this(binary, profile, capabilities, null);
	}


	public ZeUltimateFirefoxDriver(FirefoxBinary binary, ZeUltimateFirefoxProfile profile,
								   Capabilities desiredCapabilities, Capabilities requiredCapabilities) {
		//super(new LazyCommandExecutor(binary, profile),
		//		dropCapabilities(desiredCapabilities, BINARY, PROFILE),
		//		dropCapabilities(requiredCapabilities, BINARY, PROFILE));
		this.binary = binary;
	}


	@Override
	public void setFileDetector(FileDetector detector) {
		throw new WebDriverException(
				"Setting the file detector only works on remote webdriver instances obtained " +
				"via RemoteWebDriver");
	}

	/**
	 * Attempt to forcibly kill this Killable at the OS level. Useful where the extension has
	 * stopped responding, and you don't want to leak resources. Should not ordinarily be called.
	 */
	public void kill() {
		//binary.quit();
	}

	@Override
	public Options manage() {
		return new RemoteWebDriverOptions() {
			@Override
			public Timeouts timeouts() {
				return new RemoteTimeouts() {
					public Timeouts implicitlyWait(long time, TimeUnit unit) {
						execute(DriverCommand.SET_TIMEOUT, ImmutableMap.of(
								"type", "implicit",
								"ms", TimeUnit.MILLISECONDS.convert(time, unit)));
						return this;
					}

					public Timeouts setScriptTimeout(long time, TimeUnit unit) {
						execute(DriverCommand.SET_TIMEOUT, ImmutableMap.of(
								"type", "script",
								"ms", TimeUnit.MILLISECONDS.convert(time, unit)));
						return this;
					}
				};
			}
		};
	}

	/*
	@Override
	protected void startClient() {
		LazyCommandExecutor exe = (LazyCommandExecutor) getCommandExecutor();
		ZeUltimateFirefoxProfile profileToUse = getProfile(exe.profile);

		ExtensionConnection connection = connectTo(exe.binary, profileToUse, "localhost");
		exe.setConnection(connection);

		try {
			connection.start();
		} catch (IOException e) {
			throw new WebDriverException("An error occurred while connecting to Firefox", e);
		}
	}
	*/

	private static ZeUltimateFirefoxProfile getProfile(ZeUltimateFirefoxProfile profile) {
		ZeUltimateFirefoxProfile profileToUse = profile;
		String suggestedProfile = System.getProperty("webdriver.firefox.profile");
		if (profileToUse == null && suggestedProfile != null) {
			throw new RuntimeException("Ze ultimate firefox driver doesn't support 'webdriver.firefox.profile'");
		} else if (profileToUse == null) {
			profileToUse = new ZeUltimateFirefoxProfile();
		}
		return profileToUse;
	}

	/*
	protected Lock obtainLock() {
		return new SocketLock();
	}

	@Override
	protected void stopClient() {
		((LazyCommandExecutor) this.getCommandExecutor()).quit();
	}
	*/

	/**
	 * Drops capabilities that we shouldn't send over the wire.
	 * <p>
	 * Used for capabilities which aren't BeanToJson-convertable, and are only used by the local
	 * launcher.
	 */
	private static Capabilities dropCapabilities(Capabilities capabilities, String... keysToRemove) {

		if (capabilities == null) {
			return new DesiredCapabilities();
		}
		final Set<String> toRemove = Sets.newHashSet(keysToRemove);
		DesiredCapabilities caps = new DesiredCapabilities(Maps.filterKeys(capabilities.asMap(), new Predicate<String>() {
			public boolean apply(String key) {
				return !toRemove.contains(key);
			}
		}));

		// Ensure that the proxy is in a state fit to be sent to the extension
		Proxy proxy = Proxy.extractFrom(capabilities);
		if (proxy != null) {
			//caps.setCapability(PROXY, new BeanToJsonConverter().convert(proxy));
		}

		return caps;
	}

	public <X> X getScreenshotAs(OutputType<X> target) {
		// Get the screenshot as base64.
		String base64 = execute(DriverCommand.SCREENSHOT).getValue().toString();
		// ... and convert it.
		return target.convertFromBase64Png(base64);
	}

	private static class LazyCommandExecutor implements CommandExecutor, NeedsLocalLogs {
		//private ExtensionConnection connection;
		private final FirefoxBinary binary;
		private final ZeUltimateFirefoxProfile profile;
		private LocalLogs logs = LocalLogs.getNullLogger();

		private LazyCommandExecutor(FirefoxBinary binary, ZeUltimateFirefoxProfile profile) {
			this.binary = binary;
			this.profile = profile;
		}

		/*
		public void setConnection(ExtensionConnection connection) {
			//this.connection = connection;
			//connection.setLocalLogs(logs);
		}
		*/

		public void quit() {
			/*if (connection != null) {
				connection.quit();
				connection = null;
			}*/
			if (profile != null) {
				profile.cleanTemporaryModel();
			}
		}

		public Response execute(Command command)
				throws IOException {
			/*
			if (connection == null) {
				if (command.getName().equals(DriverCommand.QUIT)) {
					return new Response();
				}
				throw new SessionNotFoundException(
						"The FirefoxDriver cannot be used after quit() was called.");
			}
			return connection.execute(command);
			*/
			return null;
		}

		public void setLocalLogs(LocalLogs logs) {
			this.logs = logs;
			/*if (connection != null) {
				connection.setLocalLogs(logs);
			}*/
		}
	}

	/*
	protected ExtensionConnection connectTo(FirefoxBinary binary, ZeUltimateFirefoxProfile profile, String host) {
		ExtensionConnection connection;
		long connectionStart = new Date().getTime();
		Lock lock = obtainLock();
		try {
			FirefoxBinary bin = binary == null ? newFirefoxBinary() : binary;

			if (USE_MARIONETTE) {
				//        System.out.println("************************** Using marionette");
				connection = new MarionetteConnection(lock, bin, profile, host);
			} else if ("localhost".equals(host)) {
				connection = new ZeUltimateLocalhostFirefoxConnection(lock, bin, profile);

			} else {
				connection = new NewProfileExtensionConnection(lock, bin, profile, host);
			}
		} catch (Exception e) {
			throw new WebDriverException(e);
		} finally {
			lock.unlock();
		}

		return connection;
	}
	*/

	@Override
	public String toString() {
		return "FirefoxDriver";
	}
}
