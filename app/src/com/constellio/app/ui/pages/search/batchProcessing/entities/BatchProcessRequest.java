package com.constellio.app.ui.pages.search.batchProcessing.entities;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BatchProcessRequest {

	private LogicalSearchQuery query;

	private Map<String, Object> modifiedMetadatas = new HashMap<>();

	private User user;

	private MetadataSchemaType schemaType;

	public BatchProcessRequest(LogicalSearchQuery query, User user,
							   MetadataSchemaType schemaType, Map<String, Object> modifiedMetadatas) {
		this.query = query;
		this.user = user;
		this.schemaType = schemaType;
		this.modifiedMetadatas = Collections.unmodifiableMap(modifiedMetadatas);
	}

	public BatchProcessRequest() {
	}

	public User getUser() {
		return user;
	}

	public MetadataSchemaType getSchemaType() {
		return schemaType;
	}

	public Map<String, Object> getModifiedMetadatas() {
		return modifiedMetadatas;
	}

	public BatchProcessRequest setModifiedMetadatas(Map<String, Object> modifiedMetadatas) {
		this.modifiedMetadatas = modifiedMetadatas;
		return this;
	}

	public BatchProcessRequest setUser(User user) {
		this.user = user;
		return this;
	}

	public BatchProcessRequest addModifiedMetadata(String metadataCode, Object value) {
		modifiedMetadatas.put(metadataCode, value);
		return this;
	}

	public BatchProcessRequest setSchemaType(MetadataSchemaType schemaType) {
		this.schemaType = schemaType;
		return this;
	}

	public LogicalSearchQuery getQuery() {
		return query;
	}

	public BatchProcessRequest setQuery(LogicalSearchQuery query) {
		this.query = query;
		return this;
	}

	@Override
	public String toString() {
		return "BatchProcessRequest{" +
				"modifiedMetadatas=" + modifiedMetadatas +
				", ids=" +  +
				'}';
	}
}
