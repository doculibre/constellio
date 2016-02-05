package com.constellio.app.modules.es.model.connectors.smb;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.es.connectors.smb.LastFetchedStatus;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorSmbFolder extends ConnectorDocument<ConnectorSmbFolder> {

	public static final String SCHEMA_TYPE = "connectorSmbFolder";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CONNECTOR = ConnectorDocument.CONNECTOR;
	public static final String CONNECTOR_TYPE = ConnectorDocument.CONNECTOR_TYPE;

	public static final String URL = "url";
	public static final String PARENT = "parent";
	public static final String LAST_FETCH_ATTEMPT = "lastFetchAttempt";
	public static final String LAST_FETCHED_STATUS = "lastFetchedStatus";
	public static final String UNRETRIEVED_COUNT = "unretrievedCount";

	public ConnectorSmbFolder(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public ConnectorSmbFolder(Record record, MetadataSchemaTypes types, String typeRequirement) {
		super(record, types, typeRequirement);
	}

	public String getUrl() {
		return get(URL);
	}

	public ConnectorSmbFolder setUrl(String url) {
		set(URL, url);
		return this;
	}

	public String getParent() {
		return get(PARENT);
	}

	public ConnectorSmbFolder setParent(String parent) {
		set(PARENT, parent);
		return this;
	}

	public ConnectorSmbFolder setParent(Record parent) {
		set(PARENT, parent);
		return this;
	}

	public ConnectorSmbFolder setParent(ConnectorSmbFolder parent) {
		set(PARENT, parent);
		return this;
	}

	public LocalDateTime getLastFetched() {
		return get(LAST_FETCH_ATTEMPT);
	}

	public ConnectorSmbFolder setLastFetched(LocalDateTime dateTime) {
		set(LAST_FETCH_ATTEMPT, dateTime);
		return this;
	}

	public LastFetchedStatus getLastFetchedStatus() {
		return get(LAST_FETCHED_STATUS);
	}

	public ConnectorSmbFolder setLastFetchedStatus(LastFetchedStatus lastFetchedStatus) {
		set(LAST_FETCHED_STATUS, lastFetchedStatus);
		return this;
	}

	public long getUnretrievedCount() {
		double count = get(UNRETRIEVED_COUNT);
		Double d = new Double(count);
		return d.longValue();
	}

	public ConnectorSmbFolder setUnretrievedCount(long unretrievedCount) {
		set(UNRETRIEVED_COUNT, unretrievedCount);
		return this;
	}

	@Override
	public List<String> getDefaultMetadata() {
		return Arrays.asList(CONNECTOR, CONNECTOR_TYPE, URL, PARENT);
	}

}