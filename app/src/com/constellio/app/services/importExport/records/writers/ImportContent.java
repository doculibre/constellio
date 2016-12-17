package com.constellio.app.services.importExport.records.writers;

import java.util.ArrayList;
import java.util.List;

public class ImportContent {

	private List<ImportContentVersion> versions;

	public ImportContent() {
		versions = new ArrayList<>();
	}

	public ImportContent(List<ImportContentVersion> versions) {
		this.versions = versions;
	}

	public void addVersion(ImportContentVersion version) {
		versions.add(version);
	}

	public List<ImportContentVersion> getVersions() {
		return versions;
	}

	public static ImportContent withSingleMajorVersion(String url, String fileName) {
		ImportContentVersion contentVersion = new ImportContentVersion(url, fileName, true);
		ImportContent importContent = new ImportContent();
		importContent.addVersion(contentVersion);
		return importContent;
	}
}
