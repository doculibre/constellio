package com.constellio.app.services.recovery;

public class ConstellioVersionInfo {
	private String version;
	private String versionDirectoryPath;

	public ConstellioVersionInfo(String version, String versionDirectoryPath) {
		this.version = version;
		this.versionDirectoryPath = versionDirectoryPath;
	}

	public String getVersion() {
		return version;
	}

	public String getVersionDirectoryPath() {
		return versionDirectoryPath;
	}
}
