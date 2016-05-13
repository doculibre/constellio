package com.constellio.app.ui.pages.search.batchProcessing.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;

public class BatchProcessRequest {

	private List<String> ids = new ArrayList<>();

	private MetadataSchema schema;

	private Map<String, Object> modifiedMetadatas = new HashMap<>();

	private User user;

	public BatchProcessRequest(List<String> ids, MetadataSchema schema, User user,
			Map<String, Object> modifiedMetadatas) {
		this.ids = Collections.unmodifiableList(ids);
		this.schema = schema;
		this.user = user;
		this.modifiedMetadatas = Collections.unmodifiableMap(modifiedMetadatas);
	}

	public BatchProcessRequest() {
	}

	public List<String> getIds() {
		return ids;
	}

	public MetadataSchema getSchema() {
		return schema;
	}

	public User getUser() {
		return user;
	}

	public Map<String, Object> getModifiedMetadatas() {
		return modifiedMetadatas;
	}

	public BatchProcessRequest setIds(List<String> ids) {
		this.ids = ids;
		return this;
	}

	public BatchProcessRequest setSchema(MetadataSchema schema) {
		this.schema = schema;
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
