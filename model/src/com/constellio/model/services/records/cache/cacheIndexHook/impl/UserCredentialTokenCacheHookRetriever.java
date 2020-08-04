package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordIdsHookDataIndexRetriever;

public class UserCredentialTokenCacheHookRetriever {

	RecordIdsHookDataIndexRetriever<Integer> retriever;
	SchemasRecordsServices systemSchemas;

	public UserCredentialTokenCacheHookRetriever(
			RecordIdsHookDataIndexRetriever<Integer> retriever, ModelLayerFactory modelLayerFactory) {
		this.retriever = retriever;
		this.systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	public UserCredential getUserByToken(String decryptedToken) {
		for (RecordId recordId : retriever.recordIdsStreamerWithKey(decryptedToken.hashCode()).list()) {
			UserCredential userCredential = systemSchemas.getUserCredential(recordId.stringValue());
			if (userCredential.getTokenKeys().contains(decryptedToken)) {
				return userCredential;
			}
		}

		return null;
	}
}
