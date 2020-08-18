package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHook;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class UserCredentialTokenCacheHook implements MetadataIndexCacheDataStoreHook<Integer> {

	private SchemasRecordsServices schemas;

	public UserCredentialTokenCacheHook(ModelLayerFactory modelLayerFactory) {
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
		List<String> decryptedTokens = record.getList(schemas.credentialTokenKeys());
		return decryptedTokens.stream().map((token) -> token.hashCode()).collect(toSet());
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
