package com.constellio.app.modules.rm.services.sip.model;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;
import java.io.InputStream;

public class EntityRetriever {
	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rm;

	public EntityRetriever(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public InputStream getContentFromHash(String hash) {
		return modelLayerFactory.getContentManager().getContentInputStream(hash, "com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.intelligid.EntityRetriever#getContentFromHash");
	}

	public Folder getFoldersFromString(String id) {
		return id == null ? null : rm.getFolder(id);
	}

	public Category getCategoryById(String id) {
		return id == null ? null : rm.getCategory(id);
	}

	public File newTempFile() {
		return modelLayerFactory.getIOServicesFactory().newIOServices().newTemporaryFile("sipfile");
	}
}
