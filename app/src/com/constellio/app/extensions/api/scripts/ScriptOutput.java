package com.constellio.app.extensions.api.scripts;

public class ScriptOutput {

	String filename;

	private ScriptOutput(String filename) {
		this.filename = filename;
	}

	public static ScriptOutput toLogFile(String logFile) {
		return new ScriptOutput(logFile);
	}

	public String getFilename() {
		return filename;
	}
}
