package com.constellio.app.services.importExport.records.writers;

public class ImportContentVersion {

	private String url;
	private String fileName;
	private boolean major;

	public ImportContentVersion(String url, String fileName, boolean major) {
		this.url = url;
		this.fileName = fileName;
		this.major = major;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isMajor() {
		return major;
	}

	public void setMajor(boolean major) {
		this.major = major;
	}
}
