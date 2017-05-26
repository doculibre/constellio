package com.constellio.model.services.records;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

public class SimpleImportContent implements ImportContent {

	private List<ContentImportVersion> versions = new ArrayList<>();

	public SimpleImportContent(List<ContentImportVersion> versions) {
		this.versions = versions;
	}

	public SimpleImportContent(String url, String fileName, boolean major, String comment, LocalDateTime lastModification) {
		versions.add(new ContentImportVersion(url, fileName, major, comment, lastModification));
	}

	public SimpleImportContent(String url, String fileName, boolean major, LocalDateTime lastModification) {
		versions.add(new ContentImportVersion(url, fileName, major, lastModification));
	}

	public String getUrl() {
		return versions.get(0).getUrl();
	}

	public String getFileName() {
		return versions.get(0).getFileName();
	}

	public boolean isMajor() {
		return versions.get(0).isMajor();
	}

	public List<ContentImportVersion> getVersions() {
		return versions;
	}
}
