/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.model.connectors;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public abstract class ConnectorDocument<T extends ConnectorDocument> extends RecordWrapper {

	public static final String TRAVERSAL_CODE = "traversalCode";

	public static final String CONNECTOR = "connector";

	public static final String CONNECTOR_TYPE = "connectorType";

	public static final String FETCHED = "fetched";

	private final KeyListMap<String, Object> properties = new KeyListMap<>();

	public ConnectorDocument(Record record, MetadataSchemaTypes types, String typeRequirement) {
		super(record, types, typeRequirement);
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

	public Map<String, List<Object>> getProperties() {
		return Collections.unmodifiableMap(properties.getNestedMap());
	}

	public T removeProperty(String key) {
		properties.remove(key);
		return (T) this;
	}

	public T addProperty(String key, List<Object> values) {
		properties.addAll(key, values);
		return (T) this;
	}

	public T addProperty(String key, Object... value) {
		properties.addAll(key, asList(value));
		return (T) this;
	}

	public T add(String key, List<Object> values) {
		properties.addAll(key, values);
		return (T) this;
	}

	public abstract List<String> getDefaultMetadata();

	public void clearProperties() {
		properties.clear();
	}
}
