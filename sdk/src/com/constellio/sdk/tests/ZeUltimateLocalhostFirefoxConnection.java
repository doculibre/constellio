package com.constellio.sdk.tests;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ClasspathExtension;
import org.openqa.selenium.logging.LocalLogs;
import org.openqa.selenium.logging.NeedsLocalLogs;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import static org.openqa.selenium.firefox.FirefoxProfile.PORT_PREFERENCE;


//Same as NewProfileExtensionConnection, except that "localhost" hostname is used instead of calling NetworkUtils which cause problems on some networks
public class ZeUltimateLocalhostFirefoxConnection implements /*ExtensionConnection,*/ NeedsLocalLogs {
	private final static int BUFFER_SIZE = 4096;

	private final long connectTimeout;
	private final FirefoxBinary process;
	private final FirefoxProfile profile;
	private File profileDir;

	private HttpCommandExecutor delegate;

	private LocalLogs logs = LocalLogs.getNullLogger();

	public ZeUltimateLocalhostFirefoxConnection(FirefoxBinary binary, FirefoxProfile profile)
			throws Exception {
		this.connectTimeout = binary.getTimeout();
		this.profile = profile;
		this.process = binary;
	}

	public void start()
			throws IOException {
		addWebDriverExtensionIfNeeded();

		int port = 0;

		//lock.lock(connectTimeout);
		try {
			port = determineNextFreePort(7070);
			profile.setPreference(PORT_PREFERENCE, port);

			profileDir = profile.layoutOnDisk();

			//process.clean(profile, profileDir);

			delegate = new HttpCommandExecutor(buildUrl("localhost", port));
			delegate.setLocalLogs(logs);
			String firefoxLogFile = System.getProperty("webdriver.firefox.logfile");

			if (firefoxLogFile != null) {
				if ("/dev/stdout".equals(firefoxLogFile)) {
					//process.setOutputWatcher(System.out);
				} else {
					File logFile = new File(firefoxLogFile);
					///process.setOutputWatcher(new CircularOutputStream(logFile, BUFFER_SIZE));
				}
			}

			//process.startProfile(profile, profileDir, "-foreground");

			// Just for the record; the critical section is all along while firefox is starting with the
			// profile.

			// There is currently no mechanism for the profile to notify us when it has started
			// successfully and is ready for requests. Instead, we must loop until we're able to
			// open a connection with the server, at which point it should be safe to continue
			// (since the extension shouldn't accept connections until it is ready for requests).
			long waitUntil = System.currentTimeMillis() + connectTimeout;
			while (!isConnected()) {
				if (waitUntil < System.currentTimeMillis()) {
					//throw new NotConnectedException(
					//		delegate.getAddressOfRemoteServer(), connectTimeout, process.getConsoleOutput());
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException ignored) {
					// Do nothing
				}
			}
		} catch (WebDriverException e) {
			throw new WebDriverException();
			//String.format("Failed to connect to binary %s on port %d; process output follows: \n%s",
			//		process.toString(), port, process.getConsoleOutput()), e);
		} catch (Exception e) {
			throw new WebDriverException(e);
		} finally {
			//lock.unlock();
		}
	}

	protected void addWebDriverExtensionIfNeeded() {
		if (profile.containsWebDriverExtension()) {
			return;
		}

		ClasspathExtension extension = new ClasspathExtension(FirefoxProfile.class,
				"/" + FirefoxProfile.class.getPackage().getName().replace(".", "/") + "/webdriver.xpi");
		profile.addExtension("webdriver", extension);
	}

	public Response execute(Command command)
			throws IOException {
		return delegate.execute(command);
	}

	protected int determineNextFreePort(int port) {
		// Attempt to connect to the given port on the host
		// If we can't connect, then we're good to use it
		int newport;

		for (newport = port; newport < port + 2000; newport++) {
			Socket socket = new Socket();
			InetSocketAddress address = new InetSocketAddress("localhost", newport);

			try {
				socket.bind(address);
				return newport;
			} catch (IOException e) {
				// Port is already bound. Skip it and continue
			} finally {
				try {
					socket.close();
				} catch (IOException ignored) {
					// Nothing sane to do. Ignore this.
				}
			}
		}

		throw new WebDriverException(
				String.format("Cannot find free port in the range %d to %d ", port, newport));
	}

	public void quit() {
		// This should only be called after the QUIT command has been sent,
		// so go ahead and clean up our process and profile.
		//process.quit();
		if (profileDir != null) {
			profile.clean(profileDir);
		}
	}
	/*
	@Override
	public URI getAddressOfRemoteServer() {
		return null;
	}
	*/

	/**
	 * Builds the URL for the Firefox extension running on the given host and port. If the host is
	 * {@code localhost}, an attempt will be made to find the correct loopback address.
	 *
	 * @param host The hostname the extension is running on.
	 * @param port The port the extension is listening on.
	 * @return The URL of the Firefox extension.
	 */
	private static URL buildUrl(String host, int port) {
		String hostToUse = "localhost".equals(host) ? "localhost" : host;
		try {
			return new URL("http", hostToUse, port, "/hub");
		} catch (MalformedURLException e) {
			throw new WebDriverException(e);
		}
	}

	public boolean isConnected() {
		try {
			// TODO: use a more intelligent way of testing if the server is ready.
			delegate.getAddressOfRemoteServer().openConnection().connect();
			return true;
		} catch (IOException e) {
			// Cannot connect yet.
			return false;
		}
	}

	public void setLocalLogs(LocalLogs logs) {
		if (delegate != null) {
			delegate.setLocalLogs(logs);
		}
		this.logs = logs;
	}
}