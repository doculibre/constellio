package com.constellio.app.modules.es.model.connectors.smb;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.es.connectors.smb.LastFetchedStatus;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorSmbDocument extends ConnectorDocument<ConnectorSmbDocument> {

	public static final String SCHEMA_TYPE = "connectorSmbDocument";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CONNECTOR = ConnectorDocument.CONNECTOR;
	public static final String CONNECTOR_TYPE = ConnectorDocument.CONNECTOR_TYPE;
	public static final String LAST_MODIFIED = ConnectorDocument.LAST_MODIFIED;

	public static final String URL = "url";
	public static final String PARSED_CONTENT = "parsedContent";
	public static final String PERMISSIONS_HASH = "permissionsHash";
	public static final String LAST_FETCH_ATTEMPT = "lastFetchAttempt";
	public static final String SIZE = "size";

	public static final String PARENT = "parent";
	public static final String LAST_FETCH_ATTEMPT_STATUS = "lastFetchAttemptStatus";
	public static final String LANGUAGE = "language";
	public static final String EXTENSION = "extension";
	public static final String LAST_FETCH_ATTEMPT_DETAILS = "lastFetchAttemptDetails";
	public static final String UNRETRIEVED_COUNT = "unretrievedCount";

	public ConnectorSmbDocument(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public ConnectorSmbDocument(Record record, MetadataSchemaTypes types, String typeRequirement) {
		super(record, types, typeRequirement);
	}

	public ConnectorSmbDocument setURL(String url) {
		set(URL, url);
		return this;
	}

	public ConnectorSmbDocument setUrl(String url) {
		set(URL, url);
		return this;
	}

	public String getParent() {
		return get(PARENT);
	}

	public ConnectorSmbDocument setParent(String parent) {
		set(PARENT, parent);
		return this;
	}

	public ConnectorSmbDocument setParent(Record parent) {
		set(PARENT, parent);
		return this;
	}

	public ConnectorSmbDocument setParent(ConnectorSmbFolder parent) {
		set(PARENT, parent);
		return this;
	}

	public String getParsedContent() {
		return get(PARSED_CONTENT);
	}

	public ConnectorSmbDocument setParsedContent(String parsedContent) {
		set(PARSED_CONTENT, parsedContent);
		return this;
	}

	public double getSize() {
		double size;
		try {
			size = get(SIZE);
		} catch (Exception e) {
			size = -3;
		}
		return size;
	}

	public ConnectorSmbDocument setSize(long size) {
		set(SIZE, size);
		return this;
	}

	public LocalDateTime getLastFetched() {
		return get(LAST_FETCH_ATTEMPT);
	}

	public ConnectorSmbDocument setLastFetched(LocalDateTime dateTime) {
		set(LAST_FETCH_ATTEMPT, dateTime);
		return this;
	}

	public String getPermissionsHash() {
		return get(PERMISSIONS_HASH);
	}

	public ConnectorSmbDocument setPermissionsHash(String permissionsHash) {
		set(PERMISSIONS_HASH, permissionsHash);
		return this;
	}

	public LastFetchedStatus getLastFetchAttemptStatus() {
		return get(LAST_FETCH_ATTEMPT_STATUS);
	}

	public ConnectorSmbDocument setLastFetchAttemptStatus(LastFetchedStatus lastFetchedStatus) {
		set(LAST_FETCH_ATTEMPT_STATUS, lastFetchedStatus);
		return this;
	}

	public String getLastFetchAttemptDetails() {
		return get(LAST_FETCH_ATTEMPT_DETAILS);
	}

	public ConnectorSmbDocument setLastFetchAttemptDetails(String lastFetchAttemptDetails) {
		set(LAST_FETCH_ATTEMPT_DETAILS, lastFetchAttemptDetails);
		return this;
	}

	public String getLanguage() {
		return get(LANGUAGE);
	}

	public ConnectorSmbDocument setLanguage(String language) {
		set(LANGUAGE, language);
		return this;
	}

	public String getExtension() {
		return get(EXTENSION);
	}

	public ConnectorSmbDocument setExtension(String extension) {
		set(EXTENSION, extension);
		return this;
	}

	public long getUnretrievedCount() {
		double count = get(UNRETRIEVED_COUNT);
		Double d = new Double(count);
		return d.longValue();
	}

	public ConnectorSmbDocument setUnretrievedCount(long unretrievedCount) {
		set(UNRETRIEVED_COUNT, unretrievedCount);
		return this;
	}

	@Override
	public List<String> getDefaultMetadata() {
		return Arrays.asList(CONNECTOR, CONNECTOR_TYPE, URL);
	}

}