package com.constellio.app.ui.pages.search.batchProcessing.entities;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;

public class BatchProcessRequest {

	private final List<String> ids;

	private final MetadataSchema schema;

	private final Map<String, Object> modifiedMetadatas;

	private final User user;

	public BatchProcessRequest(List<String> ids, MetadataSchema schema, User user,
			Map<String, Object> modifiedMetadatas) {
		this.ids = Collections.unmodifiableList(ids);
		this.schema = schema;
		this.user = user;
		this.modifiedMetadatas = Collections.unmodifiableMap(modifiedMetadatas);
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
}
