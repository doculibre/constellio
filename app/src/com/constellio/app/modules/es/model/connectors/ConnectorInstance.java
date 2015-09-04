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

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.MapStringListStringStructure;

public class ConnectorInstance<T extends ConnectorInstance> extends RecordWrapper {

	public static final String SCHEMA_TYPE = "connectorInstance";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String CODE = "code";
	public static final String TRAVERSAL_CODE = "traversalCode";
	public static final String LAST_TRAVERSAL_ON = "lastTraversalOn";
	public static final String CONNECTOR_TYPE = "connectorType";
	public static final String ENABLED = "enabled";
	public static final String PROPERTIES_MAPPING = "propertiesMapping";

	public ConnectorInstance(Record record, MetadataSchemaTypes types) {
		super(record, types, "connector");
	}

	protected ConnectorInstance(Record record, MetadataSchemaTypes types, String schemaCode) {
		super(record, types, schemaCode);
	}

	public String getCode() {
		return get(CODE);
	}

	public ConnectorInstance setCode(String code) {
		this.set(CODE, code);
		return this;
	}

	@Override
	public ConnectorInstance setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public Boolean getEnabled() {
		return get(ENABLED);
	}

	public boolean isEnabled() {
		Boolean enabled = getEnabled();
		return enabled != null && enabled;
	}

	public T setEnabled(Boolean enabled) {
		set(ENABLED, enabled);
		return (T) this;
	}

	public String getConnectorType() {
		return get(CONNECTOR_TYPE);
	}

	public T setConnectorType(String connectorType) {
		this.set(CONNECTOR_TYPE, connectorType);
		return (T) this;
	}

	public T setConnectorType(Record connectorType) {
		this.set(CONNECTOR_TYPE, connectorType);
		return (T) this;
	}

	public T setConnectorType(ConnectorType connectorType) {
		this.set(CONNECTOR_TYPE, connectorType);
		return (T) this;
	}

	public String getDocumentsCustomSchemaCode() {
		return getId().replace("-", "");
	}

	public MapStringListStringStructure getPropertiesMapping() {
		return get(PROPERTIES_MAPPING);
	}

	public void setPropertiesMapping(MapStringListStringStructure propertiesMapping) {
		this.set(PROPERTIES_MAPPING, propertiesMapping);
	}

	public String getTraversalCode() {
		return this.get(TRAVERSAL_CODE);
	}

	public T setTraversalCode(String traversalCode) {
		this.set(TRAVERSAL_CODE, traversalCode);
		return (T) this;
	}

	public LocalDateTime getLastTraversalOn() {
		return this.get(LAST_TRAVERSAL_ON);
	}

	public T setLastTraversalOn(LocalDateTime lastTraversalOn) {
		this.set(LAST_TRAVERSAL_ON, lastTraversalOn);
		return (T) this;
	}

	public String readToken(String code) {
		return "r" + getId() + code;
	}
}
