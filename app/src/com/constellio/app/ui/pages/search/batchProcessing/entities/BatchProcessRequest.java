package com.constellio.app.ui.pages.search.batchProcessing.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class BatchProcessRequest {

	private List<String> ids = new ArrayList<>();

	private Map<String, Object> modifiedMetadatas = new HashMap<>();

	private User user;

	private MetadataSchemaType schemaType;

	public BatchProcessRequest(List<String> ids, User user,
			MetadataSchemaType schemaType, Map<String, Object> modifiedMetadatas) {
		this.ids = Collections.unmodifiableList(ids);
		this.user = user;
		this.schemaType = schemaType;
		this.modifiedMetadatas = Collections.unmodifiableMap(modifiedMetadatas);
	}

	public BatchProcessRequest() {
	}

	public List<String> getIds() {
		return ids;
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

	public BatchProcessRequest setIds(List<String> ids) {
		this.ids = ids;
		return this;
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
}
