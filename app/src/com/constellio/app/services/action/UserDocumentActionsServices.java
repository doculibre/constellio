package com.constellio.app.services.action;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.SchemasRecordsServices;

public class UserDocumentActionsServices {
	private AppLayerCollectionExtensions appLayerCollectionExtensions;
	private SchemasRecordsServices schemaRecordActionsServices;

	public UserDocumentActionsServices(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerCollectionExtensions = appLayerFactory.getExtensions().forCollection(collection);
		this.schemaRecordActionsServices = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());

	}

	public boolean isFileActionPossible(Record record, User user) {
		return appLayerCollectionExtensions.isClassifyActionPossibleOnUserDocument(schemaRecordActionsServices.wrapUserDocument(record), user);
	}
}
