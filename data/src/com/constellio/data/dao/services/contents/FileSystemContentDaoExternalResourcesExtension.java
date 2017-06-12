package com.constellio.data.dao.services.contents;

import java.io.InputStream;

public abstract class FileSystemContentDaoExternalResourcesExtension {

	private String id;

	public FileSystemContentDaoExternalResourcesExtension(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public abstract InputStream get(String hash, String streamName);
}
