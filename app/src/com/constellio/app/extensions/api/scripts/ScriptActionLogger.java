package com.constellio.app.extensions.api.scripts;

public interface ScriptActionLogger {

	void appendToFile(String message);

	void appendToFileWithoutLogging(String message);

	void info(String message);

	void warn(String message);

	void error(String message);

}
