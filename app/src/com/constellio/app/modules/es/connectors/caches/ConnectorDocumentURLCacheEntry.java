package com.constellio.app.modules.es.connectors.caches;

import com.constellio.model.entities.schemas.Metadata;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Map;

public class ConnectorDocumentURLCacheEntry implements Serializable {
	String id;
	ConnectorDocumentURLCacheStatus status;
	Map<String, Object> metadatas;
	LocalDateTime fetchingStartTime;

	public ConnectorDocumentURLCacheEntry(String id, ConnectorDocumentURLCacheStatus status,
										  LocalDateTime fetchingStartTime, Map<String, Object> metadatas) {
		this.id = id;
		this.status = status;
		this.metadatas = metadatas;
		this.fetchingStartTime = fetchingStartTime;
	}

	public String getId() {
		return id;
	}

	public ConnectorDocumentURLCacheStatus getStatus() {
		return status;
	}

	public Map<String, Object> getMetadatas() {
		return metadatas;
	}

	public <T> T getMetadata(Metadata metadata) {
		return (T) metadatas.get(metadata.getLocalCode());
	}

	public <T> T getMetadata(String metadataLocalCode) {
		return (T) metadatas.get(metadataLocalCode);
	}

	public LocalDateTime getFetchingStartTime() {
		return fetchingStartTime;
	}
}
