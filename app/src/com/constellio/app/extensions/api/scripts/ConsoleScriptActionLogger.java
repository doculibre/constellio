package com.constellio.app.extensions.api.scripts;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleScriptActionLogger implements ScriptActionLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleScriptActionLogger.class);

	@Override
	public void appendToFile(String message) {
		LOGGER.info(message);
	}

	@Override
	public File getTempFile() {
		return null;
	}

	@Override
	public void appendToFileWithoutLogging(String message) {
		System.out.println(message);

	}

	@Override
	public void info(String message) {
		LOGGER.info(message);
	}

	@Override
	public void warn(String message) {
		LOGGER.warn(message);
	}

	@Override
	public void error(String message) {
		LOGGER.error(message);
	}

	@Override
	public void error(String message, Exception exception) {
		LOGGER.error(message, exception);
	}
}
