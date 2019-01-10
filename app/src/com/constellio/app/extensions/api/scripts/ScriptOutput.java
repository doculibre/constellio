package com.constellio.app.extensions.api.scripts;

public class ScriptOutput {

	String filename;

	public ScriptOutput(String filename) {
		this.filename = filename;
	}

	public static ScriptOutput toLogFile(String logFile) {
		return new ScriptOutput(logFile);
	}


	public static ScriptOutput toZipFile(String filename) {
		return new ScriptOutput(filename);
	}

	public String getFilename() {
		return filename;
	}

}
