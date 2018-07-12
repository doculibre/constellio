package com.constellio.app.extensions.api.scripts;

import java.io.File;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class StringBuilderActionLogger implements ScriptActionLogger {

	StringBuilder stringBuilder = new StringBuilder();

	@Override
	public void appendToFile(String message) {
		stringBuilder.append("INFO:" + message);
	}

	@Override
	public File getTempFile() {
		return null;
	}

	@Override
	public void appendToFileWithoutLogging(String message) {
		stringBuilder.append(message);

	}

	@Override
	public void info(String message) {
		stringBuilder.append("INFO:" + message);
	}

	@Override
	public void warn(String message) {
		stringBuilder.append("WARN:" + message);
	}

	@Override
	public void error(String message) {
		stringBuilder.append("ERROR:" + message);
	}

	@Override
	public void error(String message, Exception exception) {
		stringBuilder.append("ERROR:" + ExceptionUtils.getMessage(exception) + "\n" + ExceptionUtils.getStackTrace(exception));
	}

	public String getReport() {
		return stringBuilder.toString();
	}
}
