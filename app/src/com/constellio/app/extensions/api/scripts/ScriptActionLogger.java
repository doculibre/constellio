package com.constellio.app.extensions.api.scripts;

import java.io.File;

public interface ScriptActionLogger {

	void appendToFile(String message);

	File getTempFile();

	void appendToFileWithoutLogging(String message);

	void info(String message);

	void warn(String message);

	void error(String message);

}
