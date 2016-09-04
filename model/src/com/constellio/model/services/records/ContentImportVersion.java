package com.constellio.model.services.records;

import org.joda.time.LocalDateTime;

public class ContentImportVersion {

	String url;

	String fileName;

	String comment;

	boolean major;

	LocalDateTime lastModification;

	public ContentImportVersion(String url, String fileName, boolean major, String comment, LocalDateTime lastModification) {
		this.url = url;
		this.fileName = fileName;
		this.major = major;
		this.comment = comment;
		this.lastModification = lastModification;
	}

	public ContentImportVersion(String url, String fileName, boolean major, LocalDateTime lastModification) {
		this.url = url;
		this.fileName = fileName;
		this.major = major;
		this.lastModification = lastModification;
	}

	public String getUrl() {
		return url;
	}

	public String getFileName() {
		return fileName;
	}

	public String getComment() {
		return comment;
	}

	public boolean isMajor() {
		return major;
	}

	public LocalDateTime getLastModification() {
		return lastModification;
	}
}
