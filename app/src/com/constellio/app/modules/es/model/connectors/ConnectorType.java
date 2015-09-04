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

import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.MapStringStringStructure;

public class ConnectorType extends RecordWrapper implements SchemaLinkingType {

	public static final String SCHEMA_TYPE = "connectorType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";

	public static final String CONNECTOR_CLASS_NAME = "connectorClassName";

	public static final String LINKED_SCHEMA = "linkedSchema";

	public static final String CODE_HTTP = "http";
	
	public static final String CODE_SMB = "smb";

	public static final String DEFAULT_AVAILABLE_PROPERTIES = "defaultAvailableProperties";

	public ConnectorType(Record record, MetadataSchemaTypes types) {
		super(record, types, "connectorType");
	}

	public String getCode() {
		return get(CODE);
	}

	public ConnectorType setCode(String code) {
		this.set(CODE, code);
		return this;
	}

	@Override
	public ConnectorType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getConnectorClassName() {
		return get(CONNECTOR_CLASS_NAME);
	}

	public ConnectorType setConnectorClassName(String connectorClassName) {
		this.set(CONNECTOR_CLASS_NAME, connectorClassName);
		return this;
	}

	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public ConnectorType setLinkedSchema(String connectorSchema) {
		set(LINKED_SCHEMA, connectorSchema);
		return this;
	}

	public MapStringStringStructure getDefaultAvailableProperties() {
		return get(DEFAULT_AVAILABLE_PROPERTIES);
	}

	public ConnectorType setDefaultAvailable(MapStringStringStructure defaultAvailableProperties) {
		set(DEFAULT_AVAILABLE_PROPERTIES, defaultAvailableProperties);
		return this;
	}
}
