package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordIdsHookDataIndexRetriever;

public class UserCredentialServiceKeyCacheHookRetriever {
	RecordIdsHookDataIndexRetriever<Integer> retriever;
	SchemasRecordsServices systemSchemas;

	public UserCredentialServiceKeyCacheHookRetriever(
			RecordIdsHookDataIndexRetriever<Integer> retriever, ModelLayerFactory modelLayerFactory) {
		this.retriever = retriever;
		this.systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	public UserCredential getUserByServiceKey(String decryptedServiceKey) {
		for (RecordId recordId : retriever.recordIdsStreamerWithKey(decryptedServiceKey.hashCode()).list()) {
			UserCredential userCredential = systemSchemas.getUserCredential(recordId.stringValue());
			if (userCredential.getServiceKey().equals(decryptedServiceKey)) {
				return userCredential;
			}
		}

		return null;
	}
}
