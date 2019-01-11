package com.constellio.app.servlet;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.contents.ContentManager;

import java.io.File;
import java.io.FileNotFoundException;

public class ConstellioUploadContentInVaultService {
	AppLayerFactory appLayerFactory;
	UploadContentServiceInput input;

	public ConstellioUploadContentInVaultService(AppLayerFactory appLayerFactory, UploadContentServiceInput input) {
		this.appLayerFactory = appLayerFactory;
		this.input = input;
	}

	public String uploadContentAndGetHash() {
		String hash = uploadDocument(input.file);
		return hash;
	}

	private String uploadDocument(File file) {
		ContentManager contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		String version = null;
		try {
			version = contentManager.upload(file).getHash();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}

	public static class UploadContentServiceInput {
		File file;

		public void setFile(File file) {
			this.file = file;
		}

	}

}
