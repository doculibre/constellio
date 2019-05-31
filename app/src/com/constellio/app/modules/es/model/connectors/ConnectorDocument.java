package com.constellio.app.modules.es.model.connectors;

import com.constellio.app.modules.es.model.connectors.http.enums.FetchFrequency;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public abstract class ConnectorDocument<T extends ConnectorDocument> extends RecordWrapper {

	public static final String URL = "url";

	public static final String TRAVERSAL_CODE = "traversalCode";

	public static final String CONNECTOR = "connector";

	public static final String CONNECTOR_TYPE = "connectorType";

	public static final String FETCHED = "fetched";

	public static final String SEARCHABLE = "searchable";

	public static final String FETCHED_DATETIME = "fetchedDateTime";

	public static final String STATUS = "status";

	public static final String MIMETYPE = "mimetype";

	public static final String FETCH_FREQUENCY = "frequency";

	public static final String FETCH_DELAY = "fetchDelay";

	public static final String NEXT_FETCH = "nextFetch";

	public static final String NEVER_FETCH = "neverFetch";

	public static final String ERROR_CODE = "errorCode";

	public static final String ERROR_MESSAGE = "errorMessage";

	public static final String ERROR_STACK_TRACE = "errorStackTrace";

	public static final String ERRORS_COUNT = "errorsCount";

	public static final String LAST_MODIFIED = "lastModified";

	private final KeyListMap<String, Object> properties = new KeyListMap<>();

	private final Map<String, ConnectorField> fieldsDeclarations = new HashMap<>();

	public ConnectorDocument(Record record, MetadataSchemaTypes types, String typeRequirement) {
		super(record, types, typeRequirement);
	}

	public int getErrorsCount() {
		return getInteger(ERRORS_COUNT);
	}

	public T resetErrorsCount() {
		set(ERRORS_COUNT, 0);

		return (T) this;
	}

	public T incrementErrorsCount() {

		Integer errorsCount = getInteger(ERRORS_COUNT);

		if (errorsCount == null) {
			set(ERRORS_COUNT, 1);
		} else {
			set(ERRORS_COUNT, errorsCount + 1);
		}

		return (T) this;
	}

	public String getURL() {
		return getUrl();
	}

	public String getUrl() {
		return get(URL);
	}

	public T setURL(String url) {

		if (url.equals("https://en.wikipedia.org/wiki/Main_Page")) {
			System.out.println("here");
		}

		set(URL, url);
		return (T) this;
	}

	public T setUrl(String url) {
		set(URL, url);
		return (T) this;
	}

	public String getConnectorType() {
		return get(CONNECTOR_TYPE);
	}

	public T setConnectorType(String connectorType) {
		set(CONNECTOR_TYPE, connectorType);
		return (T) this;
	}

	public T setConnectorType(Record connectorType) {
		set(CONNECTOR_TYPE, connectorType);
		return (T) this;
	}

	public T setConnectorType(ConnectorType connectorType) {
		set(CONNECTOR_TYPE, connectorType);
		return (T) this;
	}

	public String getConnector() {
		return get(CONNECTOR);
	}

	public T setConnector(String connector) {
		set(CONNECTOR, connector);
		return (T) this;
	}

	public T setConnector(Record connector) {
		set(CONNECTOR, connector);
		return (T) this;
	}

	public T setConnector(ConnectorInstance connectorInstance) {
		set(CONNECTOR, connectorInstance);
		return (T) this;
	}

	public String getTraversalCode() {
		return this.get(TRAVERSAL_CODE);
	}

	public T setTraversalCode(String traversalCode) {
		set(TRAVERSAL_CODE, traversalCode);
		return (T) this;
	}

	@Override
	public T setModifiedBy(String modifiedBy) {
		super.setModifiedBy(modifiedBy);
		return (T) this;
	}

	@Override
	public T setCreatedBy(String createdBy) {
		super.setCreatedBy(createdBy);
		return (T) this;
	}

	@Override
	public T setModifiedOn(LocalDateTime modifiedOn) {
		super.setModifiedOn(modifiedOn);
		return (T) this;
	}

	@Override
	public T setCreatedOn(LocalDateTime createdOn) {
		super.setCreatedOn(createdOn);
		return (T) this;
	}

	@Override
	public T setTitle(String title) {
		super.setTitle(title);
		return (T) this;
	}

	@Override
	public T setLegacyId(String legacyId) {
		super.setLegacyId(legacyId);
		return (T) this;
	}

	public ConnectorDocumentStatus getStatus() {
		return get(STATUS);
	}

	public T setStatus(ConnectorDocumentStatus status) {
		set(STATUS, status);
		return (T) this;
	}

	public String getMimetype() {
		return get(MIMETYPE);
	}

	public T setMimetype(String mimetype) {
		set(MIMETYPE, mimetype);
		return (T) this;
	}

	public boolean isFetched() {
		Boolean fetched = getFetched();
		return fetched == null || fetched;
	}

	public Boolean getFetched() {
		return get(FETCHED);
	}

	public T setFetched(Boolean fetched) {
		set(FETCHED, fetched);
		return (T) this;
	}

	public boolean isSearchable() {
		Boolean searchable = getSearchable();
		return searchable == null || searchable;
	}

	public Boolean getSearchable() {
		return get(SEARCHABLE);
	}

	public T setSearchable(Boolean searchable) {
		set(SEARCHABLE, searchable);
		return (T) this;
	}

	public LocalDateTime getFetchedDateTime() {
		return get(FETCHED_DATETIME);
	}

	public T setFetchedDateTime(LocalDateTime fetchedDateTime) {
		set(FETCHED_DATETIME, fetchedDateTime);
		return (T) this;
	}

	public FetchFrequency getFetchFrequency() {
		return get(FETCH_FREQUENCY);
	}

	public T setFetchFrequency(FetchFrequency fetchFrequency) {
		set(FETCH_FREQUENCY, fetchFrequency);
		return (T) this;
	}

	public int getFetchDelay() {
		return getInteger(FETCH_DELAY);
	}

	public T setFetchDelay(int fetchDelay) {
		set(FETCH_DELAY, fetchDelay);
		return (T) this;
	}

	public LocalDateTime getNextFetch() {
		return get(NEXT_FETCH);
	}

	public Boolean getNeverFetch() {
		return get(NEVER_FETCH);
	}

	public T setNeverFetch(Boolean nextFetch) {
		set(NEVER_FETCH, nextFetch);
		return (T) this;
	}

	public String getErrorCode() {
		return get(ERROR_CODE);
	}

	public T setErrorCode(String errorCode) {
		set(ERROR_CODE, errorCode);
		return (T) this;
	}

	public String getErrorMessage() {
		return get(ERROR_MESSAGE);
	}

	public T setErrorMessage(String errorMessage) {
		set(ERROR_MESSAGE, errorMessage);
		return (T) this;
	}

	public String getErrorStackTrace() {
		return get(ERROR_STACK_TRACE);
	}

	public T setErrorStackTrace(String errorStackTrace) {
		set(ERROR_STACK_TRACE, errorStackTrace);
		return (T) this;
	}

	public Map<String, List<Object>> getProperties() {
		return Collections.unmodifiableMap(properties.getNestedMap());
	}

	public T removeProperty(String key) {
		properties.remove(key);
		return (T) this;
	}

	public T addBooleanProperty(String key, Boolean value) {
		addProperty(key, MetadataValueType.BOOLEAN, value);
		return (T) this;
	}

	public T addDateTimeProperty(String key, LocalDateTime value) {
		addProperty(key, MetadataValueType.DATE_TIME, value);
		return (T) this;
	}

	public T addDateProperty(String key, LocalDate value) {
		addProperty(key, MetadataValueType.DATE, value);
		return (T) this;
	}

	public T addStringProperty(String key, String value) {
		addProperty(key, MetadataValueType.STRING, value);
		return (T) this;
	}

	public T addStringProperty(String key, String... values) {
		addProperty(key, MetadataValueType.STRING, asList(values));
		return (T) this;
	}

	public T addStringProperty(String key, List<String> values) {
		addProperty(key, MetadataValueType.STRING, values == null ? new Object[0] : values.toArray());
		return (T) this;
	}

	public T addProperty(String key, MetadataValueType type, List<Object> values) {
		properties.addAll(key, values);
		setPropertyTypeDeclaration(key, type);
		return (T) this;
	}

	public T addProperty(String key, MetadataValueType type, Object... value) {
		properties.addAll(key, asList(value));
		setPropertyTypeDeclaration(key, type);
		return (T) this;
	}

	public T add(String key, MetadataValueType type, List<Object> values) {
		properties.addAll(key, values);
		setPropertyTypeDeclaration(key, type);
		return (T) this;
	}

	void setPropertyTypeDeclaration(String propertyCode, MetadataValueType type) {
		if (!fieldsDeclarations.containsKey(propertyCode)) {
			String schemaType = new SchemaUtils().getSchemaTypeCode(getSchemaCode());
			String fullCode = schemaType + ":" + propertyCode;
			fieldsDeclarations.put(fullCode, new ConnectorField(fullCode, propertyCode, type));
		}
	}

	public T withPropertyLabel(String fieldCode, String fieldLabel) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(getSchemaCode());
		String fullCode = schemaType + ":" + fieldCode;
		fieldsDeclarations.get(fullCode).setLabel(fieldLabel);
		return (T) this;
	}

	public abstract List<String> getDefaultMetadata();

	public void clearProperties() {
		properties.clear();
		fieldsDeclarations.clear();
	}

	public LocalDateTime getLastModified() {
		return get(LAST_MODIFIED);
	}

	public T setLastModified(LocalDateTime dateTime) {
		set(LAST_MODIFIED, dateTime);
		return (T) this;
	}

	public Map<String, ConnectorField> getFieldsDeclarations() {
		return fieldsDeclarations;
	}
}
