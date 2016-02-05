package com.constellio.app.modules.es.connectors.spi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleConnectorLogger implements ConnectorLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleConnectorLogger.class);

	@Override
	public void info(String title, String description, Map<String, String> properties) {
		LOGGER.info(title + " " + description + " " + properties);
	}

	@Override
	public void error(String title, String description, Map<String, String> properties) {
		LOGGER.error(title + " " + description + " " + properties);
	}

	@Override
	public void debug(String title, String description, Map<String, String> properties) {
		LOGGER.debug(title + " " + description + " " + properties);
	}

	@Override
	public void info(LoggedException exception) {
		LOGGER.info(exception.getMessage());
	}

	@Override
	public void error(LoggedException exception) {
		LOGGER.error(exception.getMessage());
	}

	@Override
	public void error(String documentUrl, Exception exception) {
		LOGGER.error("Error in document '" + documentUrl + "' :\n" + exception.getMessage() + "\n" + getStackTrace(exception));
	}

	@Override
	public void debug(LoggedException exception) {
		LOGGER.debug(exception.getMessage());
	}

	@Override
	public void errorUnexpected(Throwable exception) {
		LOGGER.error(exception.getMessage() + "\n" + getStackTrace(exception));
	}

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
