package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHook;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UserCredentialServiceKeyCacheHook implements MetadataIndexCacheDataStoreHook<Integer> {
	private SchemasRecordsServices schemas;

	public UserCredentialServiceKeyCacheHook(ModelLayerFactory modelLayerFactory) {
		this.schemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	@Override
	public String getCollection() {
		return Collection.SYSTEM_COLLECTION;
	}

	@Override
	public boolean isHooked(MetadataSchemaType schemaType) {
		return UserCredential.SCHEMA_TYPE.equals(schemaType.getCode());
	}

	@Override
	public boolean requiresDataUpdate(Record record) {
		return record.isModified(schemas.credentialTokenKeys());
	}

	@Override
	public Set<Integer> getKeys(Record record) {
		String decryptedTokens = record.get(schemas.credentialServiceKey());
		if (decryptedTokens == null) {
			return Collections.emptySet();
		}
		Set<Integer> valueAsSet = new HashSet<>();
		valueAsSet.add(decryptedTokens.hashCode());
		return valueAsSet;
	}

	@Override
	public Class<?> getKeyType() {
		return Integer.class;
	}

	@Override
	public int getKeyMemoryLength() {
		return Integer.BYTES;
	}
}
