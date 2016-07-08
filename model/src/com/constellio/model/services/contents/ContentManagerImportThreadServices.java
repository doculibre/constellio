package com.constellio.model.services.contents;

import java.io.File;
import java.util.Map;

public class ContentManagerImportThreadServices {

	ContentManager contentManager;
	File importFolder;

	public ContentManagerImportThreadServices(ContentManager contentManager, File importFolder) {
		this.contentManager = contentManager;
		this.importFolder = importFolder;
	}

	public void importFiles() {

	}

	public Map<String, String> readFileNameSHA1Index() {
		return null;
	}

}
