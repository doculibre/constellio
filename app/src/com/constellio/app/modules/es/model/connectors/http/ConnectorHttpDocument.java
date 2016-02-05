package com.constellio.app.modules.es.model.connectors.http;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorHttpDocument extends ConnectorDocument<ConnectorHttpDocument> {

	public static final String SCHEMA_TYPE = "connectorHttpDocument";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CONNECTOR = ConnectorDocument.CONNECTOR;
	public static final String CONNECTOR_TYPE = ConnectorDocument.CONNECTOR_TYPE;
	public static final String URL = ConnectorDocument.URL;
	public static final String FETCHED = ConnectorDocument.FETCHED;
	public static final String FETCHED_DATETIME = ConnectorDocument.FETCHED_DATETIME;
	public static final String STATUS = ConnectorDocument.STATUS;
	public static final String SEARCHABLE = ConnectorDocument.SEARCHABLE;
	public static final String FETCH_FREQUENCY = ConnectorDocument.FETCH_FREQUENCY;
	public static final String FETCH_DELAY = ConnectorDocument.FETCH_DELAY;
	public static final String NEXT_FETCH = ConnectorDocument.NEXT_FETCH;
	public static final String NEVER_FETCH = ConnectorDocument.NEVER_FETCH;
	public static final String ERROR_CODE = ConnectorDocument.ERROR_CODE;
	public static final String ERROR_MESSAGE = ConnectorDocument.ERROR_MESSAGE;
	public static final String ERROR_STACK_TRACE = ConnectorDocument.ERROR_STACK_TRACE;
	public static final String ERRORS_COUNT = ConnectorDocument.ERRORS_COUNT;

	public static final String PARSED_CONTENT = "parsedContent";
	public static final String LEVEL = "level";
	public static final String PRIORITY = "priority";
	public static final String ON_DEMAND = "onDemand";
	public static final String COPY_OF = "copyOf";
	public static final String OUTLINKS = "outlinks";
	public static final String INLINKS = "inlinks";
	public static final String CHARSET = "charset";
	public static final String DIGEST = "digest";
	public static final String CONTENT_TYPE = "contentType";
	public static final String DOWNLOAD_TIME = "downloadTime";

	public ConnectorHttpDocument(Record record, MetadataSchemaTypes types) {
		super(record, types, "connectorHttpDocument");
	}

	public ConnectorHttpDocument(Record record, MetadataSchemaTypes types, String typeRequirement) {
		super(record, types, typeRequirement);
	}

	@Override
	public List<String> getDefaultMetadata() {
		return Arrays.asList(CONNECTOR, CONNECTOR_TYPE, URL, PARSED_CONTENT);
	}

	public String getParsedContent() {
		return get(PARSED_CONTENT);
	}

	public ConnectorHttpDocument setParsedContent(String parsedContent) {
		set(PARSED_CONTENT, parsedContent);
		return this;
	}

	public int getLevel() {
		return getInteger(LEVEL);
	}

	public ConnectorHttpDocument setLevel(int level) {
		set(LEVEL, new Integer(level).doubleValue());
		return this;
	}

	public Double getDownloadTime() {
		return get(DOWNLOAD_TIME);
	}

	public ConnectorHttpDocument setDownloadTime(Double downloadTime) {
		set(DOWNLOAD_TIME, downloadTime);
		return this;
	}

	public Double getPriority() {
		return get(PRIORITY);
	}

	public ConnectorHttpDocument setPriority(Double priority) {
		set(PRIORITY, priority);
		return this;
	}

	public Boolean getOnDemand() {
		return get(ON_DEMAND);
	}

	public ConnectorHttpDocument setOnDemand(Boolean onDemand) {
		set(ON_DEMAND, onDemand);
		return this;
	}

	public String getCopyOf() {
		return get(COPY_OF);
	}

	public ConnectorHttpDocument setCopyOf(String copyOf) {
		set(COPY_OF, copyOf);
		return this;
	}

	public List<String> getOutlinks() {
		return getList(OUTLINKS);
	}

	public ConnectorHttpDocument setOutlinks(List<String> outLinks) {
		set(OUTLINKS, outLinks);
		return this;
	}

	public List<String> getInlinks() {
		return getList(INLINKS);
	}

	public ConnectorHttpDocument setInlinks(List<String> inlinks) {
		set(INLINKS, inlinks);
		return this;
	}

	public String getCharset() {
		return get(CHARSET);
	}

	public ConnectorHttpDocument setCharset(String charset) {
		set(CHARSET, charset);
		return this;
	}

	public String getDigest() {
		return get(DIGEST);
	}

	public ConnectorHttpDocument setDigest(String digest) {
		set(DIGEST, digest);
		return this;
	}

	public String getContentType() {
		return get(CONTENT_TYPE);
	}

	public ConnectorHttpDocument setContentType(String digest) {
		set(CONTENT_TYPE, digest);
		return this;
	}
}