package com.constellio.app.modules.es.connectors.spi;

import java.util.Map;

/**
 * Threadsafe
 *
 * @author Nicolas
 *
 */
public interface ConnectorLogger {

	public void info(String title, String description, Map<String, String> properties);

	public void error(String title, String description, Map<String, String> properties);

	public void debug(String title, String description, Map<String, String> properties);

	public void info(LoggedException exception);

	public void error(LoggedException exception);

	public void error(String documentUrl, Exception exception);

	public void debug(LoggedException exception);

	public void errorUnexpected(Throwable exception);
}
